/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.webapp.search.struts.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.searchengine.ResultWrapper;
import uk.ac.ebi.intact.searchengine.SearchClass;
import uk.ac.ebi.intact.searchengine.SearchHelper;
import uk.ac.ebi.intact.webapp.search.SearchWebappContext;
import uk.ac.ebi.intact.webapp.search.business.IntactUserIF;
import uk.ac.ebi.intact.webapp.search.struts.framework.IntactBaseAction;
import uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * Provides the actions required to carry out search operations for intact via a web-based interface. The search
 * criteria are obtained from a Form object and then the search is carried out, via the IntactUser functionality, which
 * provides the business logic. Once a search has been carried out, this class will forward to another Action class as
 * appropriate to generate specific types of data that will later be displayed via the View classes.
 *
 * @author Chris Lewington (clewing@ebi.ac.uk)
 * @version $Id$
 */
public class SearchAction extends IntactBaseAction {

    private static final Log logger = LogFactory.getLog(SearchAction.class);

    /**
     * Process the specified HTTP request, and create the corresponding HTTP response (or forward to another web
     * component that will create it). Return an ActionForward instance describing where and how control should be
     * forwarded, or null if the response has already been completed.
     *
     * @param mapping  - The <code>ActionMapping</code> used to select this instance
     * @param form     - The optional <code>ActionForm</code> bean for this request (if any)
     * @param request  - The HTTP request we are processing
     * @param response - The HTTP response we are creating
     *
     * @return - represents a destination to which the controller servlet, <code>ActionServlet</code>, might be directed
     *         to perform a RequestDispatcher.forward() or HttpServletResponse.sendRedirect() to, as a result of
     *         processing activities of an <code>Action</code> class
     *
     * @throws Exception ...
     */
    @Override
    public ActionForward execute( ActionMapping mapping, ActionForm form,
                                  HttpServletRequest request,
                                  HttpServletResponse response ) throws Exception {

        logger.info( "search action" );
        // Clear any previous errors.
        super.clearErrors();

        // Get the page to search, if using paginated search
        String strPage = request.getParameter("page");
        int page = 0;
        boolean paginatedSearch = false;

        if (strPage != null && strPage.length() != 0)
        {
            paginatedSearch = true;
            page = Integer.valueOf(strPage);
        }

        if (logger.isDebugEnabled() && paginatedSearch)
        {
            logger.debug("Performing paginated search. Page: "+page);
        }

        // Session to access various session objects. This will create
        //a new session if one does not exist.
        HttpSession session = super.getSession( request );

        // Handle to the Intact User.
        IntactUserIF user = super.getIntactUser( session );
        if ( user == null ) {
            //just set up a new user for the session - if it fails, need to give up!
            user = super.setupUser( request );
            if ( user == null ) {
                return mapping.findForward( SearchConstants.FORWARD_FAILURE );
            }
        }

        //set up a highlight list - needs to exist in ALL cases to avoid
        //JSPs having to check for 'null' all the time...
        List<String> labelList = (List<String>) request.getAttribute( SearchConstants.HIGHLIGHT_LABELS_LIST );
        if ( labelList == null ) {
            labelList = new ArrayList<String>();
        } else {
            labelList.clear();     //set one up or wipe out an existing one
        }

        DynaActionForm dyForm = (DynaActionForm) form;

        String searchValue = (String) dyForm.get( "searchString" );
        SearchClass searchClass = SearchClass.valueOfShortName((String)dyForm.get( "searchClass" ));
        String binaryValue = (String) dyForm.get( "binary" );
        String viewValue = (String) dyForm.get( "view" );
        String filterValue = (String) dyForm.get( "filter" );

        // Feature Request #1485467 : Add a wildcard at the end
        String acPrefix = IntactContext.getCurrentInstance().getConfig().getAcPrefix();

        if (!searchValue.endsWith("*") && !searchValue.toLowerCase().startsWith(acPrefix.toLowerCase()))
        {
            searchValue = searchValue+"*";
        }


        //set a few useful user beans
        user.setSearchValue( searchValue );
        user.setSearchClass( searchClass.getMappedClass() );
        user.setBinaryValue( binaryValue );
        user.setView( viewValue );

        logger.info( "searchValue: " + searchValue );
        logger.info( "searchClass: " + searchClass );
        logger.info( "binaryValue: " + binaryValue );

        //clean out previous single object views
        session.setAttribute( SearchConstants.VIEW_BEAN, null );

        // Holds the result from the initial search.
        ResultWrapper results = null;
        logger.info( "Classname = " + searchClass.getMappedClass() );

        try {

            //first save the search parameters for results page to display.
            session.setAttribute( SearchConstants.SEARCH_CRITERIA, "'" + searchValue + "'" );

            if ( searchValue.equalsIgnoreCase( "%" ) || searchValue.equalsIgnoreCase( "" ) ) {
                return mapping.findForward( SearchConstants.FORWARD_NO_MATCHES );
            }

            SearchHelper searchHelper = new SearchHelper(request);

            // TODO this should probably move to the dispatcher action

            // if it's a binary request first look for this one
            if ( binaryValue != null && !binaryValue.equals( "" ) ) {
                logger.info( "calculate binary Value with : " + binaryValue );
                session.setAttribute( "binary", binaryValue );
                // it's an binary request
                // convert to binary query to a normal one
                binaryValue = binaryValue.replaceAll( "\\,%20", "," );
                session.setAttribute( SearchConstants.SEARCH_CRITERIA, "'" + binaryValue + "'" );

                // split query in single criterias
                Collection<String> queries = new LinkedList<String>();
                StringTokenizer st = new StringTokenizer( binaryValue, "," );
                while ( st.hasMoreTokens() ) {
                    queries.add( st.nextToken().trim() );
                }

                for (String criteria : queries)
                {
                    logger.info("criteria : " + criteria);

                    // first check for ac only
                    // that takes care of a potential bug when searching for a protein AC
                    // having splice variant. That would pull the master + all splice variants
                    ResultWrapper subResults = this.getResults(searchHelper, SearchClass.PROTEIN, criteria, "ac",
                                                               user, paginatedSearch, page);

                    if (subResults.isEmpty())
                    {
                        // then look for all fields if nothing has been found.
                        //finished all current options, and still nothing - return a failure
                        subResults =
                                this.getResults(searchHelper, SearchClass.PROTEIN, criteria, "all", user, paginatedSearch, page);
                    }

                    if ( subResults.isTooLarge() && !paginatedSearch) {
                    // resultset too large, forward to statistic page
                        logger.info( "subresult is too large" );
                        request.setAttribute( SearchConstants.RESULT_INFO, subResults.getInfo() );
                        return mapping.findForward( SearchConstants.FORWARD_TOO_LARGE );
                    }

                    if (subResults.isEmpty())
                    {
                        // no protein found
                        logger.info("result is empty");
                        return mapping.findForward(SearchConstants.FORWARD_NO_INTERACTOR_FOUND);
                    }

                    // search was a sucess

                    if (results == null)
                    {
                        results = subResults;
                    }
                    else
                    {
                        // merge both results together
                        Set<AnnotatedObject> mergedResults = new HashSet<AnnotatedObject>();
                        mergedResults.addAll(subResults.getResult());
                        mergedResults.addAll(results.getResult());

                        int totalResultsCount = subResults.getTotalResultsCount()+results.getTotalResultsCount();
                        request.setAttribute(SearchConstants.TOTAL_RESULTS_ATT_NAME, totalResultsCount);

                        logger.info("mergedResults : " + mergedResults);
                        // create a new ResultInfo
                        Map<String, Integer> resultInfo = new HashMap<String, Integer>();
                        resultInfo.put("uk.ac.ebi.intact.model.ProteinImpl",
                                       mergedResults.size());

                        logger.info("create statistic : " + resultInfo);
                        // create a new resultWerapper
                        List<AnnotatedObject> temp = new ArrayList<AnnotatedObject>();
                        temp.addAll(mergedResults);
                        results =
                                new ResultWrapper(temp,
                                                  SearchConstants.MAXIMUM_RESULT_SIZE, resultInfo, totalResultsCount);

                    }
                } // for
            } // binary value
            else {
               //try now the specified String case first
                results =
                        this.getResults( searchHelper, searchClass, searchValue, filterValue, user, paginatedSearch, page);

                request.setAttribute(SearchConstants.TOTAL_RESULTS_ATT_NAME, results.getTotalResultsCount());
            }

            if ( results.isTooLarge() && !paginatedSearch) {

                logger.info( "Results set is too Large for the specified search criteria" );
                request.setAttribute( SearchConstants.SEARCH_CRITERIA, "'" + searchValue + "'" );
                request.setAttribute( SearchConstants.RESULT_INFO, results.getInfo() );
                return mapping.findForward( SearchConstants.FORWARD_TOO_LARGE );

            }
            if ( results.isEmpty() ) {
                //finished all current options, and still nothing - return a failure
                logger.info( "No matches were found for the specified search criteria" );
                return mapping.findForward( SearchConstants.FORWARD_NO_MATCHES );
            }

            logger.info( "search action: search results retrieved OK" );

            // ************* Search was a success. ********************************

            logger.info( "found results - forwarding to relevant Action for processing..." );

            //determine the shortlabel highlighting list for display...
            logger.info( "building highlight list..." );
            for (AnnotatedObject annotatedObject : results.getResult())
            {
                logger.debug("Search result: " + annotatedObject.getShortLabel());
                labelList.add(annotatedObject.getShortLabel());
            }

            //put both the results and also a list of the shortlabels for highlighting into the request

            Collection<? extends AnnotatedObject> searchResult = results.getResult();
            request.setAttribute( SearchConstants.SEARCH_RESULTS, searchResult );
            request.setAttribute( SearchConstants.HIGHLIGHT_LABELS_LIST, labelList );

            //set the original search criteria into the session for use by
            //the view action - needed because if the 'back' button is used from
            //single object views, the original search beans are lost

            session.setAttribute( SearchConstants.LAST_VALID_SEARCH, searchValue );
            
            return mapping.findForward( SearchConstants.FORWARD_DISPATCHER_ACTION );

        }
        catch ( IntactException se ) {
            se.printStackTrace();

            // Something failed during search...
            logger.error( "Error occured in SearchAction ...", se );

            Throwable t = se.getCause();
            while ( t != null ) {
                logger.error( "Caused by:", t );
                t = t.getCause();
            }

            // clear in case there is some old errors in there.
            super.clearErrors();

            // The errors to report back.
            super.addError( "error.search", se.getMessage() );
            super.saveErrors( request );
            return mapping.findForward( SearchConstants.FORWARD_FAILURE );

        }
    }

    /**
     * Decides how to perform the search, based upon whether or not the intact type has been specified. If not then all
     * the currently defined search types will be iterated through to collect all matches. If the type is specified then
     * the search request must have come from a JSP and so only single-type results are possible. NB this is different
     * to before: Protein 'no type' requests used to go to a binary view, but now this view results from a request from
     * the main JSP result page.
     *
     * @param helper      The search helper instance that will do the searching for us
     * @param searchClass The class to search on - may be null or empty
     * @param searchValue The value to search on
     * @param user        The Intact user object (needed by the search helper)
     *
     * @return Collection A Collection of the results, or empty if none found.
     *
     * @throws IntactException Thrown if there was a searching problem
     */
    private ResultWrapper getResults( SearchHelper helper, SearchClass searchClass,
                                      String searchValue, String filterValue, IntactUserIF user, boolean paginatedSearch, int page)
            throws IntactException {

        ResultWrapper result = null;

        int firstResult = 0;
        int maxResults = SearchConstants.MAXIMUM_RESULT_SIZE;

        if (paginatedSearch)
        {
            int resultsPerPage = SearchWebappContext.getCurrentInstance().getResultsPerPage();
            firstResult = (page-1)* resultsPerPage;
            maxResults = resultsPerPage;
        }

        //BRUNO changed here
    //    if ( searchClass.isSpecified() || SearchValidator.isSearchable( searchClass )) {
            logger.info( "SearchAction: searchfast: " + searchValue + "; searchClass: " + searchClass+ "; page: "+page+"; firstResult: "+firstResult );
            result = helper.searchFast( searchValue, searchClass, filterValue, maxResults, firstResult, paginatedSearch);
   /*     } else {
            // this is a normal request from the servlet, we know the class, we know the value.
            Collection temp = new ArrayList();
            logger.info( "SearchAction: doLookup: " + searchValue + " searchClass: " + searchClass );
            temp.addAll( helper.doLookup( searchClass, searchValue, user ) );
            result = new ResultWrapper( temp, SearchConstants.MAXIMUM_RESULT_SIZE );
        }    */

        return result;
    }
}