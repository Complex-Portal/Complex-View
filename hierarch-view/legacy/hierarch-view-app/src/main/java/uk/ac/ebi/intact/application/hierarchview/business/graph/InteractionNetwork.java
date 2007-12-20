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
import uk.ac.ebi.intact.application.hierarchview.business.image.ImageDimension;
import uk.ac.ebi.intact.application.hierarchview.highlightment.source.edge.ConfidenceHighlightmentSource;
import uk.ac.ebi.intact.application.hierarchview.highlightment.source.edge.PMIDHighlightmentSource;
import uk.ac.ebi.intact.application.hierarchview.highlightment.source.node.GoHighlightmentSource;
import uk.ac.ebi.intact.application.hierarchview.highlightment.source.node.InterproHighlightmentSource;
import uk.ac.ebi.intact.application.hierarchview.highlightment.source.node.MoleculeTypeHighlightmentSource;
import uk.ac.ebi.intact.application.hierarchview.highlightment.source.node.RoleHighlightmentSource;
import uk.ac.ebi.intact.searchengine.CriteriaBean;
import uk.ac.ebi.intact.service.graph.Edge;
import uk.ac.ebi.intact.service.graph.GraphNetwork;
import uk.ac.ebi.intact.service.graph.Node;
import uk.ac.ebi.intact.service.graph.binary.BinaryGraphNetwork;
import uk.ac.ebi.intact.service.graph.binary.BinaryInteractionEdge;
import uk.ac.ebi.intact.service.graph.binary.InteractorVertex;
import uk.ac.ebi.intact.tulip.client.TulipClient;
import uk.ac.ebi.intact.tulip.client.generated.ProteinCoordinate;

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

    public static final Log logger = LogFactory.getLog( InteractionNetwork.class );

    /**
     * *********************************************************************
     * Instance variables
     */

    /**
     * Properties of the final image (size ...)
     */
    private ImageDimension dimension = null;

    private Collection<? extends Node> centralProteins;

    private BinaryGraphNetwork network;

    private Map<Node, NodeAttributes> nodeAttributMap;

    private Map<Edge, EdgeAttributes> edgeAttributMap;

    private List<Node> nodeList;

    private Map nodeHighlightMap;

    private Map edgeHighlightMap;

    private Collection<BinaryInteraction> binaryInteractions;

    private static final String GO = GoHighlightmentSource.SOURCE_KEY;

    private static final String INTERPRO = InterproHighlightmentSource.SOURCE_KEY;

    private static final String ROLE = RoleHighlightmentSource.SOURCE_KEY;

    private static final String CONFIDENCE = ConfidenceHighlightmentSource.SOURCE_KEY;

    private static final String PMID = PMIDHighlightmentSource.SOURCE_KEY;

    private static final String MOLECULE_TYPE = MoleculeTypeHighlightmentSource.SOURCE_KEY;

    /**
     * Describe how the interaction network has been built, from which query
     * strings and what is the associated target e.g. the [ShortLabel ABC] and
     * [Xref DEF] That collection contains String[2] (0:queryString, 1:target)
     */
    private List<CriteriaBean> criteriaList;
    private Map<String, CrossReference> referenceMap;
    private HashMap<String, Author> authorMap;

    public Author getAuthorByPMID( String pmid ) {
        return authorMap.get( pmid );
    }

    public InteractionNetwork( BinaryGraphNetwork network ) {
        this.network = network;
        centralProteins = new ArrayList<Node>();

        nodeAttributMap = new HashMap<Node, NodeAttributes>();
        for ( Node node : network.getNodes() ) {
            if ( !nodeAttributMap.containsKey( node ) ) {
                NodeAttributes attribute = new NodeAttributes( node );
                nodeAttributMap.put( node, attribute );
                centralProteins = network.getCentralNodes();
            }
        }

        edgeAttributMap = new HashMap<Edge, EdgeAttributes>();
        for ( Edge edge : getEdges() ) {
            if ( !edgeAttributMap.containsKey( edge ) ) {
                EdgeAttributes attribute = new EdgeAttributes( edge );
                edgeAttributMap.put( edge, attribute );
            }
        }

        nodeList = new ArrayList( network.getNodes() );

        dimension = new ImageDimension();
        criteriaList = new ArrayList();

        // a hashtable is taken to avoid null entries as key or as values
        // we only need the number of allowed sources - so the map is intialised
        // with the provided number of sources.
        nodeHighlightMap = new Hashtable( HVNetworkBuilder.NODE_SOURCES.size() );
        edgeHighlightMap = new Hashtable( HVNetworkBuilder.EDGE_SOURCES.size() );
        initNodes();
        initEdges();
    }

    public GraphNetwork getGraphNetwork() {
        return network;
    }

    /**
     * Gives the HighlightMap of the Nodes
     *
     * @return a not null Map of <SOURCE,<SOURCEID,NODE>>
     */
    public Map getNodeHighlightMap() {
        if ( nodeHighlightMap == null ) {
            nodeHighlightMap = new Hashtable( HVNetworkBuilder.NODE_SOURCES.size() );
        }
        return nodeHighlightMap;
    }

    /**
     * Gives the HighlightMap of the Edges
     *
     * @return a not null Map of <SOURCE,<SOURCEID,EDGE>>
     */
    public Map getEdgeHighlightMap() {
        if ( edgeHighlightMap == null ) {
            edgeHighlightMap = new Hashtable( HVNetworkBuilder.EDGE_SOURCES.size() );
        }
        return edgeHighlightMap;
    }

    public Collection<CrossReference> getProperties( Node node ) {
        return ( ( InteractorVertex ) node ).getProperties();
    }

    public NodeAttributes getNodeAttributes( Node node ) {
        return nodeAttributMap.get( node );
    }

    public EdgeAttributes getEdgeAttributes( Edge edge ) {
        return edgeAttributMap.get( edge );
    }

    public Collection getBinaryInteraction() {
        return binaryInteractions;
    }

    public void setBinaryInteractions( Collection binaryInteractions ) {
        this.binaryInteractions = binaryInteractions;
    }

    public List getCriteria() {
        return criteriaList;
    }

    public boolean isNodeHighlightMapEmpty() {
        return nodeHighlightMap.isEmpty();
    }

    public boolean isEdgeHighlightMapEmpty() {
        return edgeHighlightMap.isEmpty();
    }

    public Collection<Edge> getEdges() {
        return network.getEdges();
    }

    public List<Node> getNodes() {
        return nodeList;
    }

    public ImageDimension getImageDimension() {
        return this.dimension;
    }

    public List getCentralNodes() {
        return new ArrayList( centralProteins );
    }

    public int getDatabaseTermCount( String source, String id ) {
        int count = 0;

        if ( HVNetworkBuilder.NODE_SOURCES.contains( source ) ) {
            if ( isNodeHighlightMapEmpty() ) initHighlightMap();
            count = getNodesForHighlight( source, id ).size();
        }

        if ( HVNetworkBuilder.EDGE_SOURCES.contains( source ) ) {
            if ( isEdgeHighlightMapEmpty() ) initHighlightMap();
            count = getEdgesForHighlight( source, id ).size();
        }

        return count;
    }

    public void initNodes() {
        for ( Node node : network.getNodes() ) {
            NodeAttributes attributes = getNodeAttributes( node );
            attributes.put( Constants.ATTRIBUTE_COLOR_NODE, NodeAttributes.NODE_COLOR );
            attributes.put( Constants.ATTRIBUTE_COLOR_LABEL, NodeAttributes.LABEL_COLOR );
            attributes.put( Constants.ATTRIBUTE_VISIBLE, Boolean.TRUE );
        }
    }

    public void initEdges() {
        for ( Edge edge : getEdges() ) {
            EdgeAttributes attributes = getEdgeAttributes( edge );
            attributes.put( Constants.ATTRIBUTE_COLOR_EDGE, EdgeAttributes.EDGE_COLOR );
            attributes.put( Constants.ATTRIBUTE_VISIBLE, Boolean.TRUE );
        }
    }

    /**
     * Adds a new node to the source map for the given source (e.g. GO) and the
     * given source id (e.g. GO:001900).
     * <p/>
     * If the map already has the source id as key the node is added to the set
     * of other nodes for this source.
     * <p/>
     * Otherwise a new set is created and the node is added to it.
     *
     * @param source   the source to highlight (e.g. GO)
     * @param sourceID the source id (e.g. GO:001900)
     * @param node     the node which is related to the sourceID
     */

    public void addToNodeHighlightMap( String source, String sourceID, Node node ) {
        // the map for the given source is fetched
        Map<String, Set<Node>> sourceMap = ( Map ) nodeHighlightMap.get( source );

        // if no map exists a new one is created and put into the nodeHighlightMap
        if ( sourceMap == null ) {
            sourceMap = new Hashtable<String, Set<Node>>();
            nodeHighlightMap.put( source, sourceMap );
        }

        // the nodes realted to the given sourceID are fetched
        Set<Node> sourceNodes = sourceMap.get( sourceID );

        // if no set exists a new one is created and put into the sourceMap
        if ( sourceNodes == null ) {
            // a hashset is used to avoid duplicate entries
            sourceNodes = new HashSet<Node>();
            sourceMap.put( sourceID, sourceNodes );
        }
        sourceNodes.add( node );
    }

    public void addToEdgeHighlightMap( String source, String sourceID, Edge edge ) {
        // the map for the given source is fetched
        Map sourceMap = ( Map ) edgeHighlightMap.get( source );

        // if no map exists a new one is created and put into the
        // nodeHighlightMap
        if ( sourceMap == null ) {
            sourceMap = new Hashtable();
            edgeHighlightMap.put( source, sourceMap );
        }

        // the nodes realted to the given sourceID are fetched
        Set sourceEdges = ( Set ) sourceMap.get( sourceID );

        // if no set exists a new one is created and put into the sourceMap
        if ( sourceEdges == null ) {
            // a hashset is used to avoid duplicate entries
            sourceEdges = new HashSet();
            sourceMap.put( sourceID, sourceEdges );
        }
        sourceEdges.add( edge );

    }

    public void initHighlightMap() {
        logger.debug( "Init SourceHighlightMap" );

        CrossReferenceFactory factory = CrossReferenceFactory.getInstance();
        for ( Node node : getNodes() ) {
            InteractorVertex vertex = ( InteractorVertex ) node;
            if ( vertex.getProperties() != null && !vertex.getProperties().isEmpty() ) {
                for ( CrossReference property : vertex.getProperties() ) {
                    if ( property.getDatabase().equalsIgnoreCase( INTERPRO ) ) {
                        String key = property.getIdentifier();
                        addToReferenceList( key, property );
                        addToNodeHighlightMap( INTERPRO, key, node );
                    }

                    if ( property.getDatabase().equalsIgnoreCase( GO ) ) {
                        String key = property.getIdentifier();
                        addToReferenceList( key, property );
                        addToNodeHighlightMap( GO, key, node );
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
                    addToReferenceList( key, role );
                    addToNodeHighlightMap( ROLE, key, node );
                }
                if ( vertex.isBait_Experimental() && vertex.isPrey_Experimental() ) {
                    String key = "E|" + Constants.BOTH;
                    addToReferenceList( key, factory.build( "MI", Constants.BOTH, Constants.BAIT + "&" + Constants.PREY ) );
                    addToNodeHighlightMap( ROLE, key, node );
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
                    addToReferenceList( key, role );
                    addToNodeHighlightMap( ROLE, key, node );
                }
                if ( vertex.isBait_Biological() && vertex.isPrey_Biological() ) {
                    String key = "B|" + Constants.BOTH;
                    addToReferenceList( key, factory.build( "MI", Constants.BOTH, Constants.BAIT + "&" + Constants.PREY ) );
                    addToNodeHighlightMap( ROLE, key, node );
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
                    addToReferenceList( key, interactorType );
                    addToNodeHighlightMap( MOLECULE_TYPE, key, node );
                }
            }
        }

        List<double[]> ranges = new ArrayList<double[]>();
        ranges.add( new double[]{0.0, 0.2} );
        ranges.add( new double[]{0.2, 0.4} );
        ranges.add( new double[]{0.4, 0.6} );
        ranges.add( new double[]{0.6, 0.8} );
        ranges.add( new double[]{0.8, 1.0} );

        authorMap = new HashMap<String, Author>();
        for ( Object e : getEdges() ) {
            BinaryInteractionEdge edge = ( BinaryInteractionEdge ) e;
            for ( Confidence confidence : edge.getConfidenceValues() ) {
                double value = Double.valueOf( confidence.getValue() );
                String sourceId = "";
                for ( double[] range : ranges ) {
                    if ( value > range[0] && value <= range[1] ) {
                        sourceId = "Range: from >" + range[0] + " to <=" + range[1];
                        break;
                    }
                }
                addToEdgeHighlightMap( CONFIDENCE, sourceId, edge );
            }

            List<Author> authors = edge.getAuthors();
            List<CrossReference> publications = edge.getPublication();
            if ( publications != null || !publications.isEmpty() ) {
                int i = 0;
                for ( CrossReference publication : edge.getPublication() ) {
                    String pmid = publication.getIdentifier();

                    if ( authors != null && !authors.isEmpty() && authors.get( i ) != null ) {
                        authorMap.put( pmid, authors.get( i ) );
                        i++;
                    }
                    addToEdgeHighlightMap( PMID, pmid, edge );
                }
            }
        }
    }

    private void addToReferenceList( String key, CrossReference value ) {
        if ( referenceMap == null ) {
            referenceMap = new HashMap<String, CrossReference>();
        }
        referenceMap.put( key, value );
    }

    public CrossReference getCrossReferenceById( String key ) {
        return referenceMap.get( key );
    }


    /**
     * Returns all proteins which are related to the given source (e.g. GO) and
     * the given sourceID (e.g. GO:001900).
     *
     * @param source   the source
     * @param sourceID the sourceID
     * @return a set of related proteins
     */
    public Set getNodesForHighlight( String source, String sourceID ) {

        if ( logger.isInfoEnabled() ) {
            logger.info( "getNodesForHighlight: source=" + source + " | sourceID=" + sourceID );
        }

        Map sourceMap = ( Map ) nodeHighlightMap.get( source );

        if ( logger.isDebugEnabled() ) {
            logger.debug( "sourceMap = " + sourceMap );
        }

        // if no nodes are given for the provided source null is returned
        if ( sourceMap == null ) {
            logger.warn( "sourceMap is null !" );
            return null;
        }

        return ( Set ) sourceMap.get( sourceID );
    }

    public Set<Edge> getEdgesForHighlight( String source, String sourceID ) {

        if ( logger.isInfoEnabled() ) {
            logger.info( "getEdgesForHighlight: source=" + source + " | sourceID=" + sourceID );
        }

        Map sourceMap = ( Map ) edgeHighlightMap.get( source );

        if ( logger.isDebugEnabled() ) {
            logger.debug( "sourceMap = " + sourceMap );
        }

        // if no nodes are given for the provided source null is returned
        if ( sourceMap == null ) {
            logger.warn( "sourceMap is null !" );
            return null;
        }

        return ( Set ) sourceMap.get( sourceID );
    }

    /**
     * Add a new criteria to the interaction network <br>
     *
     * @param aCriteria the criteria to add if it doesn't exist in the
     *                  collection already
     */
    public void addCriteria( CriteriaBean aCriteria ) {
        if ( !criteriaList.contains( aCriteria ) )
            criteriaList.add( aCriteria );
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
        List<Node> proteinList = this.getNodes();

        // declaration of a Javascript Array
        out.append( "var data = new Array(" );
        int i = 0;
        for ( Node node : proteinList ) {
            NodeAttributes attributes = getNodeAttributes( node );
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
                protein = nodeList.get( p.getId() - 1 );

                // Store coordinates in the protein
                NodeAttributes attribute = getNodeAttributes( protein );
                attribute.put( Constants.ATTRIBUTE_COORDINATE_X, x );
                attribute.put( Constants.ATTRIBUTE_COORDINATE_Y, y );
            } // for

            logger.info( "Protein coordinates updated" );
        } // else

        return null;
    } //importDataToImage
}

//            if ( vertex.getExperimentalRole() != null && !vertex.getExperimentalRole().isEmpty() ) {
//                CrossReference bait = null;
//                CrossReference prey = null;
//                boolean isBoth = false;
//
//                for ( CrossReference xref : vertex.getExperimentalRole() ) {
//                    String id = xref.getIdentifier();
//                    if ( id.equals( Constants.PREY_REF ) ) {
//                        prey = xref;
//                    }
//                    if ( id.equals( Constants.BAIT_REF ) ) {
//                        bait = xref;
//                    }
//                    if ( bait != null && prey != null ) {
//                        isBoth = true;
//                        break;
//                    }
//                }
//                if ( bait != null ) {
//                    String key = Constants.EXPERMIENTAL_ROLE + ":" + bait.getDatabase() + ":" + bait.getIdentifier();
//                    addToReferenceList( key, bait );
//                    addToNodeHighlightMap( ROLE, key, node );
//                }
//                if ( prey != null ) {
//                    String key = Constants.EXPERMIENTAL_ROLE + ":" + prey.getDatabase() + ":" + prey.getIdentifier();
//                    addToReferenceList( key, prey );
//                    addToNodeHighlightMap( ROLE, key, node );
//                }
//                if ( isBoth ) {
//                    String key = Constants.EXPERMIENTAL_ROLE + ":" + Constants.DATABASE + ":" + Constants.BOTH_REF;
//                    addToReferenceList( key, factory.build( Constants.DATABASE, Constants.BOTH_REF, Constants.BOTH ) );
//                    addToNodeHighlightMap( ROLE, key, node );
//                }
//            }
//
//            if ( vertex.getBiologicalRole() != null && !vertex.getBiologicalRole().isEmpty() ) {
//                CrossReference bait = null;
//                CrossReference prey = null;
//                boolean isBoth = false;
//
//                for ( CrossReference xref : vertex.getBiologicalRole() ) {
//                    String id = xref.getIdentifier();
//                    if ( id.equals( Constants.PREY_REF ) ) {
//                        prey = xref;
//                    }
//                    if ( id.equals( Constants.BAIT_REF ) ) {
//                        bait = xref;
//                    }
//                    if ( bait != null && prey != null ) {
//                        isBoth = true;
//                        break;
//                    }
//                }
//                if ( bait != null ) {
//                    String key = Constants.BIOLOGICAL_ROLE + ":" + bait.getDatabase() + ":" + bait.getIdentifier();
//                    addToReferenceList( key, bait );
//                    addToNodeHighlightMap( ROLE, key, node );
//                }
//                if ( prey != null ) {
//                    String key = Constants.BIOLOGICAL_ROLE + ":" + prey.getDatabase() + ":" + prey.getIdentifier();
//                    addToReferenceList( key, prey );
//                    addToNodeHighlightMap( ROLE, key, node );
//                }
//                if ( isBoth ) {
//                    String key = Constants.BIOLOGICAL_ROLE + ":" + Constants.DATABASE + ":" + Constants.BOTH_REF;
//                    addToReferenceList( key, factory.build( Constants.DATABASE, Constants.BOTH_REF, Constants.BOTH ) );
//                    addToNodeHighlightMap( ROLE, key, node );
//                }
//            }