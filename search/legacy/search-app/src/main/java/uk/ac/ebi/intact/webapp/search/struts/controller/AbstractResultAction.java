/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.webapp.search.struts.controller;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import uk.ac.ebi.intact.webapp.search.business.IntactUserIF;
import uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Perform the common behaviour of the result Action.
 * <p>
 * to integrate the new 'simple' view into here, but currently it needs to generate a LIST of viewbeans, not a SINGLE
 * one (but maybe see partners view as this is similar but is included here in the 'hacked' code below!)
 *
 * @author Michael Kleen
 * @version $Id$
 */
public abstract class AbstractResultAction extends IntactSearchAction
{

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

        // first set the request

        super.setRequest( request );

        // Session to access various session objects. This will create
        //a new session if one does not exist.
        HttpSession session = super.getSession( request );

        // Handle to the Intact User.
        IntactUserIF user = super.getIntactUser( session );
        if ( user == null ) {
            //just set up a new user for the session - if it fails, need to give up!
            user = super.setupUser( request );
            if ( user == null ) {
                return mapping.findForward( SearchConstants.FORWARD_FAILURE );
            }
        }

        //use the procesRequest method instead....
        return mapping.findForward( processResults( request ) );

    }

    /**
     * Process the request more effectively.
     * <p>
     * It avoids making any assumptions about the beans or
     * the size of search result list and keep all of the processing in a single place for each Action type.
     *
     * @param request  The request to be processed
     * @param helpLink The contextual help link
     *
     * @return String the forward code for the parent execute method to return.
     */
    protected abstract String processResults( HttpServletRequest request );
}