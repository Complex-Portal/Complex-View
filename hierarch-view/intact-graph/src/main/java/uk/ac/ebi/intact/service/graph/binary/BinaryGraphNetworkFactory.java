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
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Interactor;

/**
 * Creates <code>BinaryGraphNetwork</code>s
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk) & Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id$
 */
public class BinaryGraphNetworkFactory {

    private BinaryGraphNetworkFactory() {}

    /**
     * This methods creates a BinaryGraphNetwork from a collection of BinaryInteractions.
     * It identifies commons interactors and creates the necessary edges
     * @param binaryInteractions
     * @return BinaryGraphNetwork
     * @throws Exception 
     */
    public static BinaryGraphNetwork createBinaryGraphNetwork(Collection<BinaryInteraction> binaryInteractions) throws GraphException {
    	if (!binaryInteractions.isEmpty()){
	        // the algorithm is a progressive algorigthm where we go checking each node
	        // for the provided interactions and create new Nodes and save in a Set    	
	    	Set<BinaryNode> allNodes = new HashSet<BinaryNode>();
	    	for (BinaryInteraction interaction : binaryInteractions){
	    		Interactor interactorA = interaction.getInteractorA();
	    		Interactor interactorB = interaction.getInteractorB();
	    		
	    		BinaryNode nodeA = new BinaryNode(interactorA, interactorB, interaction);
	    		BinaryNode nodeB = new BinaryNode(interactorB, interactorA, interaction);
	    		
	    		allNodes.add(nodeA);
	    		allNodes.add(nodeB);
	    		//System.out.println("NodeA: "+nodeA.getNodeId()+"\tNodeB: "+nodeB.getNodeId());
	    	}
	    	    	
	    	// next Step is iterate over the Set of Nodes and create edges for each interaction
	    	for (BinaryNode nodeA : allNodes){
	    		Collection<Interactor> allInteractors = nodeA.getInteractors();
	    		for (Interactor interactor : allInteractors){
	    			for (BinaryNode nodeB : allNodes){
	    				if (nodeB.getInteractor().equals(interactor)){
	    					BinaryEdge edge = new BinaryEdge(nodeA, nodeB, nodeA.getData());
	    					nodeA.addBinaryEdge(edge);
	    					nodeB.addBinaryEdge(edge);
	    					break;
	    				}
	    			}
	    		}
	    	}
	    	
	       	// the last Step is to create a graphNetwork and return it.
	    	BinaryGraphNetwork binaryGraph = new BinaryGraphNetwork(allNodes);
	        return binaryGraph;
    	} else {
    		throw new GraphException("Give a not empty Collection of BinaryInteraction");
    	}
 
    }
    
    
   public static Document export(BinaryGraphNetwork binaryGraph){
	  Element graphml = new Element("graphml");
	  
	  Namespace xmlns = Namespace.getNamespace("http://graphml.graphdrawing.org/xmlns");
	  graphml.setNamespace(xmlns);
	  
	  Namespace xsi = Namespace.getNamespace("xsi","http://www.w3.org/2001/XMLSchema-instance");
	  graphml.addNamespaceDeclaration(xsi);
	  
	  Namespace schemaLocation = Namespace.getNamespace("schemaLocation", "http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd"); 
	  graphml.addNamespaceDeclaration(schemaLocation);

	  Element graph = new Element("graph");
	  graph.setAttribute("id", "G");
	  graph.setAttribute("edgedefault","undirected");
	  graphml.addContent(graph);
	  
	  for (BinaryNode binaryNode : binaryGraph.getNodes()){
		  Element node = new Element("node");
		  node.setAttribute("id",binaryNode.getNodeId());
		  graph.addContent(node);  
	  }
	  
	  for (BinaryNode binaryNode : binaryGraph.getNodes()){
		  for (BinaryEdge binaryEdge : binaryNode.getBinaryEdges()){
			  Element edge = new Element("edge");
			 
			  edge.setAttribute("source", binaryEdge.getNodeA().getNodeId());
			  edge.setAttribute("target", binaryEdge.getNodeB().getNodeId());
			  graph.addContent(edge);
		  }
	  }

	  return new Document(graphml);
    	
   }
}