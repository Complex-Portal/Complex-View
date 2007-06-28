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
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.context.impl.WebappSession;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.persistence.dao.query.QueryPhrase;
import uk.ac.ebi.intact.persistence.dao.query.impl.SearchableQuery;
import uk.ac.ebi.intact.persistence.dao.query.impl.StandardQueryPhraseConverter;
import uk.ac.ebi.intact.persistence.svc.SearchService;
import uk.ac.ebi.intact.persistence.svc.impl.SimpleSearchService;
import uk.ac.ebi.intact.webapp.search.SearchWebappContext;
import uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Abstraction of the IntactBaseAction. Gives access to the IntActUser.
 *
 * @author Michael Kleen
 * @version IntactSearchAction.java Date: Feb 17, 2005 Time: 4:13:44 PM
 */
public abstract class SearchActionBase extends IntactSearchAction
{

    private static final Log log = LogFactory.getLog(SearchActionBase.class);

    protected static Class<? extends Searchable>[] DEFAULT_SEARCHABLE_TYPES
            = new Class[] {
                            Experiment.class,
                            InteractionImpl.class,
                            ProteinImpl.class,
                            NucleicAcidImpl.class,
                            SmallMoleculeImpl.class,
                            CvObject.class };

     @Override
    public ActionForward execute( ActionMapping mapping,
                                  ActionForm form,
                                  HttpServletRequest request,
                                  HttpServletResponse response )
            throws IOException, ServletException
    {

        IntactContext intactContext = IntactContext.getCurrentInstance();

        log.debug( "in SearchActionBase");

        log.debug("   IntactContext with session id: "+((WebappSession)intactContext.getSession()).getSession().getId());

        SearchWebappContext webappContext = SearchWebappContext.getCurrentInstance(intactContext);

        // Clear any previous errors.
        super.clearErrors();

        // redirect to the dispatcher if the binary view is required
        String binaryView = request.getParameter("binary");
        if (binaryView != null && binaryView.length() > 0)
        {
             log.debug("Request URL contains the binary parameter. Preparing the binary view...");
             return prepareBinaryView(binaryView, mapping);
        }        

        //clear the error message
        getIntactContext().getSession().setAttribute(SearchConstants.ERROR_MESSAGE, "");

        request.setAttribute("search", form);

        // page
        String strPage = request.getParameter("page");

        if (strPage != null && strPage.length() > 0)
        {
            webappContext.setPaginatedSearch(true);
            webappContext.setCurrentPage(Integer.valueOf(strPage));

            if (log.isDebugEnabled())
                log.debug("Performing paginated search. Page: "+webappContext.getCurrentPage());
        }
        else
        {
            if (log.isDebugEnabled())
                log.debug("Performing non-paginated search.");

            webappContext.setPaginatedSearch(false);
        }

        // starting query
        webappContext.setCurrentSearchQuery(getSearchableQuery(form));
        webappContext.setCurrentSearchTypes(getSearchableTypes(form));

        
        boolean explicitSearch = (request.getParameter("searchClass") != null) || (request.getAttribute("searchClass") != null);

        // if the query is not paginated, count the results
        if (!explicitSearch)
        {
            countResults(mapping, form);

            // forward to a place or another depending on the counts
            ActionForward af = forwardIfNecessary(mapping);

            if (af != null)
            {
                return af;
            }
        }
        else
        {
            // paginated search, get the results from a existing query
            Class<? extends Searchable> searchClass = getSearchableTypes(form)[0];

            if (log.isDebugEnabled())
                log.debug("Getting existing result count for class "+searchClass+ " in history, using query: "+getSearchableQuery(form));

            Integer count = webappContext.getResultCountFor(getSearchableQuery(form), searchClass);

            if (count != null)
            {
                if (log.isDebugEnabled()) log.debug("Results for "+searchClass+": "+count);
                webappContext.setTotalResults(count);
            }
            else
            {
                if (log.isDebugEnabled())
                {
                    log.debug("No results count found for class "+searchClass+ " using query: "+getSearchableQuery(form));
                }

                // count results again
                countResults(mapping, form);
            }
        }

        // if results are to be fetched, get the results and return the appropriate actionForward
        return searchUsingQuery(mapping, form);
    }

    public abstract SearchableQuery createSearchableQuery(ActionForm form);

    public abstract Class<? extends Searchable>[] getSearchableTypes(ActionForm form);

    /**
     * Search using a <code>SearchableQuery</code> and not the lucene index
     */
    protected Map<Class<? extends Searchable>, Integer> countResults(ActionMapping mapping, ActionForm form)
    {
        log.debug("Checking number of results");

        SearchWebappContext webappContext = SearchWebappContext.getCurrentInstance();

        // count the results
        Map<Class<? extends Searchable>, Integer> resultInfo =
                webappContext.getResultCountsFromAppHistory(getSearchableQuery(form), getSearchableTypes(form));

        if (resultInfo == null)
        {
            if (log.isDebugEnabled())
                log.debug("No results found in the application scope, from previous searches. Counting from db.");

            SimpleSearchService searchService = new SimpleSearchService();

            if (getSearchableTypes(form) != null)
            {
                resultInfo = searchService.count(getSearchableTypes(form), getSearchableQuery(form));
            }
            else
            {
                resultInfo = searchService.count(SearchService.STANDARD_SEARCHABLES, getSearchableQuery(form));
            }
        }
        else
        {
            if (log.isDebugEnabled())
                log.debug("Results retrieved from previous searches: "+resultInfo);

        }

        webappContext.setCurrentResultCount(resultInfo);

        return resultInfo;
    }

    private ActionForward forwardIfNecessary (ActionMapping mapping)
    {
        SearchWebappContext webappContext = SearchWebappContext.getCurrentInstance();

        Map<Class<? extends Searchable>, Integer> resultInfo = webappContext.getCurrentResultCount();
        int maxResults = webappContext.getResultsPerPage();

        int size = 0;

        for (int num : resultInfo.values())
        {
            size += num;
        }

        if (log.isDebugEnabled())
        {
             log.debug("Results found: " + size+", distributed in: "+resultInfo);
        }

        webappContext.setTotalResults(size);

        if (size == 0)
        {
            //finished all current options, and still nothing - return a failure
            log.debug("No matches were found for the specified search criteria");
            return mapping.findForward(SearchConstants.FORWARD_NO_MATCHES);
        }
        else if (resultInfo.size() == 1 && size > maxResults)
        {
            if (log.isDebugEnabled())
                log.debug("Only one kind of results found, and the number of results ("+size+") is " +
                    "higher than the max page size ("+maxResults+"). Then this is a paginated search");

            webappContext.setPaginatedSearch(true);
            webappContext.setCurrentPage(1);
        }
        else if (resultInfo.size() > 1 && size > 0 && size > maxResults)
        {
            log.debug("Found results of different types. Forwarding to the summary view");
            return mapping.findForward(SearchConstants.FORWARD_TOO_LARGE);
        }

        return null;
    }

    /**
     * Search using a <code>SearchableQuery</code> and not the lucene index
     */
    public ActionForward searchUsingQuery(ActionMapping mapping, ActionForm form)
    {
        log.debug("Search using query: "+getSearchableQuery(form));

        Integer firstResult;

        // there is a little hack here for the first result, the parntner view is a special
        // view where the pagination is for the children of the element found in the results,
        // this is why when going to a page different than 1, I have to retrieve the first
        // result (the view will then get the corresponding children using the page)
        if (isGoingToParnerView() || isGoingToExperimentInteractionsPage())
        {
            firstResult = 0;
        }
        else
        {
            firstResult = getFirstResult();
        }

        Integer maxResults = SearchWebappContext.getCurrentInstance().getResultsPerPage();

        if (log.isDebugEnabled())
        {
            log.debug("Paginated query: page=" + SearchWebappContext.getCurrentInstance().getCurrentPage()
                    + " firstResult=" + firstResult + " maxResults=" + maxResults+" totalResults: "
                    + SearchWebappContext.getCurrentInstance().getTotalResults());
        }

        List<? extends Searchable> results;

        SimpleSearchService searchService = new SimpleSearchService();

        if (getSearchableTypes(form) != null)
        {
            results = searchService.search(getSearchableTypes(form), getSearchableQuery(form), firstResult, maxResults);
        }
        else
        {
            results = searchService.search(SearchService.STANDARD_SEARCHABLES, getSearchableQuery(form), firstResult, maxResults);
        }

        if (results.isEmpty())
        {
            //finished all current options, and still nothing - return a failure
            log.debug("No matches were found for the specified search criteria");
            return mapping.findForward(SearchConstants.FORWARD_NO_MATCHES);
        }

        // ************* Search was a success. ********************************

        log.debug("found results - forwarding to relevant Action for processing...");

        //determine the shortlabel highlighting list for display...
        log.debug("building highlight list...");

        List<String> labelList = new ArrayList<String>();
        List<AnnotatedObject> aoResults = new ArrayList<AnnotatedObject>(results.size());

        for (Searchable searchable : results)
        {
            AnnotatedObject ao = (AnnotatedObject)searchable;
            aoResults.add(ao);
            labelList.add(ao.getShortLabel());
        }

        //put both the results and also a list of the shortlabels for highlighting into the request

        getIntactContext().getSession().setRequestAttribute(SearchConstants.SEARCH_RESULTS, aoResults);
        getIntactContext().getSession().setRequestAttribute(SearchConstants.HIGHLIGHT_LABELS_LIST, labelList);

        //set the original search criteria into the session for use by
        //the view action - needed because if the 'back' button is used from
        //single object views, the original search beans are lost

        getIntactContext().getSession().setAttribute(SearchConstants.LAST_VALID_SEARCH, getSearchableQuery(form));

        return mapping.findForward(SearchConstants.FORWARD_DISPATCHER_ACTION);
    }


    public boolean isPaginatedSearch()
    {
        return SearchWebappContext.getCurrentInstance(getIntactContext()).getCurrentPage() != null;
    }

    public IntactContext getIntactContext()
    {
        return IntactContext.getCurrentInstance();
    }

    protected Integer getFirstResult()
    {
        Integer firstResult = 0;

        if (SearchWebappContext.getCurrentInstance().isPaginatedSearch())
        {
            Integer resultsPerPage = SearchWebappContext.getCurrentInstance().getResultsPerPage();
            Integer currentPage = SearchWebappContext.getCurrentInstance().getCurrentPage();

            if (currentPage == 0)
            {
                throw new IntactException("Current page is 0, but search is set to be paginated, so it should be at least one. " +
                        "Set the current page before trying to do a query");
            }
            else
            {
               firstResult = (currentPage-1)* resultsPerPage;
            }

        }

        return firstResult;
    }


    public SearchableQuery getSearchableQuery(ActionForm form)
    {
        SearchWebappContext webappContext =  SearchWebappContext.getCurrentInstance();
        SearchableQuery searchableQuery = webappContext.getCurrentSearchQuery();

        if (searchableQuery == null)
        {
            log.debug("Creating new searchable query");

            // replace percents

            searchableQuery = createSearchableQuery(form);

            webappContext.setCurrentSearchQuery(searchableQuery);
            //getIntactContext().getSession().setRequestAttribute("uk.ac.ebi.intact.search.internal.SEARCHABLE_QUERY", searchableQuery);
        }
        return searchableQuery;
    }

    protected HttpServletRequest getRequest()
    {
        return ((WebappSession)IntactContext.getCurrentInstance().getSession()).getRequest();
    }

    private ActionForward prepareBinaryView(String binaryValue, ActionMapping mapping)
    {
        StandardQueryPhraseConverter converter = new StandardQueryPhraseConverter();
        QueryPhrase phrase = converter.objectToPhrase(binaryValue);

        SearchableQuery query = new SearchableQuery();
        query.setAc(phrase);

        log.debug("Executing query "+query+" for searchable type: InteractorImpl");

        List results = getIntactContext().getDataContext()
                .getDaoFactory().getSearchableDao().getByQuery(new Class[] {InteractorImpl.class}, query, 0, Integer.MAX_VALUE);

        SearchWebappContext.getCurrentInstance().setCurrentSearchQuery(query);
        getIntactContext().getSession().setRequestAttribute(SearchConstants.SEARCH_RESULTS, results);

        log.debug("Got "+results.size()+" results. Forwarding to the binaryInteractor Action");

        return mapping.findForward(SearchConstants.FORWARD_BINARYINTERACTOR_ACTION);
    }

    private boolean isGoingToParnerView()
    {
        String view = getRequest().getParameter("view");

        if (view != null)
        {
            return view.equals("partner");
        }
        else
        {
            return false;
        }
    }

    private boolean isGoingToExperimentInteractionsPage()
    {
        String view = getRequest().getParameter("view");

        if (view != null)
        {
            return view.equals("intraExp");
        }
        else
        {
            return false;
        }
    }
}
