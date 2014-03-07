package uk.ac.ebi.intact.service.complex.ws;

import org.apache.solr.client.solrj.response.FacetField;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexResultIterator;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexSearchResults;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 08/11/13
 */
@XmlRootElement(name = "Complexes")
public class ComplexRestResult {
    /********************************/
    /*      Private attributes      */
    /********************************/
    private int size;
    private List<ComplexSearchResults> elements;
    private Map<String,List<ComplexFacetResults>> facets;

    /*************************/
    /*      Constructor      */
    /*************************/
    public ComplexRestResult( ) {
        this.elements = new LinkedList<ComplexSearchResults>();
        this.size = 0;
        this.facets = null;
    }

    /***************************/
    /*      Public method      */
    /***************************/
    public void add( ComplexResultIterator iterator ) {
        this.size += iterator.getNumberOfResults();
        if ( this.facets == null && iterator != null && iterator.getFacetFields() != null ) {
            this.facets = new HashMap<String, List<ComplexFacetResults>>();
            Map<String, List<FacetField.Count>> map = iterator.getFacetFields();
            for ( String field : map.keySet() ) {
                if ( map.get(field) != null ) {
                    List<ComplexFacetResults> list = new ArrayList<ComplexFacetResults>();
                    for (FacetField.Count count : map.get(field) ){
                        list.add( new ComplexFacetResults(count.getName(), count.getCount()));
                    }
                    this.facets.put(field, list);
                }
            }
        }
        while ( iterator.hasNext() ) {
            this.elements.add( iterator.next() );
        }
    }

    /*********************/
    /*      Getters      */
    /*********************/
    @XmlElement
    public int getSize() { return this.size; }
    @XmlElement
    public List<ComplexSearchResults> getElements() { return this.elements; }
    @XmlElement
    public Map<String,List<ComplexFacetResults>> getFacets() { return this.facets; }
}
