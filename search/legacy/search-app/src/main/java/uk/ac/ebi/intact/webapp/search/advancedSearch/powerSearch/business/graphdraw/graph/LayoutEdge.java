package uk.ac.ebi.intact.webapp.search.advancedSearch.powerSearch.business.graphdraw.graph;

import java.awt.*;

/**
 * Edge with layout information
 *
 * @author EGO
 * @version $Id$
 * @since 27.04.2005
 */
public interface LayoutEdge extends Edge {

    /**
     * Set the location
     */
    void setRoute(Shape route);
}