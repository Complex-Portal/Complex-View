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
import uk.ac.ebi.intact.application.hierarchview.exception.MultipleResultException;
import uk.ac.ebi.intact.application.hierarchview.exception.SessionExpiredException;
import uk.ac.ebi.intact.application.hierarchview.struts.StrutsConstants;
import uk.ac.ebi.intact.application.hierarchview.struts.framework.IntactBaseAction;

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

public final class ClickAction extends IntactBaseAction {

    private static final Log logger = LogFactory.getLog(ClickAction.class);

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

        // get the current session
        HttpSession session = getSession(request);

        // retreive user fron the session
        IntactUserI user = getIntactUser(session);

        String AC = null;

        // look in the request ...
        AC = request.getParameter ("AC");

        if ((null == AC) || (AC.trim().length() == 0)) {
            /* we display an error page is the network doesn't exists
             * else just a warning message.
             */
            String errorKey = "error.centeredAC.required"; // default is center
            if (user.clickBehaviourIsAdd()) {
               errorKey = "error.addAC.required"; // default is center
            }

            if (user.getInteractionNetwork() == null) {
                addError (errorKey);
                saveErrors (request);
                return (mapping.findForward("error"));
            } else {
                addMessage (errorKey);
                saveMessages (request);
                // no processing to do ... just redisplay the interaction network.
                return (mapping.findForward("success"));
            }
        }

        if ( user.clickBehaviourIsCenter() ) {
            // Save our data
            user.setInteractionNetwork( null );
            user.setQueryString( AC );
            user.setDepthToDefault();
            user.resetSourceURL();

            // Center the view
            // Creation of the graph and the image
            try {
                updateInteractionNetwork (user, StrutsConstants.CREATE_INTERACTION_NETWORK);
                produceImage (user);
            } catch (MultipleResultException e) {
                return (mapping.findForward("displayWithSearch"));
            }

        } else if ( user.clickBehaviourIsAdd() ) {
            // Add the network for which the central protein is the one the user clicked
            user.setQueryString (AC);

            // Creation of the graph and the image
            try {
                updateInteractionNetwork( user, StrutsConstants.ADD_INTERACTION_NETWORK );
                produceImage( user );
            } catch (MultipleResultException e) {
                // should not happen
                return (mapping.findForward("displayWithSearch"));
            }
        }


        if (false == isErrorsEmpty()) {
            // Report any errors we have discovered during the interaction network producing
            saveErrors(request);
            return (mapping.findForward("error"));
        }

        if (false == isMessagesEmpty()) {
            // Report any messages we have discovered
            saveMessages(request);
        }

        // Print debug in the log file
        logger.info ("ClickAction: AC=" + AC);


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
