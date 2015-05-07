package uk.ac.ebi.intact.service.complex.view;

import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexResultIterator;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexSearchResults;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 04/12/13
 */
public class ComplexRestResult {
    private int size;
    private List<ComplexSearchResults> elements;
    private Map<String,List<ComplexFacetResults>> facets;
    private String originaQuery = null;

    public ComplexRestResult( ) {
        this.elements = new LinkedList<ComplexSearchResults>();
        size = 0;
        this.facets = new HashMap<String, List<ComplexFacetResults>>();
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
    public void add( String facetField, List<ComplexFacetResults> list ) {
        this.facets.put(facetField, list);
    }

    public int getSize() { return size; }
    public List<ComplexSearchResults> getElements() { return elements; }
    public void setSize(int size) { this.size = size; }
    public void setElements(List<ComplexSearchResults> elements) {
        this.elements = elements;
    }
    public void setOriginaQuery(String originaQuery) {
        this.originaQuery = originaQuery;
    }
    public void setOriginalQuery(String query) { this.originaQuery = query; }
    public String getOriginaQuery () { return this.originaQuery; }

    public Map<String, List<ComplexFacetResults>> getFacets() {
        return facets;
    }

    public void setFacets(Map<String, List<ComplexFacetResults>> facets) {
        this.facets = facets;
    }

    @Override
    public String toString(){
        return new StringBuilder() .append("size:") .append(size)
                .append("; query: ") .append(originaQuery)
                .append("; elements: ") .append(elements) .toString();
    }
}
