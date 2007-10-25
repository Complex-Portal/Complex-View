package uk.ac.ebi.intact.service.graph;


/**
 * Interface for edge.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk) & Nadin Neuhauser (nneuhauser@ebi.ac.uk)
 * @version $Id$
 */
public interface Edge<T extends Node> extends edu.uci.ics.jung.graph.Edge
{
    T getNodeA();

    T getNodeB();

    String getId();
}
