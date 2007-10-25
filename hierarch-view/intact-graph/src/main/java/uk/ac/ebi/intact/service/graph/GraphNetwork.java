package uk.ac.ebi.intact.service.graph;

import edu.uci.ics.jung.graph.Graph;

import java.util.Collection;


/**
 * Interface for Graph networks.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @author Nadin Neuhauser (nneuhauser@ebi.ac.uk)
 * @version $Id$
 */
public interface GraphNetwork<N extends Node, E extends Edge> extends Graph {

    Collection<N> getNodes();

}
