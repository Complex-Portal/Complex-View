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

/**
 * Holds user query and filters.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 0.9
 */
public class UserQuery {

    private static final Log log = LogFactory.getLog( UserQuery.class );

    private String searchQuery;
    private String ontologySearchQuery;
    private String displayQuery;
    private boolean currentOntologyQuery;

    /**
     * The current list of filters to be applied onto the search query.
     */
    private List<String> filters;

//    /**
//     * Incoming filter that should be processed in order to be added into the filter collection.
//     */
//    private String filter;

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
            if ( !filters.contains( filter ) ) {
                filters.add( filter );
            }
        }

        if ( log.isDebugEnabled() ) {
            log.debug( "After processing, filters are: " + filters );
        }
    }

    private String getCurrentQuery() {
        if( currentOntologyQuery ) {
            return ontologySearchQuery;
        } else {
            return searchQuery;
        }
    }

    public String getInteractionSearchQuery() {
        final String query;
        if ( filters.isEmpty() ) {
            query = getCurrentQuery();
        } else {
            query = "+(" + getCurrentQuery() + ") " + formatFilter( "properties" );
        }
        log.debug( "getInteractorSearchQuery(): " + query );
        return query;
    }

    public String getInteractorSearchQuery() {
        final String query;
        if ( filters.isEmpty() ) {
            query = getCurrentQuery();
        } else {
            query = "+(" + getCurrentQuery() + ") " + formatFilter( "propertiesA" );
        }
        log.debug( "getInteractorSearchQuery(): " + query );
        return query;
    }

    private String formatFilter( String luceneField ) {
        StringBuilder sb = new StringBuilder( filters.size() * 10 );
        for ( Iterator<String> stringIterator = filters.iterator(); stringIterator.hasNext(); ) {
            String filter = stringIterator.next();
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
        for ( String filter : filters ) {
            sb.append( " and " ).append( filter );
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