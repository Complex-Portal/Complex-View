/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.predict.util;


import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.business.IntactHelper;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.persistence.DAOSource;
import uk.ac.ebi.intact.persistence.DAOFactory;
import uk.ac.ebi.intact.persistence.DataSourceException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Runs the PAYG algorithm for Postgres database. Type ant payg-pg from
 * the application/predict directory.
 *
 * @author konrad.paszkiewicz (konrad.paszkiewicz@ic.ac.uk)
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class PredictPg {

    // Inner Classes

    // ------------------------------------------------------------------------

    private class Edge {

        private String myIda;
        private String myIdb;

        private Edge(String ida, String idb) {
            myIda = ida;
            myIdb = idb;
        }

        // Getter methods.

        private String getIda() {
            return myIda;
        }

        private String getIdb() {
            return myIdb;
        }
    }

    private class Node {

        private String myNid;
        private String mySpecies;

        private Node(String nid, String species) {
            myNid = nid;
            mySpecies = species;
        }

        private String getNid() {
            return myNid;
        }

        private String getSpecies() {
            return mySpecies;
        }
    }

    // ------------------------------------------------------------------------

    // End of Inner classes

    /**
     * The intact helper.
     */
    private IntactHelper myHelper;

    /**
     * Default constructor.
     */
    private PredictPg() throws IntactException, DataSourceException {
        DAOSource ds = DAOFactory.getDAOSource(
                "uk.ac.ebi.intact.persistence.ObjectBridgeDAOSource");
        myHelper = new IntactHelper(ds);
    }

    private Connection getConnection() throws IntactException {
        return myHelper.getJDBCConnection();
    }

    private void closeConnection() {
        try {
            myHelper.closeStore();
        }
        catch (IntactException e) {
            e.printStackTrace();
        }
    }

    private List getNodes(ResultSet rs) throws SQLException {
        List list = new ArrayList();
        while (rs.next()) {
            list.add(new Node(rs.getString(1), rs.getString(2)));
        }
        return list;
    }

    private List getEdges(ResultSet rs) throws SQLException {
        List list = new ArrayList();
        while (rs.next()) {
            list.add(new Edge(rs.getString(1), rs.getString(2)));
        }
        return list;
    }

    public static void main(String[] args) {
        PredictPg pred = null;
        try {
            pred = new PredictPg();
            System.out.println("Preparing tables...");
            pred.PrepareTables();           // Prepare Tables
            ArrayList species_list = pred.getSpeciesTypes();

            for (ListIterator iterator = species_list.listIterator(); iterator.hasNext();) {
                String species = (String) iterator.next();
                System.out.println("Performing Pay-As-You-Go Strategy for Taxonomic ID: " + species);
                pred.doPayAsYouGo(species);         //Perform Pay-As-You-Go algorithm on the interaction network for each species
            }
        }
        catch (SQLException sqle) {
            while (sqle != null) {
                System.err.println("**** SQLException ****");
                System.err.println("** SQLState: " + sqle.getSQLState());
                System.err.println("** Message: " + sqle.getMessage());
                System.err.println("** Error Code: " + sqle.getErrorCode());
                System.err.println("***********");
                sqle = sqle.getNextException();
            }
        }
        catch (IntactException ie) {
            ie.printStackTrace();
        }
        catch (DataSourceException dse) {
            dse.printStackTrace();
        }
        finally {
            // Close the connection regardless.
            pred.closeConnection();
        }
    } //end main

    private void PrepareTables() throws IntactException, SQLException {
        Statement S = null;
        try {
            //Create current_edge table
            S = getConnection().createStatement();
            S.executeUpdate("delete from current_edge;");
            fillCurrentEdgesTable(); //Get interactions from intact database

            S.executeUpdate("update current_edge set seen=0, conf=0;");

            //Setup the Pay-As-You-Go table
            S.executeUpdate("delete from ia_payg;");

            S.executeUpdate("delete from temp_node;");
            S.executeUpdate("insert into temp_node select distinct nidA, "
                    + " species from current_edge;");
            S.executeUpdate("insert into temp_node select distinct nidB, "
                    + " species from current_edge;");

            ResultSet R = S.executeQuery("select distinct nid, species from "
                    + " temp_node where nid!=\'\';");
            for (Iterator iter = getNodes(R).iterator(); iter.hasNext();) {
                Node tn = (Node) iter.next();
                String nid = tn.getNid();
                String species = tn.getSpecies();
                System.out.println("Inserting ia_payg value " + nid);
                ResultSet RS = S.executeQuery("select count(*) from current_edge "
                        + " where nidA=\'" + nid + "\';");
                if (RS.next()) {
                    if (RS.getInt(1) == 0) {
                        S.executeUpdate("INSERT INTO ia_payg (nid, bait, prey,"
                                + " indegree, outdegree, qdegree, eseen, econf, "
                                + "really_used_as_bait, species) values(\'" + nid
                                + "\',0,0,0,0,0.0,0,0,'N',\'" + species + "\')");
                    }
                    else {
                        S.executeUpdate("INSERT INTO ia_payg (nid, bait, prey,"
                                + "indegree, outdegree, qdegree, eseen, econf,"
                                + "really_used_as_bait, species) values(\'" + nid
                                + "\',0,0,0,0,0.0,0,0,'Y',\'" + species + "\')");
                    }
                }
                RS.close();
            } //end while
            R.close();
        }
        finally {
            if (S != null) {
                try {
                    S.close();
                }
                catch (SQLException e) {
                }
            }
        }
    }// end Prepare Tables

    private void fillCurrentEdgesTable() throws IntactException, SQLException {

        String bait = "";

        // Get the interactions
        Collection interactions = myHelper.search(Interaction.class.getName(), "ac", null);
        Statement S = null;

        try {
            S = getConnection().createStatement();

            // Iterate through the interactions
            System.out.println(interactions.size() + " interactions found.");
            for (Iterator iterator = interactions.iterator(); iterator.hasNext();) {
                Interaction interaction = (Interaction) iterator.next();

                Collection components = interaction.getComponents();

                // For each interaction get the components
                for (Iterator iterator2 = components.iterator(); iterator2.hasNext();) {
                    Component component = (Component) iterator2.next();

                    Interactor interactor = component.getInteractor();
                    if (interactor != null) {
                        String role = component.getCvComponentRole().getShortLabel();
                        String species = interactor.getBioSource().getTaxId();
                        System.out.println("Role is " + role + " Species: " + species);

                        if (role.equals("bait")) {
                            bait = interactor.getShortLabel();
                            System.out.println("Bait: " + bait);
                        }

                        if (role.equals("prey")) {
                            String prey = interactor.getShortLabel();
                            S.executeUpdate(
                                    "insert into current_edge values(\'" + bait + "\',\'"
                                    + prey + "\',0,0,\'" + species + "\');");
                            System.out.println("Prey: " + prey);
                        }
                    }
                }
            }
        }
        finally {
            if (S != null) {
                try {
                    S.close();
                }
                catch (SQLException e) {
                }
            }
        }
    } // end fill_current_edge_table

    private ArrayList getSpeciesTypes() throws IntactException, SQLException {
        ArrayList speciesList = new ArrayList();
        Statement S = null;

        try {
            S = getConnection().createStatement();
            ResultSet R = S.executeQuery("select distinct species from ia_payg;");
            while (R.next()) {
                speciesList.add(R.getString(1));
            }
        }
        finally {
            if (S != null) {
                try {
                    S.close();
                }
                catch (SQLException e) {
                }
            }
        }
        return speciesList;

    }// end get_species_types

    private void doPayAsYouGo(String species) throws IntactException, SQLException {
        String nextbait = getNextNode(species);
        // while we have uncovered node
        for (int counter = 1; !nextbait.equals(""); counter++) {
            virtualPullOut(nextbait, counter, species);
            nextbait = getNextNode(species);
        }
    }

    private String getNextNode(String species) throws IntactException, SQLException {
        // selecting the next bait but only from one species
        Statement S = null;
        int in = 0, out = 0;
        double q = 0.0, avg = 0.0;
        String nid = "";
        try {
            System.out.println("looking for the NextNode");
            // determine avg Q in of node sampled so far
            S = getConnection().createStatement();
            String query = "select avg( indegree) from ia_payg where bait=0 and "
                    + " species =\'" + species + "\';";
            // System.out.println("Q>"+Query);
            ResultSet R = S.executeQuery(query);
            if (R.next()) {
                avg = R.getDouble(1);
            }
            R.close();
            System.out.println("Current avg=" + avg);

            // get the node with max indegree
            R = S.executeQuery("select nID, indegree, qdegree, outdegree from "
                    + " ia_payg where bait=0 and species =\'" + species
                    + "\' order by indegree DESC, qdegree DESC limit 1;");

            if (R.next()) {
                nid = R.getString(1);
                in = R.getInt(2);
                q = R.getDouble(3);
                out = R.getInt(4);
            }
            R.close();
            System.out.println("nextNode:" + nid + " out=" + out + "\tin=" + in + "\tq=" + q);

            // if we are below average : random sampling
            if (in <= avg) { // we have nothing above average or q is empty
                // since we have nothing yet, so random jumpstart
                System.out.println(">>>> random!");
                R = S.executeQuery("select nID from ia_payg where bait=0  and species =\'"
                        + species + "\' order by random() limit 1;");
                if (R.next()) {
                    nid = R.getString(1);
                }
                R.close();
            } // end if nid = 0
        }
        finally {
            if (S != null) {
                try {
                    S.close();
                }
                catch (SQLException e) {
                }
            }
        }
        // System.out.println("getNextNode:"+nid);
        return nid;
    } // end getNextNode

    private void virtualPullOut(String ID, int step, String species)
            throws IntactException, SQLException {
        Statement S = null;
        try {
            // determine k & deltaK
            S = getConnection().createStatement();
            ResultSet R = S.executeQuery("select count(*) from current_edge where species =\'"
                    + species + "\' and (nidA=\'" + ID + "\' or nidB=\'" + ID
                    + "\') and (nidA!=nidB) and nidA!=\'\' and nidB!=\'\';");
            int k = 0;
            if (R.next()) {
                k = R.getInt(1);
            }
//            R.close();
            System.out.println("K = " + k);
            double delta = 1 / (double) k;
            System.out.println("delta = " + delta);

            // mark the bait & set k
            S.executeUpdate("update ia_payg set bait=" + step + ", outdegree=" + k
                    + " where nID=\'" + ID + "\' and species =\'" + species + "\';");

            // then mark all the adjacent edge as covered
            S.executeUpdate("update current_edge set conf=" + step
                    + " where (nidA=\'" + ID + "\' or nidB=\'" + ID + "\')  "
                    + " and species =\'" + species + "\' and nidA!=nidB and "
                    + " seen>0 and conf=0 and nidA!=\'\' and nidB!=\'\';");

            S.executeUpdate("update current_edge set seen=" + step
                    + " where (nidA=\'" + ID + "\' or nidB=\'" + ID
                    + "\') and nidA!=nidB  and species =\'" + species + "\' and seen=0;");

            // conduct the virtualPullOut
            // 1. mark all the neighbours as prey

            R = S.executeQuery("select nidA, nidB from current_edge where species =\'"
                    + species + "\' and (nidA=\'" + ID + "\' or nidB=\'" + ID
                    + "\') and (nidA!=nidB) and nidA!=\'\' and nidB!=\'\';");
            for (Iterator iter = getEdges(R).iterator(); iter.hasNext();) {
                Edge edge = (Edge) iter.next();
                String aId = edge.getIda();
                String bId = edge.getIdb();
                String preyId = "";
                if (aId.equals(ID)) {
                    preyId = bId;
                }
                else {
                    preyId = aId;
                }
                System.out.println(">> " + ID + " =-> " + preyId);
                S.executeUpdate("update ia_payg set prey=" + step + " where nID=\'"
                        + preyId + "\' and prey=0 and species =\'" + species + "\';");
                // update the indegree and delta for all adjacent node
                S.executeUpdate("update ia_payg set indegree=indegree+1,qdegree=qdegree+"
                        + delta + "::numeric where nID=\'" + preyId
                        + "\' and species =\'" + species + "\';");
            } // next interaction of this ID
//            R.close();
            // 2. compute the Nr. of edge seen & confirmed so far
            int nSeen = 0;
            R = S.executeQuery("select count(*) from current_edge where seen>0 "
                    + " and (nidA!=nidB) and nidA!=\'\' and nidB!=\'\' and "
                    + " species =\'" + species + "\';");
            if (R.next()) {
                nSeen = R.getInt(1);
            }
            System.out.println("seen=" + nSeen);
//            R.close();

            int nConfirm = 0;
            R = S.executeQuery("select count(*) from current_edge where conf>0 "
                    + " and (nidA!=nidB) and nidA!=\'\' and nidB!=\'\' and "
                    + " species =\'" + species + "\';");
            if (R.next()) {
                nConfirm = R.getInt(1);
            }
            System.out.println("conf=" + nConfirm);

            // Results saved into ia_payg
            S.executeUpdate("update ia_payg set eseen =" + nSeen + ", econf ="
                    + nConfirm + " where nID=\'" + ID + "\' and species =\'"
                    + species + "\';");
        }
        finally {
            if (S != null) {
                try {
                    S.close();
                }
                catch (SQLException e) {
                }
            }
        }
    } // end virtualPullOut
} // end intact_test class
