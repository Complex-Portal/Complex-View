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
import psidev.psi.mi.tab.model.*;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUser;
import uk.ac.ebi.intact.application.hierarchview.business.image.ImageDimension;
import uk.ac.ebi.intact.application.hierarchview.highlightment.source.edge.ConfidenceHighlightmentSource;
import uk.ac.ebi.intact.application.hierarchview.highlightment.source.edge.PublicationHighlightmentSource;
import uk.ac.ebi.intact.application.hierarchview.highlightment.source.node.*;
import uk.ac.ebi.intact.service.graph.Edge;
import uk.ac.ebi.intact.service.graph.GraphNetwork;
import uk.ac.ebi.intact.service.graph.Node;
import uk.ac.ebi.intact.service.graph.binary.BinaryGraphNetwork;
import uk.ac.ebi.intact.service.graph.binary.BinaryInteractionEdge;
import uk.ac.ebi.intact.service.graph.binary.InteractorVertex;
import uk.ac.ebi.intact.tulip.client.TulipClient;
import uk.ac.ebi.intact.tulip.client.generated.ProteinCoordinate;

import javax.servlet.http.HttpServletRequest;
import java.rmi.RemoteException;
import java.util.*;

/**
 * Give a specific behaviour to the generic graph definition.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 1.6.0-SNAPSHOT
 */
public class InteractionNetwork implements Network {

    private static final Log logger = LogFactory.getLog( InteractionNetwork.class );

    /**
     * *********************************************************************
     * Instance variables
     */

    /**
     * Properties of the final image (size ...)
     */
    private ImageDimension dimension = null;

    private Collection<? extends Node> centralNodes;

    private BinaryGraphNetwork network;

    private Map<String, Node> nodeMap;

    private HashMap<String, Edge> edgeMap;

    private Map<String, NodeAttributes> nodeAttributMap;

    private Map<Edge, EdgeAttributes> edgeAttributMap;

    private static final int DEFAULT_DEPTH = 1;

    private int currentDepth;

    private Collection<BinaryInteraction> binaryInteractions;

    private static final String GO_KEY = GoHighlightmentSource.SOURCE_KEY;

    private static final String INTERPRO_KEY = InterproHighlightmentSource.SOURCE_KEY;

    public static final String BOTH = "both";

    public InteractionNetwork( BinaryGraphNetwork network ) {
        dimension = new ImageDimension();

        this.network = network;
        centralNodes = new ArrayList<Node>( network.getCentralNodes() );

        initNodeAttributeMap();
        initEdgeAttributeMap();

        initNodes();
        initEdges();

        //initHighlightMap(request);

        setDepthToDefault();

        binaryInteractions = network.getBinaryInteractions();
    }

    private void initEdgeAttributeMap() {
        edgeAttributMap = new HashMap<Edge, EdgeAttributes>();
        for ( Edge edge : getEdges() ) {
            if ( !edgeAttributMap.containsKey( edge ) ) {
                EdgeAttributes attribute = new EdgeAttributes( edge );
                edgeAttributMap.put( edge, attribute );
            }
        }
    }

    private void initNodeAttributeMap() {
        nodeAttributMap = new HashMap<String, NodeAttributes>();
        for ( Node node : network.getNodes() ) {
            if ( !nodeAttributMap.containsKey( node.getId() ) ) {
                NodeAttributes attribute = new NodeAttributes( node );
                nodeAttributMap.put( node.getId(), attribute );
            }
        }
    }
    /////////////////////////
    // Getters & Setters

    /**
     * Increase the currentDepth of the interaction network.
     */
    public void increaseDepth() {
        currentDepth++;
    }

    /**
     * Desacrease the currentDepth of the interraction network.
     */
    public void decreaseDepth() {
        if ( currentDepth > 1 ) {
            currentDepth--;
        } else {
            logger.error( "Could not decrease! Current currentDepth is " + currentDepth );
        }
    }

    public void setDepth( int depth ) {
        currentDepth = depth;
    }

    public void setDepthToDefault() {
        // read the Graph.properties file
        Properties properties = IntactUser.GRAPH_PROPERTIES;

        if ( null != properties ) {
            String depth = properties.getProperty( "hierarchView.graph.currentDepth.default" );
            int defaultDepth;
            if ( depth != null ) {
                defaultDepth = Integer.parseInt( depth );
            } else {
                defaultDepth = DEFAULT_DEPTH;
            }
            currentDepth = defaultDepth;
        }
    }

    public GraphNetwork getGraphNetwork() {
        return network;
    }

    public Collection<CrossReference> getProperties( Node node ) {
        return ( ( InteractorVertex ) node ).getProperties();
    }

    public NodeAttributes getNodeAttributes( String nodeId ) {
        return nodeAttributMap.get( nodeId );
    }

    public EdgeAttributes getEdgeAttributes( Edge edge ) {
        return edgeAttributMap.get( edge );
    }

    public Collection getBinaryInteraction() {
        return binaryInteractions;
    }

    public Collection<Edge> getEdges() {
        return network.getEdges();
    }

    public Collection<Node> getNodes() {
        return new ArrayList<Node>( network.getNodes() );
    }

    public Set<Node> getNodesByIds( Set<String> nodeIds ) {
        if (nodeIds == null) return null;
        Set<Node> nodes = new HashSet<Node>(nodeIds.size());
        for (String id : nodeIds){
            nodes.add(nodeMap.get( id ));
        }
        return nodes;
    }

    public Set<Edge> getEdgesByIds( Set<String> edgeIds ) {
        if (edgeIds == null) return null;
        Set<Edge> edges = new HashSet<Edge>(edgeIds.size());
        for (String id : edgeIds){
            edges.add(edgeMap.get( id ));
        }
        return edges;
    }

    public int getCurrentDepth() {
        return currentDepth;
    }

    public ImageDimension getImageDimension() {
        return dimension;
    }

    public List getCentralNodes() {
        return new ArrayList( centralNodes );
    }

    public void initNodes() {
        nodeMap = new HashMap<String, Node>();
        if ( getNodes() != null ) {
            for ( Node node : getNodes() ) {
                NodeAttributes attributes = getNodeAttributes( node.getId() );
                attributes.put( Constants.ATTRIBUTE_COLOR_NODE, NodeAttributes.NODE_COLOR );
                attributes.put( Constants.ATTRIBUTE_COLOR_LABEL, NodeAttributes.LABEL_COLOR );
                attributes.put( Constants.ATTRIBUTE_VISIBLE, Boolean.TRUE );
                nodeMap.put(node.getId(), node);
            }
        }
    }

    public void initEdges() {
        edgeMap = new HashMap<String, Edge>();
        if ( getEdges() != null ) {
            for ( Edge edge : getEdges() ) {
                EdgeAttributes attributes = getEdgeAttributes( edge );
                attributes.put( Constants.ATTRIBUTE_COLOR_EDGE, EdgeAttributes.EDGE_COLOR );
                attributes.put( Constants.ATTRIBUTE_VISIBLE, Boolean.TRUE );
                edgeMap.put(edge.getId(), edge);
            }
        }
    }

    public void initHighlightMap(HttpServletRequest request) {
        logger.debug( "Init SourceHighlightMap" );


        // clear existing highlights
        ConfidenceHighlightmentSource.getInstance(request).prepare();
        PublicationHighlightmentSource.getInstance(request).prepare();
        GoHighlightmentSource.getInstance(request).prepare();
        InterproHighlightmentSource.getInstance(request).prepare();
        MoleculeTypeHighlightmentSource.getInstance(request).prepare();
        RoleHighlightmentSource.getInstance(request).prepare();
        SpeciesHighlightmentSource.getInstance(request).prepare();

        CrossReferenceFactory factory = CrossReferenceFactory.getInstance();
        for ( Node node : getNodes() ) {
            InteractorVertex vertex = ( InteractorVertex ) node;
            if ( vertex.getProperties() != null && !vertex.getProperties().isEmpty() ) {
                for ( CrossReference property : vertex.getProperties() ) {
                    if ( property.getDatabase().equalsIgnoreCase( INTERPRO_KEY ) ) {
                        String key = property.getIdentifier();
                        InterproHighlightmentSource.getInstance(request).addToSourceMap( key, property );
                        InterproHighlightmentSource.getInstance(request).addToNodeMap( key, node );
                    }

                    if ( property.getDatabase().equalsIgnoreCase( GO_KEY ) ) {
                        String key = property.getIdentifier();
                        GoHighlightmentSource.getInstance(request).addToSourceMap( key, property );
                        GoHighlightmentSource.getInstance(request).addToNodeMap( key, node );
                    }
                }
            }

            if ( vertex.getExperimentalRoles() != null && !vertex.getExperimentalRoles().isEmpty() ) {

                for ( CrossReference role : vertex.getExperimentalRoles() ) {
                    String key = "E|";
                    if ( role.getIdentifier().contains( ":" ) ) {
                        key += role.getIdentifier();
                    } else {
                        key += role.getDatabase() + ":" + role.getIdentifier();
                    }
                    RoleHighlightmentSource.getInstance(request).addToSourceMap( key, role );
                    RoleHighlightmentSource.getInstance(request).addToNodeMap( key, node );
                }
                if ( vertex.isBaitWithExperimental() && vertex.isPreyWithExperimental() ) {
                    String key = "E|" + BOTH;
                    RoleHighlightmentSource.getInstance(request).addToSourceMap( key, factory.build( "MI", BOTH, "bait & prey" ) );
                    RoleHighlightmentSource.getInstance(request).addToNodeMap( key, node );
                }
            }

            if ( vertex.getBiologicalRoles() != null && !vertex.getBiologicalRoles().isEmpty() ) {

                for ( CrossReference role : vertex.getBiologicalRoles() ) {
                    String key = "B|";
                    if ( role.getIdentifier().contains( ":" ) ) {
                        key += role.getIdentifier();
                    } else {
                        key += role.getDatabase() + ":" + role.getIdentifier();
                    }
                    RoleHighlightmentSource.getInstance(request).addToSourceMap( key, role );
                    RoleHighlightmentSource.getInstance(request).addToNodeMap( key, node );
                }
                if ( vertex.isBaitWithBiological() && vertex.isPreyWithBiological() ) {
                    String key = "B|" + BOTH;
                    RoleHighlightmentSource.getInstance(request).addToSourceMap( key, factory.build( "MI", BOTH, "bait & prey" ) );
                    RoleHighlightmentSource.getInstance(request).addToNodeMap( key, node );
                }
            }

            if ( vertex.getInteractorType() != null ) {
                for ( CrossReference interactorType : vertex.getInteractorType() ) {
                    String key = null;
                    if ( interactorType.getIdentifier().contains( ":" ) ) {
                        key = interactorType.getIdentifier();
                    } else {
                        key = interactorType.getDatabase() + ":" + interactorType.getIdentifier();
                    }
                    MoleculeTypeHighlightmentSource.getInstance(request).addToSourceMap( key, interactorType );
                    MoleculeTypeHighlightmentSource.getInstance(request).addToNodeMap( key, node );
                }
            }
            Organism organism = vertex.getOrganism();
            if ( organism != null ) {
                String key = organism.getTaxid();
                Collection<CrossReference> organsimIDs = organism.getIdentifiers();
                if ( organsimIDs != null && !organsimIDs.isEmpty() ) {
                    SpeciesHighlightmentSource.getInstance(request).addToSourceMap( key, organism );
                    SpeciesHighlightmentSource.getInstance(request).addToNodeMap( key, node );
                } else {
                    logger.error( "Could not found organism identifiers for " + organism + " of interactor " + vertex.getId() );
                }
            }
        }

        for ( Object e : getEdges() ) {
            BinaryInteractionEdge edge = ( BinaryInteractionEdge ) e;
            for ( Confidence confidence : edge.getConfidenceValues() ) {
                String value = confidence.getValue();
                ConfidenceHighlightmentSource.getInstance(request).addToSourceMap( value, confidence );
                ConfidenceHighlightmentSource.getInstance(request).addToEdgeMap( value, edge );
//                addToEdgeHighlightMap( CONFIDENCE, value, edge );
            }

            List<Author> authors = edge.getAuthors();
            List<CrossReference> publications = edge.getPublication();
            if ( publications != null || !publications.isEmpty() ) {
                int i = 0;
                for ( CrossReference publication : edge.getPublication() ) {
                    String pmid = publication.getIdentifier();

                    if ( authors != null && !authors.isEmpty() ) {
                        if ( i < authors.size() ) {
                            if ( authors.get( i ) != null ) {
                                PublicationHighlightmentSource.getInstance(request).addToSourceMap( pmid, authors.get( i ) );
                                i++;
                            } else {
                                logger.error( "No Author available for PUBMED " + pmid );
                            }
                        } else {
                            logger.error( "We have " + edge.getPublication().size() + " publications " +
                                          "which not mapping to " + edge.getAuthors().size() + " authors." +
                                          "-> Error happens when I want to map " + pmid +
                                          " for Interaction " + edge.getBinaryInteraction().getInteractionAcs().get( 1 ) );
                        }
                    }
                    PublicationHighlightmentSource.getInstance(request).addToEdgeMap( pmid, edge );
                }
            }
        }
    }

    /**
     * Create a String giving informations for the Tulip treatment the
     * informations are : - the number of nodes, - the whole of the edges and
     * nodes associeted for each edge, - the label of each node.
     *
     * @return an object String
     */
    public String exportTlp() {
        // TODO : could be possible to optimize the size of the string buffer to * avoid as much as possible to extend the buffer size.
        Edge edge;
        StringBuilder out = new StringBuilder( 512 );
        String separator = System.getProperty( "line.separator" );
        int i, max;


        out.append( "(nodes " );
        List<Node> orderedNodeList = new ArrayList( network.getNodes() );
        max = orderedNodeList.size();
        for ( i = 1; i <= max; i++ )
            out.append( i ).append( ' ' );

        out.append( ')' ).append( separator );
        Object[] myEdges = network.getEdges().toArray();

        max = getEdges().size();
        for ( i = 1; i <= max; i++ ) {
            edge = ( Edge ) myEdges[i - 1];
            out.append( "(edge " ).append( i ).append( " " );
            out.append( orderedNodeList.indexOf( edge.getNodeA() ) + 1 ).append( " " );
            out.append( orderedNodeList.indexOf( edge.getNodeB() ) + 1 ).append( ")" ).append( separator );
        }
        return out.toString();
    } // exportTlp

    public String exportJavascript( float rateX, float rateY, int borderSize ) {
        StringBuilder out = new StringBuilder( 512 );

        // get the list of nodes and the dimension of the image
        ImageDimension dimension = this.getImageDimension();
        Collection<Node> proteinList = this.getNodes();

        // declaration of a Javascript Array
        out.append( "var data = new Array(" );
        int i = 0;
        for ( Node node : proteinList ) {
            NodeAttributes attributes = getNodeAttributes( node.getId() );
            float proteinX = ( Float ) attributes.get( Constants.ATTRIBUTE_COORDINATE_X );
            float proteinY = ( Float ) attributes.get( Constants.ATTRIBUTE_COORDINATE_Y );
            float proteinLength = ( Float ) attributes.get( Constants.ATTRIBUTE_LENGTH );
            float proteinHeight = ( Float ) attributes.get( Constants.ATTRIBUTE_HEIGHT );

            if ( logger.isDebugEnabled() ) {
                logger.debug( "Initial coordinates of node : " + node.getId() + " | X=" + proteinX + " | Y=" + proteinY );
            }

            // convertion to real coordinates
            float x = ( proteinX - dimension.xmin() ) / rateX - ( proteinLength / 2 ) + borderSize;
            float y = ( proteinY - dimension.ymin() ) / rateY - ( proteinHeight / 2 ) + borderSize;

            if ( logger.isDebugEnabled() ) {
                logger.debug( "Real coordinates : X=" + x + " | Y=" + y );
            }

            out.append( "new Array(\"" ).append( node.getId() )
                    .append( "\",\"" ).append( x )
                    .append( "\",\"" ).append( y )
                    .append( "\")" );
            if ( i < proteinList.size() - 1 ) {
                out.append( ", " );
            }
            i++;
        }

        out.append( ");" );

        return out.toString();
    }

    /**
     * Send a String to Tulip to calculate coordinates Enter the obtained
     * coordinates in the graph.
     *
     * @param dataTlp The obtained String by the exportTlp() method
     * @return an array of error message or <b>null </b> if no error occurs.
     */
    public String[] importDataToImage( String dataTlp ) throws RemoteException {

        ProteinCoordinate[] result;
        TulipClient client = new TulipClient();

        // Call Tulip Web Service : get
        try {
            result = client.getComputedTlpContent( dataTlp );
        } catch ( RemoteException e ) {
            logger.error( "couldn't get coodinate from the TLP content", e );
            throw e;
        }

        if ( null == result ) {
            // throw new IOException ("Tulip send back no data.");
            String[] errors = client.getErrorMessages( true );
            logger.warn( errors.length + " error(s) returned by the Tulip web service" );
            return errors;

        } else {

            // update protein coordinates
            ProteinCoordinate p;
            Float x, y;
            Node protein;

            for ( ProteinCoordinate aResult : result ) {
                p = aResult;

                x = p.getX();
                y = p.getY();

                // nodes are labelled from 1 to n int the tlp file and from 0 to
                // n-1 int the collection.
                List<Node> nodes = new ArrayList<Node>( getNodes() );
                protein = nodes.get( p.getId() - 1 );

                // Store coordinates in the protein
                NodeAttributes attribute = getNodeAttributes( protein.getId() );
                attribute.put( Constants.ATTRIBUTE_COORDINATE_X, x );
                attribute.put( Constants.ATTRIBUTE_COORDINATE_Y, y );
            } // for

            logger.info( "Protein coordinates updated" );
        } // else

        return null;
    } //importDataToImage
}