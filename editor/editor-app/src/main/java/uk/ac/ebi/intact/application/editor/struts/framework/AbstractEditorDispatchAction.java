/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.framework;

import org.apache.log4j.Logger;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.LookupDispatchAction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.application.editor.business.EditorService;
import uk.ac.ebi.intact.application.editor.exception.SessionExpiredException;
import uk.ac.ebi.intact.application.editor.struts.framework.util.EditorConstants;
import uk.ac.ebi.intact.application.editor.struts.framework.util.ForwardConstants;
import uk.ac.ebi.intact.application.editor.struts.view.wrappers.ResultRowData;
import uk.ac.ebi.intact.application.editor.util.DaoProvider;
import uk.ac.ebi.intact.application.editor.util.LockManager;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.persistence.dao.AnnotatedObjectDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * The super class for all the dispatch actions.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $id$
 */
public abstract class AbstractEditorDispatchAction extends LookupDispatchAction
        implements ForwardConstants {

    /**
     * The logger for Editor. Allow access from the subclasses.
     */
    protected static final Log LOGGER = LogFactory.getLog(AbstractEditorDispatchAction.class);

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
     * Returns the session from given request. No new session is created.
     * @param request the request to get the session from.
     * @return session associated with given request.
     * @exception uk.ac.ebi.intact.application.editor.exception.SessionExpiredException for an expired session.
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
     * Returns the Intact User instance saved in a session for given
     * Http request.
     *
     * @param request the Http request to access the Intact user object.
     * @return an instance of <code>EditUser</code> stored in a session.
     * No new session is created.
     * @exception uk.ac.ebi.intact.application.editor.exception.SessionExpiredException for an expired session.
     *
     * <pre>
     * post: return <> Undefined
     * </pre>
     */
    protected EditUserI getIntactUser(HttpServletRequest request)
            throws SessionExpiredException {
        EditUserI user = (EditUserI)
                getSession(request).getAttribute(EditorConstants.INTACT_USER);
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
        return acquire(ac, owner, ActionMessages.GLOBAL_MESSAGE);
    }

    /**
     * Tries to acquire a lock for given id and owner.
     * @param ac the id or the accession number to lock.
     * @param owner the owner of the lock.
     * @param errGroup the error group for action errors (JSPs can display errors
     * under this name)
     * @return null if there are no errors in acquiring the lock or else
     * non null value is returned to indicate errors.
     */
    protected ActionMessages acquire(String ac, String owner, String errGroup) {
        // Try to acuire the lock.
        if (!getLockManager().acquire(ac, owner)) {
            ActionMessages errors = new ActionMessages();
            // The owner of the lock (not the current user).
            errors.add(errGroup, new ActionMessage("error.lock", ac,
                    getLockManager().getOwner(ac)));
            return errors;
        }
        return null;
    }

       public List<ResultRowData> getResults(Class searchClass, String searchString,
                                            int max, HttpServletRequest request,
                                            String errorType){

        AnnotatedObjectDao annotatedObjectDao = null;
        try{
            annotatedObjectDao = DaoProvider.getDaoFactory(searchClass);
        }catch(IntactException ie){
            ActionMessages errors = new ActionMessages();
            errors.add(errorType, new ActionMessage("error.intact"));
            saveErrors(request, errors);
            log.error("Problem getting the dao" + ie.getCause());
        }

        if(annotatedObjectDao == null){
            ActionMessages errors = new ActionMessages();
            errors.add(errorType, new ActionMessage("error.intact"));
            saveErrors(request, errors);
            return Collections.EMPTY_LIST;
        }
        List<AnnotatedObject> results = new ArrayList();//  annotatedObjectDao.getByShortlabelOrAcLike(searchString);
        try {
            results = annotatedObjectDao.getByShortlabelOrAcLike(searchString);
        }
        catch (IntactException ie) {
            // This can only happen when problems with creating an internal helper
            // This error is already logged from the User class.
            ActionMessages errors = new ActionMessages();
            errors.add(errorType, new ActionMessage("error.intact"));
            saveErrors(request, errors);
            return Collections.EMPTY_LIST;
        }

        if (results.size() > max) {
            ActionMessages errors = new ActionMessages();
            errors.add(errorType, new ActionMessage("error.search.large",
                            Integer.toString(results.size())));
            saveErrors(request, errors);
            return Collections.EMPTY_LIST;
        }

        if (results.isEmpty()) {
            // No matches found - forward to a suitable page
            ActionMessages errors = new ActionMessages();
            errors.add(errorType, new ActionMessage("error.search.nomatch",
                                                                           searchString, searchString));
            saveErrors(request, errors);
            return Collections.EMPTY_LIST;
        }

        return makeRowData(results);
    }

    /**
     * Returns an array of row data
     * @param iter the iterator to loop.
     * @param searchClass the search class to construct an instance of RowData
     * @return a list consists of RowData objects.
     */
    protected List makeRowData(Iterator iter, Class searchClass) {
        // The results to return.
        List results = new ArrayList();

        // Convert to result row data.
        while (iter.hasNext()) {
            results.add(new ResultRowData((Object[]) iter.next(), searchClass));
        }
        return results;
    }

    /**
     * Returns an array of row data
     * @return a list consists of RowData objects.
     */
    protected List<ResultRowData> makeRowData(List<AnnotatedObject> annotatedObjects) {
        // The results to return.
        List<ResultRowData>  results = new ArrayList<ResultRowData>();

        Iterator<AnnotatedObject> iter = annotatedObjects.iterator();
        // Convert to result row data.
        while (iter.hasNext()) {
            results.add(new ResultRowData(iter.next()));
        }
        return results;
    }

    /**
     * A convenient method to retrieve an application object from a session.
     * @param attrName the attribute name.
     * @return an application object stored in a session under <tt>attrName</tt>.
     */
    private Object getApplicationObject(String attrName) {
        return super.servlet.getServletContext().getAttribute(attrName);
    }
}
