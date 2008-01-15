/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package uk.ac.ebi.intact.application.hierarchview.business.graph;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.data.DataService;
import uk.ac.ebi.intact.application.hierarchview.exception.HierarchViewDataException;
import uk.ac.ebi.intact.application.hierarchview.exception.MultipleResultException;
import uk.ac.ebi.intact.application.hierarchview.exception.NetworkBuildException;
import uk.ac.ebi.intact.application.hierarchview.exception.ProteinNotFoundException;
import uk.ac.ebi.intact.service.graph.Node;
import uk.ac.ebi.intact.service.graph.binary.BinaryGraphNetwork;
import uk.ac.ebi.intact.service.graph.binary.BinaryGraphNetworkBuilder;
import uk.ac.ebi.intact.service.graph.binary.label.AliasLabelStrategy;
import uk.ac.ebi.intact.service.graph.binary.merger.InteractionBasedMerger;

import java.util.*;

/**
 * Builds the Network and contains general information about the graph.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 1.6.0-Snapshot
 */
public class HVNetworkBuilder {

    private static final Log logger = LogFactory.getLog( HVNetworkBuilder.class );

    /**
     * *********************************************************************
     * StrutsConstants
     */
    private static final int DEFAULT_MAX_PROTEIN = 500;

    private static int MAX_PROTEINS;

    public static final List<String> ALL_SOURCES;

    public static final List<String> NODE_SOURCES;

    public static final List<String> EDGE_SOURCES;

    private String currentQuery;

    static {
        // the default size should be big enough
        ALL_SOURCES = new ArrayList<String>( 4 );
        NODE_SOURCES = new ArrayList<String>( 2 );
        EDGE_SOURCES = new ArrayList<String>( 2 );

        // the properties which holds the information about the sources to
        // highlight
        Properties properties = IntactUserI.HIGHLIGHTING_PROPERTIES;

        if ( null != properties ) {
            // stores all allowed sources provided by the property
            String allowedNodeSources = IntactUserI.HIGHLIGHTING_PROPERTIES.getProperty( "highlightment.source.node.allowed" );
            String NodeSourceDelimiter = IntactUserI.HIGHLIGHTING_PROPERTIES.getProperty( "highlightment.source.node.token" );

            // if sources are found and if a delimiter token was found
            // the sources are split and each source is added to the source list
            if ( null != allowedNodeSources ) {
                if ( null != NodeSourceDelimiter ) {
                    StringTokenizer tokens = new StringTokenizer( allowedNodeSources, NodeSourceDelimiter );
                    while ( tokens.hasMoreTokens() ) {
                        // the current source is added to the source list
                        String nextToken = tokens.nextToken();
                        ALL_SOURCES.add( nextToken );
                        NODE_SOURCES.add( nextToken );
                    }
                } else {
                    logger.warn( "Unable to find the property highlightment.source.node.token" );
                }
            } else {
                logger.warn( "Unable to find the property highlightment.source.node.allowed" );
            }

            String allowedEdgeSources = IntactUserI.HIGHLIGHTING_PROPERTIES.getProperty( "highlightment.source.edge.allowed" );
            String EdgeSourceDelimiter = IntactUserI.HIGHLIGHTING_PROPERTIES.getProperty( "highlightment.source.edge.token" );

            // if sources are found and if a delimiter token was found
            // the sources are split and each source is added to the source list
            if ( null != allowedEdgeSources ) {
                if ( null != EdgeSourceDelimiter ) {
                    StringTokenizer tokens = new StringTokenizer( allowedEdgeSources, EdgeSourceDelimiter );
                    while ( tokens.hasMoreTokens() ) {
                        // the current source is added to the source list
                        String nextToken = tokens.nextToken();
                        ALL_SOURCES.add( nextToken );
                        EDGE_SOURCES.add( nextToken );
                    }
                } else {
                    logger.warn( "Unable to find the property highlightment.source.edge.token" );
                }
            } else {
                logger.warn( "Unable to find the property highlightment.source.edge.allowed" );
            }

        } else {
            logger.warn( "Unable to find the property files for the source highlight" );
        }

        properties = IntactUserI.GRAPH_PROPERTIES;
        String maxCentralProtein = null;

        // it is tried to parse the colors from the properties file
        if ( null != properties ) {
            maxCentralProtein = properties.getProperty( "hierarchView.graph.max.protein" );
        } else {
            logger.warn( "properties file GRAPH_PROPERTIES could not been read" );
        }

        // try to parse the maximal number of allowed central proteins
        // if the parsing fails the default value is taken
        try {
            MAX_PROTEINS = Integer.parseInt( maxCentralProtein );
        }
        catch ( NumberFormatException e ) {
            MAX_PROTEINS = DEFAULT_MAX_PROTEIN;
        }
    }

    private final DataService dataservice;

    private final BinaryGraphNetworkBuilder builder;

    private final InteractionBasedMerger merger;
//    private IntactUser user;

    public HVNetworkBuilder( DataService dataservice ) {
        this.dataservice = dataservice;

        builder = new BinaryGraphNetworkBuilder();
        builder.setLabelStrategy( new AliasLabelStrategy() );

        merger = new InteractionBasedMerger();
        merger.setLabelStrategy( new AliasLabelStrategy() );
    }


    public HVNetworkBuilder( IntactUserI intactUser ) {
//        user = ( IntactUser ) intactUser;
        dataservice = intactUser.getDataService();

        builder = new BinaryGraphNetworkBuilder();
        builder.setLabelStrategy( new AliasLabelStrategy() );

        merger = new InteractionBasedMerger();
        merger.setLabelStrategy( new AliasLabelStrategy() );
    }

    public static int getMaxInteractions() {
        return MAX_PROTEINS;
    }

    public Network buildBinaryGraphNetwork( String queryString ) throws HierarchViewDataException, MultipleResultException, ProteinNotFoundException {
        currentQuery = queryString;
        Collection<BinaryInteraction> bis = dataservice.getBinaryInteractionsByQueryString( queryString );
        Collection<String> centralProteinAcs = dataservice.getCentralProteins();

        Network network = new InteractionNetwork( builder.createGraphNetwork( bis, centralProteinAcs ) );
        network.setBinaryInteractions( bis );

        return network;
    }

    public Network fusionBinaryGraphNetwork( Network in, String queryString ) throws HierarchViewDataException, NetworkBuildException, MultipleResultException, ProteinNotFoundException {

        in.initNodes();
        in.initEdges();
        BinaryGraphNetwork network1 = ( BinaryGraphNetwork ) in.getGraphNetwork();

        Collection<BinaryInteraction> bis = dataservice.getBinaryInteractionsByQueryString( queryString );

        if ( bis != null && !bis.isEmpty() ) {
            Collection<String> centralProteinAcs = dataservice.getCentralProteins();

            try {

                BinaryGraphNetwork network2 = builder.createGraphNetwork( bis, centralProteinAcs );
                Network network = new InteractionNetwork( merger.mergeGraphNetworks( network1, network2 ) );
                network.setBinaryInteractions( bis );
                currentQuery = queryString;

                return network;

            } catch ( Exception e ) {
                throw new NetworkBuildException( "Could not build Network for " + centralProteinAcs );
            }
        }

        return in;
    }

    public Network expandBinaryGraphNetwork( Network network ) throws HierarchViewDataException, NetworkBuildException, MultipleResultException, ProteinNotFoundException {
        if ( network == null ) {
            throw new IllegalArgumentException( "Network must not be null. " );
        }

        network.initNodes();
        network.initEdges();
        StringBuffer query = new StringBuffer();
        Iterator<Node> iterator = network.getNodes().iterator();
        while ( iterator.hasNext() ) {
            query.append( iterator.next().getId() );
            if ( iterator.hasNext() ) query.append( ", " );
        }

        network = fusionBinaryGraphNetwork( network, query.toString() );
        return network;
    }
}




