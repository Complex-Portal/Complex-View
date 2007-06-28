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
package uk.ac.ebi.intact.webapp.search.struts.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.DynaActionForm;
import uk.ac.ebi.intact.model.Searchable;
import uk.ac.ebi.intact.persistence.dao.query.QueryPhrase;
import uk.ac.ebi.intact.persistence.dao.query.impl.SearchableQuery;
import uk.ac.ebi.intact.persistence.dao.query.impl.StandardQueryPhraseConverter;
import uk.ac.ebi.intact.searchengine.SearchClass;

import java.net.URLDecoder;

/**
 * Execute simple searches
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id:NewSearchAction.java 6452 2006-10-16 17:09:42 +0100 (Mon, 16 Oct 2006) baranda $
 */
public class NewSearchAction extends SearchActionBase
{

    private static final Log log = LogFactory.getLog(NewSearchAction.class);

    public SearchableQuery createSearchableQuery(ActionForm form)
    {
        String searchValue = getParameterFromUrl("searchString");

        if (searchValue == null)
        {
            DynaActionForm dyForm = (DynaActionForm) form;
            searchValue = (String) dyForm.get( "searchString" );

            if (log.isDebugEnabled()) log.debug("Getting 'searchString' from form: "+searchValue);
        }
        else
        {
            if (log.isDebugEnabled()) log.debug("Getting 'searchString' from parameter: "+searchValue);
        }

        boolean filteredAc = (getParameterFromUrl("filter") != null);

        SearchableQuery query;

        if (SearchableQuery.isSearchableQuery(searchValue))
        {
            query = SearchableQuery.parseSearchableQuery(searchValue);
        }
        else
        {
            StandardQueryPhraseConverter converter = new StandardQueryPhraseConverter();
            QueryPhrase phrase = converter.objectToPhrase(searchValue);

            query = new SearchableQuery();
            query.setAc(phrase);

            if (!filteredAc)
            {
                query.setShortLabel(phrase);
                query.setDescription(phrase);
                query.setXref(phrase);
                query.setDisjunction(true);
            }
        }

        return query;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends Searchable>[] getSearchableTypes(ActionForm form)
    {
        DynaActionForm dyForm = (DynaActionForm) form;

        String searchClassName = (String)dyForm.get("searchClass");

        if (searchClassName == null)
        {
            searchClassName = (String) getRequest().getAttribute("searchClass");
        }

        if (searchClassName == null)
        {
            return DEFAULT_SEARCHABLE_TYPES;
        }

        SearchClass searchClass = SearchClass.valueOfShortName(searchClassName);

        if (searchClass == SearchClass.NOSPECIFIED)
        {
            return DEFAULT_SEARCHABLE_TYPES;
        }

        return new Class[] { searchClass.getMappedClass() };
    }

    private String getParameterFromUrl(String paramName)
    {
        String url = getRequest().getQueryString();

        if (url == null)
        {
            return null;
        }

        String[] params = url.split("&");

        for (String param : params)
        {
            String[] nameAndValue = param.split("=", 2);

            if (nameAndValue.length < 2)
            {
                return null;
            }

            String name = nameAndValue[0];
            String value = nameAndValue[1];

            if (name.equals(paramName))
            {
                try
                {
                    value = URLDecoder.decode(value, "UTF-8");
                }
                catch (Throwable t)
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("Could not decode to UTF-8: "+value);
                    }
                }

                return value;
            }
        }

        return null;
    }
}
