/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
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
import uk.ac.ebi.intact.application.hierarchview.struts.view.utils.LabelValueBean;
import uk.ac.ebi.intact.application.hierarchview.struts.view.utils.OptionGenerator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Properties;

/**
 * Action allowing to display an interaction network. It integrates the
 * environment initialisation so that action can be called eventually by an
 * external application. URL : <action>?AC= <YOUR_AC>&method=
 * <YOUR_METHOD>&depth= <YOUR_DEPTH>
 *
 * @author Samuel Kerrien
 * @version $Id$
 */

public final class DisplayAction extends IntactBaseAction {

    private static final Log logger = LogFactory.getLog( DisplayAction.class );

    // --------------------------------------------------------- Public Methods

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
    public ActionForward execute( ActionMapping mapping, ActionForm form,
                                  HttpServletRequest request, HttpServletResponse response )
            throws IOException, ServletException, SessionExpiredException {

        // Clear any previous errors.
        clearErrors();

        // get the current session
        HttpSession session = getNewSession( request );
        IntactUserI user = null;

        if ( !intactUserExists( session ) ) {
            /*
             * Happens in the case hierarchview is called by an external
             * applciation in order to perform a search. e.g. by using
             * http://server/hierarchview/display.do?AC=EBI-foo
             */
            user = createIntactUser( session, request );

            if ( !isErrorsEmpty() ) {
                // Report any errors we have discovered back to the original form
                saveErrors( request );
                return ( mapping.findForward( "error" ) );
            }
        } else {
            // no need to create a new one
            user = getIntactUser( session );
        }
        String AC = request.getParameter( "AC" );
        String methodLabel = request.getParameter( "method" );
        String depth = request.getParameter( "depth" );

        String methodClass = null;
        String behaviourDefault = null;

        if ( ( null == AC ) || ( AC.trim().length() == 0 ) ) {
            addError( "error.queryString.required" );
        }

        LabelValueBean lvb = null;

        if ( methodLabel == null ) {
            // in case the method is not provided, get the default one.
            lvb = OptionGenerator.getDefaultSource();
            methodLabel = lvb.getLabel();
            methodClass = lvb.getValue();
        } else {
            // get the associated name
            lvb = OptionGenerator.getSource( methodLabel );
            if ( lvb == null ) {
                // That source was not allowed or doesn't exists
                addError( "error.source.unusable", methodLabel );
            }
        }

        // read the highlighting.proterties file
        Properties properties = IntactUserI.HIGHLIGHTING_PROPERTIES;

        if ( null != properties ) {
            methodClass = properties.getProperty( "highlightment.source.node." + methodLabel + ".class" );
            behaviourDefault = properties.getProperty( "highlighting.behaviour.default.class" );
        }

        if ( !isErrorsEmpty() ) {
            // Report any errors we have discovered back to the original form
            saveErrors( request );
            return ( mapping.findForward( "error" ) );
        } else {
            // set default values
            user.init();

            String host = request.getParameter( "host" );
            logger.info( "THE HOST GIVEN BY JAVASCRIPT: " + host );

            String protocol = request.getParameter( "protocol" );
            logger.info( "THE PROTOCOL GIVEN BY JAVASCRIPT: " + protocol );

            session.setAttribute( StrutsConstants.HOST, host );
            session.setAttribute( StrutsConstants.PROTOCOL, protocol );

            String network = request.getParameter( "network" );
            String singletons = request.getParameter( "singletons" );
            // the information about MiNe are stored in the session,
            // although they should be stored in the request. But every attempt
            // to put it into the request failed because the request got lost
            // and at the result page no information could be found about these
            // parameters

            // Save user's data
            user.setQueryString( AC );
            // if the parameter 'network' is given its stored in the session
            if ( network != null && !"null".equals( network ) ) {
                session.setAttribute( "network", network );
                // the minepath is stored in the user to enable the highlighting
                // of the path
                user.setMinePath( AC );
            }
            //if the parameter 'singletons' is given its stored in the session
            if ( singletons != null && !"null".equals( singletons ) ) {
                session.setAttribute( "singletons", singletons );
            }
            Network in = user.getInteractionNetwork();

            if ( in != null ) {
                in.initEdges();
                in.initNodes();
                try {
                    int d = Integer.parseInt( depth );
                    in.setDepth( d );
                }
                catch ( NumberFormatException nfe ) {
                    in.setDepthToDefault();
                }
            }
            user.setMethodLabel( methodLabel );
            user.setMethodClass( methodClass );
            user.setBehaviour( behaviourDefault );

            // Creation of the graph and the image
            updateInteractionNetwork( user, StrutsConstants.CREATE_INTERACTION_NETWORK );
            produceImage( user );


            if ( !isErrorsEmpty() ) {
                // Report any errors we have discovered during the interaction
                // network producing
                saveErrors( request );
                return ( mapping.findForward( "error" ) );
            }

            if ( !isMessagesEmpty() ) {
                // Report any messages we have discovered
                saveMessages( request );
            }
        }

        logger.info( "DisplayAction: AC=" + AC + " depth=" + depth
                     + " methodLabel=" + methodLabel + " methodClass=" + methodClass );

        // Forward control to the specified success URI
        return ( mapping.findForward( "success" ) );

    } // execute
}

