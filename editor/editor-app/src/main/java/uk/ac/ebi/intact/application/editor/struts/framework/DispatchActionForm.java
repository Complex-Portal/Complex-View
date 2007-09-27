/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.framework;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.ValidatorForm;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * The super class for all the dispatch action forms.
 *
 * Reference:  receipe 5.6 (Tailor a form for a DispatchAction) in Struts
 * Recipes by George Franciscus and Danilo Gurovich (Manning publication).
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class DispatchActionForm extends ValidatorForm {
    protected static Log log = LogFactory.getLog(DispatchActionForm.class);

    /**
     * The prefix for the validator method.
     */
    private static final String ourMethodPrefix = "validate";

    /**
     * The label of the last dispatch action.
     */
    private String myDispatch;

    /**
     * The index of the current dispatch (which button was pressed).
     */
    private int myDispatchIndex;

    /**
     * Maps: method name -> method
     */
    private Map myNameToMethod = new HashMap();

    public void setDispatch(String dispatch) {
        myDispatch = dispatch;
    }

    public String getDispatch() {
        return myDispatch;
    }

    public int getDispatchIndex() {
        return myDispatchIndex;
    }

    public void setDispatch(int index, String value) {
        myDispatchIndex = index;
        setDispatch(value);
    }

    public void resetDispatch() {
        myDispatch = "";
    }

    /**
     * Validate the properties that have been set from the HTTP request.
     *
     * @param mapping the mapping used to select this instance
     * @param request the servlet request we are processing
     * @return <tt>ActionMessages</tt> object that contains validation errors. If
     * no errors are found, <tt>null</tt> or an empty <tt>ActionMessages</tt>
     * object is returned.
     */
    public ActionErrors validate(ActionMapping mapping,
                                 HttpServletRequest request) {
        ActionErrors errors = super.validate(mapping, request);

        // Only proceed if super method does not find any errors.
        if ((errors != null) && !errors.isEmpty()) {
            return errors;
        }
        // Get the name associated with the dispatch.
        String name = getDispatch();
        if (name == null) {
            // There is no name associated with the dispatch parameter
            return null;
        }

        // Construct the method name (e.g., validateDelete).
        String methodName = ourMethodPrefix + name.substring(0, 1).toUpperCase()
                + name.substring(1);
        // Replace any spaces (some button labels have spaces)
        methodName = methodName.replaceAll("\\s", "");
        // Check for Save & Continue button
        if (methodName.indexOf('&') != -1) {
            methodName = methodName.replaceAll("&", "And");
        }
        // Get the cached method
        Method method = (Method) myNameToMethod.get(methodName);

        if (method == null) {
            Class[] types = {};
            try {
                method = getClass().getMethod(methodName, types);
                myNameToMethod.put(methodName, method);
            }
            catch (NoSuchMethodException e) {
                // No validation method found. This could that we don't need validation
                return null;
            }
        }
        return dispatch(method);
    }

    private ActionErrors dispatch(Method method) {
        ActionErrors errors = null;
        Object[] args = {};
        try {
            errors = (ActionErrors) method.invoke(this, args);
        }
        catch (IllegalAccessException e) {
            // Swallow it as we are returning a null object.
        }
        catch (InvocationTargetException e) {
            // Swallow it as we are returning a null object.
        }
        return errors;
    }
}
