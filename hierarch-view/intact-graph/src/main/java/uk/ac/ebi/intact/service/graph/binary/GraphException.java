package uk.ac.ebi.intact.service.graph.binary;

/**
 * When an error occurs during interactin export.
 * 
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 *
 */
public class GraphException extends Exception{
	
	public GraphException (String s){
		super(s);
	}
	
	public GraphException (String s, Exception e){
		super(s,e);
	}
}