package uk.ac.ebi.intact.service.graph;

import java.util.Collection;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public interface Node<T extends Edge, D> {

    Collection<T> getEdges();

    D getData();

}
