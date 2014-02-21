package uk.ac.ebi.intact.service.complex.ws;

import org.apache.solr.client.solrj.response.FacetField;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexResultIterator;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexSearchResults;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 08/11/13
 */
@XmlRootElement(name = "Complexes")
public class ComplexRestResult {
    private int size;
    private List<ComplexSearchResults> elements;
    private List<ComplexFacetResults> facets;

    public ComplexRestResult( ) {
        this.elements = new LinkedList<ComplexSearchResults>();
        this.size = 0;
        this.facets = null;
    }
    public void add( ComplexResultIterator iterator ) {
        this.size += iterator.getNumberOfResults();
        if ( this.facets == null ) {
            this.facets = new LinkedList<ComplexFacetResults>();
            for ( FacetField f : iterator.getFacetFields() ) {
                for (FacetField.Count count : f.getValues() ){
                    this.facets.add( new ComplexFacetResults( count.getName(), count.getCount() ) );
                }
            }
        }
        while ( iterator.hasNext() ) {
            this.elements.add( iterator.next() );
        }
    }

    @XmlElement
    public int getSize() { return this.size; }
    @XmlElement
    public List<ComplexSearchResults> getElements() { return this.elements; }
    @XmlElement
    public List<ComplexFacetResults> getFacets() { return this.facets; }
}
