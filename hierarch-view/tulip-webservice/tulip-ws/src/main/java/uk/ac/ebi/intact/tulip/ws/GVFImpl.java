/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.tulip.ws;

// JDK

import org.apache.axis.MessageContext;
import org.apache.axis.session.Session;
import royere.bath.layout.GEM;
import royere.cwi.framework.Keys;
import royere.cwi.layout.Coordinates;
import royere.cwi.structure.Edge;
import royere.cwi.structure.Graph;
import royere.cwi.structure.Node;

import java.util.*;

import uk.ac.ebi.intact.tulip.ws.TulipAccess;
import uk.ac.ebi.intact.tulip.ws.ProteinCoordinate;

/**
 * Purpose :
 * <br>
 * Compute the TLP file using the Graph Visualization Framework and sends back the node's coordinate.
 * <br>
 * <b>GVF homepage</b>: http://gvf.sourceforge.net/
 *
 * @author Samuel KERRIEN (skerrien@ebi.ac.uk)
 */

public class GVFImpl implements TulipAccess
{

    /********************/
    /** StrutsConstants */
    /**
     * ****************
     */

    private static String ERROR_MESSAGE_KEY = "messages";

    /************************/
    /** Instance variables  */
    /************************/

    /**
     * Error messages in order to allow the user to know what happen on the web service
     */
    private Collection errorMessages;


    /****************************************/
    /** Public methods over the web service */
    /****************************************/

    /**
     * get the computed TLP content from tulip
     *
     * @param tlpContent tlp content to compute
     * @param optionMask the option of the Tulip process
     * @return the computed tlp file content or <b>null</b> if an error occurs.
     */
    public ProteinCoordinate[] getComputedTlpContent( String tlpContent, String optionMask ) {

        errorMessages = new Vector();
        MessageContext ctx = MessageContext.getCurrentContext();
        Session session = ctx.getSession();
        session.set( ERROR_MESSAGE_KEY, errorMessages );

        ProteinCoordinate[] pc = buildGraph( tlpContent );

        // Send back the computed content
        return pc;

    } // computeTlpContent


    /**
     * Allows the user to get messages produced byte the web service
     *
     * @param hasToBeCleaned delete all messages after sended them back to the client
     * @return an array of messages or <b>null</b> if no messages are stored.
     */
    public String[] getErrorMessages( boolean hasToBeCleaned ) {

        MessageContext ctx = MessageContext.getCurrentContext();
        Session session = ctx.getSession();
        Collection errorMessages = (Collection) session.get( ERROR_MESSAGE_KEY );

        if( null == errorMessages ) {
            return null;
        }

        // Don't try to use the Collection.toArray() method, it give you back Object[]
        // and also an Exception when you send back to the client (even with a String[] cast)

        int count = errorMessages.size();
        String[] messagesArray = new String[ count ];

        count = 0;
        for ( Iterator iterator = errorMessages.iterator(); iterator.hasNext(); ) {
            messagesArray[ count++ ] = (String) iterator.next();
        }

        // clean up messages
        if( hasToBeCleaned ) {
            cleanErrorMessages();
        }

        return messagesArray;

    } // getErrorMessages


    /**
     * Clean message list
     */
    public void cleanErrorMessages() {

        if( null == errorMessages ) {
            errorMessages = new Vector();
            return;
        }

        errorMessages.clear();

    } // cleanErrorMessages



    /*********************/
    /** Internal methods */
    /**
     * *****************
     */
    private final Object lock = new Object();

    /**
     * Process the TLP content and produce the nodes' coordinates.
     * <p/>
     * Algo sketch:
     * (1) Convert the TLP description to a Royere Graph
     * (2) Apply the GEM layout to it
     * (3) build an array of coordinates that we send back
     *
     * @param tlp the description of the graph
     * @return an array of NodeId -> Coordinates
     */
    private ProteinCoordinate[] buildGraph( String tlp ) {

        Map nodes = new HashMap();
        Set edges = new HashSet();


//        System.out.println( "-------------------------------------------\nTLP CONTENT\n" + tlp +
//                            "\n-----------------------------------------------\n" );

        StringTokenizer st = new StringTokenizer( tlp, "\n" );

        if( st.hasMoreTokens() ) {
            // it should be (nodes 1 2 3 ... )
            String nodeIds = st.nextToken();

            if( !nodeIds.startsWith( "(nodes " ) ) {
                System.err.println( "Format error, first line is expected to start with (nodes 1 2 3 .... )" );
                return null;
            }

            // parse it and create the nodes
            nodeIds = nodeIds.substring( 0, nodeIds.lastIndexOf( ')' ) );
            StringTokenizer stNodes = new StringTokenizer( nodeIds, " " );
            stNodes.nextToken();
            String id = null;
            while ( stNodes.hasMoreTokens() ) {
                id = stNodes.nextToken();

                Node node = new Node();
                node.setLabel( id );

                nodes.put( id, node );
//                System.out.println( "node " + id + " created." );
            }
        }

        // process edges
        while ( st.hasMoreTokens() ) {
            String edge = st.nextToken();

            if( !edge.startsWith( "(edge " ) ) {
                addErrorMessage( "Format error, line 2..n are expected to be like (edge 1 2 3)\n" + edge );
                System.err.println( "Format error, line 2..n are expected to be like (edge 1 2 3)\n" + edge );
                return null;
            }

            StringTokenizer stEdge = new StringTokenizer( edge, " " );
            stEdge.nextToken(); // skip (edge
            stEdge.nextToken(); // skip edge id
            String from = stEdge.nextToken();
            String to = stEdge.nextToken();

            // remove the last )
            to = to.trim().substring( 0, to.lastIndexOf( ')' ) );

            edges.add( new Edge( (Node) nodes.get( from ),
                                 (Node) nodes.get( to ) ) );
//            System.out.println( "Edge created: " + from + " <-> " + to );
        }

        // create the graph
        Graph graph = new Graph();
        graph.setProperty( Keys.ISDIRECTED, Boolean.FALSE );

        // add nodes
        for ( Iterator iterator = nodes.values().iterator(); iterator.hasNext(); ) {
            Node node = (Node) iterator.next();
            graph.add( node );
        }

        // add edges
        for ( Iterator iterator = edges.iterator(); iterator.hasNext(); ) {
            Edge edge = (Edge) iterator.next();
//            System.out.println( "add edge: " + edge );

            graph.add( edge );
        }


        // apply the layout
//        long start = System.currentTimeMillis();
        GEM gemLayout = new GEM();
        // multiple thread getting together in there seems to crash the layout process sometimes.
        synchronized ( lock ) {
            gemLayout.position( graph, null );
        }
//        long stop = System.currentTimeMillis();
//        System.out.println( (stop - start) + "ms to apply the layout." );


        Collection allNodes = nodes.values();
        ProteinCoordinate[] pc = new ProteinCoordinate[ allNodes.size() ];
        int i = 0;
        for ( Iterator iterator = allNodes.iterator(); iterator.hasNext(); ) {
            Node node = (Node) iterator.next();
            Coordinates coordinates = gemLayout.getCoordinates( node, graph );
            int id = 0;
            try {
                id = Integer.parseInt( node.getLabel() );
            } catch ( NumberFormatException e ) {
                System.err.println( "Could not retreive the node having the label: " + node.getLabel() );
                continue;
            }

//            System.out.println( "Extract Coordimates: " + id + " X:" +(float)coordinates.getX() +" Y:"+ (float)coordinates.getY() );

            pc[ i++ ] = new ProteinCoordinate( id, (float) coordinates.getX(), (float) coordinates.getY() );
        }

        return pc;
    }

    /**
     * Add a message to the message list
     *
     * @param aMessage the message to add
     */
    private void addErrorMessage( String aMessage ) {

        if( null == errorMessages ) {
            errorMessages = new Vector();
        }
        errorMessages.add( aMessage );

    } // addErrorMessage

} // RoyereAccessImpl