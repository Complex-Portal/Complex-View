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
import uk.ac.ebi.intact.application.hierarchview.struts.framework.IntactBaseAction;
import uk.ac.ebi.intact.application.hierarchview.struts.view.HighlightmentForm;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Implementation of <strong>Action</strong> that validates an highlightment submisson.
 *
 * @author Samuel Kerrien
 * @version $Id$
 */

public final class HighlightmentAction extends IntactBaseAction {

    private static final Log logger = LogFactory.getLog(HighlightmentAction.class);

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
            throws IOException, ServletException, SessionExpiredException {

        // Clear any previous errors.
        clearErrors();

        // get the current session
        HttpSession session = getSession(request);

        // retreive user fron the session
        IntactUserI user = getIntactUser(session);

        String behaviour = null;

        if (null != form) {
            behaviour = ((HighlightmentForm) form).getBehaviour ();

            // get the class method name to create an instance
            String source = user.getMethodClass();

            // save options (given in this request) of the source in the user's session
            HighlightmentSource highlightmentSource = HighlightmentSource.getHighlightmentSource(source);
            if (null != highlightmentSource) {
                highlightmentSource.saveOptions (request, session);
            } else {
                addError ("error.HighlightmentSource.unknown", source);
            }
        }

        // Report any errors we have discovered back to the original form
        if (false == isErrorsEmpty()) {
            saveErrors(request);
            return (mapping.findForward("error"));
        }

        if (false == isMessagesEmpty()) {
            // Report any messages we have discovered
            saveMessages(request);
        }

        // Save our data in the session
        user.setBehaviour (behaviour);

        // Print debug in the log file
        logger.info ("HighlightmentAction: behaviour=" + behaviour +
                     "\nlogged on in session " + session.getId());

        // Remove the obsolete form bean
        if (mapping.getAttribute() != null) {
            if ("request".equals(mapping.getScope()))
                request.removeAttribute(mapping.getAttribute());
            else
                session.removeAttribute(mapping.getAttribute());
        }

        // Forward control to the specified success URI
        return (mapping.findForward("success"));
    }
}
