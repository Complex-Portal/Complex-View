package uk.ac.ebi.intact.webapp.search.advancedSearch.powerSearch.business.graphdraw.graph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Graph, contains the nodes, edges and utility methods.
 *
 * @author EGO
 * @version $Id$
 * @since 27.04.2005
 */
public class Graph {

    /**
     * Nodes of the Graph.
     */
    public Set nodes = new HashSet();

    /**
     * Edges of the Graph.
     */
    public Set edges = new HashSet();

    /**
     *
     * @param a
     * @return all parent terms of the given node.
     */
    public Set parents(Node a) {
        Set results = new HashSet();
        for (Iterator i = edges.iterator(); i.hasNext();) {
            Edge e = (Edge) i.next();
            if (e.getChild() == a) results.add(e.getParent());
        }
        return results;
    }

    /**
     *
     * @param a
     * @return all children of the given node.
     */
    public Set children(Node a) {
        Set results = new HashSet();
        for (Iterator i = edges.iterator(); i.hasNext();) {
            Edge e = (Edge) i.next();
            if (e.getParent() == a) results.add(e.getChild());
        }
        return results;
    }

    /**
     *
     * @param p
     * @param c
     * @return an edge
     */
    public Edge findEdge(Node p, Node c) {
        for (Iterator i = edges.iterator(); i.hasNext();) {
            final Object o = i.next();

            Edge e = (Edge) o;
            if ((e.getParent() == p) && (e.getChild() == c)) return e;
        }
        return null;
    }

    /**
     *
     * @param a
     * @param b
     * @return true is node a and b are connected.
     */
    public boolean connected(Node a, Node b) {
        return (findEdge(a, b) != null) || (findEdge(b, a) != null);
    }
}