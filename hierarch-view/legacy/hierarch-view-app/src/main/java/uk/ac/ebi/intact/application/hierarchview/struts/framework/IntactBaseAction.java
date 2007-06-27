/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */

package uk.ac.ebi.intact.application.hierarchview.struts.framework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.*;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUser;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.graph.GraphHelper;
import uk.ac.ebi.intact.application.hierarchview.business.graph.InteractionNetwork;
import uk.ac.ebi.intact.application.hierarchview.business.image.DrawGraph;
import uk.ac.ebi.intact.application.hierarchview.business.image.ImageBean;
import uk.ac.ebi.intact.application.hierarchview.exception.MultipleResultException;
import uk.ac.ebi.intact.application.hierarchview.exception.SessionExpiredException;
import uk.ac.ebi.intact.application.hierarchview.struts.StrutsConstants;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.persistence.SearchException;
import uk.ac.ebi.intact.searchengine.CriteriaBean;
import uk.ac.ebi.intact.searchengine.SearchClass;
import uk.ac.ebi.intact.searchengine.SearchHelper;
import uk.ac.ebi.intact.searchengine.SearchHelperI;
import uk.ac.ebi.intact.util.simplegraph.BasicGraphI;
import uk.ac.ebi.intact.util.Chrono;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

/**
 * Super class for all hierarchview related action classes.
 * 
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */

public abstract class IntactBaseAction extends Action {

    private static final Log logger = LogFactory.getLog(IntactBaseAction.class);

    /** The global Intact error key. */
    public static final String INTACT_ERROR = "IntactError";

    /** Error container */
    private ActionErrors myErrors = new ActionErrors();

    /** The global Intact message key. */
    public static final String INTACT_MESSAGE = "IntactMessage";

    /** Message container */
    private ActionMessages myMessages = new ActionMessages();

    private SearchHelperI searchHelper = new SearchHelper();

    /**
     * Says if an IntactUser object is currently available in the session.
     * 
     * @param session the session to look into.
     * @return true is the IntactUser exists, else false.
     */
    protected boolean intactUserExists(HttpSession session) {
        IntactUserI user = (IntactUserI) IntactContext.getCurrentInstance().getSession()
                .getAttribute( Constants.USER_KEY );
        return ( null != user );
    }

    /**
     * Returns the Intact User instance saved in a session.
     * 
     * @param session the session to access the Intact user object.
     * @return an instance of <code>IntactUserImpl</code> stored in
     *         <code>session</code>
     */
    protected IntactUserI getIntactUser(HttpSession session)
            throws SessionExpiredException {
        IntactUserI user = (IntactUserI) session.getAttribute( Constants.USER_KEY );

        if ( null == user ) {
            logger.warn( "Session expired ... forward to error page." );
            throw new SessionExpiredException();
        }

        return user;
    }

    /**
     * Returns the session from given request. No new session is created.
     * 
     * @param request the request to get the session from.
     * @return session associated with given request. Null is returned if there
     *         is no session associated with <code>request</code>.
     */
    protected HttpSession getSession(HttpServletRequest request)
            throws SessionExpiredException {
        // Don't create a new session.
        HttpSession session = request.getSession( false );

        if ( null == session ) {
            logger.warn( "Session expired ... forward to error page." );
            throw new SessionExpiredException();
        }

        return session;
    }

    /**
     * Returns a session and create a new one if necessary.
     * 
     * @param request the request to get the session from.
     * @return session associated with given request.
     */
    protected HttpSession getNewSession(HttpServletRequest request) {
        return request.getSession( true );
    }

    /////////////////////
    // Error management
    /////////////////////

    /**
     * Clear error container.
     */
    protected void clearErrors() {
        if ( !myErrors.isEmpty() ) {
            myErrors.clear();
        }
    }

    /**
     * Adds an error with given key.
     * 
     * @param key the error key. This value is looked up in the
     *            IntactResources.properties bundle.
     */
    protected void addError(String key) {
        myErrors.add( INTACT_ERROR, new ActionError( key ) );
    }

    /**
     * Adds an error with given key and value.
     * 
     * @param key the error key. This value is looked up in the
     *            IntactResources.properties bundle.
     * @param value the value to substitute for the first place holder in the
     *            IntactResources.properties bundle.
     */
    protected void addError(String key, String value) {
        myErrors.add( INTACT_ERROR, new ActionError( key, value ) );
    }

    /**
     * Saves the errors in given request for <struts:errors>tag.
     * 
     * @param request the request to save errors.
     */
    protected void saveErrors(HttpServletRequest request) {
        super.saveErrors( request, myErrors );

        // As an error occured, remove the image data stored in the session
        try {
            HttpSession session = this.getSession( request );
            IntactUserI user = getIntactUser( session );
            user.setImageBean( null );
        }
        catch ( SessionExpiredException see ) {
        }
    }

    /**
     * Specify if an the error set is empty.
     * 
     * @return boolean false is there are any error registered, else true
     */
    protected boolean isErrorsEmpty() {
        return myErrors.isEmpty();
    }

    //////////////////////
    // Message management
    //////////////////////

    /**
     * Clear Message container.
     */
    protected void clearMessages() {
        if ( !myMessages.isEmpty() ) {
            myMessages.clear();
        }
    }

    /**
     * Adds an Message with given key.
     * 
     * @param key the Message key. This value is looked up in the
     *            Struts.properties bundle.
     */
    protected void addMessage(String key) {
        myMessages.add( INTACT_MESSAGE, new ActionMessage( key ) );
    }

    /**
     * Adds an Message with given key and value.
     * 
     * @param key the Message key. This value is looked up in the
     *            Struts.properties bundle.
     * @param value the value to substitute for the first place holder in the
     *            Struts.properties bundle.
     */
    protected void addMessage(String key, String value) {
        myMessages.add( INTACT_MESSAGE, new ActionMessage( key, value ) );
    }

    /**
     * Saves the Messages in given request for <struts:messages>tag.
     * 
     * @param request the request to save errors.
     */
    protected void saveMessages(HttpServletRequest request) {
        super.saveMessages( request, myMessages );
    }

    /**
     * Specify if an the Message set is empty.
     * 
     * @return boolean false is there are any Message registered, else true
     */
    protected boolean isMessagesEmpty() {
        return myMessages.isEmpty();
    }

    /////////////////////
    // Helper methods.
    /////////////////////

    /**
     * Create a new IntactUser and store it in the session. <br>
     * A datasource is also initialised inside the IntactUser.
     * 
     * @param session the user session where to store the IntactUser
     * @return a brand new IntactUser or null is something goes wrong.
     * 
     * <post>check is the errorsAction is empty, any errors are reported in
     * there. </post>
     */
    protected IntactUser createIntactUser(HttpSession session,
            HttpServletRequest aRequest) {
        IntactUser user = null;

        // Create an instance of IntactUser which we'll store in the Session
        try {
            String applicationPath = aRequest.getContextPath();

            user = new IntactUser( applicationPath );
            IntactContext.getCurrentInstance().getSession().setAttribute(Constants.USER_KEY, user);
        }
        catch ( IntactException ie ) {
            logger.error( "Could not initialize user's settings", ie );
            logger.error( "Could not initialize user's settings - ROOT CAUSE:",
                    ie.getRootCause() );
            String applicationPath = aRequest.getContextPath();
            addError( "error.datasource.notCreated", applicationPath );
            return null;
        }

        return user;
    }

    /**
     * Produces image accordingly to the interaction network stored in the user :
     * Any errors are stored in the <i>ActionErrors </i> object. A test need to
     * be done afterward to check if any errors have occured. <br>
     * nothing is done if there is no existing interaction network.
     * 
     * @param user where are saved produced data
     */
    protected void produceImage(IntactUserI user) {

        InteractionNetwork in = user.getInteractionNetwork();
        String applicationPath = user.getApplicationPath();

        if ( in == null )
            return;

        // TODO : If depth desacrease we don't have to access IntAct, we have to
        // reduce the current graph.

        String dataTlp = in.exportTlp();

        try {
            String[] errorMessages;
            errorMessages = in.importDataToImage( dataTlp );

            if ( ( null != errorMessages ) && ( errorMessages.length > 0 ) ) {
                for (int i = 0; i < errorMessages.length; i++) {
                    addError( "error.webService", errorMessages[i] );
                    logger.error( errorMessages[i] );
                }
                return;
            }
        }
        catch ( RemoteException e ) {
            addError( "error.webService", e.getMessage() );
            logger.error( e.getMessage(), e );
            return;
        }

        Chrono chrono = new Chrono();
        chrono.start();

        DrawGraph te = new DrawGraph( in, applicationPath, user.getMinePath() );
        te.draw();

        chrono.stop();
        String msg = "Time for rendering the interaction network " + chrono;
        logger.info( msg );

        ImageBean ib = te.getImageBean();

        if ( null == ib ) {
            addError( "error.ImageBean.build" );
            return;
        }

        // store the image data and the graph
        user.setImageBean( ib );
        user.setNodeCoordinates( te.getNodeCoordinates() );
    }

    /**
     * Update the interaction network according to the specified action type:
     * <blockquote>
     * <li><code>StrutsConstants.CREATE_INTERACTION_NETWORK</code>: create a
     * new interaction network</li>
     * <li><code>StrutsConstants.ADD_INTERACTION_NETWORK</code>: add a new
     * interaction network to the existing's one</li>
     * <li><code>StrutsConstants.UPDATE_INTERACTION_NETWORK</code>: rebuild
     * the current interaction network by taking into account any depth change
     * </li>
     * </blockquote> Any errors are stored in the <i>ActionErrors </i> object. A
     * test need to be done afterward to check if any errors have occured.
     * 
     * @param user where are saved produced data
     * @param action to perform
     * @see StrutsConstants
     * 
     * @throws MultipleResultException in case your query gives multiple results
     */
    public void updateInteractionNetwork(IntactUserI user, int action)
            throws MultipleResultException {

        InteractionNetwork in = user.getInteractionNetwork();
        String queryString = user.getQueryString();
        int depth = user.getCurrentDepth();
        GraphHelper gh = new GraphHelper( user );
        Collection interactors = null;
        Collection criterias = null;

        Chrono chrono = new Chrono();
        chrono.start();
        try {
            if ( action != StrutsConstants.UPDATE_INTERACTION_NETWORK ) {
                interactors = find( queryString, user, searchHelper );
                criterias = searchHelper.getSearchCritera();
                /**
                 * Check feasability number-of-interactor wise
                 */
                int maxInteractor = InteractionNetwork.getMaxCentralProtein();
                int interactorsFound = interactors.size();
                int currentCount = 0;
                if ( in != null )
                    currentCount = in.getCurrentCentralProteinCount();

                if ( ( interactorsFound + currentCount ) > maxInteractor ) {

                    logger.error( queryString
                            + " gave us too many results (max set to "
                            + maxInteractor + ")" );
                    throw new MultipleResultException();

                }
                else if ( interactorsFound == 0 ) {

                    logger.error( "nothing found for: " + queryString );

                    for (Iterator iterator = criterias.iterator(); iterator
                            .hasNext();) {
                        CriteriaBean criteria = (CriteriaBean) iterator.next();

                        if ( in == null )
                            addError( "error.protein.notFound", criteria
                                    .getQuery() );
                        else
                            addMessage( "warning.protein.notFound", criteria
                                    .getQuery() );
                    }

                    return; // stop there !
                }
            } // if

            switch ( action ) {
                case StrutsConstants.CREATE_INTERACTION_NETWORK:
                    in = null; // it should be null already but wipe the current
                    // network out if it does exist.
                    for (Iterator iterator = interactors.iterator(); iterator
                            .hasNext();) {
                        Interactor interactor = (Interactor) iterator.next();
                        in = gh.addInteractionNetwork( in, interactor, depth );
                    }

                    for (Iterator iterator = criterias.iterator(); iterator
                            .hasNext();) {
                        CriteriaBean criteria = (CriteriaBean) iterator.next();

                        if ( criteria.hasGivenResults() )
                            in.addCriteria( criteria );
                        else {
                            addMessage( "warning.protein.notFound", criteria
                                    .getQuery() );
                            addError( "error.protein.notFound", criteria
                                    .getQuery() );
                        }
                    }

                    // if no network built after processing all sub query,
                    // display any errors.
                    // Else any messages.
                    if ( in == null ) {
                        clearMessages(); // display only errors
                    }
                    else {
                        clearErrors(); // display only messages
                    }

                    user.setInteractionNetwork( in );
                    break;

                case StrutsConstants.ADD_INTERACTION_NETWORK:
                    for (Iterator iterator = interactors.iterator(); iterator
                            .hasNext();) {
                        Interactor interactor = (Interactor) iterator.next();
                        in = gh.addInteractionNetwork( in, interactor, depth );
                    }

                    for (Iterator iterator = criterias.iterator(); iterator
                            .hasNext();) {
                        CriteriaBean criteria = (CriteriaBean) iterator.next();
                        if ( criteria.hasGivenResults() ) {
                            in.addCriteria( criteria );
                        }
                        else {
                            addMessage( "warning.protein.notFound", criteria
                                    .getQuery() );
                        }
                    }
                    break;

                case StrutsConstants.UPDATE_INTERACTION_NETWORK:
                    criterias = in.getCriteria();
                    // get the central interactors of the current network
                    interactors = in.getCentralInteractors();
                    // if no interactors are given the graph was built by the
                    // mine database table -> so fetch the central proteins
                    // instead
                    if ( interactors == null ) {
                        interactors = in.getCentralProteins();
                    }
                    in = null;

                    for (Iterator iterator = interactors.iterator(); iterator
                            .hasNext();) {
                        Object interactor = iterator.next();

                        // if the current element is an instance of BasicGraphI
                        // the mine database table is used to create the new
                        // network
                        if ( GraphHelper.BUILT_WITH_MINE_TABLE ) {
                            try {
                                in = gh.addInteractionNetwork( in,
                                        (BasicGraphI) interactor, depth );
                            }
                            catch ( SQLException e1 ) {
                                addError(
                                        "error.interactionNetwork.notCreated",
                                        e1.getMessage() );
                                return;
                            }
                        }
                        // the conservative way is used to build the new network
                        else {
                            in = gh.addInteractionNetwork( in,
                                    (Interactor) interactor, depth );
                        }
                    }

                    for (Iterator iterator = criterias.iterator(); iterator
                            .hasNext();) {
                        CriteriaBean criteria = (CriteriaBean) iterator.next();
                        in.addCriteria( criteria );
                    }
                    break;

                default:
                    logger
                            .error( "That option is not supported by that method !" );
                    return; // other choice ? nothing to be done !
            }

            chrono.stop();

            String msg = null;
            if ( in == null ) {
                msg = new StringBuffer( 128 ).append(
                        "No interaction network retreived, took " ).append(
                        chrono ).toString();
            }
            else {
                msg = new StringBuffer( 128 ).append(
                        "Time for retreiving the interaction network ( " )
                        .append( in.sizeNodes() ).append( " proteins, " )
                        .append( in.sizeEdges() ).append( " edges) :" ).append(
                                chrono ).toString();
            }
            logger.info( msg );

            if ( in == null ) {
                // no protein found
                return;
            }

        }
        catch ( SearchException e ) {
            addError( "error.search.process", e.getMessage() );
            return;

        }
        catch ( IntactException e ) {
            addError( "error.interactionNetwork.notCreated", e.getMessage() );
            return;

        }
        catch ( MultipleResultException mre ) {
            throw mre;
        }

        if ( 0 == in.sizeNodes() ) {
            addError( "error.interactionNetwork.noProteinFound" );
            return;
        }

        user.setInteractionNetwork( in );
    }

    /**
     * Search in the database Interactor related to the query string.
     * 
     * @param queryString the criteria to search for.
     * @return a collection of interactor or empty if none are found.
     * @throws IntactException in case of search error.
     */
    private Collection find(String queryString, IntactUserI user,
            SearchHelperI searchHelper) throws IntactException {

        Collection results;

        //first try search string 'as is' - some DBs allow mixed case....
        results = searchHelper.doLookup( SearchClass.INTERACTOR, queryString, user );

        if ( results.isEmpty() ) {
            //now try all lower case....
            String lowerCaseValue = queryString.toLowerCase();
            results = searchHelper
                    .doLookup( SearchClass.INTERACTOR, lowerCaseValue, user );
            if ( results.isEmpty() ) {
                //finished all current options, and still nothing - return a
                // failure
                logger
                        .info( "No matches were found for the specified search criteria" );
            }
        }

        return results;
    }
}