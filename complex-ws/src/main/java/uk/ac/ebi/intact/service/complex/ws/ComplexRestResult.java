package uk.ac.ebi.intact.service.complex.ws;

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

    public ComplexRestResult( ) {
        this.elements = new LinkedList<ComplexSearchResults>();
        this.size = 0;
    }
    public void add( ComplexResultIterator iterator ) {
        this.size += iterator.getNumberOfResults();
        while ( iterator.hasNext() ) {
            this.elements.add( iterator.next() );
        }
    }

    @XmlElement
    public int getSize() { return this.size; }
    @XmlElement
    public List<ComplexSearchResults> getElements() { return this.elements; }
}
