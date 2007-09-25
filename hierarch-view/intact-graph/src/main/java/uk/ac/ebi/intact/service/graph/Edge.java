package uk.ac.ebi.intact.service.graph;


/**
 * Interface for edge.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk) & Nadin Neuhauser (nneuhauser@ebi.ac.uk)
 * @version $Id$
 */
public interface Edge<N extends Node,D>
{
    N getNodeA();

    N getNodeB();

    D getData();
    
    boolean equals(Object o);
    
    int hashCode();
    
    String toString();
    
    
}
