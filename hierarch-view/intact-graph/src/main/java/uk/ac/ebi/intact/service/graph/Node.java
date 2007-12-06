package uk.ac.ebi.intact.service.graph;

import edu.uci.ics.jung.graph.Vertex;

import java.util.Collection;

/**
 * Interface for nodes.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)& Nadin Neuhauser (nneuhauser@ebi.ac.uk)
 * @version $Id$
 */
public interface Node<T extends Edge> extends Vertex {
        
    Collection<T> getEdges();
    
    String getId();

    String getLabel();

    boolean isCentralNode();
}
