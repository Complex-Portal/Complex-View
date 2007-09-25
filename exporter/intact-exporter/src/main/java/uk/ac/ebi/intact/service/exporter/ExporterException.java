package uk.ac.ebi.intact.service.exporter;

/**
 * When an error occurs during interactin export.
 * 
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 *
 */
public class ExporterException extends Exception{
	
	public ExporterException (String s){
		super(s);
	}
	
	public ExporterException (String s, Exception e){
		super(s,e);
	}
}
