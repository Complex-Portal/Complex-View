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
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.image.ImageDimension;
import uk.ac.ebi.intact.application.hierarchview.highlightment.source.AllHighlightmentSource;
import uk.ac.ebi.intact.searchengine.CriteriaBean;
import uk.ac.ebi.intact.service.graph.Edge;
import uk.ac.ebi.intact.service.graph.GraphNetwork;
import uk.ac.ebi.intact.service.graph.Node;
import uk.ac.ebi.intact.service.graph.binary.BinaryGraphNetwork;
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
public class BinaryInteractionNetwork implements Network {

    public static final Log logger = LogFactory.getLog( BinaryInteractionNetwork.class );

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

    private List<Node> nodeList;

    private Map sourceHighlightMap;

    private Collection<BinaryInteraction> binaryInteractions;

    private static final String INTERPRO = "interpro";

    private static final String GO = "go";

    private static final String ALL = "all";


    /**
     * Describe how the interaction network has been built, from which query
     * strings and what is the associated target e.g. the [ShortLabel ABC] and
     * [Xref DEF] That collection contains String[2] (0:queryString, 1:target)
     */
    private List<CriteriaBean> criteriaList;

    public BinaryInteractionNetwork( BinaryGraphNetwork network ) {
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

        nodeList = new ArrayList( network.getNodes() );

        dimension = new ImageDimension();
        criteriaList = new ArrayList();

        // a hashtable is taken to avoid null entries as key or as values
        // we only need the number of allowed sources - so the map is intialised
        // with the provided number of sources.
        sourceHighlightMap = new Hashtable( HVNetworkBuilder.SOURCES.size() );
        initNodes();
    }

    public GraphNetwork getGraphNetwork() {
        return network;
    }

    public NodeAttributes getNodeAttributes( Node node ) {
        return nodeAttributMap.get( node );
    }

    public int getBinaryInteractionSize() {
        return binaryInteractions.size();
    }

    public void setBinaryInteractions( Collection binaryInteractions ) {
        this.binaryInteractions = binaryInteractions;
    }

    public List getCriteria() {
        return criteriaList;
    }

    public boolean isSourceHighlightMapEmpty() {
        return sourceHighlightMap.isEmpty();
    }

    public Collection getEdges() {
        return network.getEdges();
    }

    public List<Node> getNodes() {
        return nodeList;
    }

    public List<Node> getBaitNodes() {
        List<Node> baitNodes = new ArrayList<Node>();
        for ( Node node : network.getNodes() ) {
            InteractorVertex vertex = ( InteractorVertex ) node;
            if ( vertex.isBait() || vertex.isNeutralComponent() ) {
                baitNodes.add( node );
            }
        }
        return baitNodes;
    }

    public ImageDimension getImageDimension() {
        return this.dimension;
    }

    public List getCentralNodes() {
        return new ArrayList( centralProteins );
    }

    public int getDatabaseTermCount( String goTermId ) {
        int count = 0;
        for ( Node node : getNodes() ) {
            InteractorVertex vertex = ( InteractorVertex ) node;
            if ( vertex.getPropertiesIds().contains( goTermId ) ) {
                count++;
            }
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

    public void addToSourceHighlightMap( String source, String sourceID, Object node ) {
        // the map for the given source is fetched
        Map sourceMap = ( Map ) sourceHighlightMap.get( source );

        // if no map exists a new one is created and put into the
        // sourceHighlightMap
        if ( sourceMap == null ) {
            sourceMap = new Hashtable();
            sourceHighlightMap.put( source, sourceMap );
        }

        // the nodes realted to the given sourceID are fetched
        Set sourceNodes = ( Set ) sourceMap.get( sourceID );

        // if no set exists a new one is created and put into the sourceMap
        if ( sourceNodes == null ) {
            // a hashset is used to avoid duplicate entries
            sourceNodes = new HashSet();
            sourceMap.put( sourceID, sourceNodes );
        }
        sourceNodes.add( node );
    }

    public void initSourceHighlightMap() {
        logger.debug( "Init SourceHighlightMap" );
        for ( Node node : getNodes() ) {
            InteractorVertex vertex = ( InteractorVertex ) node;
            for ( CrossReference property : vertex.getProperties() ) {
                if ( property.getDatabase().equals( INTERPRO ) ) {
                    addToSourceHighlightMap( INTERPRO, property.getIdentifier(), node );
                }

                if ( property.getDatabase().equals( GO ) ) {
                    addToSourceHighlightMap( GO, property.getIdentifier(), node );
                }

                addToSourceHighlightMap( ALL, property.getIdentifier(), node );
            }
        }
    }


    /**
     * Returns all proteins which are related to the given source (e.g. GO) and
     * the given sourceID (e.g. GO:001900).
     *
     * @param source   the source
     * @param sourceID the sourceID
     * @return a set of related proteins
     */
    public Set getProteinsForHighlight( String source, String sourceID ) {
        if ( logger.isDebugEnabled() ) {
            logger.info( "in getProteinsForHighlight looking for sourceID=" + sourceID );
        }

        // get the list of all the source terms allowed
        Properties properties = IntactUserI.HIGHLIGHTING_PROPERTIES;
        String sourceList = properties.getProperty( "highlightment.source.allowed" );

        // get the delimiter token
        String delimiter = IntactUserI.HIGHLIGHTING_PROPERTIES.getProperty( "highlightment.source.token" );

        // split the list with the delimiter
        String[] listSource = sourceList.split( delimiter );

        Map sourceMap = null;

        if ( source.equals( AllHighlightmentSource.SOURCE_KEY ) ) {

            Map sourceMapTmp;
            boolean init = false;

            // build the sourceMap with all source allowed first element, "All", is ignored
            for ( int i = 1; i < listSource.length; i++ ) {
                if ( !init ) {
                    sourceMap = ( Map ) sourceHighlightMap.get( listSource[i].toLowerCase() );
                    init = true;
                }
                sourceMapTmp = ( Map ) sourceHighlightMap.get( listSource[i].toLowerCase() );
                sourceMap.putAll( sourceMapTmp );
                //sourceMapTmp = null;
            }
        } else {
            sourceMap = ( Map ) sourceHighlightMap.get( source );
        }

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
