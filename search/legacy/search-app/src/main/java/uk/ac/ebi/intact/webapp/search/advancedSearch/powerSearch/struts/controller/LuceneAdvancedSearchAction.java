/*
* Copyright (c) 2002 The European Bioinformatics Institute, and others.
* All rights reserved. Please see the file LICENSE
* in the root directory of this distribution.
*/
package uk.ac.ebi.intact.webapp.search.advancedSearch.powerSearch.struts.controller;

import org.apache.commons.collections.IterableMap;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.BooleanQuery;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.searchengine.business.SearchEngineImpl;
import uk.ac.ebi.intact.searchengine.business.dao.SearchDAOImpl;
import uk.ac.ebi.intact.searchengine.lucene.IntactAnalyzer;
import uk.ac.ebi.intact.searchengine.parser.IQLParserImpl;
import uk.ac.ebi.intact.util.PropertyLoader;
import uk.ac.ebi.intact.webapp.search.advancedSearch.powerSearch.struts.business.QueryBuilder;
import uk.ac.ebi.intact.webapp.search.business.IntactUserIF;
import uk.ac.ebi.intact.webapp.search.struts.framework.IntactBaseAction;
import uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * This class gets the search information out of the web form, converts it into an IQL statement and retrives the
 * results. Having the results it forwards to the AdvDispatcherAction
 * <p>
 * copied and modified from the SearchAction in search
 *
 * @author Anja Friedrichsen, Michael Kleen
 * @version $Id:LuceneAdvancedSearchAction.java 6452 2006-10-16 17:09:42 +0100 (Mon, 16 Oct 2006) baranda $
 *
 * @deprecated
 */
public class LuceneAdvancedSearchAction extends IntactBaseAction {

    private static final Log logger = LogFactory.getLog(LuceneAdvancedSearchAction.class);

    private String indexPath;

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
     * @throws IOException      ...
     * @throws ServletException ...
     */
    public ActionForward execute( ActionMapping mapping,
                                  ActionForm form,
                                  HttpServletRequest request,
                                  HttpServletResponse response )
            throws IOException, ServletException {

        logger.info( "in AdvancedSearchActionTest" );
        // Clear any previous errors.
        super.clearErrors();

        // Session to access various session objects. This will create
        //a new session if one does not exist.
        HttpSession session = super.getSession( request );

        //clear the error message
        session.setAttribute( SearchConstants.ERROR_MESSAGE, "" );

        // Handle to the Intact User.
        IntactUserIF user = super.getIntactUser( session );
        if ( user == null ) {
            //just set up a new user for the session - if it fails, need to give up!
            user = super.setupUser( request );
            if ( user == null ) {
                return mapping.findForward( SearchConstants.FORWARD_FAILURE );
            }
        }

        String relativeHelpLink = getServlet().getServletContext().getInitParameter( "helpLink" );
        //build the help link out of the context path - strip off the 'search' bit...

        // build the path for the Lucene index
        indexPath = null;

        final String configFilename = "/config/advancedSearch.properties";
        Properties props = PropertyLoader.load( configFilename );
        if ( props != null ) {
            indexPath = props.getProperty( "lucene.index.directory" );

            File directory = new File( indexPath );
            if ( !( directory.isDirectory() && directory.canRead() ) ) {
                logger.error( indexPath + " is not a directory or is not readable." );
                return mapping.findForward( SearchConstants.FORWARD_INDEX_ERROR );
            }

        } else {
            logger.error( "Unable to open the properties file: " + configFilename );
            return mapping.findForward( SearchConstants.FORWARD_INDEX_ERROR );
        }


        logger.info( "index path: " + indexPath );

        //set up a highlight list - needs to exist in ALL cases to avoid
        //JSPs having to check for 'null' all the time...
        List labelList = (List) request.getAttribute( SearchConstants.HIGHLIGHT_LABELS_LIST );
        if ( labelList == null ) {
            labelList = new ArrayList();
        } else {
            labelList.clear();     //set one up or wipe out an existing one
        }


        DynaActionForm dyForm = (DynaActionForm) form;
        String searchClassString = (String) dyForm.get( "searchObject" );
        String ac = (String) dyForm.get( "acNumber" );
        String shortlabel = (String) dyForm.get( "shortlabel" );
        String description = (String) dyForm.get( "description" );
        String fulltext = (String) dyForm.get( "fulltext" );
        String annotation = (String) dyForm.get( "annotation" );
        String cvTopic = (String) dyForm.get( "cvTopic" );
        String xref = (String) dyForm.get( "xRef" );
        String cvDB = (String) dyForm.get( "cvDB" );
        String iqlStmt = (String) dyForm.get( "iqlStatement" );

        cvDB = cvDB.trim();
        cvTopic = cvTopic.trim();

        logger.info( "searchClass: " + searchClassString );
        logger.info( "ac: " + ac );
        logger.info( "shortlabel: " + shortlabel );
        logger.info( "description: " + description );
        logger.info( "annotation: " + annotation );
        logger.info( "cvtopic: " + cvTopic );
        logger.info( "xref: " + xref );
        logger.info( "cvDB: " + cvDB );
        logger.info( "cvInteraction: " + dyForm.get( "cvInteraction" ) );
        logger.info( "cvInteractionType: " + dyForm.get( "cvInteractionType" ) );
        logger.info( "cvIdentification: " + dyForm.get( "cvIdentification" ) );

        //set the searchObject to avoid strange html pages (CVs)
        request.setAttribute( SearchConstants.SEARCH_OBJECT, searchClassString );

        // todo store the error messages in an errorBean
        // check if the search strings have the right syntax
        if ( ac.startsWith( "*" ) || ac.startsWith( "?" ) || ac.startsWith( "~" ) ) {
            logger.error( "syntax error in ac: " + ac );
            session.setAttribute( SearchConstants.ERROR_MESSAGE, "Sorry your specified query can't be searched!" );
            return mapping.findForward( SearchConstants.FORWARD_ERROR );
        }
        if ( shortlabel.startsWith( "*" ) || shortlabel.startsWith( "?" ) || shortlabel.startsWith( "~" ) ) {
            logger.error( "syntax error in shortlabel: " + shortlabel );
            session.setAttribute( SearchConstants.ERROR_MESSAGE, "Sorry your specified query can't be searched!" );
            return mapping.findForward( SearchConstants.FORWARD_ERROR );
        }
        if ( description.startsWith( "*" ) || description.startsWith( "?" ) || description.startsWith( "~" ) ) {
            logger.error( "syntax error in description: " + description );
            session.setAttribute( SearchConstants.ERROR_MESSAGE, "Sorry your specified query can't be searched!" );
            return mapping.findForward( SearchConstants.FORWARD_ERROR );
        }
        if ( fulltext.startsWith( "*" ) || fulltext.startsWith( "?" ) || fulltext.startsWith( "~" ) ) {
            logger.error( "syntax error in description: " + description );
            session.setAttribute( SearchConstants.ERROR_MESSAGE, "Sorry your specified query can't be searched!" );
            return mapping.findForward( SearchConstants.FORWARD_ERROR );
        }

        // get an error if a database is selected but the textfield is empty
        // because if it is empty but a database selected it would be like a * on that database and
        // a star is unfortunately not allowed with Lucene
        // todo don't send an error, but search for all occurences
        if ( ( !( cvDB.equalsIgnoreCase( "-all databases-" ) ) && !cvDB.equals( "" ) ) && ( ( xref == null ) || ( xref.equals( "" ) ) ) )
        {
            logger.error( "empty field with database '" + cvDB + "'" );
            session.setAttribute( SearchConstants.ERROR_MESSAGE, "Please fill the textfield of CvDatabase" );
            return mapping.findForward( SearchConstants.FORWARD_ERROR );
        }

        //get an error if a topic is selected but the textfield is empty
        if ( ( !( cvTopic.equalsIgnoreCase( "-all topics-" ) ) && !cvTopic.equals( "" ) ) && ( ( annotation == null ) || ( annotation.equals( "" ) ) ) )
        {
            logger.error( "empty field with cvTopic '" + cvTopic + "'" );
            session.setAttribute( SearchConstants.ERROR_MESSAGE, "Please fill the textfield of CvTopic" );
            return mapping.findForward( SearchConstants.FORWARD_ERROR );
        }

        // the String holding the complete IQL statement
        String iqlStatement = null;
        // the String holding only the part of the IQL statement to display
        String iqlToDisplay = null;
        Map searchKeys = null;
        Map results = null;

        logger.info( "iqlStmt: " + iqlStmt );
        if ( iqlStmt == null || iqlStmt.equals( "" ) ) {
            QueryBuilder qBuilder = null;
            qBuilder = new QueryBuilder( dyForm );
            // create a SQL-like statement out of the search information of the form
            try {
                iqlStatement = qBuilder.getSqlLikeStatement();
            } catch ( IntactException e ) {
                logger.error( " problems with building the IQL statement", e );
                session.setAttribute( SearchConstants.ERROR_MESSAGE, "Sorry your specified query can't be searched!" );
                return mapping.findForward( SearchConstants.FORWARD_ERROR );
            }
        } else {
            // the iqlStatement is already set in the URL (coming from tooLarge)
            iqlStatement = iqlStmt;
        }
        logger.info( "IQL statement: " + iqlStatement );
        iqlToDisplay = iqlStatement;

        request.setAttribute( "search", form );

// uncommnent this part because it was not working how I wanted it, the text overMouse was not shown completely
// it was in fact shorter than the iqlToDisplay
//
//        // cut the iqlToDisplay if it is to long to be displayed nicely
//        if(iqlToDisplay.length() > 150){
//            iqlToDisplay = iqlToDisplay.substring(0, 150);
//            iqlToDisplay += " ...";
//            String iqlTemp = iqlStatement;
//            iqlTemp = iqlTemp.replaceAll("\"", "'");
//            // make a mouseOver effect to see the whole statement
//            iqlToDisplay = "<span title=\"" + iqlTemp + "\">" + iqlToDisplay + "</span>";
//        }

        // make the iql statement a little bit nicer to view
        iqlToDisplay = iqlToDisplay.replaceFirst( "select", "<span class=\"largetext\">select</span>" );
        iqlToDisplay = iqlToDisplay.replaceFirst( "where", "<span class=\"largetext\">where</span>" );
        iqlToDisplay = iqlToDisplay.replaceFirst( "from", "<span class=\"largetext\">from</span>" );

        logger.info( "IQL query: " + iqlStatement );

        session.setAttribute( SearchConstants.SEARCH_CRITERIA, "\"" + iqlToDisplay + "\"" );
        session.setAttribute( SearchConstants.SEARCH_CLASS, searchClassString );

        // search with the IQL statement
        try {
            // retrieve the searchKeys
            searchKeys = this.estimateResults( iqlStatement );
            logger.info( "got " + searchKeys.size() + " searchKeys" );
        } catch ( IntactException e ) {
            logger.error( "Problems with getting the results by IQL: " + iqlStatement, e );
            session.setAttribute( SearchConstants.ERROR_MESSAGE, "Sorry your specified query can't be searched!" );
            return mapping.findForward( SearchConstants.FORWARD_ERROR );
        } catch ( BooleanQuery.TooManyClauses be ) {
            logger.error( "To many hits to handle with Lucene", be );
            session.setAttribute( SearchConstants.ERROR_MESSAGE, "Sorry your specified query caused too many hits!" );
            return mapping.findForward( SearchConstants.FORWARD_ERROR );
        }

        // check if the number of hits is too large
        if ( searchKeys.size() > SearchConstants.MAXIMUM_RESULT_SIZE ) {
            request.setAttribute( SearchConstants.RESULT_INFO, searchKeys );
            logger.info( "forward to 'tooLarge' ..." );
            return mapping.findForward( SearchConstants.TOO_LARGE_DATASET );
        } else {
            try {
                // get the intact object out of the database
                results = getResults( searchKeys );
                logger.info( "got " + results.size() + " results" );
            } catch ( IntactException e ) {
                logger.error( "Problems with getting the results out of the database" );
                session.setAttribute( SearchConstants.ERROR_MESSAGE, "Sorry there is a problem with the database" );
                return mapping.findForward( SearchConstants.FORWARD_ERROR );
            }
        }

        // set the map with the results to the request
        request.setAttribute( SearchConstants.SEARCH_RESULTS_MAP, results );
        logger.info( "SEARCH: " + session.getAttribute( SearchConstants.SEARCH_CRITERIA ) );

        //set a few useful user beans
        user.setSearchValue( iqlStatement );
        //user.setSearchClass(searchClassString);


        if ( results.isEmpty() ) {
            //finished all current options, and still nothing - return a failure
            logger.info( session.getAttribute( SearchConstants.SEARCH_CRITERIA ) );
            logger.info( "No matches were found for the specified search criteria" );
            session.setAttribute( SearchConstants.ERROR_MESSAGE, "Sorry, could not find any entry by trying to match that IQL statement: <br>"
                                                                 + iqlStatement );
            return mapping.findForward( SearchConstants.FORWARD_ERROR );
        }


        IterableMap searchResult = (IterableMap) results;

        logger.info( "found results - forwarding to relevant Action for processing..." );

        //determine the shortlabel highlighting list for display...
        AnnotatedObject obj = null;
        logger.info( "building highlight list..." );

        // add all shortlabel to the highlighting list
        MapIterator it = searchResult.mapIterator();
        Object key = null;
        Collection value = null;
        while ( it.hasNext() ) {
            key = it.next();
            value = (ArrayList) it.getValue();
            for ( Iterator iterator = value.iterator(); iterator.hasNext(); ) {
                obj = (AnnotatedObject) iterator.next();
                if ( obj != null ) {
                    labelList.add( obj.getShortLabel() );
                    logger.debug( "Search result: " + obj.getShortLabel() );
                } else {
                    logger.error( "null object?" );
                }
            }
        }
        //put a list of the shortlabels for highlighting into the request
        request.setAttribute( SearchConstants.HIGHLIGHT_LABELS_LIST, labelList );

        // modifie the iqlStatement, so that it is better readable in the webpage
        iqlStatement = iqlStatement.replaceFirst( "select", "<b>select</b>" );
        iqlStatement = iqlStatement.replaceFirst( "from", "<b>from</b>" );
        iqlStatement = iqlStatement.replaceFirst( "where", "<b>where</b>" );

        //set the original search criteria into the session for use by
        //the view action - needed because if the 'back' button is used from
        //single object views, the original search beans are lost
        session.setAttribute( SearchConstants.LAST_VALID_SEARCH, iqlStatement );

        return mapping.findForward( SearchConstants.FORWARD_ADV_DISPATCHER_ACTION );
    }

    /**
     * this method searches the database with the given iql query and returns a collection with all located ac numbers
     * and their objclass.
     *
     * @param iqlQuery query string to search for
     *
     * @return collection with all searchKeys
     *
     * @throws IntactException
     */
    public Map estimateResults( String iqlQuery ) throws IntactException {

        Map searchKeys = null;
        SearchEngineImpl engine = new SearchEngineImpl( new IntactAnalyzer(), new File( indexPath ), new SearchDAOImpl(), new IQLParserImpl() );

        // get all hits out of the database
        searchKeys = engine.findObjectByIQL( iqlQuery );

        return searchKeys;
    }

    /**
     * this method get a Map with hits and retrieves the corresponding intact objects out of the database.
     *
     * @param searchKeys Map with all hits, the key is the AC number and the value is the objectclass
     *
     * @return a map with all intact objects corresponding to the searchKeys, the map will have the name of the
     *         searchobjects (e.g. experiment) as keys and all found intact objects (e.g all experiment objects) as
     *         value
     *
     * @throws IntactException
     */
    public Map getResults( Map searchKeys ) throws IntactException {
        Map results = null;
        SearchEngineImpl engine = new SearchEngineImpl( new IntactAnalyzer(), new File( indexPath ), new SearchDAOImpl(), null );

        // retrieve the intact search objects in a map
        results = engine.getResult( searchKeys );

        return results;
    }
}