package uk.ac.ebi.intact.service.complex.view;

import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexResultIterator;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexSearchResults;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 04/12/13
 */
public class ComplexRestResult {
    private int size;
    private List<ComplexSearchResults> elements;
    private String originaQuery = null;
    private Page pageInfo;

    public ComplexRestResult( ) {
        this.elements = new LinkedList<ComplexSearchResults>();
        size = 0;
        this.pageInfo = new Page("0", 10, 0);
    }
    public void add( ComplexResultIterator iterator ) {
        size += iterator.getNumberOfResults();
        while ( iterator.hasNext() ) {
            this.elements.add( iterator.next() );
        }
    }
    public void add( ComplexSearchResults result ) {
        ++size;
        this.elements.add(result);
    }

    public int getSize() { return size; }
    public List<ComplexSearchResults> getElements() { return elements; }
    public void setPageInfo ( Page p ) { this.pageInfo = p; }
    public int getPrevPage() { return this.pageInfo.getPrevPage(); }
    public int getPage() { return this.pageInfo.getPage(); }
    public int getNextPage () { return this.pageInfo.getNextPage(); }
    public int getNumberOfElementsPerPage () { return this.pageInfo.getNumberOfElementsPerPage(); }
    public int getTotalNumberOfElements () { return this.pageInfo.getTotalNumberOfElements(); }
    public int getLastPage () { return this.pageInfo.getLastPage(); }
    public int getStartListCount() { return this.pageInfo.getStartListCount(); }
    public void setOriginalQuery(String query) { this.originaQuery = query; }
    public String getOriginaQuery () { return this.originaQuery; }
}
