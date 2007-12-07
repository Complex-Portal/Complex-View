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
import uk.ac.ebi.intact.application.hierarchview.business.IntactUser;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.data.DataService;
import uk.ac.ebi.intact.application.hierarchview.exception.HierarchViewDataException;
import uk.ac.ebi.intact.application.hierarchview.exception.MultipleResultException;
import uk.ac.ebi.intact.application.hierarchview.exception.NetworkBuildException;
import uk.ac.ebi.intact.application.hierarchview.exception.ProteinNotFoundException;
import uk.ac.ebi.intact.service.graph.Node;
import uk.ac.ebi.intact.service.graph.binary.BinaryGraphNetwork;
import uk.ac.ebi.intact.service.graph.binary.BinaryGraphNetworkBuilder;
import uk.ac.ebi.intact.service.graph.binary.label.AliasLabelBuilder;
import uk.ac.ebi.intact.service.graph.binary.merger.BinaryGraphNetworkMerger;
import uk.ac.ebi.intact.service.graph.binary.merger.InteractionBasedMerger;

import java.util.*;

/**
 * TODO comment that class header
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 1.6.0-Snapshot
 */
public class HVNetworkBuilder {

    public static final Log logger = LogFactory.getLog( HVNetworkBuilder.class );

    /**
     * *********************************************************************
     * StrutsConstants
     */
    public static final int DEFAULT_MAX_CENTRAL_PROTEIN = 7;

    private static int MAX_CENTRAL_PROTEINS;

    public static final List<String> SOURCES;

    static {
        // the default size should be big enough
        SOURCES = new ArrayList<String>();

        // the properties which holds the information about the sources to
        // highlight
        Properties properties = IntactUserI.HIGHLIGHTING_PROPERTIES;

        if ( null != properties ) {
            // stores all allowed sources provided by the property
            String allowedSources = IntactUserI.HIGHLIGHTING_PROPERTIES.getProperty( "highlightment.source.allowed" );
            String delimiter = IntactUserI.HIGHLIGHTING_PROPERTIES.getProperty( "highlightment.source.token" );

            // if sources are found and if a delimiter token was found
            // the sources are split and each source is added to the source list
            if ( null != allowedSources ) {
                if ( null != delimiter ) {
                    StringTokenizer tokens = new StringTokenizer( allowedSources, delimiter );
                    while ( tokens.hasMoreTokens() ) {
                        // the current source is added to the source list
                        SOURCES.add( tokens.nextToken().toLowerCase() );
                    }
                } else {
                    logger.warn( "Unable to find the property highlightment.source.token" );
                }
            } else {
                logger.warn( "Unable to find the property highlightment.source.allowed" );
            }
        } else {
            logger.warn( "Unable to find the property files for the source highlight" );
        }

        properties = IntactUserI.GRAPH_PROPERTIES;
        String maxCentralProtein = null;

        // it is tried to parse the colors from the properties file
        if ( null != properties ) {
            maxCentralProtein = properties.getProperty( "hierarchView.graph.max.cental.protein" );
        } else {
            logger.warn( "properties file GRAPH_PROPERTIES could not been read" );
        }

        // try to parse the maximal number of allowed central proteins
        // if the parsing fails the default value is taken
        try {
            MAX_CENTRAL_PROTEINS = Integer.parseInt( maxCentralProtein );
        }
        catch ( NumberFormatException e ) {
            MAX_CENTRAL_PROTEINS = DEFAULT_MAX_CENTRAL_PROTEIN;
        }
    }

    private DataService dataservice;

    private BinaryGraphNetworkBuilder builder;
    private IntactUser user;

    public HVNetworkBuilder( DataService dataservice ) {
        this.dataservice = dataservice;

        builder = new BinaryGraphNetworkBuilder();
        builder.setLabelBuilder( new AliasLabelBuilder() );
    }


    public HVNetworkBuilder( IntactUserI intactUser ) {
        user = ( IntactUser ) intactUser;
        dataservice = intactUser.getDataService();

        builder = new BinaryGraphNetworkBuilder();
        builder.setLabelBuilder( new AliasLabelBuilder() );
    }

    public static int getMaxCentralProtein() {
        return MAX_CENTRAL_PROTEINS;
    }

    public Network buildBinaryGraphNetwork( String queryString ) throws HierarchViewDataException, MultipleResultException, ProteinNotFoundException {

        Collection<BinaryInteraction> bis = dataservice.getBinaryInteractionsByQueryString( queryString );
        Collection<String> centralProteinAcs = dataservice.getCentralProteins();

        Network network = new InteractionNetwork( builder.createGraphNetwork( bis, centralProteinAcs ) );
        network.setBinaryInteractions( bis );

        return network;
    }

    public Network fusionBinaryGraphNetwork( Network in, String queryString ) throws HierarchViewDataException, NetworkBuildException, MultipleResultException, ProteinNotFoundException {
        in.initNodes();
        BinaryGraphNetwork network1 = ( BinaryGraphNetwork ) in.getGraphNetwork();
        Collection<BinaryInteraction> bis = dataservice.getBinaryInteractionsByQueryString( queryString );

        if ( bis != null && !bis.isEmpty() ) {
            Collection<String> centralProteinAcs = dataservice.getCentralProteins();

            try {
                BinaryGraphNetwork network2 = builder.createGraphNetwork( bis, centralProteinAcs );
                BinaryGraphNetworkMerger merger = new InteractionBasedMerger();
                Network network = new InteractionNetwork( merger.mergeGraphNetworks( network1, network2 ) );
                network.setBinaryInteractions( bis );

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

        StringBuffer query = new StringBuffer();
        Iterator<Node> iterator = network.getBaitNodes().iterator();
        while ( iterator.hasNext() ) {
            query.append( iterator.next().getId() );
            if ( iterator.hasNext() ) query.append( ", " );
        }
        user.setQueryString( query.toString() );

        return fusionBinaryGraphNetwork( network, query.toString() );
    }
}




