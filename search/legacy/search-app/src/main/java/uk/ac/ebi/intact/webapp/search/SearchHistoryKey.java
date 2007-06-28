/**
 * Copyright 2006 The European Bioinformatics Institute, and others.
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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.webapp.search;

import uk.ac.ebi.intact.model.Searchable;
import uk.ac.ebi.intact.persistence.dao.query.impl.SearchableQuery;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Key for the search queryString history map
*
* @author Bruno Aranda (baranda@ebi.ac.uk)
* @version $Id$
*/
public class SearchHistoryKey implements Serializable
{
    private String queryString;
    private Class<? extends Searchable>[] searchableTypes;
    private SearchableQuery query;

    public SearchHistoryKey(SearchableQuery query, Class<? extends Searchable>[] searchableTypes)
    {
        this.queryString = query.toString();
        this.searchableTypes = searchableTypes;
        this.query = query;
    }

    public SearchableQuery getQuery()
    {
        return query;
    }

    public Class<? extends Searchable>[] getSearchableTypes()
    {
        return searchableTypes;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        if (!(o instanceof SearchHistoryKey))
        {
            return false;
        }

        SearchHistoryKey that = (SearchHistoryKey) o;

        if (queryString != null ? !queryString.equals(that.queryString) : that.queryString != null)
        {
            return false;
        }
        if (!Arrays.equals(searchableTypes, that.searchableTypes))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result;
        result = (queryString != null ? queryString.hashCode() : 0);
        result = 31 * result + (searchableTypes != null ? Arrays.hashCode(searchableTypes) : 0);
        return result;
    }
    
    @Override
    public String toString()
    {
        return "SearchHistoryKey{" +
                "queryString='" + queryString + '\'' +
                ", searchableTypes=" + (searchableTypes == null ? null : Arrays.asList(searchableTypes)) +
                '}';
    }
}
