/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.webapp.search.struts.controller;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import uk.ac.ebi.intact.searchengine.SearchHelper;
import uk.ac.ebi.intact.searchengine.SearchHelperI;
import uk.ac.ebi.intact.webapp.search.struts.framework.IntactBaseAction;
import uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Performs the required initialisations for a user search session when they have been through the welcome page.
 *
 * @author Chris Lewington
 * @version $Id$
 */
public class WelcomeAction extends IntactBaseAction {

    /**
     * Process the specified HTTP request, and create the corresponding HTTP response (or forward to another web
     * component that will create it). Return an <code>ActionForward</code> instance describing where and how control
     * should be forwarded, or <code>null</code> if the response has already been completed.
     *
     * @param mapping  The ActionMapping used to select this instance
     * @param form     The optional ActionForm bean for this request (if any)
     * @param request  The HTTP request we are processing
     * @param response The HTTP response we are creating
     *
     * @return an ActionForward object
     *
     * @throws IOException      if an input/output error occurs
     * @throws ServletException if a servlet exception occurs
     */
    public ActionForward execute( ActionMapping mapping, ActionForm form,
                                  HttpServletRequest request,
                                  HttpServletResponse response )
            throws IOException, ServletException {

        SearchHelperI helper = new SearchHelper();
        //all we need to do here is set up a valid user if possible
        if ( super.setupUser( request ) == null ) {
            // not possible to set up an user, forward to errorpage
            return mapping.findForward( SearchConstants.FORWARD_FAILURE );

        } else if ( !helper.connected() ) {
            // the database is not connected forward to errorpage
            return mapping.findForward( SearchConstants.FORWARD_NO_RESOURCE );

        } else {
            // everything is fine, forward to initial page
            return mapping.findForward( SearchConstants.FORWARD_SUCCESS );
        }
    }
}