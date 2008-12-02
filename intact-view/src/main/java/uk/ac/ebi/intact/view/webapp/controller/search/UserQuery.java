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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class UserQuery {

    private static final Log log = LogFactory.getLog( UserQuery.class );

    public static final Pattern CHEBI_PATTERN = Pattern.compile( "CHEBI:\\d+" );
    public static final Pattern GO_PATTERN = Pattern.compile( "GO:\\d+" );

    private enum QueryType { INTERACTOR_QUERY, INTERACTION_QUERY }

    private String searchQuery;
    private String ontologySearchQuery;

    private String interactorTypeMi;

    private List<String> properties;

    private String[] datasets;
    private String[] sources;

    private List<String> filters;

    public UserQuery() {
        this.filters = new ArrayList<String>();

        this.properties = new ArrayList<String>();
    }

    public String createInteractorQuery() {
        return createFilteredQuery(QueryType.INTERACTOR_QUERY);
    }

    public String createInteractionQuery() {
        return createFilteredQuery(QueryType.INTERACTION_QUERY);
    }

    protected String createFilteredQuery(QueryType type) {
        String propertiesField = "";

        switch (type) {
            case INTERACTION_QUERY:
                propertiesField = "properties";
                break;
            case INTERACTOR_QUERY:
                propertiesField = "propertiesA";
                break;
            default:
                throw new IllegalArgumentException("Unexpected QueryType: "+type);
        }

        StringBuilder sbQuery = new StringBuilder(filters.size() * 16);

        if (searchQuery != null && !"*".equals(searchQuery) && !"?".equals(searchQuery)) {
            append(sbQuery, null, searchQuery);
        }

        if (ontologySearchQuery != null) {
            append(sbQuery, null, prepareOntologyQuery(ontologySearchQuery));
        }

        if (type == QueryType.INTERACTOR_QUERY && interactorTypeMi != null) {
            append(sbQuery, "typeA", interactorTypeMi);
        }

        if (properties.size() > 0) {
            for (String term : properties) {
                append(sbQuery, propertiesField, term);
            }
        }

        return sbQuery.toString().trim();
    }

    private void append(StringBuilder sbQuery, String field, String value) {
        if (value == null) return;

        if (!value.startsWith("+")) sbQuery.append("+");

        if (field != null) {
            sbQuery.append(field).append(":");
            value = escapeIfNecessary(value);
        }

        sbQuery.append(value);


        sbQuery.append(" ");
    }

    private String prepareOntologyQuery(String ontologySearchQuery) {
        String identifier = escapeIfNecessary(ontologySearchQuery);
        return "(detmethod:"+identifier+" type:"+identifier+" properties:"+identifier+")";
    }

    public String getDisplayQuery() {
        List<String> displayedItems = new ArrayList<String>();

        if (searchQuery != null && !"*".equals(searchQuery) && !"?".equals(searchQuery)) {
            displayedItems.add(searchQuery);
        }

        if (ontologySearchQuery != null) {
            displayedItems.add(escapeIfNecessary(ontologySearchQuery));
        }

        for (String property : properties) {
            displayedItems.add(escapeIfNecessary(property));
        }

        if (displayedItems.isEmpty()) {
            return "*";
        }

        return StringUtils.join(displayedItems, " AND ").trim();
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

    private static boolean isGoIdentifier( String s ) {
        return GO_PATTERN.matcher( s.toUpperCase() ).matches();
    }

    private static boolean isChebiIdentifier( String s ) {
        return CHEBI_PATTERN.matcher( s.toUpperCase() ).matches();
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public String getOntologySearchQuery() {
        return ontologySearchQuery;
    }

    public void setOntologySearchQuery(String ontologySearchQuery) {
        this.ontologySearchQuery = ontologySearchQuery;
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
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

    public List<String> getProperties() {
        return properties;
    }

    public void setProperties(List<String> properties) {
        this.properties = properties;
    }

    public String getInteractorTypeMi() {
        return interactorTypeMi;
    }

    public void setInteractorTypeMi(String interactorTypeMi) {
        this.interactorTypeMi = interactorTypeMi;
    }
}
