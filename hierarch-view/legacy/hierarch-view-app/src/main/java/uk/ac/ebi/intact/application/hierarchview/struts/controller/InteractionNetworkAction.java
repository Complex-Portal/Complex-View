/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.hierarchview.struts.controller;


import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.exception.MultipleResultException;
import uk.ac.ebi.intact.application.hierarchview.exception.SessionExpiredException;
import uk.ac.ebi.intact.application.hierarchview.struts.StrutsConstants;
import uk.ac.ebi.intact.application.hierarchview.struts.framework.IntactBaseAction;
import uk.ac.ebi.intact.application.hierarchview.struts.view.InteractionNetworkForm;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Implementation of <strong>Action</strong> that perform hndling of the current interaction network.
 *
 * @author Samuel Kerrien
 * @version $Id$
 */

public final class InteractionNetworkAction extends IntactBaseAction {

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

        InteractionNetworkForm myForm = (InteractionNetworkForm) form;
        
        if (myForm.expandSelected()) {
            user.increaseDepth();
        } else if (myForm.contractSelected()) {
            user.desacreaseDepth();
        } else {
            addError ("error.graph.command.notRecognized", myForm.getAction());
            saveErrors(request);
            return (mapping.findForward("error"));
        }

        try {
            updateInteractionNetwork (user, StrutsConstants.UPDATE_INTERACTION_NETWORK);
            produceImage (user);
        } catch (MultipleResultException e) {
            return (mapping.findForward("displayWithSearch"));
        }

        // Forward control to the specified success URI
        return (mapping.findForward("success"));
    }
}
