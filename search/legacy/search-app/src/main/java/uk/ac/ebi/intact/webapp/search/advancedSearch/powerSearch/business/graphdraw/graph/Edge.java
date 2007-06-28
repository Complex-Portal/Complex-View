package uk.ac.ebi.intact.webapp.search.advancedSearch.powerSearch.business.graphdraw.graph;

/**
 * General graph edge.
 *
 * @author EGO
 * @version $Id$
 * @since 27.04.2005
 */
public interface Edge {

    /**
     * Returns the parent value.
     * @return a Node object representing the parent value
     */
    Node getParent();

    /**
     * Returns the child value.
     * @return a Node object representing the child value
     */
    Node getChild();
}