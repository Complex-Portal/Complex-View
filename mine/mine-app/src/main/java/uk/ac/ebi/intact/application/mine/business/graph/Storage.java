/*
 * Created on 09.06.2004
 */

package uk.ac.ebi.intact.application.mine.business.graph;

import jdsl.core.api.Locator;
import jdsl.graph.api.Edge;
import jdsl.graph.api.Vertex;

/**
 * A class can implement the <tt>Storage</tt> interface if it is used as a
 * storage class in the mine project. The storage class stores different
 * informations for the jdsl algorithm which is used in the mine project. <br>
 * The algorithm needs additional information for each node in the graph (e.g.
 * the ancestor node, the distance from the source node and so on). <br>
 * To enable multiple simultaneous searches in the same graph these informations
 * are stored in an extra class and not directly in the graph.
 * 
 * @author Andreas Groscurth
 */
public interface Storage {
    // flag as initial value for all distances of node
    public int UNREACHABLE_DISTANCE = Integer.MIN_VALUE;

    /**
     * Cleans up the structure element which stores the informations.
     */
    public void cleanup();

    /**
     * Sets the distance for the given node. The distance indicates how far (how
     * many levels) the node is away from the start node.
     * 
     * @param v the node
     * @param dis the distance of the node from the start node
     */
    public void setDistance(Vertex v, int dis);

    /**
     * Sets the edge for the given node to its ancestor. This information is
     * needed to trace back the shortest path for two nodes.
     * 
     * @param v the nodes
     * @param edge the edge to the ancestor.
     */
    public void setEdgeToParent(Vertex v, Edge edge);

    /**
     * Sets the locator for the given node.
     * 
     * @param v the node
     * @param loc the locator
     */
    public void setLocator(Vertex v, Locator loc);

    /**
     * Returns the locator for the given node.
     * 
     * @param v the node
     * @return the locator for the node
     */
    public Locator getLocator(Vertex v);

    /**
     * Returns the edge to the ancestor of the given node.
     * 
     * @param v the node
     * @return the edge to its ancestor
     */
    public Edge getEdgeToParent(Vertex v);

    /**
     * Returns the distance of the given node to the start node.
     * 
     * @param v the node
     * @return the distance
     */
    public int getDistance(Vertex v);

    /**
     * Tests whether a node has a distance or if it has still the initial
     * UNREACHABLE_DISTANCE value.
     * 
     * @param v the node
     * @return whether the distance is set
     */
    public boolean hasDistance(Vertex v);

    /**
     * Tests whether the node has already an edge to an ancestor.
     * 
     * @param v the node
     * @return the edge to its ancestor
     */
    public boolean hasEdgeToParent(Vertex v);

    /**
     * Tests whether the node has a locator
     * 
     * @param v the node
     * @return the locator
     */
    public boolean hasLocator(Vertex v);
}