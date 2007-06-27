/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.predict.struts.framework;

import org.apache.struts.action.Action;
import uk.ac.ebi.intact.application.commons.exception.SessionExpiredException;
import uk.ac.ebi.intact.application.predict.business.PredictUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Super class for all the Predict action classes.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class AbstractPredictAction extends Action {

    // Struts forwards. Used in various action classes to define where to
    // forward to on different conditions.  See the struts-config.xml file
    // to see where the page that is using this forwards to.

    /**
     * The key to success action.
     */
    protected static final String SUCCESS = "success";

    /**
     * The key to failure action.
     */
    protected static final String FAILURE = "failure";

    /**
     * Returns the Intact User instance saved in a session for given
     * Http request.
     *
     * @param request the Http request to access the Predict user object.
     * @return an instance of <code>PredictUser</code> stored in a session.
     * No new session is created.
     * @exception SessionExpiredException for an expired session.
     *
     * <pre>
     * post: return <> Undefined
     * </pre>
     */
    protected PredictUser getPredictUser(HttpServletRequest request)
            throws SessionExpiredException {
        PredictUser user = (PredictUser)
                getSessionObject(request, PredictConstants.USER);
        if (user == null) {
            throw new SessionExpiredException();
        }
        return user;
    }

    /**
     * Retrieve a session object based on the request and attribute name.
     *
     * @param request the HTTP request to retrieve a session object stored
     * under <tt>name</tt>.
     * @param name the name of the attribute.
     * @return the session object stored in <tt>request</tt> under
     * <tt>name</tt>.
     * @exception SessionExpiredException for an expired session.
     *
     * <pre>
     * post: return <> Undefined
     * </pre>
     */
    private Object getSessionObject(HttpServletRequest request, String name)
            throws SessionExpiredException {
        // Don't create a new session.
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new SessionExpiredException();
        }
        return session.getAttribute(name);
    }
}
