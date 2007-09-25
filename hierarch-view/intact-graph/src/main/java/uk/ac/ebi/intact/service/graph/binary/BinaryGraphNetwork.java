package uk.ac.ebi.intact.service.graph.binary;

import java.util.Collection;
import java.util.HashSet;

import uk.ac.ebi.intact.service.graph.GraphNetwork;

/**
 * Represent a GraphNetwork of BinaryInteractions (includes BinaryNode & BinaryEdge)
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk) & Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id$
 */
public class BinaryGraphNetwork implements GraphNetwork<BinaryNode>
{
	///////////////////////////////////////////////////
	// Instance variable
	private Collection<BinaryNode> listOfAllNodes;
	
	///////////////////////////////////////////////////
	// Constructors
	public BinaryGraphNetwork(Collection<BinaryNode> nodes){
		this.listOfAllNodes = nodes;
		//getRootNodes();
	}
	
	public Collection<BinaryNode> getNodes(){
		return listOfAllNodes;
	}
	
    public Collection<BinaryNode> getRootNodes() {
    	Collection<BinaryNode> listRootNode = new HashSet<BinaryNode>();
    	Collection<BinaryNode> potentialListRootNode = new HashSet<BinaryNode>();
    	
    	BinaryNode rootNode = null;
    	for (BinaryNode node : listOfAllNodes){
    		int nodeSize = node.getBinaryEdges().size();
    		if (nodeSize > 1){
    			potentialListRootNode.add(node);
    		}
    	}
    	rootNode = potentialListRootNode.iterator().next();
    	listRootNode.add(rootNode);
    	for (BinaryNode node : potentialListRootNode){
    		Collection<BinaryEdge> rootEdges = rootNode.getBinaryEdges();   	
        	int numberOfRootEdge = rootEdges.size();
        	
        	Collection<BinaryEdge> nodeEdges = node.getBinaryEdges(); 
    		int numberOfEdge = nodeEdges.size();

    		if(nodeEdges.containsAll(rootEdges)){
    			if(numberOfRootEdge < numberOfEdge){
    				rootNode = node;
    			}
    		} else {
    			for (BinaryNode rNode : listRootNode){
    		  		Collection<BinaryEdge> rEdges = rNode.getBinaryEdges();   	
    	        	int numberOfrEdge = rEdges.size();
    	        	
    				if (nodeEdges.containsAll(rEdges) && numberOfrEdge < numberOfEdge){
    					listRootNode.remove(rNode);
    					listRootNode.add(node);
    				} else if (numberOfrEdge < numberOfEdge){
    					listRootNode.add(node);
    					break;
    				}
    			}
    		}
    	}
    	if (listRootNode.isEmpty()) listRootNode.add(rootNode);
        System.out.println("rootNode is: " + listRootNode);
        return listRootNode;
    }
    
    @Override
    public String toString(){
    	String result = "";
    	for (BinaryNode node : listOfAllNodes){
    		result += node.getNodeId() + "  \t" + node.getBinaryEdges() +"\n";
    	}
    	
    	return result;
    }

}
