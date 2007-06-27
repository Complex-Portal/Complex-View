/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.hierarchview.struts.view;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.struts.framework.IntactBaseForm;
import uk.ac.ebi.intact.context.IntactContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Form bean for the main form of the view.jsp page.  
 * This form has the following fields, with default values in square brackets:
 * <ul>
 * <li><b>queryString</b> - Entered queryString value
 * <li><b>method</b> - Selected method value
 * </ul>
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */

public final class SearchForm extends IntactBaseForm {

    public static Logger logger = Logger.getLogger (Constants.LOGGER_NAME);

    /**
     */
    private static final int SEARCH = 0;

    /**
     */
    private static final int ADD = 1;

    // --------------------------------------------------- Instance Variables

    // the queryString of the protein to search.
    private String queryString = null;

    /**
     * The highlight method
     * That attribute can be null because in case only one method is available,
     * the form dont show the choice to make easier the user interface.
     */
    private String method = null;

    /**
     * Saves the user action.
     */
    private int myAction;

    private String actionName;

    /**
     * Sets the action.
     * @param action the action for the form. If this contains the word
     * 'AC' then the search is by AC otherwise the search is by label.
     */
    public void setAction(String action) {
        actionName = action;

        if (action.equals("Add")) {
            myAction = ADD;
        }
        else if (action.equals("Search")) {
            myAction = SEARCH;
        }
    }


    public String getAction () {
        return actionName;
    }


    public String getQueryString() {
        return (this.queryString);
    }


    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }


    public String getMethod() {
        return (this.method);
    }


    public void setMethod (String method) {
        this.method = method;
    }


    /**
     * True if Search button is pressed.
     */
    public boolean searchSelected() {
        return myAction == SEARCH;
    }


    /**
     * True if Add button is pressed.
     */
    public boolean addSelected() {
        return myAction == ADD;
    }


    /**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        this.queryString = null;
        this.method = null;
    } // reset


    /**
     * Validate the properties that have been set from this HTTP request,
     * and return an <code>ActionErrors</code> object that encapsulates any
     * validation errors that have been found.  If no errors are found, return
     * <code>null</code> or an <code>ActionErrors</code> object with no
     * recorded error messages.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public ActionErrors validate(ActionMapping mapping,
                                 HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        boolean networkExists = false;

        // in the case the network already exists and the user ADD with an
        // EMPTY searchString ... we display a warning MESSAGE instead of ERROR
        if ((addSelected()) && (null != session)) {
            IntactUserI user = (IntactUserI) IntactContext.getCurrentInstance().getSession().getAttribute(Constants.USER_KEY);
            if (user != null) {
                if (null != user.getInteractionNetwork()) {
                    networkExists = true;
                }
            }
        }

        if ((queryString == null) || (queryString.trim().length() == 0)) {
            if (networkExists)
                 addMessage ("error.queryString.required");
            else addError ("error.queryString.required");
        }

        if ((method == null) || (method.trim().length() == 0)) {
            if (networkExists)
                 addMessage ("error.method.required");
            else addError ("error.method.required");
        }

        if (false == isMessagesEmpty()) {
            /* save messages in the context, that feature is not included in Struts 1.1
             * currently it's only possible to manage ActionErrors when validating a form.
             */
            saveMessages (request);
        }

        return getErrors();
    }


    public String toString () {
        StringBuffer sb = new StringBuffer("SearchForm[queryString=");
        sb.append(queryString);
        if (method != null) {
            sb.append(", method=");
            sb.append(method);
        }
        sb.append("]");
        return (sb.toString());
    }
}
