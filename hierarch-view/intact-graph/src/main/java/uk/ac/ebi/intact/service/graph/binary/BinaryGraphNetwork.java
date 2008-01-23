package uk.ac.ebi.intact.service.graph.binary;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.impl.UndirectedSparseGraph;
import uk.ac.ebi.intact.service.graph.GraphNetwork;

import java.util.*;

import psidev.psi.mi.tab.model.BinaryInteraction;

/**
 * Represent a GraphNetwork of BinaryInteractions
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk) & Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id$
 */
public class BinaryGraphNetwork extends UndirectedSparseGraph implements GraphNetwork<InteractorVertex,BinaryInteractionEdge>, Graph
{

    private Map<String, InteractorVertex> vertices;
    private Map<String, BinaryInteractionEdge> edges;

    private Collection<BinaryInteraction> binaryInteractions;

    public BinaryGraphNetwork() {
        vertices = new HashMap<String, InteractorVertex>();
        edges = new HashMap<String, BinaryInteractionEdge>();
    }

    public Collection<InteractorVertex> getNodes() {
        return vertices.values();
    }

    public Map<String, InteractorVertex> getNodeMap() {
        return vertices;
    }

    public Map<String, BinaryInteractionEdge> getEdgeMap() {
        return edges;
    }

    public void addNode(InteractorVertex vertex) {
        if (!vertices.containsKey(vertex.getId())) {
            vertices.put(vertex.getId(), vertex);
            super.addVertex(vertex);
        }
    }

    public void addEdge(BinaryInteractionEdge edge) {
        if (!edges.containsKey(edge.getId())) {
            edges.put(edge.getId(), edge);
            super.addEdge(edge);

            findNode(edge.getNodeA().getId()).getEdges().add(edge);
            findNode(edge.getNodeB().getId()).getEdges().add(edge);
        }
    }

    public InteractorVertex findNode(String id) {
        return vertices.get(id);
    }

    public Collection<InteractorVertex> getCentralNodes() {
        Collection<InteractorVertex> centralNodes = new HashSet<InteractorVertex>();
        for (InteractorVertex node : getNodes()){
            if (node.isCentralNode()){
                centralNodes.add(node);
            }
        }
        return centralNodes;
    }

    public void setBinaryInteractions( Collection<BinaryInteraction> binaryInteractions ) {
        this.binaryInteractions = binaryInteractions;
    }
    
    public Collection<BinaryInteraction> getBinaryInteractions () {
        return binaryInteractions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("graph{");

        for (InteractorVertex vertex : getNodes()) {
            sb.append(vertex);
            sb.append(",");
        }

        sb.replace(sb.length()-1, sb.length(), "}");
        return sb.toString();
    }
}
