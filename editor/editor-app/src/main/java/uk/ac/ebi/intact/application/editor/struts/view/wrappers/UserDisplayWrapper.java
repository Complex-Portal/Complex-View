/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.view.wrappers;

import org.apache.taglibs.display.TableDecorator;
import uk.ac.ebi.intact.application.editor.event.AuthenticationEvent;
import uk.ac.ebi.intact.application.editor.struts.framework.util.EditorConstants;
import uk.ac.ebi.intact.application.editor.util.LockManager;

import java.text.SimpleDateFormat;
import java.util.Iterator;

/**
 * This class is the wrapper class for the display library to display user info.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class UserDisplayWrapper extends TableDecorator {

    /**
     * The formatter for the date.
     */
    private static SimpleDateFormat ourDateFormatter =
            new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");

    /**
     * @return the lock information
     */
    public String getLockData() {
        // The lock manager.
        LockManager lm = (LockManager) getPageContext().getServletContext().getAttribute(
                EditorConstants.LOCK_MGR);

        // The string buffer to construct the string.
        StringBuffer sb = new StringBuffer();

        // The user to get locks for.
        String user = ((AuthenticationEvent) getObject()).getUserName();

        // We could have multiple locks held by this user.
        for (Iterator iter = lm.getLocks(user).iterator(); iter.hasNext();) {
            LockManager.LockObject lock = (LockManager.LockObject) iter.next();
            sb.append(lock.getId() + " - ");
            sb.append(ourDateFormatter.format(lock.getLockDate()));
            if (iter.hasNext()) {
                sb.append("</br>");
            }
        }
        return sb.toString();
    }
}
