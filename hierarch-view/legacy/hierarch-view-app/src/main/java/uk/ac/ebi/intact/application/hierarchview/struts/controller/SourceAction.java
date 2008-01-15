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
import uk.ac.ebi.intact.application.hierarchview.business.graph.HVNetworkBuilder;
import uk.ac.ebi.intact.application.hierarchview.exception.SessionExpiredException;
import uk.ac.ebi.intact.application.hierarchview.highlightment.source.edge.EdgeHighlightmentSource;
import uk.ac.ebi.intact.application.hierarchview.highlightment.source.node.NodeHighlightmentSource;
import uk.ac.ebi.intact.application.hierarchview.struts.StrutsConstants;
import uk.ac.ebi.intact.application.hierarchview.struts.framework.IntactBaseAction;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of <strong>Action</strong> that validates an highlightment submisson.
 *
 * @author Samuel Kerrien
 * @version $Id$
 */

public final class SourceAction extends IntactBaseAction {

    private static final Log logger = LogFactory.getLog( SourceAction.class );

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
     * @throws java.io.IOException            if an input/output error occurs
     * @throws javax.servlet.ServletException if a servlet exception occurs
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
        String someKeys = request.getParameter( StrutsConstants.ATTRIBUTE_KEYS_LIST );
        String clickedKey = request.getParameter( StrutsConstants.ATTRIBUTE_KEY_CLICKED );
        String keyType = request.getParameter( StrutsConstants.ATTRIBUTE_KEY_TYPE );
        String selectedTabIndex = request.getParameter( "selected" );

        if ( clickedKey != null && clickedKey.trim().length() == 0 ) {
            addError( "error.selectedKeys.required" );
            saveErrors( request );
            return ( mapping.findForward( "error" ) );
        }

        // get the class method name to create an instance
        Set<String> selectedKeys = null;

        if ( HVNetworkBuilder.NODE_SOURCES.contains( keyType ) ) {
            selectedKeys = NodeHighlightmentSource.parseKeys( someKeys );
        }
        if ( HVNetworkBuilder.EDGE_SOURCES.contains( keyType ) ) {
            selectedKeys = EdgeHighlightmentSource.parseKeys( someKeys );
        }

        if ( clickedKey != null ) {
            if ( selectedKeys == null ) {
                selectedKeys = new HashSet<String>();
                selectedKeys.add( clickedKey );
            } else {
                if ( selectedKeys.isEmpty() ) {
                    selectedKeys.add( clickedKey );
                } else {
                    // if sourceId already highlighted
                    if ( selectedKeys.contains( clickedKey ) ) {
                        // deselect sourceId
                        selectedKeys.remove( clickedKey );
                    } else {
                        // do nothing
                        selectedKeys.add( clickedKey );
                    }
                }
            }
        }

        user.setSelectedKeys( selectedKeys );
        user.setClickedKey( clickedKey );
        user.setSelectedKeyType( keyType );

        if ( logger.isDebugEnabled() ) {
            logger.debug( "SourceAction: clickedKey=" + clickedKey + " | selectedKeys=" + selectedKeys +
                          " | selectedKeys type=" + keyType + " | selectedTabIndex=" + selectedTabIndex +
                          "\nlogged on in session " + session.getId() );
        }

        // Remove the obsolete form bean
        if ( mapping.getAttribute() != null ) {
            if ( "request".equals( mapping.getScope() ) )
                request.removeAttribute( mapping.getAttribute() );
            else
                session.removeAttribute( mapping.getAttribute() );
        }

        if ( selectedTabIndex != null ) {
            session.setAttribute( "selectedTabIndex", selectedTabIndex );
        }

        // Forward control to the specified success URI
        return ( mapping.findForward( "success" ) );
    }
}
