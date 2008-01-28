/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */

package uk.ac.ebi.intact.application.hierarchview.struts.framework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUser;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.graph.HVNetworkBuilder;
import uk.ac.ebi.intact.application.hierarchview.business.graph.Network;
import uk.ac.ebi.intact.application.hierarchview.business.image.DrawGraph;
import uk.ac.ebi.intact.application.hierarchview.business.image.ImageBean;
import uk.ac.ebi.intact.application.hierarchview.exception.*;
import uk.ac.ebi.intact.application.hierarchview.struts.StrutsConstants;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.util.Chrono;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.rmi.RemoteException;
import java.util.EmptyStackException;

/**
 * Super class for all hierarchview related action classes.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */

public abstract class IntactBaseAction extends Action {

    private static final Log logger = LogFactory.getLog( IntactBaseAction.class );

    /**
     * The global Intact error key.
     */
    public static final String INTACT_ERROR = "IntactError";

    /**
     * Error container
     */
    private ActionMessages myErrors = new ActionMessages();

    /**
     * The global Intact message key.
     */
    public static final String INTACT_MESSAGE = "IntactMessage";

    /**
     * Message container
     */
    private ActionMessages myMessages = new ActionMessages();

    /**
     * Says if an IntactUser object is currently available in the session.
     *
     * @param session the session to look into.
     * @return true is the IntactUser exists, else false.
     */
    protected boolean intactUserExists( HttpSession session ) {
        IntactUserI user = ( IntactUserI ) session.getAttribute( Constants.USER_KEY );
        return ( null != user );
    }

    /**
     * Returns the Intact User instance saved in a session.
     *
     * @param session the session to access the Intact user object.
     * @return an instance of <code>IntactUserImpl</code> stored in
     *         <code>session</code>
     */
    protected IntactUserI getIntactUser( HttpSession session ) throws SessionExpiredException {
        IntactUserI user = ( IntactUserI ) session.getAttribute( Constants.USER_KEY );

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
    protected HttpSession getSession( HttpServletRequest request ) throws SessionExpiredException {
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
    protected HttpSession getNewSession( HttpServletRequest request ) {
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
    protected void addError( String key ) {
        myErrors.add( INTACT_ERROR, new ActionMessage( key ) );
    }

    /**
     * Adds an error with given key and value.
     *
     * @param key   the error key. This value is looked up in the
     *              IntactResources.properties bundle.
     * @param value the value to substitute for the first place holder in the
     *              IntactResources.properties bundle.
     */
    protected void addError( String key, String value ) {
        myErrors.add( INTACT_ERROR, new ActionMessage( key, value ) );
    }

    /**
     * Saves the errors in given request for <struts:errors>tag.
     *
     * @param request the request to save errors.
     */
    protected void saveErrors( HttpServletRequest request ) {
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
    protected void addMessage( String key ) {
        myMessages.add( INTACT_MESSAGE, new ActionMessage( key ) );
    }

    /**
     * Adds an Message with given key and value.
     *
     * @param key   the Message key. This value is looked up in the
     *              Struts.properties bundle.
     * @param value the value to substitute for the first place holder in the
     *              Struts.properties bundle.
     */
    protected void addMessage( String key, String value ) {
        myMessages.add( INTACT_MESSAGE, new ActionMessage( key, value ) );
    }

    /**
     * Saves the Messages in given request for <struts:messages>tag.
     *
     * @param request the request to save errors.
     */
    protected void saveMessages( HttpServletRequest request ) {
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
     * <p/>
     * //@param session the user session where to store the IntactUser
     *
     * @return a brand new IntactUser or null is something goes wrong.
     *         <p/>
     *         <post>check is the errorsAction is empty, any errors are reported in
     *         there. </post>
     */
    protected IntactUser createIntactUser( HttpSession session, HttpServletRequest aRequest ) {
        IntactUser user;

        // Create an instance of IntactUser which we'll store in the Session
        try {
            String applicationPath = aRequest.getContextPath();

            user = IntactUser.createIntactUser( applicationPath );
            session.setAttribute( Constants.USER_KEY, user );
        }
        catch ( IntactException ie ) {
            logger.error( "Could not initialize user's settings", ie );
            logger.error( "Could not initialize user's settings - ROOT CAUSE:", ie.getRootCause() );
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
    protected void produceImage( IntactUserI user ) {

        Network in = user.getInteractionNetwork();
        String applicationPath = user.getApplicationPath();

        if ( in == null )
            return;

        String dataTlp = in.exportTlp();

        try {
            String[] errorMessages;
            errorMessages = in.importDataToImage( dataTlp );

            if ( ( null != errorMessages ) && ( errorMessages.length > 0 ) ) {
                for ( String errorMessage : errorMessages ) {
                    addError( "error.webService", errorMessage );
                    logger.error( errorMessage );
                }
                return;
            }
        } catch ( RemoteException e ) {
            addError( "error.webService", e.getMessage() );
            logger.error( e.getMessage(), e );
            return;
        }

        Chrono chrono = new Chrono();
        chrono.start();

        DrawGraph te = new DrawGraph( user, in, applicationPath, user.getMinePath() );
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
     * @param action to perform
     * @throws uk.ac.ebi.intact.application.hierarchview.exception.HierarchViewDataException
     *
     * @see uk.ac.ebi.intact.application.hierarchview.struts.StrutsConstants
     */
    public void updateInteractionNetwork( IntactUserI user, int action ) {

        HVNetworkBuilder builder = user.getHVNetworkBuilder();
        Network in = user.getInteractionNetwork();
        String queryString = formatQueryString( user.getQueryString() );
        Chrono chrono = new Chrono();
        chrono.start();
        user.clearErrorMessage();

        if ( action != StrutsConstants.UPDATE_INTERACTION_NETWORK ) {
            user.resetSourceURL();
        }
        try {
            switch ( action ) {
                case StrutsConstants.CREATE_INTERACTION_NETWORK:
                    try {
                        in = builder.buildBinaryGraphNetwork( queryString );

                        if ( logger.isDebugEnabled() ) {
                            logger.debug( "Creating a new Network with " + in.getBinaryInteraction().size() + " BinaryInteractions." );
                            logger.debug( "Number of Central Nodes is " + in.getCentralNodes().size() );
                        }
                    } catch ( ProteinNotFoundException e ) {
                        logger.error( "nothing found for: " + queryString );

                        if ( in == null )
                            addError( "error.protein.notFound", queryString );
                        else
                            addMessage( "warning.protein.notFound", queryString );

                        return; // stop there !
                    } catch ( MultipleResultException e ) {
                        logger.error( "to much hits for: " + queryString );

                        if ( in == null ) {
                            addError( "error.max.interactions.reached", Integer.toString( HVNetworkBuilder.getMaxInteractions() ) );
                        } else {
                            addMessage( "warning.max.interactions.reached", Integer.toString( HVNetworkBuilder.getMaxInteractions() ) );
                        }

                        user.setErrorMessage( "Sorry, too many interactions were found! Refine your query." );
                        return;
                    }

                    // if no network built after processing all sub query,
                    if ( in == null ) {
                        clearMessages(); // display only errors
                    } else {
                        clearErrors(); // display only messages
                    }

                    user.setInteractionNetwork( in );
                    user.pushNetwork( in );
                    break;

                case StrutsConstants.ADD_INTERACTION_NETWORK:

                    try {
                        int size_before = in.getBinaryInteraction().size();
                        in = builder.fusionBinaryGraphNetwork( in, queryString );
                        user.pushNetwork( in );
                        if ( logger.isDebugEnabled() ) {
                            logger.debug( "Adding a new Network with " + in.getBinaryInteraction().size() + " BinaryInteractions " +
                                          "to existing Network with " + size_before + " BinaryInteractions." );
                            logger.debug( "Number of Central Nodes is " + in.getCentralNodes().size() );
                        }
                    } catch ( ProteinNotFoundException e ) {
                        logger.error( "nothing found for: " + queryString );

                        if ( in == null ) {
                            addError( "error.protein.notFound", queryString );
                        } else {
                            addMessage( "warning.protein.notFound", queryString );
                        }


                        return; // stop there !

                    } catch ( MultipleResultException e ) {
                        logger.error( e.getMessage() );

                        if ( in == null ) {
                            addError( "error.max.interactions.reached", Integer.toString( HVNetworkBuilder.getMaxInteractions() ) );
                        } else {
                            addMessage( "warning.max.interactions.reached", Integer.toString( HVNetworkBuilder.getMaxInteractions() ) );
                        }
                        user.setErrorMessage( "Sorry, too many interactions were found! Refine your query." );
                        return; // stop there!
                    }

                    break;

                case StrutsConstants.UPDATE_INTERACTION_NETWORK:
                    try {
                        if ( user.getNetworkUpdateOption() == StrutsConstants.EXPAND_NETWORK ) {
                            in = builder.expandBinaryGraphNetwork( in );
                            user.pushNetwork( in );
                            in.increaseDepth();
                            if ( logger.isDebugEnabled() ) {
                                logger.debug( "Expand current Network with " + in.getBinaryInteraction().size() + " BinaryInteractions." );
                                logger.debug( "Number of Central Nodes is " + in.getCentralNodes().size() );
                            }
                        } else {
                            if ( user.getNetworkUpdateOption() == StrutsConstants.CONTRACT_NETWORK ) {
                                logger.info( "Current Depth: " + in.getCurrentDepth() );
                                if ( in.getCurrentDepth() > 1 ) {
                                    try {
                                        in = user.popNetwork();
                                    } catch ( EmptyStackException e){
                                        logger.warn("Could not decrease network");
                                    }
                                    if ( logger.isDebugEnabled() ) {
                                        logger.debug( "Decrease Network to " + in.getBinaryInteraction().size() + " BinaryInteractions." );
                                        logger.debug( "Number of Central Nodes is " + in.getCentralNodes().size() );
                                    }
                                } else {
                                    in = builder.buildBinaryGraphNetwork( queryString );
                                    in.setDepthToDefault();
                                }
                            } else {
                                logger.info( queryString );
                                in = builder.buildBinaryGraphNetwork( queryString );
                                if ( logger.isDebugEnabled() ) {
                                    logger.debug( "Update current Network with " + in.getBinaryInteraction().size() + " BinaryInteractions." );
                                    logger.debug( "Number of Central Nodes is " + in.getCentralNodes().size() );
                                }
                            }
                        }
                    } catch ( ProteinNotFoundException e ) {
                        if ( in == null ) {
                            addError( "error.protein.notFound", queryString );
                        } else {
                            addMessage( "warning.protein.notFound", queryString );
                        }
                        return;

                    } catch ( MultipleResultException e ) {
                        logger.error( e.getMessage() );
                        if ( in == null ) {
                            addError( "error.max.interactions.reached", Integer.toString( HVNetworkBuilder.getMaxInteractions() ) );
                        } else {
                            addMessage( "warning.max.interactions.reached", Integer.toString( HVNetworkBuilder.getMaxInteractions() ) );
                        }
                        user.setErrorMessage( "Sorry, too many interactions were found! Refine your query." );
                        return;
                    }

                    break;

                default:
                    logger.error( "That option is not supported by that method !" );
                    return; // other choice ? nothing to be done !


            }

        } catch ( HierarchViewDataException e ) {
            addError( "error.search.process", e.getMessage() );
            return;

        } catch ( NetworkBuildException e ) {
            addError( "error.interaction.network.not.created", e.getMessage() );
            return;

        }

        chrono.stop();

        String msg;
        if ( in == null ) {
            msg = new StringBuffer( 128 ).append( "No interaction network retreived, took " )
                    .append( chrono ).toString();
        } else {
            msg = new StringBuffer( 128 ).append( "Time for retreiving the interaction network ( " )
                    .append( in.getNodes().size() ).append( " proteins, " )
                    .append( in.getEdges().size() ).append( " edges) :" ).append( chrono ).toString();
        }
        logger.info( msg );

        if ( in == null ) {
            // no protein found
            return;
        }

        if ( 0 == in.getNodes().size() ) {
            addError( "error.interactionNetwork.noProteinFound" );
            return;
        }

        user.setInteractionNetwork( in );
    }

    /**
     * QueryString has to be formated, because if there are no space between InteractorNames
     * the WebService has problems
     *
     * @param queryString
     * @return a formated String
     */
    private String formatQueryString( String queryString ) {
        if ( queryString.contains( "," ) ) {
            StringBuffer buffer = new StringBuffer();
            for ( String query : queryString.split( "," ) ) {
                buffer.append( query );
                buffer.append( ", " );
            }
            queryString =  buffer.toString().substring( 0, queryString.length() - 1 );
        }

        return queryString;
    }
}