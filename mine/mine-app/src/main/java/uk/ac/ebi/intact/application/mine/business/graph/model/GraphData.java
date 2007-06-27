/*
 * Created on 21.05.2004
 */

package uk.ac.ebi.intact.application.mine.business.graph.model;

import java.util.Map;

import jdsl.graph.api.Graph;

/**
 * The class <tt>GraphData</tt> is a wrapper class to store the graph and a
 * map structure which maps the nodes of the graph to their string
 * representation <br>
 * The string representation is the label attached to the node in the graph.
 * This is needed because the search for the minimal connecting network provides
 * just the labels and the algorithm needs the corresponding nodes to the search
 * labels.
 * 
 * @author Andreas Groscurth
 */
public class GraphData {
    private Graph graph;
    private Map accMap;

    /**
     * Creates a new <tt>GraphData</tt> object
     * 
     * @param g the graph
     * @param m the map maps the labels of the nodes to the nodes of the graph
     * @throws IllegalArgumentException whether the graph or the map is null
     */
    public GraphData(Graph g, Map m) {
        if ( g == null || m == null ) {
            throw new IllegalArgumentException( "neither the graph "
                    + "nor the map are allowed to be null !" );
        }
        graph = g;
        accMap = m;
    }

    /**
     * Returns the map which maps the label attached to a node in the graph and
     * the actual node of the graph.
     * 
     * @return the accnr map
     */
    public Map getAccMap() {
        return accMap;
    }

    /**
     * Returns the graph
     * 
     * @return the graph
     */
    public Graph getGraph() {
        return graph;
    }
}