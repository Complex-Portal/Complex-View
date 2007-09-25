package uk.ac.ebi.intact.service.graph;

import java.util.Collection;

import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Interactor;

/**
 * Interface for nodes.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)& Nadin Neuhauser (nneuhauser@ebi.ac.uk)
 * @version $Id$
 */
public interface Node<E extends Edge, D extends BinaryInteraction, I extends Interactor> {
 
	boolean equals(Object o);
	
	int hashCode();
    
	D getData();
    
    void addInteractor(I interactor);
    
    I getInteractor();
        
    Collection<I> getInteractors();
   
    void addBinaryEdge(E edge);
    
    Collection<E> getBinaryEdges();
    
    String getNodeId();    
}
