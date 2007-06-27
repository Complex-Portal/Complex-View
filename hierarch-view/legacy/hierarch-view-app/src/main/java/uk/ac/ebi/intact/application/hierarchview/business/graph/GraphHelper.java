/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */

package uk.ac.ebi.intact.application.hierarchview.business.graph;

import org.apache.log4j.Logger;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.exception.MultipleResultException;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.persistence.SearchException;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.util.simplegraph.BasicGraph;
import uk.ac.ebi.intact.util.simplegraph.BasicGraphI;
import uk.ac.ebi.intact.util.simplegraph.EdgeI;
import uk.ac.ebi.intact.util.simplegraph.MineEdge;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Allows to retreive an interraction network from intact API
 * 
 * @author Samuel Kerrien
 * @version $Id$
 */
public class GraphHelper {
    static transient Logger logger = Logger.getLogger( Constants.LOGGER_NAME );

    // query to retrieve all preys (accession number and shortlabel) for a bait
    private static final String PREY_QUERY = "SELECT protein2_ac, shortlabel2 FROM ia_interactions "
            + "WHERE protein1_ac=?";

    // query to retrieve all baits (accession number and shortlabel) for a prey
    private static final String BAIT_QUERY = "SELECT protein1_ac, shortlabel1 FROM ia_interactions "
            + "WHERE protein2_ac=?";

    // query to retrieve all source information for a specific database
    // (e.g. GO) and a given protein
    private static final String SOURCE_QUERY = "SELECT X.primaryId, X.secondaryId FROM ia_xref X, ia_controlledvocab C "
            + "WHERE C.ac = X.database_ac and C.shortLabel=? AND x.parent_ac=?";

    // list of all available sources (e.g. GO) - it is declared public to enable
    // other classes to use the same information and to avoid redundant look ups
    // for the available sources
    public static final List SOURCES;

    // flag whether the graph was built with the mine database table
    public static final boolean BUILT_WITH_MINE_TABLE;

    static {
        // the default size should be big enough
        SOURCES = new ArrayList();

        // the properties which holds the information about the sources to
        // highlight
        Properties properties = IntactUserI.HIGHLIGHTING_PROPERTIES;

        if ( null != properties ) {
            // stores all allowed sources provided by the property
            String allowedSources = IntactUserI.HIGHLIGHTING_PROPERTIES
                    .getProperty( "highlightment.source.allowed" );

            String delimiter = IntactUserI.HIGHLIGHTING_PROPERTIES
                    .getProperty( "highlightment.source.token" );

            // if sources are found and if a delimiter token was found
            // the sources are split and each source is added to the source list
            if ( null != allowedSources ) {
                if ( null != delimiter ) {
                    StringTokenizer tokens = new StringTokenizer(
                            allowedSources, delimiter );
                    while ( tokens.hasMoreTokens() ) {
                        // the current source is added to the source list
                        SOURCES.add( tokens.nextToken().toLowerCase() );
                    }
                }
                else {
                    logger.warn( "Unable to find the property "
                            + "highlightment.source.token" );
                }
            }
            else {
                logger.warn( "Unable to find the property "
                        + "highlightment.source.allowed" );
            }
        }
        else {
            logger.warn( "Unable to find the property files for the "
                    + "source highlight" );
        }
        properties = IntactUserI.GRAPH_PROPERTIES;

        if ( null != properties ) {
            String mineBuildStr = properties
                    .getProperty( "hierarchView.graph.build.method" );
            // wheter the property equals "mine"
            BUILT_WITH_MINE_TABLE = "mine".equalsIgnoreCase( mineBuildStr );
        }
        else {
            BUILT_WITH_MINE_TABLE = false;
        }
    }

    private IntactUserI user;

    /**
     * basic constructor - sets up (hard-coded) data source and an intact
     * helper.
     */
    public GraphHelper(IntactUserI user) {
        this.user = user;
    } // IntactGraphHelper

    /**
     * Create or extend an interaction network by using the given Interactor as
     * a central node. If a network is already existing, we fusion them.
     * 
     * @param in an interaction network (can be null).
     * @param interactor the Interactor from which we add an interaction
     *            network.
     * @param depth The level of BAIT-BAIT interaction in the interaction graph.
     * @return the corresponding interaciton network
     */
    public InteractionNetwork addInteractionNetwork(InteractionNetwork in,
            Interactor interactor, int depth) throws SearchException,
            IntactException, MultipleResultException {

        if ( BUILT_WITH_MINE_TABLE ) {
            // create the central node with the informations given by the
            // interactor
            BasicGraphI centralNode = new BasicGraph( interactor.getAc(),
                    interactor.getShortLabel() );
            try {
                return addInteractionNetwork( in, centralNode, depth );
            }
            catch ( SQLException e ) {
                logger.warn( "mine building failed - normal way !" );
            }
        }

        InteractionNetwork tmp = null;
        if ( in != null ) {
            /*
             * In the case that interactor is already registered as central in
             * the current network, no need to retrieve the network, just send
             * back the current one.
             */
            ArrayList interactors = in.getCentralInteractors();
            if ( interactors.contains( interactor ) ) {
                return in;
            }

            /*
             * That interactor is not yet in the current interaction network, so
             * retreive that network and fusion them.
             */
            tmp = in;
        }
        in = new InteractionNetwork( interactor );

        in = user.subGraph( in, depth, null,
                uk.ac.ebi.intact.model.Constants.EXPANSION_BAITPREY );

        if ( tmp != null ) {
            logger.info( "Fusion interaction network" );
            tmp.fusion( in );
            in = tmp;
            tmp = null; // must be reset 'cos we could chexk it in the next
            // iteration.
        }
        return in;
    } // addInteractionNetwork


    /**
     * Create or extend an interaction network by using the given Interactor as
     * a central node. If a network is already existing, we fusion them.
     * 
     * @param in an interaction network (can be null).
     * @param interactor the Interactor from which we add an interaction
     *            network.
     * @param depth The level of BAIT-BAIT interaction in the interaction graph.
     * @return the corresponding interaciton network
     * @throws SQLException whether the building of the graph failed
     */
    public InteractionNetwork addInteractionNetwork(InteractionNetwork in,
            BasicGraphI interactor, int depth) throws SearchException,
            IntactException, MultipleResultException, SQLException {

        InteractionNetwork tmp = null;

        if ( in != null ) {
            // if the current interactor already is a central protein we dont
            // need to build a graph around it since its already there
            if ( in.isCentralProtein( interactor ) ) {
                return in;
            }
            tmp = in;
        }

        // a new interaction network is created with the given interactor as
        // central protein
        in = new InteractionNetwork( interactor );

        // create a network with the given depth
        in = buildNetwork( interactor, in, depth );

        if ( tmp != null ) {
            logger.info( "Fusion interaction network" );
            tmp.fusion( in );
            in = tmp;
            tmp = null; // must be reset 'cos we could chexk it in the next
            // iteration.
        }
        else {
            // I had problems when there was just one interaction network
            // so that the fusion method was never called -> the nodes werent
            // initialised -> call the method initNodes manually
            in.initNodes();
        }
        return in;
    } // addInteractionNetwork


    /**
     * Builds a network around the provided node as long as the depth of the
     * network is smaller than the given depth.
     * 
     * Every interaction in which the node is a bait is added to the network and
     * every interaction between the node as a prey and its baits are added
     * 
     * @param baitNode the central node of the network
     * @param network the network to build
     * @param depth the maximal depth of the network
     * @return the built network
     * @throws SQLException
     * @throws IntactException
     */
    private InteractionNetwork buildNetwork(BasicGraphI baitNode,
            InteractionNetwork network, int depth) throws SQLException,
            IntactException {
        // if the depth is 0 we have reached the maximal depth in the network
        // and therefore we dont build more
        if ( depth == 0 ) {
            return network;
        }

        // the set stores all interactors which take part in an interaction with
        // the given node
        // a hashset is taken to avoid duplicate entries
        Set interactors = new HashSet();

        String centralAc = baitNode.getAc();

        // add source highlight information for the central protein
        addSourcesToNode( baitNode, true, network );
        network.addNode( baitNode );

        Connection con = getDaoFactory().connection();

        /**
         * I1 is a bait in an interaction: -> all preys are collected of that
         * interaction and added the following:
         * 
         * I1 - P1
         * 
         * I1 - P2
         * 
         * I1 - P3
         */
        PreparedStatement psm = con.prepareStatement( PREY_QUERY );
        psm.setString( 1, centralAc );
        ResultSet set = psm.executeQuery();

        BasicGraphI preyNode;
        EdgeI edge;
        Map nodes;
        String ac;
        while ( set.next() ) {
            edge = new MineEdge();

            ac = set.getString( 1 );

            /*
             * to avoid that we create a new object with the same accession
             * number as an existing node we check first if the current
             * accession number already exists.
             * 
             * this is needed to make sure that the created edge has the correct
             * object references
             */
            nodes = network.getNodes();
            if ( nodes.containsKey( ac ) ) {
                preyNode = (BasicGraphI) nodes.get( ac );
            }
            else {
                // a new node is created and the accession number and the
                // shortlabel are set
                preyNode = new BasicGraph( set.getString( "protein2_ac" ), set
                        .getString( "shortlabel2" ) );
                // the new node is added to the the set of the interactors of
                // the central node
                interactors.add( preyNode );
                // the node is added to the network
                network.addNode( preyNode );
            }
            // the two nodes are set to the end of the edges
            edge.setNode1( baitNode );
            edge.setNode2( preyNode );
            // the edge is added to the network
            network.addEdge( edge );
            // the highlighting sources are added to the node
            addSourcesToNode( preyNode, false, network );
        }
        set.close();
        psm.close();

        /**
         * If I1 and I2 are interactions as follows:
         * 
         * I1: <br>
         * P1: Bait <br>
         * P2: Prey <br>
         * P3: Prey <br>
         * 
         * I2: <br>
         * P10: Bait <br>
         * P11: Prey <br>
         * P3: Prey <br>
         * 
         * and the requested protein is P3, then for a graph of depth one the
         * interactions <br>
         * 
         * P3-P1 <br>
         * P3-P10 <br>
         * 
         * are added.
         */
        psm = con.prepareStatement( BAIT_QUERY );
        psm.setString( 1, centralAc );
        set = psm.executeQuery();
        while ( set.next() ) {
            edge = new MineEdge();

            ac = set.getString( 1 );
            nodes = network.getNodes();

            /*
             * to avoid that we create a new object with the same accession
             * number as an existing node we check first if the current
             * accession number already exists.
             * 
             * this is needed to make sure that the created edge has the correct
             * object references
             */
            if ( nodes.containsKey( ac ) ) {
                preyNode = (BasicGraphI) nodes.get( ac );
            }
            else {
                preyNode = new BasicGraph( set.getString( "protein1_ac" ), set
                        .getString( "shortlabel1" ) );

                network.addNode( preyNode );
                interactors.add( preyNode );
            }

            edge.setNode1( baitNode );
            edge.setNode2( preyNode );
            network.addEdge( edge );
            addSourcesToNode( preyNode, false, network );
        }
        set.close();
        psm.close();

        if ( depth > 1 ) {
            // recursive call of the building with a decreased depth
            // for every interactor its interactions are added
            for (Iterator iter = interactors.iterator(); iter.hasNext();) {
                network = buildNetwork( (BasicGraphI) iter.next(), network,
                        depth - 1 );
            }
        }
        return network;
    }


    /**
     * Adds to a node all sources given by the allowed sources defined in the
     * Highlight.properties.
     * 
     * @param node the node to add the sources
     * @param central whether the node is a central protein
     * @param network the interaction network
     * @throws SQLException whether the retrieving of the sources failed due to
     *             database error
     * @throws IntactException
     */
    public void addSourcesToNode(BasicGraphI node, boolean central,
            InteractionNetwork network) throws SQLException, IntactException {
        /*
         * The method stores the source informations in two different ways
         * depending whether a central node is given or not.
         * 
         * In both cases the source highlighting information is stored in a map
         * structure provided by the network. So that one can easily and fast
         * retrieve all nodes for a source (e.g. GO) and a specific source ID
         * (e.g. GO:000190)
         * 
         * If the node is a central node. The source information is also stored
         * directly in the node. This provides easy and fast access to display
         * all available source to highlight on the right top corner of the HV
         * result page.
         */
        Connection con = getDaoFactory().connection();

        PreparedStatement sourceStm = con.prepareStatement( SOURCE_QUERY );
        sourceStm.setString( 2, node.getAc() );
        logger.info( "node.getAc() = " + node.getAc() );

        String source, sourceID;
        Collection proteinSources = null;
        ResultSet set;
        for (int i = 0; i < SOURCES.size(); i++) {
            // the current source is fetched (e.g. GO)
            source = (String) SOURCES.get( i );
            logger.info("Current source = " + source);

            /*
             * the collection stores all source informations for the current
             * source.
             * 
             * every even entry of the collection is the primaryID of the source
             * information.
             * 
             * every odd entry of the collection is the secondaryID of the
             * source information.
             * 
             * this is just relavent for the central proteins because they store
             * this information to enable fast display of the highlight
             */
            if ( central ) {
                proteinSources = new ArrayList();
            }

            sourceStm.setString( 1, source );
            set = sourceStm.executeQuery();

            while ( set.next() ) {
                sourceID = set.getString( "primaryId" );
                logger.info( "sourceID=" + sourceID );
                /*
                 * the retrieved information is added to the highlight map of
                 * the network.
                 */
                logger.info( "addToSourceHighlightMap : source=" + source + " | sourceID="
                + sourceID + " | node=" + node );
                network.addToSourceHighlightMap( source, sourceID, node );

                // if the node is a central protein the information are stored
                // in a collection to enable fast access to display the
                // Existing highlight source for the central protein(s)
                if ( central ) {
                    proteinSources.add( sourceID );
                    proteinSources.add( set.getString( "secondaryId" ) );
                    logger.info( "secondaryID=" + set.getString( "secondaryId" ) );
                }
            }
            set.close();

            // if the node is a central protein the information are stored in
            // the node
            if ( central ) {
                node.put( source, proteinSources );
            }
        }
        sourceStm.close();
    }

    private DaoFactory getDaoFactory()
    {
        return IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
    }

}