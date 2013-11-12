package uk.ac.ebi.intact.service.complex;

import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexResultIterator;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexSearchResults;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 08/11/13
 */
@XmlRootElement(name = "Complexes")
public class ComplexRestResult {
    private List<ComplexSearchResults> elements;

    public ComplexRestResult( ) { this.elements = new ArrayList<ComplexSearchResults>(); }
    public ComplexRestResult( ComplexResultIterator iterator ) {
        while ( iterator.hasNext() ) {
            this.elements.add( iterator.next() ) ;
        }
    }

    public void add( ComplexRestResult result ) { this.elements.addAll(result.elements); }
    public void add( ComplexSearchResults result ) { this.elements.add(result); }
    public void add( ComplexResultIterator iterator ) {
        while ( iterator.hasNext() ) {
            this.elements.add( iterator.next() );
        }
    }

    public void setElements( Collection< ComplexSearchResults > elems ) {
        this.elements = new ArrayList<ComplexSearchResults>(elems);
    }
    @XmlElement
    public List<ComplexSearchResults> getElements() { return elements; }

}
