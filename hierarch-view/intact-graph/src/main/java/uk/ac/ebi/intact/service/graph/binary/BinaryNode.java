/*
 * Copyright 2001-2007 The European Bioinformatics Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.service.graph.binary;

import java.util.Collection;
import java.util.HashSet;

import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.intact.service.graph.Node;

/**
 * Represent a node.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk) & Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id$
 */
public class BinaryNode implements Node<BinaryEdge, BinaryInteraction, Interactor> {
	///////////////////////////////////////////////////
	// Instance variable
	//
	// interactor contains the information about the interactor
    private Interactor interactor;
    
    // interaction contains the information about the interaction
    private BinaryInteraction interaction;
    
    // is a Set of all Interactors of this node
    private Collection<Interactor> allInteractors;
    
    // is a ArrayList of all edges of this node
    private Collection<BinaryEdge> edges = new HashSet<BinaryEdge>();

    
    //////////////////////////////////////////////////
    // Constructors
    // If you want to create an object you need two interactors and the BinaryInteraction data
    public BinaryNode(Interactor interactorA, Interactor interactorB, BinaryInteraction interaction) {
        this.interactor = interactorA;
        this.interaction = interaction;
        allInteractors =  new HashSet<Interactor>();
        allInteractors.add(interactorB);
    }
    
    /**
     * for the Set the methode hashCode had to be overwritten. 
     */
    @Override
    public int hashCode(){
    	int hash = interactor.hashCode();
    	return hash;
    }
    
    /**
     * for the Set the methode equals had to be overwritten.
     * //n.addInteractor(this.getInteractor());
     */
    @Override
	public boolean equals(Object o ) {
    	BinaryNode n  =(BinaryNode) o;
    	if (this == o){
			return true;
		}
		if (o == null || getClass() != o.getClass()){
			return false;
		}
		if (!n.getInteractor().equals(interactor)) {
			return false;
		}
		return true;
	}

    @Override
    public String toString() {
    	return getNodeId();
    }

    /////////////////////////////////////////////////
    // Getters & Setters
    
    public void addInteractor(Interactor interactorB){
    	allInteractors.add(interactorB);
    }
    
    public Collection<Interactor> getInteractors(){
    	return allInteractors;
    }
    
    public void addBinaryEdge(BinaryEdge edge){
    	edges.add(edge);    	    	
    }
    
    public Collection<BinaryEdge> getBinaryEdges() {
        return edges;
    }

    public Interactor getInteractor() {
        return interactor;
    }
    
    public BinaryInteraction getData(){
    	return interaction;
    }
	
    public String getNodeId(){
    	//return interaction.getInteractionAcs().iterator().next().getIdentifier();
    	return interactor.getIdentifiers().iterator().next().getIdentifier().toString();
    }    
}