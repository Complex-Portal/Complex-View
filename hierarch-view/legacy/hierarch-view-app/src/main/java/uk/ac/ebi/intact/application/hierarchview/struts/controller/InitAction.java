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
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.exception.SessionExpiredException;
import uk.ac.ebi.intact.application.hierarchview.struts.StrutsConstants;
import uk.ac.ebi.intact.application.hierarchview.struts.framework.IntactBaseAction;
import uk.ac.ebi.intact.application.hierarchview.struts.view.InitForm;
import uk.ac.ebi.intact.context.IntactContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;


/**
 * Implementation of <strong>Action</strong> that validates a centered submisson (from a link).
 *
 * @author Samuel Kerrien
 * @version $Id$
 */

public final class InitAction extends IntactBaseAction {

    private static final Log logger = LogFactory.getLog(InitAction.class);

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
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public ActionForward execute (ActionMapping mapping,
                                  ActionForm form,
                                  HttpServletRequest request,
                                  HttpServletResponse response)
            throws IOException, ServletException, SessionExpiredException {

        // Clear any previous errors.
        clearErrors();

        // get a session
        HttpSession session = getNewSession(request);
        IntactUserI user = (IntactUserI) IntactContext.getCurrentInstance().getSession().getAttribute (Constants.USER_KEY);

        if (null != user) {
            // user already exists
            logger.info ("User already exists ... don't create a new one !");
            // set user's data field (AC, ...) to default value
            user.init();
            return (mapping.findForward("success"));
        }

        // No user found, let's create one
        createIntactUser (session, request);

        if (false == isErrorsEmpty()) {
            // Report any errors we have discovered back to the original form
            saveErrors(request);
            return (mapping.findForward("error"));
        }

        logger.info ("User's setting ok.");

        String host = ((InitForm)form).getHost();
        logger.debug("THE HOST GIVEN BY JAVASCRIPT: " + host);

        String protocol = ((InitForm)form).getProtocol();
        logger.debug("THE PROTOCOL GIVEN BY JAVASCRIPT: " + protocol);

        session.setAttribute( StrutsConstants.HOST, host );
        session.setAttribute( StrutsConstants.PROTOCOL, protocol) ;

        // Forward control to the specified success URI
        return (mapping.findForward("success"));
    }
}