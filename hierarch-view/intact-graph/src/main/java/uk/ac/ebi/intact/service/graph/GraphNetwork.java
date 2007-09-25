package uk.ac.ebi.intact.service.graph;

import java.util.Collection;


/**
 * Interface for Graph networks.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)& Nadin Neuhauser (nneuhauser@ebi.ac.uk)
 * @version $Id$
 */
public interface GraphNetwork<N extends Node>{
	
	Collection<N> getNodes();
	
}
