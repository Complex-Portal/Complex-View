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
import uk.ac.ebi.intact.application.hierarchview.exception.SessionExpiredException;
import uk.ac.ebi.intact.application.hierarchview.highlightment.source.HighlightmentSource;
import uk.ac.ebi.intact.application.hierarchview.struts.StrutsConstants;
import uk.ac.ebi.intact.application.hierarchview.struts.framework.IntactBaseAction;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collection;

/**
 * Implementation of <strong>Action</strong> that validates an highlightment submisson.
 *
 * @author Samuel Kerrien
 * @version $Id$
 */

public final class SourceAction extends IntactBaseAction {

    private static final Log logger = LogFactory.getLog(SourceAction.class);

    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an <code>ActionForward</code> instance describing where and how
     * control should be forwarded, or <code>null</code> if the response has
     * already been completed.
     *
     * @param mapping The ActionMapping used to select this instance
     * @param form The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     *
     * @exception java.io.IOException if an input/output error occurs
     * @exception javax.servlet.ServletException if a servlet exception occurs
     */
    public ActionForward execute (ActionMapping mapping,
                                  ActionForm form,
                                  HttpServletRequest request,
                                  HttpServletResponse response)
            throws IOException, ServletException {

        // Clear any previous errors.
        clearErrors();
        HttpSession session = null;
        IntactUserI user = null;


        try {
            // get the current session
            session = getSession(request);

            // retreive user fron the session
            user = getIntactUser(session);
        } catch (SessionExpiredException see) {
            String applicationPath = request.getContextPath();
            if (applicationPath == null) applicationPath = "";
            logger.error("Session expired, gives a link to "+ applicationPath);
            addError ("error.session.expired", applicationPath);
            saveErrors(request);
            return (mapping.findForward("error"));
        }

        String someKeys         = request.getParameter(StrutsConstants.ATTRIBUTE_KEYS_LIST);
        String clickedKeys      = request.getParameter(StrutsConstants.ATTRIBUTE_KEY_CLICKED);
        String keyType          = request.getParameter(StrutsConstants.ATTRIBUTE_KEY_TYPE);
        String selectedTabIndex = request.getParameter( "selected" );

        if ((null == clickedKeys) || (clickedKeys.trim().length() == 0)) {
            addError ("error.keys.required");
            saveErrors(request);
            return (mapping.findForward("error"));
        }

        // get the class method name to create an instance
        String source = user.getMethodClass();

        HighlightmentSource highlightmentSource = HighlightmentSource.getHighlightmentSource(source);
        Collection keys = highlightmentSource.parseKeys (someKeys);

        user.setKeys(keys);
        user.setSelectedKey(clickedKeys);
        user.setSelectedKeyType(keyType);

        // Print debug in the log file
        logger.info ("SourceAction: selectedKey=" + clickedKeys + " | keys=" + someKeys +
                     " | keys type=" + keyType + " | selectedTabIndex=" + selectedTabIndex + "\nlogged on in session " + session.getId());

        // Remove the obsolete form bean
        if (mapping.getAttribute() != null) {
            if ("request".equals(mapping.getScope()))
                request.removeAttribute(mapping.getAttribute());
            else
                session.removeAttribute(mapping.getAttribute());
        }

        if( selectedTabIndex != null ) {
            session.setAttribute( "selectedTabIndex", selectedTabIndex );
        }

        // Forward control to the specified success URI
        return (mapping.findForward("success"));
    }
}
