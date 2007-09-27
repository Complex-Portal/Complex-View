/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.framework;

import org.apache.log4j.Logger;
import org.apache.struts.Globals;
import org.apache.struts.action.*;
import org.apache.struts.util.MessageResources;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.application.editor.business.EditorService;
import uk.ac.ebi.intact.application.editor.exception.SessionExpiredException;
import uk.ac.ebi.intact.application.editor.struts.framework.util.EditorConstants;
import uk.ac.ebi.intact.application.editor.struts.framework.util.ForwardConstants;
import uk.ac.ebi.intact.application.editor.util.LockManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Super class for all Editor related action classes.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public abstract class AbstractEditorAction extends Action implements ForwardConstants {

    /** The global Intact error key. */
    public static final String EDITOR_ERROR = "EditError";

    /**
     * The logger for Editor. Allow access from the subclasses.
     */
//    protected static final Logger LOGGER = Logger.getLogger(EditorConstants.LOGGER);
      protected static final Log LOGGER = LogFactory.getLog(AbstractEditorAction.class);

    // Class Methods

    /**
     * Returns true if given value is null or empty.
     * @param value the value to check for empty.
     * @return true if <code>value</code> is null or empty; any excess spaces
     * are removed before checking for empty.
     */
    public static boolean isPropertyEmpty(String value) {
        return (value == null) || (value.trim().length() == 0);
    }

    // Protected methods

    /**
     * Returns the only instance of Intact Service instance.
     * @return only instance of the <code>EditorService</code> class.
     */
    protected EditorService getService() {
        EditorService service = (EditorService)
                getApplicationObject(EditorConstants.EDITOR_SERVICE);
        return service;
    }

    /**
     * Returns the Intact User instance saved in a session for given
     * Http request.
     *
     * @param request the Http request to access the Intact user object.
     * @return an instance of <code>EditUser</code> stored in a session.
     * No new session is created.
     * @exception SessionExpiredException for an expired session.
     *
     * <pre>
     * post: return <> Undefined
     * </pre>
     */
    protected EditUserI getIntactUser(HttpServletRequest request)
            throws SessionExpiredException {
        EditUserI user = (EditUserI)
                getSessionObject(request, EditorConstants.INTACT_USER);
        if (user == null) {
            throw new SessionExpiredException();
        }
        return user;
    }

    /**
     * Returns the Intact User instance saved in a session.
     *
     * @param session the session to access the Intact user object.
     * @return an instance of <code>EditUser</code> stored in
     * <code>session</code>
     * @exception SessionExpiredException for an expired session.
     *
     * <pre>
     * post: return <> Undefined
     * </pre>
     */
    protected EditUserI getIntactUser(HttpSession session)
            throws SessionExpiredException {
        EditUserI user = (EditUserI)
                session.getAttribute(EditorConstants.INTACT_USER);
        if (user == null) {
            throw new SessionExpiredException();
        }
        return user;
    }

    /**
     * @return the lock manager stored in the application context.
     */
    protected LockManager getLockManager() {
        return (LockManager) getApplicationObject(EditorConstants.LOCK_MGR);
    }

    /**
     * Returns the session from given request. No new session is created.
     * @param request the request to get the session from.
     * @return session associated with given request.
     * @exception SessionExpiredException for an expired session.
     *
     * <pre>
     * post: return <> Undefined
     * </pre>
     */
    protected HttpSession getSession(HttpServletRequest request)
            throws SessionExpiredException {
        // Don't create a new session.
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new SessionExpiredException();
        }
        return session;
    }

    /**
     * Removes the obsolete form bean.
     * @param mapping the ActionMapping used to select this instance.
     * @param request the HTTP request we are processing.
     */
    protected void removeFormBean(ActionMapping mapping,
                                  HttpServletRequest request) {
        // Remove the obsolete form bean
        if (mapping.getAttribute() != null) {
            if ("request".equals(mapping.getScope())) {
                request.removeAttribute(mapping.getAttribute());
            }
            else {
                HttpSession session = request.getSession();
                session.removeAttribute(mapping.getAttribute());
            }
        }
    }

    /**
     * Returns true if errors in stored in the request
     * @param request Http request to search errors for.
     * @return true if strut's error is found in <code>request</code> and
     * it is not null. For all instances, false is returned.
     */
    protected boolean hasErrors(HttpServletRequest request) {
        ActionMessages errors =
                (ActionMessages) request.getAttribute(Globals.ERROR_KEY);
        if (errors != null) {
            // Empty menas no errors.
            return !errors.isEmpty();
        }
        // No errors stored in the request.
        return false;
    }

    /**
     * Returns true if the property for given name is empty.
     * @param property the property to check.
     * @return true if <code>property</code> is null or empty.
     */
    protected boolean isPropertyNullOrEmpty(String property) {
        if (property == null) {
            return true;
        }
        return property.length() == 0;
    }

    /**
     * Returns true if the property for given name is empty. This assumes the
     * property value for given name is a String object.
     * @param form the form to check.
     * @param name the name of the property to check.
     * @return true if <code>name</code> is empty in <code>form</code>.
     *
     * <pre>
     * pre: form.get(name).class.getName() == java.lang.String
     * </pre>
     */
    protected boolean isPropertyNullOrEmpty(DynaActionForm form, String name) {
        return isPropertyNull(form, name) || isPropertyEmpty((String) form.get(name));
    }

    /**
     * Returns true if the property for given name is null.
     * @param form the form to check.
     * @param name the name of the property to check.
     * @return true if <code>name</code> is null in <code>form</code>.
     */
    protected boolean isPropertyNull(DynaActionForm form, String name) {
        return form.get(name) == null;
    }

    /**
     * Returns true if given property is empty.
     * @param form the form to check.
     * @param name the name of the property; the value stored under this property
     * must be a String type.
     * @return true if property <code>name</code> is empty; any excess spaces
     * are removed before checking for empty.
     */
    protected boolean isPropertyEmpty(DynaActionForm form, String name) {
        return ((String) form.get(name)).trim().length() == 0;
    }

    /**
     * A convenient method to retrieve an application object from a session.
     * @param attrName the attribute name.
     * @return an application object stored in a session under <tt>attrName</tt>.
     */
    protected Object getApplicationObject(String attrName) {
        return super.servlet.getServletContext().getAttribute(attrName);
    }

    /**
     * Sets the anchor in the form if an anchor exists.
     *
     * @param request the HTTP request to get anchor.
     * @param form the form to set the anchor and also to extract the dispath
     * event.
     *
     * @see EditorService#getAnchor(java.util.Map, HttpServletRequest, String)
     */
    protected void setAnchor(HttpServletRequest request, EditorFormI form) {
        // The map containing anchors.
        Map map = (Map) getApplicationObject(EditorConstants.ANCHOR_MAP);

        // Any anchors to set?
        String anchor = getService().getAnchor(map, request, form.getDispatch());
        // Set the anchor only if it is set.
        if (anchor != null) {
            form.setAnchor(anchor);
        }
    }

    /**
     * Tries to acquire a lock for given id and owner.
     * @param ac the id or the accession number to lock.
     * @param owner the owner of the lock.
     * @return null if there are no errors in acquiring the lock or else
     * non null value is returned to indicate errors.
     */
    protected ActionMessages acquire(String ac, String owner) {
        // Try to acuire the lock.
        if (!getLockManager().acquire(ac, owner)) {
            ActionMessages errors = new ActionMessages();
            // The owner of the lock (not the current user).
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("error.lock", ac, getLockManager().getOwner(ac)));
            return errors;
        }
        return null;
    }

    /**
     * @param form the current form holds the label of the currently selected
     * button.
     * @param request the request object to get application resources
     * @param label the label of the button to check
     * @return true if <code>label</code> equals to currently selected button
     */
    protected boolean hasPressedButton(EditorFormI form, HttpServletRequest request,
                                       String label) {
        // Message resources to access button labels.
        MessageResources msgres = getResources(request);

        return form.getDispatch().equals(msgres.getMessage(label));
    }

    // Helper Methods

    /**
     * Retrieve a session object based on the request and attribute name.
     *
     * @param request the HTTP request to retrieve a session object stored
     * under <tt>attrName</tt>.
     * @param attrName the name of the attribute.
     * @return the session object stored in <tt>request</tt> under
     * <tt>attrName</tt>.
     * @exception SessionExpiredException for an expired session.
     *
     * <pre>
     * post: return <> Undefined
     * </pre>
     */
    private Object getSessionObject(HttpServletRequest request,
                                    String attrName) throws SessionExpiredException {
        return getSession(request).getAttribute(attrName);
    }
}
