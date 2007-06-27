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
 * Runs the PAYG algorithm for Oracle database. Type ant payg-ora from
 * the application/predict directory. Also, ensure that repository.xml contains
 * entries for a user with write priviledges.
 *
 * @author konrad.paszkiewicz (konrad.paszkiewicz@ic.ac.uk)
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class PredictOra {

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
     * To generate a random number.
     */
    private Random myRandom = new Random();

    /**
     * Default constructor.
     */
    private PredictOra() throws IntactException, DataSourceException {
        DAOSource ds = DAOFactory.getDAOSource(
                "uk.ac.ebi.intact.persistence.ObjectBridgeDAOSource");
        //set the config details, ie repository file for OJB in this case
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
        PredictOra pred = null;
        try {
            pred = new PredictOra();
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
            if (pred != null) {
                pred.closeConnection();
            }
        }
    } //end main

    private void PrepareTables() throws IntactException, SQLException {
        Statement S = null;
        try {
            //Create current_edge table
            S = getConnection().createStatement();
            S.executeUpdate("delete FROM ia_payg_current_edge");
            fillCurrentEdgesTable(); //Get interactions from intact database

            S.executeUpdate("UPDATE ia_payg_current_edge SET seen=0, conf=0");

            //Setup the Pay-As-You-Go table
            S.executeUpdate("delete FROM ia_payg");

            S.executeUpdate("delete FROM ia_payg_temp_node");
            S.executeUpdate("INSERT INTO ia_payg_temp_node SELECT distinct nidA, species FROM ia_payg_current_edge");
            S.executeUpdate("INSERT INTO ia_payg_temp_node SELECT distinct nidB, species FROM ia_payg_current_edge");

            ResultSet R = S.executeQuery("SELECT distinct nid, species FROM ia_payg_temp_node WHERE nid IS NOT NULL");
            for (Iterator iter = getNodes(R).iterator(); iter.hasNext();) {
                Node tn = (Node) iter.next();
                String nid = tn.getNid();
                String species = tn.getSpecies();
//                System.out.println("Inserting ia_payg value " + nid);
                ResultSet RS = S.executeQuery("SELECT COUNT(*) FROM ia_payg_current_edge WHERE nidA=\'"
                        + nid + "\'");
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
        catch (SQLException sqle) {
            sqle.printStackTrace();
            throw sqle;
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
//                        System.out.println("Role is " + role + " Species: " + species);

                        if (role.equals("bait")) {
                            bait = interactor.getShortLabel();
//                            System.out.println("Bait: " + bait);
                        }

                        if (role.equals("prey")) {
                            String prey = interactor.getShortLabel();
                            S.executeUpdate(
                                    "INSERT INTO ia_payg_current_edge values(\'" + bait + "\',\'"
                                    + prey + "\',0,0,\'" + species + "\')");
//                            System.out.println("Prey: " + prey);
                        }
                    }
                }
            }
        }
        catch (SQLException sqle) {
            sqle.printStackTrace();
            throw sqle;
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
            ResultSet R = S.executeQuery("SELECT distinct species FROM ia_payg");
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
        int in = 0;
        double avg = 0.0;
        String nid = "";
        try {
            System.out.print('.');
            // determine avg Q in of node sampled so far
            S = getConnection().createStatement();
            String query = "SELECT avg( indegree) FROM ia_payg WHERE bait=0 AND species =\'"
                    + species + "\'";
            // System.out.println("Q>"+Query);
            ResultSet R = S.executeQuery(query);
            if (R.next()) {
                avg = R.getDouble(1);
            }
            R.close();
//            System.out.println("Current avg=" + avg);

            // get the node with max indegree
            R = S.executeQuery("SELECT nID, indegree, qdegree, outdegree FROM ia_payg WHERE ROWNUM=1 AND bait=0 AND species =\'"
                    + species + "\' ORDER BY indegree DESC, qdegree DESC");

            if (R.next()) {
                nid = R.getString(1);
                in = R.getInt(2);
//                double q = R.getDouble(3);
//                out = R.getInt(4);
            }
            R.close();
//            System.out.println("nextNode:" + nid + " out=" + out + "\tin=" + in + "\tq=" + q);

            // if we are below average : random sampling
            if (in <= avg) { // we have nothing above average or q is empty
                // since we have nothing yet, so random jumpstart
//                System.out.println(">>>> random!");
//                System.out.println("Species is: " + species);
                R = S.executeQuery("SELECT nID FROM ia_payg WHERE bait=0 AND species =\'"
                        + species + "\'");
                String rnid = getRandomNid(R);
                // Only set it if it isn't null.
                if (rnid != null) {
                    nid = rnid;
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
            ResultSet R = S.executeQuery("SELECT COUNT(*) FROM ia_payg_current_edge WHERE species =\'"
                    + species + "\' AND (nidA=\'" + ID + "\' OR nidB=\'" + ID
                    + "\') AND (nidA!=nidB) AND nidA IS NOT NULL AND nidB IS NOT NULL");
            int k = 0;
            if (R.next()) {
                k = R.getInt(1);
            }
            R.close();
//            System.out.println("K = " + k);
            double delta = 1 / (double) k;
//            System.out.println("delta = " + delta);

            // mark the bait & set k
            S.executeUpdate("UPDATE ia_payg SET bait=" + step + ", outdegree=" + k
                    + " WHERE nID=\'" + ID + "\' AND species =\'" + species + "\'");

            // then mark all the adjacent edge as covered
            S.executeUpdate("UPDATE ia_payg_current_edge SET conf=" + step
                    + " WHERE (nidA=\'" + ID + "\' OR nidB=\'" + ID + "\')  AND species =\'"
                    + species + "\' AND nidA!=nidB AND seen>0 AND conf=0 AND nidA"
                    + " IS NOT NULL AND nidB IS NOT NULL");

            S.executeUpdate("UPDATE ia_payg_current_edge SET seen=" + step
                    + " WHERE (nidA=\'" + ID + "\' OR nidB=\'" + ID
                    + "\') AND nidA!=nidB  AND species =\'" + species + "\' AND seen=0");

            // conduct the virtualPullOut
            // 1. mark all the neighbours as prey

            R = S.executeQuery("SELECT nidA, nidB FROM ia_payg_current_edge WHERE species =\'"
                    + species + "\' AND (nidA=\'" + ID + "\' OR nidB=\'" + ID
                    + "\') AND (nidA!=nidB) AND nidA IS NOT NULL AND nidB IS NOT NULL");
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
//                System.out.println(">> " + ID + " =-> " + preyId);
                S.executeUpdate("UPDATE ia_payg SET prey=" + step + " WHERE nID=\'"
                        + preyId + "\' AND prey=0 AND species =\'" + species + "\'");
                // update the indegree and delta for all adjacent node
                S.executeUpdate("UPDATE ia_payg SET indegree=indegree+1,qdegree=qdegree+"
                        + delta + " WHERE nID=\'" + preyId + "\' AND species =\'" + species + "\'");
            } // next interaction of this ID
            R.close();
            // 2. compute the Nr. of edge seen & confirmed so far
            int nSeen = 0;
            R = S.executeQuery("SELECT COUNT(*) FROM ia_payg_current_edge WHERE seen>0"
                    + " AND (nidA!=nidB) AND nidA IS NOT NULL AND nidB IS NOT"
                    + " NULL AND species =\'" + species + "\'");
            if (R.next()) {
                nSeen = R.getInt(1);
            }
//            System.out.println("seen=" + nSeen);
            R.close();

            int nConfirm = 0;
            R = S.executeQuery("SELECT COUNT(*) FROM ia_payg_current_edge"
                    + " WHERE conf>0 AND (nidA!=nidB) AND nidA IS NOT NULL"
                    + " AND nidB IS NOT NULL AND species =\'" + species + "\'");
            if (R.next()) {
                nConfirm = R.getInt(1);
            }
//            System.out.println("conf=" + nConfirm);

            // Results saved into ia_payg
            S.executeUpdate("UPDATE ia_payg SET eseen =" + nSeen + ", econf ="
                    + nConfirm + " WHERE nID=\'" + ID + "\' AND species =\'"
                    + species + "\'");
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

    private String getRandomNid(ResultSet rs) throws SQLException {
        // The temp list to collect values from the result set.
        List list = new ArrayList();
        // Add to the list.
        while (rs.next()) {
            list.add(rs.getString(1));
        }
        if (list.isEmpty()) {
            return null;
        }
        // Get a random value between 0 and the list size - 1.
        return (String) list.get(myRandom.nextInt(list.size()));
    }
} // end intact_test class
