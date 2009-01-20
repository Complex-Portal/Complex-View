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
import org.apache.commons.lang.StringUtils;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
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

    private String searchQuery;
    private String ontologySearchQuery;

    private String interactorTypeMi;

    private String[] datasets;
    private String[] sources;
    private String[] expansions;

    private List<SearchFilter> interactionFilters;
    private List<SearchFilter> interactorFilters;

    //for sorting and ordering
    private static final String DEFAULT_SORT_COLUMN = "relevancescore_s";
    private static final boolean DEFAULT_SORT_ORDER = true;


    private String userSortColumn = DEFAULT_SORT_COLUMN;
    private boolean userSortOrder = DEFAULT_SORT_ORDER;


    public UserQuery() {
    }

    @PostConstruct
    public void reset() {
        this.searchQuery = null;
        this.ontologySearchQuery = null;
        this.interactionFilters = new LinkedList<SearchFilter>();
        this.interactorFilters = new LinkedList<SearchFilter>();

        this.userSortColumn = DEFAULT_SORT_COLUMN;
        this.userSortOrder = DEFAULT_SORT_ORDER;

        if (filterPopulator != null) {
            setDatasets(filterPopulator.getDatasets().toArray(new String[filterPopulator.getDatasets().size()]));
            setSources(filterPopulator.getSources().toArray(new String[filterPopulator.getSources().size()]));
            setExpansions(filterPopulator.getExpansions().toArray(new String[filterPopulator.getExpansions().size()]));
        }
    }

    public void clearSearchFilters(ActionEvent evt) {
        removeClearableFilters();
    }

    public void removeClearableFilters() {
        removeClearableFilters(interactionFilters);
        removeClearableFilters(interactorFilters);
    }

    protected void removeClearableFilters(Collection<SearchFilter> filters) {
        for (Iterator<SearchFilter> filterIterator = filters.iterator(); filterIterator.hasNext();) {
            SearchFilter searchFilter =  filterIterator.next();
            if (searchFilter.isClearable()) {
                filterIterator.remove();
            }
        }
    }
    
    public String getInteractorQuery() {
        return createFilteredQuery(interactorFilters);
    }

    public String getInteractionQuery() {
        return createFilteredQuery(interactionFilters);
    }

    protected String createFilteredQuery(Collection<SearchFilter> filters) {
        StringBuilder sbQuery = new StringBuilder(filters.size() * 32);

        for (SearchFilter filter : filters) {

            String filterLucene = filter.toLuceneSyntax();

            if (filterLucene.length() > 0) {
                if (filter.isNegated()) {
                    if (!filterLucene.startsWith("-")) sbQuery.append("-");
                } else {
                    if (!filterLucene.startsWith("+")) sbQuery.append("+");
                }
                
                sbQuery.append(filterLucene);
                sbQuery.append(" ");
            }

        }

        return sbQuery.toString().trim();
    }
    
    private String prepareOntologyQuery(String ontologySearchQuery) {
        String identifier = escapeIfNecessary(ontologySearchQuery);
        return "(detmethod:"+identifier+" type:"+identifier+" properties:"+identifier+")";
    }

    public String getDisplayQuery() {
        if (interactionFilters.isEmpty()) {
            return "*";
        }

        List<SearchFilter> nonEmptyFilters = new ArrayList<SearchFilter>();

        for (SearchFilter filter : interactionFilters) {
            if (filter.toDisplay().length() > 0) {
                nonEmptyFilters.add(filter);
            }
        }

        return StringUtils.join(nonEmptyFilters, " AND ");
    }

    public boolean isCurrentOntologyQuery() {
        return (searchQuery == null && ontologySearchQuery != null);
    }

    public String getCurrentQuery() {
        String query;
        if ( isCurrentOntologyQuery() ) {
            query = ontologySearchQuery;
        } else {
            query = searchQuery;
        }

        if ("*".equals(query) || "?".equals(query)) {
            query = "";
        } 

        query = escapeIfNecessary(query);

        return query;
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
        removeSearches();

        this.searchQuery = searchQuery;
        this.ontologySearchQuery = null;

        if (searchQuery != null && !"*".equals(searchQuery) && !"?".equals(searchQuery)) {
            final SimpleFilter searchFilter = new SimpleFilter(searchQuery, false);
            interactionFilters.add(searchFilter);
            interactorFilters.add(searchFilter);
        }
    }

    public void removeCurrentSearchQuery() {
        removeFiltersWithDisplayValue(this.searchQuery);
    }

    public String getOntologySearchQuery() {
        return ontologySearchQuery;
    }

    public void setOntologySearchQuery(String ontologySearchQuery) {
        removeSearches();

        this.ontologySearchQuery = ontologySearchQuery;
        this.searchQuery = null;

        if (ontologySearchQuery != null) {
            final SimpleFilter searchFilter = new SimpleFilter(prepareOntologyQuery(ontologySearchQuery), null, false, ontologySearchQuery);
            interactionFilters.add(searchFilter);
            interactorFilters.add(searchFilter);
        }
    }

    public void removeCurrentOntologySearchQuery() {
        removeFiltersWithDisplayValue(this.ontologySearchQuery);
    }

    public void removeSearches() {
        removeFiltersWithDisplayValue(this.searchQuery);
        removeFiltersWithDisplayValue(this.ontologySearchQuery);
    }

    private void removeFiltersWithDisplayValue(String searchQuery) {
        removeFilterWithDisplayValue(searchQuery, interactionFilters);
        removeFilterWithDisplayValue(searchQuery, interactorFilters);
    }

    private void removeFilterWithDisplayValue(String searchQuery, Collection<SearchFilter> filters) {
        for (Iterator<SearchFilter> searchFilterIterator = filters.iterator(); searchFilterIterator.hasNext();) {
            SearchFilter searchFilter = searchFilterIterator.next();

            if (searchFilter.getDisplayValue().equals(searchQuery)) {
                searchFilterIterator.remove();
            }
        }
    }

    private void removeFiltersWithField(String field) {
        removeFiltersWithField(field, interactionFilters);
        removeFiltersWithField(field, interactorFilters);
    }

    private void removeFiltersWithField(String field, Collection<SearchFilter> filters) {
        if (field == null) return;

        for (Iterator<SearchFilter> searchFilterIterator = filters.iterator(); searchFilterIterator.hasNext();) {
            SearchFilter searchFilter = searchFilterIterator.next();

            if (field.equals(searchFilter.getField())) {
                searchFilterIterator.remove();
            }
        }
    }

    private DisjunctionSearchFilter createDisjunctionFilter(String[] sources, String field) {
        List<SearchFilter> filters = new ArrayList<SearchFilter>();

        if (sources != null) {
            for (String source : sources) {
                final SimpleFilter searchFilter = new SimpleFilter(source, field, true);
                filters.add(searchFilter);
            }
        }

        return new DisjunctionSearchFilter(filters, field);
    }

    public String[] getSources() {
        return sources;
    }

    public void setSources(String[] sources) {
        removeFiltersWithField("source");

        this.sources = sources;

        if (sources != null && sources.length > 0) {
            DisjunctionSearchFilter disjFilter = createDisjunctionFilter(sources, "source");

            interactionFilters.add(disjFilter);
            interactorFilters.add(disjFilter);
        }
    }

    public String[] getDatasets() {
        return datasets;
    }

    public void setDatasets(String[] datasets) {
        removeFiltersWithField("dataset");

        this.datasets = datasets;

        // If the 'not specified' item is selected, we exclude for the search those items
        // in the list wich are not selected.
        DisjunctionSearchFilter disjFilter = null;

        if (datasets != null && datasets.length > 0) {
            if (containsNotSpecified(datasets)) {
               List<String> allDatasets = filterPopulator.getDatasets();
               Collection<String> unselectedDatasets = CollectionUtils.subtract(allDatasets, Arrays.asList(datasets));

                disjFilter = createDisjunctionFilter(unselectedDatasets.toArray(new String[unselectedDatasets.size()]), "dataset");
                disjFilter.setNegated(true);
                
            } else {
                disjFilter = createDisjunctionFilter(datasets, "dataset");
            }
        }

        interactionFilters.add(disjFilter);
        interactorFilters.add(disjFilter);
    }

    public String[] getExpansions() {
        return expansions;
    }

    public void setExpansions(String[] expansions) {
        removeFiltersWithField("expansion");

        this.expansions = expansions;

        if (expansions == null) return;

        // exclude spoke expansions if the spoke checkbox is not selected

        boolean spokeSelected = false;

        for (String expansion : expansions) {
            if (FilterPopulatorController.EXPANSION_SPOKE_VALUE.equals(expansion)) {
                spokeSelected = true;
            }
        }

        if (!spokeSelected) {
            SimpleFilter filter = new SimpleFilter("Spoke", "expansion", true, "Spoke Expansion");
            filter.setNegated(true);
            
            interactionFilters.add(filter);
            interactorFilters.add(filter);
        }
    }

    public static boolean containsNotSpecified(String[] values) {
        for (String value : values) {
            if (FilterPopulatorController.NOT_SPECIFIED_VALUE.equals(value)) {
                return true;
            }
        }
        return false;
    }

    public void addProperty(String term) {
        if (term == null) return;

        interactionFilters.add(new SimpleFilter(term, "properties", true));
        interactorFilters.add(new SimpleFilter(term, "propertiesA", true));
    }

    public String getInteractorTypeMi() {
        return interactorTypeMi;
    }

    public void setInteractorTypeMi(String interactorTypeMi) {
        removeFiltersWithField("typeA");

        this.interactorTypeMi = interactorTypeMi;

        if (interactorTypeMi != null) {
            final SimpleFilter searchFilter = new SimpleFilter(interactorTypeMi, "typeA", true);
            interactorFilters.add(searchFilter);
        }
    }

    public List<SearchFilter> getInteractionFilters() {
        return interactionFilters;
    }

    public void setInteractionFilters(List<SearchFilter> interactionFilters) {
        this.interactionFilters = interactionFilters;
    }

    public List<SearchFilter> getInteractorFilters() {
        return interactorFilters;
    }

    public void setInteractorFilters(List<SearchFilter> interactorFilters) {
        this.interactorFilters = interactorFilters;
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
}
