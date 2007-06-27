/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.  
All rights reserved. Please see the file LICENSE 
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.graph2MIF.conversion;

import uk.ac.ebi.intact.util.simplegraph.Edge;
import uk.ac.ebi.intact.util.simplegraph.Graph;
import uk.ac.ebi.intact.util.simplegraph.Node;
import uk.ac.ebi.intact.util.simplegraph.NodeI;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Extention of a <code>Graph</code> allowing to fusion two <code>Graph</code>.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class FusionableGraph extends Graph {

    /**
     * Fusion a graph to the current one.<br>
     *
     * For each edge of the new graph we check if it exists in the current one.<br>
     * If the edge already exists : continue.<br>
     * If not, we check if the two Nodes of the edge already exists in the current graph :
     * <blockquote>
     *     if the node exists, update the edge with its reference<br>
     *     if not add it to the current graph
     * </blockquote>
     * finally we add the up-to-date edge to the current graph<br>
     *
     * @param graph the graph we want to fusioned to the current one.
     */
    public void fusion (FusionableGraph graph) {

        Collection newEdges = graph.getEdges();
        Collection edges    = getEdges();
        HashMap    nodes    = getNodes();
        Edge aNewEdge;
        NodeI aNode;
        String ACNode;

        Iterator iterator = newEdges.iterator();
        while (iterator.hasNext()) {
            aNewEdge = (Edge) iterator.next();

            // see also the equals method of Edge
            if (false == edges.contains (aNewEdge)) {
                // check if both nodes are present
                aNode = (NodeI) aNewEdge.getNode1 ();
                ACNode = aNode.getAc();
                // see also the equals method of Node
                if (false == nodes.containsKey(ACNode)) {
                    nodes.put (ACNode, aNode);
                } else {
                    aNewEdge.setNode1 ((Node) nodes.get(ACNode));
                }

                aNode = (NodeI) aNewEdge.getNode2 ();
                ACNode = aNode.getAc();
                if (false == nodes.containsKey(ACNode)) {
                    nodes.put (ACNode, aNode);
                } else {
                    aNewEdge.setNode2 ((Node) nodes.get(ACNode));
                }

                edges.add (aNewEdge);
            }
        }
    } // fusion
}
