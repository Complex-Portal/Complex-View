/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.predict.struts.action;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.predict.business.PredictUser;
import uk.ac.ebi.intact.application.predict.struts.framework.AbstractPredictAction;
import uk.ac.ebi.intact.application.predict.struts.framework.PredictConstants;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.persistence.DataSourceException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Performs the required initialisations for a user search session when
 * they have been through the welcome page.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class WelcomeAction extends AbstractPredictAction {

    private static final Log log = LogFactory.getLog(WelcomeAction.class);

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
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws Exception {
        // Create a new session if it doesn't exist.
        HttpSession session = request.getSession(true);

        // Try to retriev an existing PredictUser from the session.
        PredictUser user = (PredictUser) session.getAttribute(
                PredictConstants.USER);

        // Create a new user if it doesn't exist in the session.
        if (user == null) {
            // Save the context to avoid repeat calls.
            try {
                user = PredictUser.create();
                log.info("Predict user created");
            }
            catch (DataSourceException dse) {
                // Unable to get a data source...can't proceed
                dse.printStackTrace();
            }
            catch (IntactException ie) {
                // Can't create the helper.. can't proceed
                ie.printStackTrace();
            }
            session.setAttribute(PredictConstants.USER, user);
        }
        else {
            log.debug("Using an existing session, user");
        }
        return mapping.findForward(SUCCESS);
    }
}
