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

import org.apache.commons.collections.CollectionUtils;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;
import uk.ac.ebi.intact.view.webapp.util.JsfUtils;

import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller("userQuery")
@Scope("conversation.access")
@ConversationName("general")
public class UserQuery {

    @Autowired
    private FilterPopulatorController filterPopulator;

    @Autowired
    private IntactViewConfiguration intactViewConfiguration;

    private String searchQuery = "*:*";
    private String ontologySearchQuery;

    private String[] datasets;
    private String[] sources;
    private String[] expansions;

    private List<String> goTerms;
    private List<String> chebiTerms;

    //for sorting and ordering
    private static final String DEFAULT_SORT_COLUMN = "rigid";
    private static final boolean DEFAULT_SORT_ORDER = true;

    private String userSortColumn = DEFAULT_SORT_COLUMN;
    private boolean userSortOrder = DEFAULT_SORT_ORDER;

    private int pageSize = 30;

    public UserQuery() {
        this.goTerms = new ArrayList<String>();
        this.chebiTerms = new ArrayList<String>();
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
        setDatasets(filterPopulator.getDatasets().toArray(new String[filterPopulator.getDatasets().size()]));
        setSources(filterPopulator.getSources().toArray(new String[filterPopulator.getSources().size()]));
        setExpansions(filterPopulator.getExpansions().toArray(new String[filterPopulator.getExpansions().size()]));

        chebiTerms.clear();
        goTerms.clear();
    }

    public SolrQuery createSolrQuery() {
        if (searchQuery == null || searchQuery.equals("*") || searchQuery.equals("?")) {
            searchQuery = "*:*";
        }
        
        SolrQuery query = new SolrQuery(searchQuery);
        query.setSortField(userSortColumn, (userSortOrder)? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc);
        query.setRows(pageSize);

        if (ontologySearchQuery != null) {
            String ontologySearch = escapeIfNecessary(ontologySearchQuery);
            
            query.addFilterQuery("+(detmethod:"+ontologySearch+" type:"+ontologySearch+" properties:"+ontologySearch+")");
        }

        addFilteredQuery(query, "dataset", filterPopulator.getDatasets(), datasets);
        addFilteredQuery(query, "source", filterPopulator.getSources(), sources);
        addFilteredQuery(query, "expansion", filterPopulator.getExpansions(), expansions);

        return query;
    }

    public String getDisplayQuery() {
        return searchQuery;
    }

    public String getHierarchViewImageUrl() {
        StringBuilder sb = new StringBuilder(256);

        sb.append(intactViewConfiguration.getHierarchViewImageUrl());
        sb.append("?sq=");

        try {
            sb.append(URLEncoder.encode(createSolrQuery().toString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return sb.toString();
    }

    public String getHierarchViewUrl() {
        StringBuilder sb = new StringBuilder(256);

        sb.append(intactViewConfiguration.getHierarchViewUrl());
        sb.append("?sq=");

        try {
            sb.append(URLEncoder.encode(createSolrQuery().toString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return sb.toString();
    }

    public boolean isUsingFilters() {
        final String[] filterQueries = createSolrQuery().getFilterQueries();

        return (filterQueries != null && filterQueries.length > 0);
    }

    private void addFilteredQuery(SolrQuery query, String field, Collection<String> allItems, String[] selectedItems) {

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
            String lq = createLuceneQuery(included);
            query.addFilterQuery("+"+field+":"+lq);
        }

        if (!excluded.isEmpty()) {
            String lq = createLuceneQuery(excluded);
            query.addFilterQuery("-"+field+":"+lq);
        }
    }

    private String createLuceneQuery(Collection<String> items) {
        StringBuilder sb = new StringBuilder(items.size()*64);

        sb.append('(');

        for (String item : items) {
            sb.append('\"').append(item).append("\" ");

        }
        sb.append(')');
        return sb.toString();
    }

    public void addGoTerm(ActionEvent evt) {
        goTerms.add(JsfUtils.getFirstParamValue(evt));
    }

    public void addChebiTerm(ActionEvent evt) {
        chebiTerms.add(JsfUtils.getFirstParamValue(evt));
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

        private String escapeIfNecessary( String query ) {
        query = query.trim();

         if (query.startsWith("\"") && query.endsWith("\"")) {
             return query;
         }

        if (query.contains(":") || query.contains("(") || query.contains(")")) {
            query = "\"" + query + "\"";
        }

        return query;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
        this.ontologySearchQuery = null;
    }

    public String getOntologySearchQuery() {
        return ontologySearchQuery;
    }

    public void setOntologySearchQuery(String ontologySearchQuery) {
        this.ontologySearchQuery = ontologySearchQuery;
        this.searchQuery = null;
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
}
