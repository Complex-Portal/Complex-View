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

import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.service.graph.Edge;

/**
 * Represent a edge. 
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk) & Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id$
 */
public class BinaryEdge implements Edge<BinaryNode,BinaryInteraction> {
	///////////////////////////////////////////////////
	// Instance variable
	//
	// data contains information about the interaction
    private BinaryInteraction data;
    
    // Node A and B are the proteins, which interact
    private BinaryNode nodeA;
    private BinaryNode nodeB;
    
    
    //////////////////////////////////////////////////
    // Constructors
    // If you want to create an object you need two nodes and the BinaryInteraction data
    public BinaryEdge(BinaryNode nodeA, BinaryNode nodeB, BinaryInteraction data) {
        this.data = data;
        this.nodeA = nodeA;
        this.nodeB = nodeB;
    }
  
    /////////////////////////////////////////////////
    // Getters & Setters
    public BinaryNode getNodeA() {
        return nodeA;
    }

    public BinaryNode getNodeB() {
        return nodeB;
    }
 
    public BinaryInteraction getData() {
        return data;
    }
    
    @Override
    public boolean equals(Object o){
    	
    	if (this == o) return true;
    	if (o == null || getClass() != o.getClass()) return false;
    	BinaryEdge e = (BinaryEdge) o;
    	BinaryNode nodeA = this.getNodeA();
    	BinaryNode nodeB = this.getNodeB();
    	BinaryNode nodea = e.getNodeA();
    	BinaryNode nodeb = e.getNodeB();
    	if (nodeA.getNodeId()==nodea.getNodeId()&& nodeB.getNodeId()== nodeb.getNodeId()) return true;
    	if (nodeA.getNodeId()==nodeb.getNodeId()&& nodeB.getNodeId()== nodea.getNodeId()) return true;
    	return true;
    }
    
    @Override
    public int hashCode(){
    	int result;
    	result = nodeA.hashCode();
    	result += nodeB.hashCode();
    	
    	return result;
    }
    
    @Override
    public String toString(){
    	return (nodeA.getNodeId()+"-"+nodeB.getNodeId());
    }
    
}