/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */

package uk.ac.ebi.intact.application.hierarchview.business.graph;

/**
 * Give a specific behaviour to the generic graph definition
 * 
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id: InteractionNetwork.java,v 1.28 2004/08/27 09:26:21 groscurth
 *          Exp $
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.image.ImageDimension;
import uk.ac.ebi.intact.application.hierarchview.business.image.Utilities;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.searchengine.CriteriaBean;
import uk.ac.ebi.intact.util.simplegraph.BasicGraphI;
import uk.ac.ebi.intact.util.simplegraph.EdgeI;
import uk.ac.ebi.intact.util.simplegraph.Graph;
import uk.ac.ebi.intact.util.simplegraph.Node;
import uk.ac.ebi.intact.tulip.client.TulipClient;
import uk.ac.ebi.intact.tulip.client.generated.ProteinCoordinate;
import uk.ac.ebi.intact.util.Chrono;

import java.awt.*;
import java.rmi.RemoteException;
import java.util.*;
import java.util.List;

public class InteractionNetwork extends Graph {

    private static final Log logger = LogFactory.getLog(InteractionNetwork.class);

    public static final int DEFAULT_MAX_CENTRAL_PROTEIN = 7;

    /**
     * *********************************************************************
     * StrutsConstants
     */
    private final static Color DEFAULT_NODE_COLOR = new Color( 0, 0, 255 );
    private final static Color DEFAULT_LABEL_COLOR = new Color( 255, 255, 255 );

    private final static Color NODE_COLOR;
    private final static Color LABLE_COLOR;

    private static int MAX_CENTRAL_PROTEINS;

    static {
        Properties properties = IntactUserI.GRAPH_PROPERTIES;
        String stringColorNode = null;
        String stringColorLabel = null;
        String maxCentralProtein = null;
        // it is tried to parse the colors from the properties file
        if ( null != properties ) {
            stringColorNode = properties
                    .getProperty( "hierarchView.image.color.default.node" );
            stringColorLabel = properties
                    .getProperty( "hierarchView.image.color.default.label" );
            maxCentralProtein = properties
                    .getProperty( "hierarchView.graph.max.cental.protein" );
        }
        else {
            logger.warn( "properties file GRAPH_PROPERTIES could not been read" );
        }
        // the color for the node is parsed
        NODE_COLOR = Utilities.parseColor( stringColorNode, DEFAULT_NODE_COLOR );
        // the color for the lables parsed
        LABLE_COLOR = Utilities.parseColor( stringColorLabel,
                DEFAULT_LABEL_COLOR );

        // try to parse the maximal number of allowed central proteins
        // if the parsing fails the default value is taken
        try {
            MAX_CENTRAL_PROTEINS = Integer.parseInt( maxCentralProtein );
        }
        catch ( NumberFormatException e ) {
            MAX_CENTRAL_PROTEINS = DEFAULT_MAX_CENTRAL_PROTEIN;
        }
    }

    /**
     * *********************************************************************
     * Instance variables
     */

    /**
     * Allow to record nodes and keep their order
     */
    private ArrayList nodeList;
    private boolean isInitialized;

    /**
     * Properties of the final image (size ...)
     */
    private ImageDimension dimension;

    /**
     * Protein from which has been created the interaction network.
     */
    //private Interactor centralProtein;
    private String centralProteinAC; // avoid numerous call to
    // interactor.getAc()

    /**
     * Stores a set of central nodes and interactor. There is one by fusioned
     * interaction network We need to store both because in the case of an
     * interaction, no node are stored.
     */
    private ArrayList<BasicGraphI> centralNodes;
    private ArrayList centralInteractors;

    /**
     * Describe how the interaction network has been built, from which query
     * strings and what is the associated target e.g. the [ShortLabel ABC] and
     * [Xref DEF] That collection contains String[2] (0:queryString, 1:target)
     */
    private ArrayList criteriaList;

    /**
     * Map to enable fast access to the nodes for a specific source. <br>
     * The map has the following structure:
     * 
     * Map <String, Map <String, Set <BasicGraphI>>>
     * 
     * this means - the map stores the source name (e.g. GO) as key and another
     * map as value. This other map stores the specific source id (e.g.
     * GO:001900) as key and a set of nodes which are related to this source id.
     */
    private Map sourceHighlightMap;
    private BasicGraphI centralNode;

    /**
     * Constructor which is called when the graph is build in the "normal" way
     * its distinguishable to the other graph since it stores interactors in its
     * nodes
     */
    public InteractionNetwork(Interactor aCentralProtein) {
        Collection xrefs = aCentralProtein.getXrefs();
        logger.info( "Create an Interaction Network with centralProtein:"
                + aCentralProtein.getAc() + " #xref="
                + ( xrefs == null ? 0 : xrefs.size() ) );
        initNetwork();
        //centralProtein = aCentralProtein;
        centralNode = new Node( aCentralProtein );
        centralProteinAC = aCentralProtein.getAc();
        centralInteractors = new ArrayList();
        centralInteractors.add( aCentralProtein ); // can be an Interaction or a Protein
    }


    /**
     * Creates a interactionnetwork with the given node as central node
     * 
     * @param centralNode
     */
    public InteractionNetwork(BasicGraphI centralNode) {
        initNetwork();
        this.centralNode = centralNode;
        centralProteinAC = centralNode.getAc();
        centralNodes.add( centralNode );
    }


    /**
     * initialise some instance variables of the network
     */
    private void initNetwork() {
        criteriaList = new ArrayList();
        centralNodes = new ArrayList();
        // wait the user to add some node to reference the central one
        dimension = new ImageDimension();
        isInitialized = false;
        // a hashtable is taken to avoid null entries as key or as values
        // we only need the number of allowed sources - so the map is intialised
        // with the provided number of sources.
        sourceHighlightMap = new Hashtable( GraphHelper.SOURCES.size() );
    }


    public boolean isSourceHighlightMapEmpty() {
        return sourceHighlightMap.isEmpty();
    }


    /**
     * Adds a new node to the source map for the given source (e.g. GO) and the
     * given source id (e.g. GO:001900).
     * 
     * If the map already has the source id as key the node is added to the set
     * of other nodes for this source.
     * 
     * Otherwise a new set is created and the node is added to it.
     * 
     * @param source the source to highlight (e.g. GO)
     * @param sourceID the source id (e.g. GO:001900)
     * @param node the node which is related to the sourceID
     */
    public void addToSourceHighlightMap(String source, String sourceID,
            BasicGraphI node) {

        // the map for the given source is fetched
        Map sourceMap = (Map) sourceHighlightMap.get( source );

        // if no map exists a new one is created and put into the
        // sourceHighlightMap
        if ( sourceMap == null ) {
            sourceMap = new Hashtable();
            sourceHighlightMap.put( source, sourceMap );
        }

        // the nodes realted to the given sourceID are fetched
        Set sourceNodes = (Set) sourceMap.get( sourceID );

        // if no set exists a new one is created and put into the sourceMap
        if ( sourceNodes == null ) {
            // a hashset is used to avoid duplicate entries
            sourceNodes = new HashSet();
            sourceMap.put( sourceID, sourceNodes );
        }
        sourceNodes.add( node );
    }


    /**
     * Returns all proteins which are related to the given source (e.g. GO) and
     * the given sourceID (e.g. GO:001900).
     * 
     * @param source the source
     * @param sourceID the sourceID
     * @return a set of related proteins
     */
    public Set getProteinsForHighlight(String source, String sourceID) {

        logger.info("in getProteinsForHighlight looking for sourceID=" + sourceID);

        // get the list of all the source terms allowed
        Properties properties = IntactUserI.HIGHLIGHTING_PROPERTIES;
        String sourceList = properties.getProperty ("highlightment.source.allowed");

        // get the delimiter token
        String delimiter = IntactUserI.HIGHLIGHTING_PROPERTIES.getProperty( "highlightment.source.token" );

        // split the list with the delimiter
        String[] listSource = sourceList.split(delimiter);

        Map sourceMap = null, sourceMapTmp = null;
        boolean init = false;

        // build the sourceMap with all source allowed
        // first element, "All", is ignored
        for(int i=1; i<listSource.length; i++) {
            if ( !init ) {
                sourceMap = (Map) sourceHighlightMap.get( listSource[i].toLowerCase() );
                init = true;
            }
            sourceMapTmp = (Map) sourceHighlightMap.get( listSource[i].toLowerCase() );
            sourceMap.putAll(sourceMapTmp);
            sourceMapTmp = null;
        }

        logger.info( "sourceMap = " + sourceMap );

        // if no nodes are given for the provided source null is returned
        if ( sourceMap == null ) {
            logger.warn( "sourceMap is null !" );
            return null;
        }

        return (Set) sourceMap.get( sourceID );
    }


    /**
     * Returns the maximal number of allowed central proteins
     *
     * @return
     */
    public static int getMaxCentralProtein() {
        return MAX_CENTRAL_PROTEINS;
    }


    /**
     * Returns the current number of central proteins
     * 
     * @return
     */
    public int getCurrentCentralProteinCount() {
        return centralNodes.size();
    }


    /**
     * Returns the accession number of the central protein
     * 
     * @return
     */
    public String getCentralProteinAC() {
        return centralProteinAC;
    }


    /**
     * Returns the central node. It has the same behaviour as
     * <code>getCentralNode()</code> but to consistency with the building of
     * the graph it is kept.
     * 
     * @return
     */
    public BasicGraphI getCentralProtein() {
        return getCentralNode();
    }


    /**
     * Returns the central node
     * 
     * @return
     */
    public BasicGraphI getCentralNode() {
        return centralNode;
    }


    /**
     * Add the node to the central nodes if this node is already a central node
     * 
     * @param node
     */
    public void addCentralProtein(BasicGraphI node) {
        if ( ( !centralNodes.contains( node ) ) && ( node != null ) )
            centralNodes.add( node );
    }


    /**
     * Returns all central nodes
     * 
     * @return
     */
    public ArrayList<BasicGraphI> getCentralProteins() {
        return centralNodes;
    }


    /**
     * Returns all central interactors
     * 
     * @return
     */
    public ArrayList getCentralInteractors() {
        return centralInteractors;
    }


    /**
     * Returns whether the provided node is a central protein
     * 
     * @param node the node to check
     * @return whether the protein is a central protein
     */
    public boolean isCentralProtein(BasicGraphI node) {
        return centralNodes.contains( node );
    }


    public ArrayList getCriteria() {
        return criteriaList;
    }


    /**
     * Add a new criteria to the interaction network <br>
     * 
     * @param aCriteria the criteria to add if it doesn't exist in the
     *            collection already
     */
    public void addCriteria(CriteriaBean aCriteria) {
        if ( !criteriaList.contains( aCriteria ) )
            criteriaList.add( aCriteria );
    }


    /**
     * remove all existing criteria from the interaction network
     */
    public void resetCriteria() {
        criteriaList.clear();
    }


    /**
     * Initialization of the interaction network Record the nodes in a list
     * which allows to keep an order ...
     */
    public void init() {
        HashMap myNodes = super.getNodes();

        // create a new collection (ordered) to allow a indexed access to the
        // node list
        this.nodeList = new ArrayList( myNodes.values() );
        this.isInitialized = true;
    }


    /**
     * add the initialization part to the super class method
     * 
     * @param anInteractor the interactor to add in the graph
     * @return the created Node
     */
    public BasicGraphI addNode(Interactor anInteractor) {
        BasicGraphI aNode = super.addNode( anInteractor );

        // initialization of the node
        if ( null != aNode ) {
            if ( anInteractor.equals( ( (Node) centralNode ).getInteractor() ) ) {
                addCentralProtein( aNode );
                // TODO: could add the interactor in a Collection ... would
                // solve the problem of an interaction
            }
            aNode.put( Constants.ATTRIBUTE_LABEL, anInteractor.getAc() );
            initNodeDisplay( aNode );
        }

        isInitialized = false;
        return aNode;
    } // addNode


    /**
     * return a list of nodes ordered
     */
    public ArrayList getOrderedNodes() {
        return this.nodeList;
    }


    /**
     * Allow to put the default color and default visibility for each protein of
     * the interaction network
     */
    public void initNodes() {
        BasicGraphI aNode;

        HashMap someNodes = super.getNodes();
        Set keys = someNodes.keySet();
        Iterator iterator = keys.iterator();
        while ( iterator.hasNext() ) {
            aNode = (BasicGraphI) someNodes.get( iterator.next() );
            initNodeDisplay( aNode );
        }
    } // initNodes


    /**
     * initialisation of one Node about its color, its visible attribute
     * 
     * @param aNode the node to update
     */
    public void initNodeDisplay(BasicGraphI aNode) {
        aNode.put( Constants.ATTRIBUTE_COLOR_NODE, NODE_COLOR );
        aNode.put( Constants.ATTRIBUTE_COLOR_LABEL, LABLE_COLOR );
        // all nodes are visible
        // replaces new Boolean(true) with Boolean.TRUE which is a object
        // reprasentation of the true value
        aNode.put( Constants.ATTRIBUTE_VISIBLE, Boolean.TRUE );
    }


    /**
     * Return the number of node
     * 
     * @return the number of nodes
     */
    public int sizeNodes() {
        return super.getNodes().size();
    }


    /**
     * Return the number of edge
     * 
     * @return the number of edge
     */
    public int sizeEdges() {
        return super.getEdges().size();
    }


    /**
     * Return the object ImageDimension which correspond to the graph
     * 
     * @return an object ImageDimension
     */
    public ImageDimension getImageDimension() {
        return this.dimension;
    }


    /**
     * Create a String giving informations for the Tulip treatment the
     * informations are : - the number of nodes, - the whole of the edges and
     * nodes associeted for each edge, - the label of each node.
     * 
     * @return an object String
     */
    public String exportTlp() {
        /*
         * TODO : could be possible to optimize the size of the string buffer to
         * avoid as much as possible to extend the buffer size.
         */

        EdgeI edge;
        StringBuffer out = new StringBuffer();
        String separator = System.getProperty( "line.separator" );
        int i, max;

        if ( false == this.isInitialized )
            this.init();

        out.append( "(nodes " );

        max = nodeList.size();
        for (i = 1; i <= max; i++)
            out.append( i + " " );

        out.append( ")" + separator );

        ArrayList myEdges = (ArrayList) super.getEdges();

        max = sizeEdges();
        for (i = 1; i <= max; i++) {
            edge = (EdgeI) myEdges.get( i - 1 );
            out.append( "(edge " + i + " "
                    + ( nodeList.indexOf( edge.getNode1() ) + 1 ) + " "
                    + ( nodeList.indexOf( edge.getNode2() ) + 1 ) + ")"
                    + separator );
        }

        return out.toString();
    } // exportTlp


    /**
     * Create a String giving informations for the bioLayout EMBL software the
     * informations are just pairwise of protein label.
     * 
     * @return an object String
     */
    public String exportBioLayout() {

        EdgeI edge;
        StringBuffer out = new StringBuffer();
        String separator = System.getProperty( "line.separator" );
        int i, max;

        if ( false == this.isInitialized )
            this.init();

        Vector myEdges = (Vector) super.getEdges();
        max = sizeEdges();
        for (i = 1; i <= max; i++) {
            edge = (EdgeI) myEdges.get( i - 1 );
            String label1 = ( (BasicGraphI) edge.getNode1() ).getLabel();
            String label2 = ( (BasicGraphI) edge.getNode2() ).getLabel();

            out.append( label1 + "\t" + label2 + separator );
        }

        return out.toString();
    } // exportBioLayout


    /**
     * Create a String giving the Javascript structure of the interaction network
     *
     * @return an object String
     */
    public String exportJavascript( float rateX, float rateY, int borderSize ) {
        StringBuffer out = new StringBuffer();

        if ( false == this.isInitialized )
            this.init();

        // get the list of nodes and the dimension of the image
        ImageDimension dimension = this.getImageDimension();
        List proteinList = this.getOrderedNodes();

        // declaration of a Javascript Array
        out.append("var data = new Array(");

        for( int i=0; i < proteinList.size(); i++ ) {
            BasicGraphI currentProtein = (BasicGraphI) proteinList.get(i);
            float proteinX = ( (Float) currentProtein.get( Constants.ATTRIBUTE_COORDINATE_X ) )
                .floatValue();
            float proteinY = ( (Float) currentProtein.get( Constants.ATTRIBUTE_COORDINATE_Y ) )
                .floatValue();
            float proteinLength = ( (Float) currentProtein.get( Constants.ATTRIBUTE_LENGTH ) )
                 .floatValue();
            float proteinHeight = ( (Float) currentProtein.get( Constants.ATTRIBUTE_HEIGHT ) )
                 .floatValue();

            logger.debug( "Initial coordinates of node : " + currentProtein.getAc() + " | X=" + proteinX + " | Y=" + proteinY);

            // convertion to real coordinates
            float x = ( proteinX - dimension.xmin() ) / rateX - ( proteinLength / 2 ) + borderSize;
            float y = ( proteinY - dimension.ymin() ) / rateY - ( proteinHeight / 2 ) + borderSize;

            logger.debug( "Real coordinates : X=" + x + " | Y=" + y);

            out.append( "new Array(\"" + currentProtein.getAc() + "\",\"" + x + "\",\"" + y + "\")" );
            if ( i < proteinList.size() - 1 ) {
                 out.append(", ");
            }
        }

        out.append(");");

        return out.toString();
    }


    /**
     * Send a String to Tulip to calculate coordinates Enter the obtained
     * coordinates in the graph.
     * 
     * @param dataTlp The obtained String by the exportTlp() method
     * @return an array of error message or <b>null </b> if no error occurs.
     */
    public String[] importDataToImage(String dataTlp) throws RemoteException {

        if ( false == this.isInitialized )
            this.init();

        ProteinCoordinate[] result;
        TulipClient client = new TulipClient();

        // Call Tulip Web Service : get
        try {
            result = client.getComputedTlpContent( dataTlp );
        }
        catch ( RemoteException e ) {
            logger.error( "couldn't get coodinate from the TLP content", e );
            throw e;
        }

        if ( null == result ) {
            // throw new IOException ("Tulip send back no data.");
            String[] errors = client.getErrorMessages( true );
            logger.warn( errors.length
                    + " error(s) returned by the Tulip web service" );
            return errors;
        }
        else {
            // update protein coordinates
            ProteinCoordinate p = null;
            Float x, y;
            BasicGraphI protein;

            for (int i = 0; i < result.length; i++) {
                p = result[i];

                x = new Float( p.getX() );
                y = new Float( p.getY() );

                // nodes are labelled from 1 to n int the tlp file and from 0 to
                // n-1 int the collection.
                protein = (BasicGraphI) this.nodeList.get( p.getId() - 1 );

                // Store coordinates in the protein
                protein.put( Constants.ATTRIBUTE_COORDINATE_X, x );
                protein.put( Constants.ATTRIBUTE_COORDINATE_Y, y );
            } // for

            logger.info( "Protein coordinates updated" );
        } // else

        return null;
    } //importDataToImage


    /**
     * Fusion a interaction network to the current one. <br>
     * 
     * For each edge of the new network we check if it exists in the current
     * one. <br>
     * If the edge already exists : continue. <br>
     * If not, we check if the two Nodes of the edge already exists in the
     * current network : <blockquote>if the node exists, update the edge with
     * its reference <br>
     * if not add it to the current network </blockquote> finally we add the
     * up-to-date edge to the current network <br>
     * 
     * @param network the interaction network we want to fusioned to the current
     *            one.
     */
    public void fusion(InteractionNetwork network) {

        logger.info( "BEGIN fusion" );
        Chrono chrono = new Chrono();
        chrono.start();

        Collection newEdges = network.getEdges();
        Collection edges = getEdges();
        HashMap nodes = getNodes();
        EdgeI aNewEdge;
        BasicGraphI aNode;
        String ACNode;

        Iterator iterator = newEdges.iterator();
        while ( iterator.hasNext() ) {
            aNewEdge = (EdgeI) iterator.next();

            // (!) see also the equals method of Edge
            if ( false == edges.contains( aNewEdge ) ) {
                // check if both nodes are present
                aNode = aNewEdge.getNode1();
                ACNode = aNode.getAc();
                // see also the equals method of Node
                if ( false == nodes.containsKey( ACNode ) ) {
                    nodes.put( ACNode, aNode );
                    logger.info( "fusion: add node " + ACNode );
                }
                else {
                    aNewEdge.setNode1( (BasicGraphI) nodes.get( ACNode ) );
                }

                aNode = aNewEdge.getNode2();
                ACNode = aNode.getAc();
                if ( false == nodes.containsKey( ACNode ) ) {
                    nodes.put( ACNode, aNode );
                    logger.info( "fusion: add node " + ACNode );
                }
                else {
                    aNewEdge.setNode2( (BasicGraphI) nodes.get( ACNode ) );
                }

                edges.add( aNewEdge );
                logger.info( "fusion: add edge " + aNewEdge.getNode1().getAc()
                        + "<->" + aNewEdge.getNode2().getAc() );
            }
        }

        // update internal reference
        init();
        initNodes();

        // fusion central proteins
        ArrayList _centralNodes = network.getCentralProteins();
        for (Iterator iter = _centralNodes.iterator(); iter.hasNext();) {
            // get the current central protein of the other network
            aNode = (BasicGraphI) iter.next();

            /*
             * the central protein of the other network stores the SOURCE
             * information but the corresponding node of this network stores all
             * other informations needed for the display - therefore the SOURCE
             * informations are added to the node of this network and added as
             * centralNode
             */
            BasicGraphI thisNode = (BasicGraphI) nodes.get( aNode.getAc() );

            // every source information is transformed to the node of this
            // network
            for (int i = 0; i < GraphHelper.SOURCES.size(); i++) {
                String source = (String) GraphHelper.SOURCES.get( i );
                thisNode.put( source, aNode.get( source ) );
            }

            centralNodes.add( thisNode );
        }

        // fusion search criteria using the addCriteria method to avois
        // duplicates (addAll would not !)
        Collection criterias = network.getCriteria();
        for (Iterator iterator2 = criterias.iterator(); iterator2.hasNext();) {
            CriteriaBean criteriaBean = (CriteriaBean) iterator2.next();
            if ( !criteriaList.contains( criteriaBean ) ) {
                addCriteria( criteriaBean );
            }
        }

        /*
         * if the sourceHighlightMap is present (then we have built the network
         * with the mine database table) - we have to fusion also the two maps
         */
        if ( network.sourceHighlightMap != null ) {
            // get all entries of the other source highlight map
            Set entrySet = network.sourceHighlightMap.entrySet();
            for (Iterator iter = entrySet.iterator(); iter.hasNext();) {
                Map.Entry element = (Map.Entry) iter.next();
                // source is the id of a source (e.g. GO)
                String source = (String) element.getKey();

                // get all entries of the specific source map
                Set sourceSets = ( (Map) element.getValue() ).entrySet();

                for (Iterator it = sourceSets.iterator(); it.hasNext();) {
                    Map.Entry el = (Map.Entry) it.next();
                    // sourceID is the id of a specific source (e.g. GO:001900)
                    String sourceID = (String) el.getKey();
                    // all nodes who are related to the sourceID
                    Collection sourceNodes = (Collection) el.getValue();
                    for (Iterator i = sourceNodes.iterator(); i.hasNext();) {
                        // add the node to this source highlight map
                        addToSourceHighlightMap( source, sourceID,
                                (BasicGraphI) i.next() );
                    }
                }
            }
        }

        // fusion interactor list - if the list is initialised
        // TODO: does it needs to check the non redondancy ?!
        if ( centralInteractors != null ) {
            centralInteractors.addAll( network.getCentralInteractors() );
        }

        chrono.stop();
        String msg = "Network Fusion took " + chrono;
        logger.info( msg );

        logger.info( "END fusion" );
    }

} // InteractionNetwork

