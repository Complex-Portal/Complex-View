package uk.ac.ebi.intact.service.graph;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public interface Edge<T extends Node,D>
{
    T getNodeA();

    T getNodeB();

    D getData();
}
