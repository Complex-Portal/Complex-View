package uk.ac.ebi.intact.service.complex.ws;

import org.apache.solr.client.solrj.response.FacetField;

import java.util.List;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 21/02/14
 */
public class ComplexFacetResults {
    /********************************/
    /*      Private attributes      */
    /********************************/
    private String name;
    private Long count;

    /**************************/
    /*      Constructors      */
    /**************************/
    public ComplexFacetResults() {
        this.name = null;
        this.count = null;
    }

    public ComplexFacetResults(String name, Long count) {
        this.name = name;
        this.count = count;
    }

    /*********************************/
    /*      Getters and Setters      */
    /*********************************/
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
