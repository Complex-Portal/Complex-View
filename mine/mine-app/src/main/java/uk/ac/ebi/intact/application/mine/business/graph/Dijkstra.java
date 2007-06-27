/*
 * Created on 08.04.2004
 */

package uk.ac.ebi.intact.application.mine.business.graph;

import java.util.Map;

import jdsl.core.api.Locator;
import jdsl.core.api.Sequence;
import jdsl.core.ref.NodeSequence;
import jdsl.graph.algo.IntegerDijkstraPathfinder;
import jdsl.graph.api.Edge;
import jdsl.graph.api.Vertex;
import uk.ac.ebi.intact.application.mine.business.IntactUserI;
import uk.ac.ebi.intact.application.mine.business.graph.model.EdgeElement;
import uk.ac.ebi.intact.application.mine.business.graph.model.SearchObject;

/**
 * The class <tt>Dijsktra</tt> implements the Dijkstra algorithm to find the
 * shortest path between two nodes. <br>
 * The class extends the <tt>IntegerDijkstraPathfinder</tt> from the
 * <tt>jdsl</tt> library and uses its implementation. The class overrides
 * several methods to provide its own use.
 * 
 * @author Andreas Groscurth
 */
public class Dijkstra extends IntegerDijkstraPathfinder {
    // the maximal depth to search for the path
    // if no property could ne found the default depth is 5
    private static final int MAX_LEVEL = Integer
            .parseInt( IntactUserI.MINE_PROPERTIES.getProperty(
                    "dijkstra.maxDepth", "5" ) );
    // the structure to store the additional information needed by the algorithm
    private Storage storage;
    // the map with the nodes -> search objects
    private Map searchObjectMap;
    // indicates how fare the algorithm is away from the start node
    private int currentLevel;
    // flag which is set if the algorithm doesnt find a shortest path
    private boolean unreachableFlag;

    /**
     * Creates a new <tt>Dijkstra</tt> object.
     * 
     * @param storage the structure to store the information for the algorithm
     * @param searches search map maps the nodes to the search objects
     */
    public Dijkstra(Storage storage, Map searches) {
        this.storage = storage;
        this.searchObjectMap = searches;
        unreachableFlag = false;
    }

    /**
     * Returns the weight of the given edge. <br>
     * The weight is determined by the object which is attached to the edge.
     * <br>
     * Because the library just works with int values but the weight of the
     * edges can be a decimal number, the weight is multiplied with 100 and
     * casted to an int (e.g. 0.54 -> 54, 1.0 -> 100). The high value of the
     * weight does not distract the algorithm and so no information gets lost.
     * 
     * @see jdsl.graph.algo.IntegerDijkstraTemplate#weight(jdsl.graph.api.Edge)
     */
    protected int weight(Edge edge) {
        double weight = ( (EdgeElement) edge.element() ).getWeight();
        return (int) weight * 100;
    }

    /*
     * {non-Javadoc}
     * 
     * @see jdsl.graph.algo.IntegerDijkstraTemplate#vertexNotReachable(jdsl.graph.api.Vertex)
     */
    protected void vertexNotReachable(Vertex v) {
        // in the storage the distance from the node to the start node is set as
        // UNREACHABLE to indicate that the node is not reachable
        storage.setDistance( v, Storage.UNREACHABLE_DISTANCE );
        // there is no path for this node, so there is no edge we could
        // backtrace
        storage.setEdgeToParent( v, null );
        // the unreachableFlag is set to indicate that the algorithm does not
        // need to go on.
        unreachableFlag = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jdsl.graph.algo.IntegerDijkstraTemplate#isFinished(jdsl.graph.api.Vertex)
     */
    protected boolean isFinished(Vertex v) {
        // the flag 'isFinished' indicates that the algorithm has found the
        // shortest path for this node. If the shortest path was found the
        // distance element of the storage class has been set.
        boolean isFinished = storage.hasDistance( v );
        if ( isFinished ) {
            // in this state the shortest path to the given node has been found.
            // so we are trace back the path from this node to the start node to
            // find the path
            traceBackPath( v );
        }
        // the flag is returned to come along with the API from the jdsl library
        return isFinished;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jdsl.graph.algo.IntegerDijkstraTemplate#setLocator(jdsl.graph.api.Vertex,
     *      jdsl.core.api.Locator)
     */
    protected void setLocator(Vertex v, Locator vLoc) {
        storage.setLocator( v, vLoc );
    }

    /*
     * (non-Javadoc)
     * 
     * @see jdsl.graph.algo.IntegerDijkstraTemplate#getLocator(jdsl.graph.api.Vertex)
     */
    protected Locator getLocator(Vertex v) {
        return storage.getLocator( v );
    }

    /*
     * (non-Javadoc)
     * 
     * @see jdsl.graph.algo.IntegerDijkstraTemplate#setEdgeToParent(jdsl.graph.api.Vertex,jdsl.graph.api.Edge)
     */
    protected void setEdgeToParent(Vertex v, Edge vEdge) {
        // the method is called when the algorithm has gone one depth farer away
        // from the start node - therefore the given node gets the distance
        // 'currentLevel' + 1
        storage.setDistance( v, currentLevel + 1 );
        storage.setEdgeToParent( v, vEdge );
    }

    /*
     * (non-Javadoc)
     * 
     * @see jdsl.graph.algo.IntegerDijkstraTemplate#getEdgeToParent(jdsl.graph.api.Vertex)
     */
    public Edge getEdgeToParent(Vertex v) {
        return storage.getEdgeToParent( v );
    }

    /*
     * (non-Javadoc)
     * 
     * @see jdsl.graph.algo.IntegerDijkstraPathfinder#shouldContinue()
     */
    protected boolean shouldContinue() {
        // the node which is at the top of the priority queue (which stores all
        // nodes of the graph for the algorithm) is fetched, but not popped from
        // the queue.
        Vertex v = (Vertex) pq_.min().element();
        // the distance of the node to the start node is fetched
        int distance = storage.getDistance( v );

        // if the level is the UNREACHABLE_DISTANCE that means we have started
        // just the algorithm (because thats the default distance every node
        // gets). so the currentLevel is set to 0
        if ( distance == Storage.UNREACHABLE_DISTANCE ) {
            currentLevel = 0;
        }
        // otherwise we are already in the algorithm the current level is set to
        // the distance of the nearest node
        else {
            currentLevel = distance;
        }
        // tests whether the end node is already reached
        return super.shouldContinue() &&
        // tests whether the current level is less than the maximal allowed
                // depth
                currentLevel <= MAX_LEVEL &&
                // tests whether the algorithm has encountered a nonreachable
                // node which means all other remaining nodes will also be non
                // reachable and therefore we can stop
                !unreachableFlag;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jdsl.graph.algo.IntegerDijkstraTemplate#shortestPathFound(jdsl.graph.api.Vertex,int)
     */
    protected void shortestPathFound(Vertex vertex, int vDist) {
        // the distance of the node is set
        storage.setDistance( vertex, vDist );

        // if the node which shortest path was found is not the source node
        // source_ is an instance variable of the
        // IntegerDijkstraTemplate class and represent the start node.
        if ( vertex != source_ ) {
            // it is tried to get the search object to the given node
            SearchObject currentSearchObject = (SearchObject) searchObjectMap
                    .get( vertex );

            // if there is a search object for the current node, we have found a
            // shortest path which is interesting for the minimal connecting
            // network because the corresponding node is one of the nodes
            // one is searching for.
            if ( currentSearchObject != null ) {
                SearchObject startSearch = ( (SearchObject) searchObjectMap
                        .get( source_ ) );
                // if we havent stored a path from the current search object
                // and the start searchobject, then the path is traced back
                // and stored.
                if ( !currentSearchObject.hasPathAlreadyFound( startSearch ) ) {
                    traceBackPath( vertex );
                }
            }
        }
    }

    /**
     * Traces back the path from the given end node to the start node. <br>
     * If on this path a node is found which is interesting for the minimal
     * connecting network it is stored that for the end node and the found node
     * a path is found. <br>
     * It is then checked if this found path is shorter than the previous path
     * and updated if so.
     * 
     * @param end the node from where the tracing starts
     */
    public void traceBackPath(Vertex end) {
        // JDSL datastructure to store the paths
        Sequence retval = new NodeSequence();
        Vertex currVertex = end;
        // the search object of the end node is fetched
        // no test is needed if there is a searchobject for this node
        // because the method is just called when we already know that the
        // node is interesting because its part of the search.
        SearchObject searchEnd = (SearchObject) searchObjectMap.get( end );
        SearchObject currentSearch;
        // as long as we havent reached the start node
        while ( currVertex != source_ ) {
            // the edge from the current node is fetched
            // this edge is the edge of the shortest path, so its not
            // an arbitrary one.
            Edge currEdge = getEdgeToParent( currVertex );
            // the edge is added to the path
            retval.insertFirst( currEdge );
            // the next node is fetched
            // g_ is the graph the algorithm is working on, the
            // method 'opposite' is from the <tt>IntegerDijkstraTemplate</tt>
            // class
            currVertex = g_.opposite( currVertex, currEdge );
            // it is tried to get a searchobject for the current node
            currentSearch = (SearchObject) searchObjectMap.get( currVertex );
            // if a searchobject was found we now update the found path
            if ( currentSearch != null ) {
                // both searchobjects memorize that they found the shortest
                // path to each other
                currentSearch.pathWasFound( searchEnd );
                searchEnd.pathWasFound( currentSearch );

                /*
                 * in the method it is checked whether we have found a shorter
                 * path for the given searchObjects. that does not contradict
                 * that we call the 'pathWasFound' method for the two objects,
                 * because the 'pathWasFound' method means we have found the
                 * shortest path between these two nodes, whereas this check is
                 * more global and tests whether the current path is the
                 * shortest of all found paths for the two objects.
                 */
                checkShortestPath( retval, currentSearch, searchEnd );
            }
        }
    }

    /**
     * Checks whether the algorithm has found a shorter path to either of the
     * search objects than the previous one the two have.
     * 
     * @param path the found path
     * @param search1 one of the search objects
     * @param search2 the other of the search objects
     */
    private void checkShortestPath(Sequence path, SearchObject search1,
            SearchObject search2) {
        // if the found path is shorter than the exisiting
        // the new path is set as shortest path
        if ( search1.getPathLength() > path.size() ) {
            search1.setPath( path );
        }
        // if the found path is shorter than the exisiting
        // the new path is set as shortest path
        if ( search2.getPathLength() > path.size() ) {
            search2.setPath( path );
        }
    }
}