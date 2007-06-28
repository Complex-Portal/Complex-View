package uk.ac.ebi.intact.webapp.search.advancedSearch.powerSearch.business.graphdraw.graph;

/**
 * Node with layout information
 *
 * @author EGO
 * @version $Id$
 * @since 27.04.2005
 */
public interface LayoutNode extends Node {

    /**
     * Get node width
     */
    int getWidth();

    /**
     * Get node height
     */
    int getHeight();

    /**
     * Set the location.
     * The layout algorithm assumes x and y will be the centre of the node.
     */
    void setLocation(int x, int y);
}