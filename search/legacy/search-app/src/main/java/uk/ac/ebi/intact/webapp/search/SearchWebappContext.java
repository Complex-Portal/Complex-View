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

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.context.IntactSession;
import uk.ac.ebi.intact.context.impl.WebappSession;
import uk.ac.ebi.intact.model.Searchable;
import uk.ac.ebi.intact.persistence.dao.query.impl.SearchableQuery;
import uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * Convenience class to access all stored variables in a clear way
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id:SearchWebappContext.java 6452 2006-10-16 17:09:42 +0100 (Mon, 16 Oct 2006) baranda $
 * @since 1.5
 */
public class SearchWebappContext
{
    private static final Log log = LogFactory.getLog(SearchWebappContext.class);

    private static final String SEARCHABLE_QUERY_ATT_NAME = "uk.ac.ebi.intact.search.internal.SEARCHABLE_QUERY";
    private static final String SEARCHABLE_TYPES_ATT_NAME = "uk.ac.ebi.intact.search.internal.SEARCHABLE_TYPES";
    private static final String RESULTS_INFO_ATT_NAME = "uk.ac.ebi.intact.search.internal.RESULTS_INFO";
    private static final String TOTAL_RESULTS_ATT_NAME = "uk.ac.ebi.intact.search.internal.TOTAL_RESULTS";
    private static final String CURRENT_PAGE_ATT_NAME = "uk.ac.ebi.intact.search.internal.CURRENT_PAGE_ATT_NAME";
    private static final String IS_PAGINATED_SEARCH_ATT_NAME = "uk.ac.ebi.intact.search.internal.PAGINATED_SEARCH";
    private static final String RESULTS_PER_PAGE_ATT_NAME = "uk.ac.ebi.intact.search.internal.RESULTS_PER_PAGE_ATT_NAME";
    private static final String QUERY_HISTORY_ATT_NAME = "uk.ac.ebi.intact.search.internal.QUERY_HISTORY_ATT_NAME";
    private static final String QUERY_RESULT_COUNTS_APPL_ATT_NAME = "uk.ac.ebi.intact.search.internal.QUERY_RESULT_COUNTS_APPL_ATT_NAME";

    // Be careful when using class variables, because they are initialized each request. So get/set the values
    // to session attributes if you want to maintain an attribute across requests

    private IntactSession session;
    private static final int APPL_HISTORY_SIZE = 10000;


    private SearchWebappContext()
    {
    }

    public static SearchWebappContext getCurrentInstance()
    {
        return getCurrentInstance(IntactContext.getCurrentInstance());
    }

    public static SearchWebappContext getCurrentInstance(IntactContext context)
    {
        SearchWebappContext swc = new SearchWebappContext();
        /*SearchWebappContext swc = currentInstance.get();

        if (swc == null)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Creating new instance of SearchWebappContext, with session id: "+((WebappSession)context.getSession()).getSession().getId());
            }
            swc = new SearchWebappContext();
            currentInstance.set(swc);
        }
        else
        {
            if (log.isDebugEnabled())
            {
                log.debug("Using SearchWebappContext, with session id: "+((WebappSession)context.getSession()).getSession().getId());
            }
        }  */

        swc.setSession(context.getSession());

        return swc;    
    }

    private static ThreadLocal<SearchWebappContext> currentInstance = new ThreadLocal<SearchWebappContext>()
    {
        protected SearchWebappContext initialValue()
        {
            return null;
        }
    };

    public String getAbsolutePathWithContext()
    {
        HttpServletRequest request = ((WebappSession)session).getRequest();

        if (request == null) return null;

        String port = (request.getServerPort() == 80)? "" : ":"+request.getServerPort();

        return request.getProtocol()+"://"+request.getServerName()+port+request.getContextPath();
    }

    public SearchableQuery getCurrentSearchQuery()
    {
        SearchableQuery sq = (SearchableQuery) session.getRequestAttribute(SEARCHABLE_QUERY_ATT_NAME);

        HttpSession s = ((WebappSession)session).getSession();

        log.debug("Getting current search query: ("+sq+") from Session "+s.getId());

        return sq;
    }

    public void setCurrentSearchQuery(SearchableQuery query)
    {
        session.setAttribute(SEARCHABLE_QUERY_ATT_NAME, query);

        // TODO this will be elimintated eventually
        // backwards compatibility
        session.setRequestAttribute(SEARCHABLE_QUERY_ATT_NAME, query);
        session.setAttribute(SearchConstants.SEARCH_CRITERIA, query);
    }

    public void setCurrentSearchTypes(Class<? extends Searchable>[] searchTypes)
    {
        session.setRequestAttribute(SEARCHABLE_TYPES_ATT_NAME, searchTypes);
    }

    @SuppressWarnings("unchecked")
    public Class<? extends Searchable>[] getCurrentSearchTypes()
    {
        return (Class[]) session.getRequestAttribute(SEARCHABLE_TYPES_ATT_NAME);
    }

    public Integer getTotalResults()
    {
        return (Integer) session.getRequestAttribute(TOTAL_RESULTS_ATT_NAME);
    }

    public void setTotalResults(Integer totalResults)
    {
        session.setRequestAttribute(TOTAL_RESULTS_ATT_NAME, totalResults);
    }

    public Integer getResultsPerPage()
    {
        Integer resPage = (Integer) session.getRequestAttribute(RESULTS_PER_PAGE_ATT_NAME);

        if (resPage == null)
        {
            resPage = getMaxResultsPerPage();
        }

        return resPage;
    }

    public void setResultsPerPage(Integer resultsPerPage)
    {
        session.setRequestAttribute(RESULTS_PER_PAGE_ATT_NAME, resultsPerPage);
    }

    public Integer getCurrentPage()
    {
        Integer page = (Integer) session.getRequestAttribute(CURRENT_PAGE_ATT_NAME);

        if (page == null)
        {
            log.debug("Current page is null. Setting to 0");
            page = 0;
        }

        return page;
    }

    public void setCurrentPage(Integer currentPage)
    {
        log.debug("Current page set to: "+currentPage);
        session.setRequestAttribute(CURRENT_PAGE_ATT_NAME, currentPage);
    }

    public Boolean isPaginatedSearch()
    {
        Boolean isPaginated = (Boolean) session.getRequestAttribute(IS_PAGINATED_SEARCH_ATT_NAME);

        if (isPaginated == null)
        {
            isPaginated = false;
        }

        return isPaginated;
    }

    public void setPaginatedSearch(Boolean paginatedSearch)
    {
        session.setRequestAttribute(IS_PAGINATED_SEARCH_ATT_NAME, paginatedSearch);
    }
    
    public String getHelpLink()
    {
        String relativeHelpLink = session.getInitParam(SearchEnvironment.HELP_LINK);

        //build the help link out of the context path - strip off the 'search' bit...
        String absPathWithoutContext = absolutePathWithoutContext(((WebappSession)session));

        return absPathWithoutContext.concat(relativeHelpLink);
    }

    public String getSearchUrl()
    {
        String appPath = session.getInitParam(SearchEnvironment.SEARCH_LINK);

        return ((WebappSession)session).getRequest().getContextPath().concat(appPath);
    }

    public Integer getMaxResultsPerPage()
    {
        String strMax = IntactContext.getCurrentInstance()
                .getSession().getInitParam(SearchEnvironment.MAX_RESULTS_PER_PAGE);

        if (strMax != null)
        {
            return Integer.valueOf(strMax);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public HashMap<SearchHistoryKey,Map<Class<? extends Searchable>, Integer>> getQueryHistory()
    {
        HashMap queryHistory = (HashMap)
                    IntactContext.getCurrentInstance()
                .getSession().getAttribute(QUERY_HISTORY_ATT_NAME);
        
        if (queryHistory == null)
        {
            queryHistory = new HashMap<SearchHistoryKey,Map<Class<? extends Searchable>, Integer>>();
            IntactContext.getCurrentInstance()
                .getSession().setAttribute(QUERY_HISTORY_ATT_NAME, queryHistory);
        }

        return queryHistory;
    }

    public void addQueryToHistory(SearchHistoryKey key, Map<Class<? extends Searchable>, Integer> resultsCount)
    {
        if (resultsCount == null)
        {
            return;
        }

        if (log.isDebugEnabled()) log.debug("Adding query to history: "+key.toString());
        
        getQueryHistory().put(key, resultsCount);

        // disgreggate the results, and create a new entry with application scope for each search type
        for (Map.Entry<Class<? extends Searchable>,Integer> entry : resultsCount.entrySet())
        {
            SearchHistoryKey appKey = new SearchHistoryKey(key.getQuery(), new Class[] {entry.getKey()});
            Integer value = entry.getValue();

            if (value != null)
            {
                if (log.isDebugEnabled())
                    log.debug("Adding query to app history: "+appKey.toString()+" ; Value: "+value);
            
                getApplicationQueryHistory().put(appKey, value);
            }
        }
    }

    public Map<Class<? extends Searchable>, Integer> getResultCountsFromAppHistory(SearchableQuery query, Class<? extends Searchable>[] types)
    {
        if (query == null)
        {
            throw new NullPointerException("query cannot be null");
        }

        if (types == null)
        {
            throw new NullPointerException("types cannot be null");
        }

        Map<Class<? extends Searchable>, Integer> counts = new HashMap<Class<? extends Searchable>, Integer>();

        // if all the results are in the application history, create the map.
        // If any of the results if missing, return null and they will be counted again later
        for (Class<? extends Searchable> searchable : types)
        {
            Integer count = getResultCountFor(query, searchable);

            if (count != null)
                counts.put(searchable, count);
        }

        if (counts.size() == types.length)
        {
            return counts;
        }

        return null;
    }

    public void setSession(IntactSession session)
    {
        this.session = session;
    }

    public String getAbsolutePathWithoutContext()
    {
        return absolutePathWithoutContext((WebappSession)session);
    }

    public String getHierarchViewAbsoluteUrl()
    {
        return getAbsolutePathWithoutContext().concat(
            session.getInitParam(SearchEnvironment.HIERARCH_VIEW_URL));
    }

    public String getHierarchViewMethod()
    {
        return session.getInitParam(SearchEnvironment.HIERARCH_VIEW_METHOD);
    }

    public String getHierarchViewDepth()
    {
        return session.getInitParam(SearchEnvironment.HIERARCH_VIEW_DEPTH);
    }

    public String getMineAbsoluteUrl()
    {
        return getAbsolutePathWithoutContext().concat(
                session.getInitParam(SearchEnvironment.MINE_URL));
    }

    @SuppressWarnings("unchecked")
    public Map<Class<? extends Searchable>, Integer> getCurrentResultCount()
    {
        return (Map) session.getRequestAttribute(RESULTS_INFO_ATT_NAME);
    }

    public void setCurrentResultCount(Map<Class<? extends Searchable>, Integer> currentResultCount)
    {
        session.setRequestAttribute(RESULTS_INFO_ATT_NAME, currentResultCount);

        SearchableQuery query = getCurrentSearchQuery();
        Class<? extends Searchable>[] types = getCurrentSearchTypes();
        SearchHistoryKey key = new SearchHistoryKey(query, types);

        addQueryToHistory(key, currentResultCount);
    }

    public Integer getResultCountFor(SearchableQuery query, Class<? extends Searchable> searchable)
    {
        return getApplicationQueryHistory().get(new SearchHistoryKey(query, new Class[]{searchable}));
    }

    @SuppressWarnings("unchecked")
    private Map<SearchHistoryKey,Integer> getApplicationQueryHistory()
    {
        Map queryHistory = (LRUMap)
                    IntactContext.getCurrentInstance()
                .getSession().getApplicationAttribute(QUERY_RESULT_COUNTS_APPL_ATT_NAME);

        if (queryHistory == null)
        {
            queryHistory = new LRUMap(APPL_HISTORY_SIZE);
            IntactContext.getCurrentInstance()
                .getSession().setApplicationAttribute(QUERY_RESULT_COUNTS_APPL_ATT_NAME, queryHistory);
        }

        return queryHistory;
    }

    /**
     * Gets the absolute path stripping off the context name from the URL
     * @return The absolute path without the context part
     */
    private static String absolutePathWithoutContext(WebappSession session)
    {
        String ctxtPath = session.getRequest().getContextPath();
        String absolutePathWithoutContext = ctxtPath.substring(0, ctxtPath.lastIndexOf( "/" )+1);

        return absolutePathWithoutContext;
    }

}
