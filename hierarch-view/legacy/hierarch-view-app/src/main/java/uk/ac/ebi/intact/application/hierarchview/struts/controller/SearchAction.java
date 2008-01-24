/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.hierarchview.struts.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.graph.Network;
import uk.ac.ebi.intact.application.hierarchview.exception.SessionExpiredException;
import uk.ac.ebi.intact.application.hierarchview.struts.StrutsConstants;
import uk.ac.ebi.intact.application.hierarchview.struts.framework.IntactBaseAction;
import uk.ac.ebi.intact.application.hierarchview.struts.view.SearchForm;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Properties;

/**
 * Implementation of <strong>Action</strong> that validates a visualize submisson.
 *
 * @author Samuel Kerrien
 * @version $Id$
 */

public final class SearchAction extends IntactBaseAction {

    private static final Log logger = LogFactory.getLog( SearchAction.class );

    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an <code>ActionForward</code> instance describing where and how
     * control should be forwarded, or <code>null</code> if the response has
     * already been completed.
     *
     * @param mapping  The ActionMapping used to select this instance
     * @param form     The optional ActionForm bean for this request (if any)
     * @param request  The HTTP request we are processing
     * @param response The HTTP response we are creating
     * @throws IOException      if an input/output error occurs
     * @throws ServletException if a servlet exception occurs
     */
    public ActionForward execute( ActionMapping mapping,
                                  ActionForm form,
                                  HttpServletRequest request,
                                  HttpServletResponse response )
            throws IOException, ServletException, SessionExpiredException {

        // Clear any previous errors.
        clearErrors();
        HttpSession session = null;
        IntactUserI user = null;

        // get the current session
        session = getSession( request );

        // retreive user fron the session
        user = getIntactUser( session );

        String queryString = null;
        String methodLabel = null;
        String methodClass = null;
        String behaviourDefault = null;
        SearchForm searchForm = ( SearchForm ) form;

        if ( null != form ) {
            // read form values from the bean
            queryString = searchForm.getQueryString().trim();
            methodLabel = searchForm.getMethod();

            // read the highlighting.proterties file
            Properties properties = IntactUserI.HIGHLIGHTING_PROPERTIES;

            if ( null != properties ) {
                methodClass = properties.getProperty( "highlightment.source.node." + methodLabel + ".class" );
                if ( methodClass == null ) {
                    methodClass = properties.getProperty( "highlightment.source.edge." + methodLabel + ".class" );
                }
                behaviourDefault = properties.getProperty( "highlighting.behaviour.default.class" );
            }
        }

        // store the bean (by taking care of the scope)

        if ( "request".equals( mapping.getScope() ) ) {
            request.setAttribute( mapping.getAttribute(), form );
        } else {
            session.setAttribute( mapping.getAttribute(), form );
        }

        if ( !isErrorsEmpty() ) {
            // Report any errors we have discovered back to the original form
            saveErrors( request );
            return ( mapping.findForward( "error" ) );

        } else {

            if ( searchForm.searchSelected() ) {
                user.init();
                // Save user's data
                Network network = user.getInteractionNetwork();
                if ( network != null ) {
                    network.setDepthToDefault();
                }
                user.setMethodLabel( methodLabel );
                user.setMethodClass( methodClass );
                user.setBehaviour( behaviourDefault );
            }

            user.setQueryString( queryString );
            user.setMinePath( null );

            // Creation of the graph and the image

            int action = StrutsConstants.CREATE_INTERACTION_NETWORK;
            if ( searchForm.addSelected() ) {
                action = StrutsConstants.ADD_INTERACTION_NETWORK;
            }

            updateInteractionNetwork( user, action );
            produceImage( user );

            if ( !isErrorsEmpty() ) {
                // Report any errors we have discovered during the interaction network producing
                saveErrors( request );
                return ( mapping.findForward( "error" ) );
            }

            if ( !isMessagesEmpty() ) {
                // Report any messages we have discovered
                saveMessages( request );
            }
        }

        logger.info( "SearchAction: query=" + queryString +
                     " methodLabel=" + methodLabel +
                     " methodClass=" + methodClass );

        // Forward control to the specified success URI
        return ( mapping.findForward( "success" ) );
    }
}




