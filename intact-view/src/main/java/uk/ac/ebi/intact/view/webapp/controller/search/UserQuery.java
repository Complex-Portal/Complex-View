/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.view.webapp.controller.search;

import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.view.webapp.controller.browse.ChebiBrowserController;
import uk.ac.ebi.intact.view.webapp.controller.browse.GoBrowserController;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;
import uk.ac.ebi.intact.view.webapp.util.JsfUtils;

import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * User query object wrapper.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller("userQuery")
@Scope("conversation.access")
@ConversationName("general")
public class UserQuery {

    private static final Log log = LogFactory.getLog( UserQuery.class );

    private static final String TERM_NAME_PARAM = "termName";
    public static final String STAR_QUERY = "*:*";

    private enum BooleanOperator {
        AND, OR;
    }

    @Autowired
    private FilterPopulatorController filterPopulator;

    @Autowired
    private IntactViewConfiguration intactViewConfiguration;

    private String searchQuery = STAR_QUERY;
    private String ontologySearchQuery;

    private String[] datasets;
    private String[] sources;
    private String[] expansions;

    private String[] goTerms;
    private String[] chebiTerms;

    private Map<String, String> termMap = Maps.newHashMap();

    //for sorting and ordering
    private static final String DEFAULT_SORT_COLUMN = "rigid";
    private static final boolean DEFAULT_SORT_ORDER = true;

    private String userSortColumn = DEFAULT_SORT_COLUMN;
    private boolean userSortOrder = DEFAULT_SORT_ORDER;

    private int pageSize = 30;

    public UserQuery() {
    }

    @PostConstruct
    public void reset() {
        this.searchQuery = null;
        this.ontologySearchQuery = null;
        this.userSortColumn = DEFAULT_SORT_COLUMN;
        this.userSortOrder = DEFAULT_SORT_ORDER;

        clearFilters();
    }

    public void clearFilters() {
        setSources(filterPopulator.getSources().toArray(new String[filterPopulator.getSources().size()]));
        setExpansions(filterPopulator.getExpansions().toArray(new String[filterPopulator.getExpansions().size()]));

        datasets = new String[0];
        chebiTerms = new String[0];
        goTerms = new String[0];
        termMap.clear();
    }

    public void clearSearchFilters(ActionEvent evt) {
        clearFilters();
    }

    public SolrQuery createSolrQuery() {
        return createSolrQuery( true ); // by default include filters.
    }

    public SolrQuery createSolrQuery( final boolean includeFilters ) {

        if ( log.isTraceEnabled() ) {
            log.trace( "query state: [ontologySearchQuery:"+ontologySearchQuery+"] [searchQuery:"+searchQuery+"]" );
        }

        if( ontologySearchQuery != null && searchQuery != null ) {
            throw new IllegalStateException( "Unexpectedly the user query holds both a quick search ("+ searchQuery +
                                             ") and ontlogy search query ("+ ontologySearchQuery +") !" );
        }

        if( ontologySearchQuery == null &&
            (searchQuery == null || searchQuery.equals("*") || searchQuery.equals("?"))) {
            if ( log.isTraceEnabled() ) {
                log.trace( "Resetting the searchQuery to *:*" );
            }
            searchQuery = STAR_QUERY;
        }

        // select one or the other depending which one is not null
        String q = null;
        if( searchQuery != null ) {
            q = searchQuery;
        } else if( ontologySearchQuery != null ) {
            q = buildSolrOntologyQuery( ontologySearchQuery );
        }

        if (q == null) {
            throw new IllegalStateException("Could build query. It was null");
        }

        q = q.trim();

        q = quoteIfCommonIdWithColon(q);

        SolrQuery query = new SolrQuery( q );
        query.setSortField(userSortColumn, (userSortOrder)? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc);
        query.setRows(pageSize);

        if( includeFilters ) {
            addFilteredQuery(query, "dataset", filterPopulator.getDatasets(), selectDatasetNames( datasets ));
            addFilteredQuery(query, "source", filterPopulator.getSources(), sources);
            addFilteredQuery(query, "expansion", filterPopulator.getExpansions(), expansions);

            addFilteredQuery(query, GoBrowserController.FIELD_NAME, goTerms);
            addFilteredQuery(query, ChebiBrowserController.FIELD_NAME, chebiTerms);
        }

        return query;
    }

    private String quoteIfCommonIdWithColon(String q) {
        if ( q.matches( "CHEBI:\\w+" ) || q.matches( "GO:\\w+" ) || q.matches( "MI:\\w+" ) ) {
            q = "\"" + q + "\"";
        }
        return q;
    }

    private String buildSolrOntologyQuery( String q ) {
        if ( ! ( q.startsWith( "\"" ) && q.endsWith( "\"" ) ) ) {
            // if the query is not escaped already, then do it.
            q = "\"" + q + "\"";
        }
        return "+(detmethod:" + q + " type:" + q + " properties:" + q + ")";
    }

    /**
     * Strips the dataset of their dataset definition, only keeping the dataset name.
     * @param datasets an array of datasets, foramtted as: 'name - description'
     * @return a null array of the same length as datasets containing only the names of the given datasets.
     */
    private String[] selectDatasetNames( String[] datasets ) {
        String[] datasetNames = new String[datasets.length];
        for ( int i = 0; i < datasets.length; i++ ) {
            String dataset = datasets[i];
            final int idx = dataset.indexOf( '-' );
            String name = dataset;
            if( idx != -1 ) {
                name = dataset.substring( 0, idx ).trim();
            }
            datasetNames[i] = name ;
        }
        return datasetNames;
    }

    public String getDisplayQuery() {
        String query = ontologySearchQuery != null ? ontologySearchQuery : searchQuery;

        if ( STAR_QUERY.equals(query)) {
            query = "*";
        }

        if ( termMap.containsKey( query ) ) {
            query = query + " (" + termMap.get( query ) + ")";
        }
        return query;
    }

    private SolrQuery createSolrQueryForHierarchView() {
        // export all available rows
        return createSolrQuery( true ).setRows( 0 );
    }

    /**
     * Builds a String representation of a Solr query without size constraint. The query would return all document hit.
     * @return a non null string.
     */
    public String getSolrQueryString() {
        return getSolrQueryString( createSolrQuery( true ).setRows( 0 ));
    }

    private String getSolrQueryString( SolrQuery query ) {
        StringBuilder sb = new StringBuilder(128);
        boolean first=true;
        final Iterator<String> namesIterator = query.getParameterNamesIterator();
        while ( namesIterator.hasNext() ) {
            String paramName =  namesIterator.next();
            final String[] params = query.getParams( paramName );

            for (String param : params) {
                if (!first) sb.append('&');
                first=false;
                sb.append(paramName);
                sb.append('=');
                if( param != null ) {
                    sb.append( param );
                }
            }
        }
        return sb.toString();
    }

    public String getHierarchViewImageUrl() {
        return buildHierarchViewURL( intactViewConfiguration.getHierarchViewImageUrl() );
    }

    public String getHierarchViewSearchUrl() {
        return buildHierarchViewURL( intactViewConfiguration.getHierarchViewSearchUrl() );
    }

    public String getHierarchViewUrl() {
        return buildHierarchViewURL( intactViewConfiguration.getHierarchViewUrl() );
    }

    private String buildHierarchViewURL( String prefix ) {
        StringBuilder sb = new StringBuilder(256);

        sb.append(prefix);
        sb.append("?sq=");

        try {
            final SolrQuery solrQuery = createSolrQueryForHierarchView();
            final String q = getSolrQueryString( solrQuery );
            final String qe = URLEncoder.encode( q, "UTF-8" );
            sb.append( qe );
        } catch (UnsupportedEncodingException e) {
            // cannot happen
            throw new RuntimeException(e);
        }

        return sb.toString();
    }

    public boolean isUsingFilters() {
        final String[] filterQueries = createSolrQuery( true ).getFilterQueries();

        return (filterQueries != null && filterQueries.length > 0);
    }

    private void addFilteredQuery(SolrQuery query, String field, String[] items) {
        if (items == null || items.length == 0) return;
        query.addFilterQuery( "+" + field + ":" + createLuceneQuery(Arrays.asList(items), BooleanOperator.AND ));
    }

    private void addFilteredQuery(SolrQuery query,
                                  String field,
                                  Collection<String> allItems,
                                  String[] selectedItems) {

        if (selectedItems == null) {
            selectedItems = new String[0];
        }

        if (allItems.size() == selectedItems.length) {
            // all items are selected - nothing to be done
            return;
        }

        Collection<String> included;

        if (!containsNotSpecified(selectedItems)) {
            included = Arrays.asList(selectedItems);
        } else {
            included = Collections.EMPTY_LIST;
        }

        Collection<String> excluded;

        if (containsNotSpecified(selectedItems)) {
            excluded = CollectionUtils.subtract(allItems, Arrays.asList(selectedItems));
        } else {
            excluded = Collections.EMPTY_LIST;
        }

        if (!included.isEmpty()) {
            String lq = createLuceneQuery(included, BooleanOperator.OR );
            query.addFilterQuery( "+" + field + ":" + lq );
        }

        if (!excluded.isEmpty()) {
            String lq = createLuceneQuery(excluded, BooleanOperator.OR );
            query.addFilterQuery( "-" + field + ":" + lq );
        }
    }

    private String createLuceneQuery(Collection<String> items, BooleanOperator operator) {
        StringBuilder sb = new StringBuilder( items.size() * 64 );

        sb.append('(');
        String operatorStr = operator.equals(BooleanOperator.AND) ? "+" : "";

        for (String item : items) {
            sb.append(operatorStr).append('\"').append(item).append("\" ");
        }
        sb.deleteCharAt( sb.length() - 1 ).append(')');
        return sb.toString();
    }

    public void addGoTerm(ActionEvent evt) {
        String param = JsfUtils.getFirstParamValue(evt);
        String termName = (String)JsfUtils.getParameterValue( TERM_NAME_PARAM, evt);
        goTerms = (String[])ArrayUtils.add(goTerms, param);
        termMap.put( param,termName );
    }

    public void addChebiTerm(ActionEvent evt) {
        String param = JsfUtils.getFirstParamValue(evt);
        String termName = (String)JsfUtils.getParameterValue( TERM_NAME_PARAM, evt);
        chebiTerms = (String[]) ArrayUtils.add(chebiTerms, param);
        termMap.put( param,termName );
    }

    public Collection<String> getDatasetsToInclude() {
        if (!containsNotSpecified(datasets)) {
            return Arrays.asList(datasets);
        }
        return Collections.EMPTY_LIST;
    }

    public boolean isCurrentOntologyQuery() {
        return (searchQuery == null && ontologySearchQuery != null);
    }

    public void onOntologySearchCheckboxChanged(ValueChangeEvent evt) {
        if (Boolean.FALSE.equals(evt.getNewValue())) {
            ontologySearchQuery = null;
        }
    }

    public void doSelectAllDatasets(ActionEvent evt) {
        setDatasets(filterPopulator.getDatasets().toArray(new String[filterPopulator.getDatasets().size()]));
    }

    public void doUnselectDatasets(ActionEvent evt) {
        datasets = new String[0];
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
        this.ontologySearchQuery = null;
    }

    public void resetSearchQuery(){
        this.searchQuery = null;
    }

    public String getOntologySearchQuery() {
        return ontologySearchQuery;
    }

    public void setOntologySearchQuery(String ontologySearchQuery) {
        this.ontologySearchQuery = ontologySearchQuery;
    }

    public String[] getSources() {
        return sources;
    }

    public void setSources(String[] sources) {
        this.sources = sources;
    }

    public String[] getDatasets() {
        return datasets;
    }

    public void setDatasets(String[] datasets) {
        this.datasets = datasets;
    }

    public String[] getExpansions() {
        return expansions;
    }

    public void setExpansions(String[] expansions) {
        this.expansions = expansions;
    }

    public static boolean containsNotSpecified(String[] values) {
        for (String value : values) {
            if (isNotSpecified(value)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNotSpecified(String value) {
        return FilterPopulatorController.NOT_SPECIFIED_VALUE.equals(value);
    }

    public String getUserSortColumn() {
        return userSortColumn;
    }

    public void setUserSortColumn( String userSortColumn ) {
        this.userSortColumn = userSortColumn;
    }

    public boolean getUserSortOrder() {
        return userSortOrder;
    }

    public void setUserSortOrder( boolean userSortOrder ) {
        this.userSortOrder = userSortOrder;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String[] getGoTerms() {
        return goTerms;
    }

    public void setGoTerms(String[] goTerms) {
        this.goTerms = goTerms;
    }

    public String[] getChebiTerms() {
        return chebiTerms;
    }

    public void setChebiTerms(String[] chebiTerms) {
        this.chebiTerms = chebiTerms;
    }

    public List<SelectItem> getGoTermsSelectItems() {
        return createSelectItems(goTerms);
    }

    public List<SelectItem> getChebiTermsSelectItems() {
        return createSelectItems(chebiTerms);
    }

    private List<SelectItem> createSelectItems(String[] values) {
        List<SelectItem> selectItems = new ArrayList<SelectItem>(values.length);

        for ( String term : values ) {
            if ( termMap.containsKey( term ) ) {
                selectItems.add( new SelectItem( term, term + " (" + termMap.get( term ) + ")" ) );
            } else {
                selectItems.add( new SelectItem( term ) );
            }
        }

        return selectItems;
    }

    public Map<String, String> getTermMap() {
        return termMap;
    }

    public void setTermMap( Map<String, String> termMap ) {
        this.termMap = termMap;
    }
}