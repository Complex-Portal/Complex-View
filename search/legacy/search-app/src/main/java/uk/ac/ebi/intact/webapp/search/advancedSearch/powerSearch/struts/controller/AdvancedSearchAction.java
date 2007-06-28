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
package uk.ac.ebi.intact.webapp.search.advancedSearch.powerSearch.struts.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.DynaActionForm;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.model.Searchable;
import uk.ac.ebi.intact.persistence.dao.query.QueryPhrase;
import uk.ac.ebi.intact.persistence.dao.query.impl.SearchableQuery;
import uk.ac.ebi.intact.persistence.dao.query.impl.StandardQueryPhraseConverter;
import uk.ac.ebi.intact.webapp.search.advancedSearch.powerSearch.struts.business.CvLists;
import uk.ac.ebi.intact.webapp.search.struts.controller.SearchActionBase;
import uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants;

/**
 * Advanced search action querying directly the Database
 * 
 * @author Bruno Aranda
 * @version $Id:AdvancedSearchAction.java 6452 2006-10-16 17:09:42 +0100 (Mon, 16 Oct 2006) baranda $
 */
public class AdvancedSearchAction extends SearchActionBase
{

    private static final Log logger = LogFactory.getLog(AdvancedSearchAction.class);

    private Class<? extends Searchable>[] searchableTypes;

    public Class<? extends Searchable>[] getSearchableTypes(ActionForm form)
    {
        DynaActionForm dyForm = (DynaActionForm) form;
        String searchClassString = (String) dyForm.get( "searchObject" );

        getIntactContext().getSession().setAttribute( SearchConstants.SEARCH_CLASS, searchClassString );

        logger.debug("Search class: "+searchClassString);

        if (searchClassString.equals("any"))
        {
            return DEFAULT_SEARCHABLE_TYPES;
        }

        try
        {
            searchableTypes = new Class[] { Class.forName(searchClassString) };
            return searchableTypes;
        }
        catch (ClassNotFoundException e)
        {
            throw new IntactException(e.getMessage());
        }
    }

    public SearchableQuery createSearchableQuery(ActionForm form)
    {
        DynaActionForm dyForm = (DynaActionForm) form;
        String ac = (String) dyForm.get( "acNumber" );
        String shortlabel = (String) dyForm.get( "shortlabel" );
        String description = (String) dyForm.get( "description" );
        String annotation = (String) dyForm.get( "annotation" );
        String cvTopic = (String) dyForm.get( "cvTopic" );
        String xref = (String) dyForm.get( "xRef" );
        String cvDB = (String) dyForm.get( "cvDB" );
        String cvInteraction = (String) dyForm.get( "cvInteraction" );
        String cvInteractionType = (String) dyForm.get( "cvInteractionType" );
        String cvIdentification = (String) dyForm.get( "cvIdentification" );
        Boolean cvInteractionIncludeChildren = (Boolean) dyForm.get("cvInteractionIncludeChildren");
        Boolean cvInteractionTypeIncludeChildren = (Boolean) dyForm.get("cvInteractionTypeIncludeChildren");
        Boolean cvIdentificationIncludeChildren = (Boolean) dyForm.get("cvIdentificationIncludeChildren");

        cvDB = cvDB.trim();
        cvTopic = cvTopic.trim();

        // create the searchable query object
        SearchableQuery searchableQuery = new SearchableQuery();

        searchableQuery.setAcOrId(toPhrase(ac));
        searchableQuery.setShortLabel(toPhrase(shortlabel));
        //searchableQuery.setFullText(fulltext);
        searchableQuery.setDescription(toPhrase(description));
        searchableQuery.setAnnotationText(toPhrase(annotation));
        searchableQuery.setXref(toPhrase(xref));

        if (!cvTopic.equalsIgnoreCase(CvLists.ALL_TOPICS_SELECTED))
        {
            searchableQuery.setCvTopicLabel(toPhrase(cvTopic));
        }

        if (!cvDB.equalsIgnoreCase(CvLists.ALL_DATABASES_SELECTED)) {
            searchableQuery.setCvDatabaseLabel(toPhrase(cvDB));
        }

        if (!cvInteraction.equalsIgnoreCase(CvLists.NO_CV_INTERACTION_SELECTED))
        {
            searchableQuery.setCvInteractionLabel(toPhrase(cvInteraction, true));

            if (cvIdentificationIncludeChildren != null)
                searchableQuery.setIncludeCvInteractionChildren(cvInteractionIncludeChildren);
        }

        if (!cvInteractionType.equalsIgnoreCase(CvLists.NO_CV_INTERACTION_TYPE_SELECTED))
        {
            searchableQuery.setCvInteractionTypeLabel(toPhrase(cvInteractionType, true));

            if (cvInteractionTypeIncludeChildren != null)
                searchableQuery.setIncludeCvInteractionTypeChildren(cvInteractionTypeIncludeChildren);
        }

        if (!cvIdentification.equalsIgnoreCase(CvLists.NO_CV_IDENTIFICATION_SELECTED))
        {
            searchableQuery.setCvIdentificationLabel(toPhrase(cvIdentification, true));

            if (cvIdentificationIncludeChildren != null)
                searchableQuery.setIncludeCvIdentificationChildren(cvIdentificationIncludeChildren);
        }

        return searchableQuery;
    }

    private static QueryPhrase toPhrase(String value)
    {
        if (value.trim().length() == 0)
        {
            return null;
        }

        return new StandardQueryPhraseConverter().objectToPhrase(value);
    }

    private static QueryPhrase toPhrase(String value, boolean oneTermPhrase)
    {
        if (value.trim().length() == 0)
        {
            return null;
        }

        if (oneTermPhrase)
        {
            value = "\""+value+"\"";
        }

        return new StandardQueryPhraseConverter().objectToPhrase(value);
    }
}
