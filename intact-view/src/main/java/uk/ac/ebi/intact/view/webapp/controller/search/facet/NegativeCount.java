package uk.ac.ebi.intact.view.webapp.controller.search.facet;

import org.apache.solr.client.solrj.response.FacetField;

/**
 * Count negative interactions
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>18/09/12</pre>
 */

public class NegativeCount extends AbstractCount {

    public NegativeCount(FacetField facetField) {
        super(facetField);
    }

    public long getNegativeCount() {
        return getCount("true");
    }

    public long getPhysicalCount() {
        return getCount("false");
    }
}
