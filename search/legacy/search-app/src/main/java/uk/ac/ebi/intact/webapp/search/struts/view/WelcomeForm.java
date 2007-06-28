/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.  
All rights reserved. Please see the file LICENSE 
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.webapp.search.struts.view;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Dummy form bean for the welcome page. It allows us to use
 * the struts Action mechanism to set up the necessary DB classes
 * for use in a user search session.
 *
 * @author Chris Lewington
 * @version $Id$
 */
public class WelcomeForm extends ActionForm {


    /**
     * Validate the properties that have been set from the HTTP request.
     *
     * @param mapping the mapping used to select this instance
     * @param request the servlet request we are processing
     * @return <tt>ActionErrors</tt> object that contains validation errors. If
     * no errors are found, <tt>null</tt> or an empty <tt>ActionErrors</tt>
     * object is returned.
     */
    @Override
    public ActionErrors validate(ActionMapping mapping,
                                 HttpServletRequest request) {

        return null;
    }

    /**
     * Reset all properties to their default values.
     *
     * @param mapping the mapping used to select this instance
     * @param request the servlet request we are processing
     */
    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {

    }
}
