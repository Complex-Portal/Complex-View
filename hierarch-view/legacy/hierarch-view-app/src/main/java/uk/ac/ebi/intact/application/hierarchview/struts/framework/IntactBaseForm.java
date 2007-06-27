/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.hierarchview.struts.framework;

import org.apache.log4j.Logger;
import org.apache.struts.action.*;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;

import javax.servlet.http.HttpServletRequest;

/**
 * Super class for all hierarchview related form classes.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */

public class IntactBaseForm extends ActionForm {

    public static Logger logger = Logger.getLogger (Constants.LOGGER_NAME);


    /** The global Intact error key. */
    public static final String INTACT_ERROR = "IntactError";

    /** Error container */
    private ActionErrors myErrors = new ActionErrors();


    /** The global Intact message key. */
    public static final String INTACT_MESSAGE = "IntactMessage";

    /** Message container */
    private ActionMessages myMessages = new ActionMessages();


    /**
     * Error managment
     */

    /**
     * return the error set
     * @return the error set
     */
    protected ActionErrors getErrors () {
        return myErrors;
    }

    /**
     * Adds an error with given key.
     *
     * @param key the error key. This value is looked up in the
     * IntactResources.properties bundle.
     */
    protected void addError(String key) {
        myErrors.add(INTACT_ERROR, new ActionError(key));
    }

    /**
     * Adds an error with given key and value.
     *
     * @param key the error key. This value is looked up in the
     * IntactResources.properties bundle.
     * @param value the value to substitute for the first place holder in the
     * IntactResources.properties bundle.
     */
    protected void addError(String key, String value) {
        myErrors.add( INTACT_ERROR, new ActionError( key, value ) );
    }

    /**
     * Specify if an the error set is empty.
     *
     * @return boolean false is there are any error registered, else true
     */
    protected boolean isErrorsEmpty () {
        return myErrors.isEmpty();
    }


    /**
     * Message managment
     */

    /**
     * return the error set
     * @return the error set
     */
    protected ActionMessages getMessages () {
        return myMessages;
    }

    /**
     * Adds an error with given key.
     *
     * @param key the error key. This value is looked up in the
     * IntactResources.properties bundle.
     */
    protected void addMessage (String key) {
        myMessages.add( INTACT_MESSAGE, new ActionMessage( key ) );
    }

    /**
     * Adds an error with given key and value.
     *
     * @param key the error key. This value is looked up in the
     * IntactResources.properties bundle.
     * @param value the value to substitute for the first place holder in the
     * IntactResources.properties bundle.
     */
    protected void addMessage (String key, String value) {
        myMessages.add( INTACT_MESSAGE, new ActionMessage( key, value ) );
    }

    /**
     * Specify if an the error set is empty.
     *
     * @return boolean false is there are any error registered, else true
     */
    protected boolean isMessagesEmpty () {
        return myMessages.isEmpty();
    }

    /**
     * Saves the Messages in given request for <struts:messages> tag.
     *
     * @param request the request to save errors.
     */
    protected void saveMessages(HttpServletRequest request) {
        request.setAttribute( org.apache.struts.action.ActionMessages.GLOBAL_MESSAGE, getMessages() );
    }
}
