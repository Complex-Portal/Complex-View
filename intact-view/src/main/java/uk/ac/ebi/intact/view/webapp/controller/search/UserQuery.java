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

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * Holds user query and filters.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 0.9
 */
public class UserQuery {

    public static final Pattern CHEBI_PATTERN = Pattern.compile( "CHEBI:\\d+" );
    public static final Pattern GO_PATTERN = Pattern.compile( "GO:\\d+" );

    private static final Log log = LogFactory.getLog( UserQuery.class );

    private String searchQuery;
    private String ontologySearchQuery;
    private String displayQuery;
    private boolean currentOntologyQuery;

    /**
     * The current list of filters to be applied onto the search query.
     */
    private List<String> filters;

    //////////////////
    // Constructors

    public UserQuery() {
        filters = new ArrayList<String>();
    }

    public void processIncomingFilter( String filter ) {
        if ( log.isDebugEnabled() ) {
            log.debug( "Processing incomming filter: " + filter );
        }

        if ( filter != null ) {
            if ( !filters.contains( filter ) && filter != null && filter.trim().length() > 0) {
                filters.add( filter );
            }
        }

        if ( log.isDebugEnabled() ) {
            log.debug( "After processing, filters are: " + filters );
        }
    }

    public String getCurrentQuery() {
        String query;
        if ( currentOntologyQuery ) {
            query = ontologySearchQuery;
        } else {
            query = searchQuery;
        }

        if ("*".equals(query) || "?".equals(query)) {

            query = "";

        } else if ( queryNeedsEscaping( query ) ) {

            query = "\"" + query + "\"";
            if ( log.isDebugEnabled() ) {
                log.debug( "This looks like a GO or CHEBI identifier, we are escaping to: " + query );
            }
        }

        return query;
    }

    private boolean queryNeedsEscaping( String query ) {
        query = query.toUpperCase().trim();
        return isGoIdentifier( query ) || isChebiIdentifier( query );
    }

    private static boolean isGoIdentifier( String s ) {
        return GO_PATTERN.matcher( s ).matches();
    }

    private static boolean isChebiIdentifier( String s ) {
        return CHEBI_PATTERN.matcher( s ).matches();
    }

    public String getInteractionSearchQuery() {
        String query = buildFilteredLuceneQuery( "properties" );
        log.debug( "getInteractionSearchQuery(): " + query );
        return query;
    }

    public String getInteractorSearchQuery() {
        String query = buildFilteredLuceneQuery( "propertiesA" );
        log.debug( "getInteractorSearchQuery(): " + query );
        return query;
    }

    private String buildFilteredLuceneQuery( String luceneFilterField ) {
        final String query;
        if ( filters.isEmpty() ) {
            query = getCurrentQuery();
        } else {
            String cq = getCurrentQuery();
            if( cq.length() == 0 ) {
                query = formatFilter( "propertiesA" );
            } else {
                query = "+(" + cq + ") " + formatFilter( "propertiesA" );
            }
        }
        return query;
    }

    private String formatFilter( String luceneField ) {
        StringBuilder sb = new StringBuilder( filters.size() * 10 );
        for ( Iterator<String> iterator = filters.iterator(); iterator.hasNext(); ) {
            String filter = iterator.next();
            if ( luceneField != null ) {
                sb.append( "+" ).append( luceneField ).append( ":" );
            }
            sb.append( "\"" ).append( filter ).append( "\"" ).append( " " );
        }
        return sb.toString().trim();
    }

    ///////////////////////////
    // Getters and Setters

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery( String searchQuery ) {
        this.searchQuery = searchQuery;
    }

    public String getOntologySearchQuery() {
        return ontologySearchQuery;
    }

    public void setOntologySearchQuery( String ontologySearchQuery ) {
        this.ontologySearchQuery = ontologySearchQuery;
    }

    public String getDisplayQuery() {
        return displayQuery + buildDisplayFilter();
    }

    private String buildDisplayFilter() {
        StringBuilder sb = new StringBuilder( filters.size() * 10 );
        if( getCurrentQuery().length() > 0 && !filters.isEmpty()) {
            sb.append( " and " );
        }
        for ( Iterator<String> iterator = filters.iterator(); iterator.hasNext(); ) {
            String filter = iterator.next();
            sb.append( filter );
            if(iterator.hasNext()) {
                sb.append( " and " );
            }
        }
        return sb.toString();
    }

    public void setDisplayQuery( String displayQuery ) {
        this.displayQuery = displayQuery;
    }

    public boolean isCurrentOntologyQuery() {
        return currentOntologyQuery;
    }

    public void setCurrentOntologyQuery( boolean currentOntologyQuery ) {
        this.currentOntologyQuery = currentOntologyQuery;
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setFilters( List<String> filters ) {
        this.filters = filters;
    }

    @Override
    public String toString() {
        return "UserQuery{" +
               "searchQuery='" + searchQuery + '\'' +
               ", ontologySearchQuery='" + ontologySearchQuery + '\'' +
               ", displayQuery='" + displayQuery + '\'' +
               ", currentOntologyQuery=" + currentOntologyQuery +
               ", filters=" + filters +
               '}';
    }
}