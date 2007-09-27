/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.action;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.application.editor.struts.framework.AbstractEditorAction;
import uk.ac.ebi.intact.model.AnnotatedObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This action handles the event when the user clicks on Cancel button (common
 * to all the editors except for Feature editor).
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class CancelFormAction extends AbstractEditorAction {

    /**
     * Action for cancelling changes to the current edit.
     *
     * @param mapping  the <code>ActionMapping</code> used to select this instance
     * @param form     the optional <code>ActionForm</code> bean for this request
     *                 (if any).
     * @param request  the HTTP request we are processing
     * @param response the HTTP response we are creating
     * @return failure mapping for any errors in cancelling the CV object; search
     *         mapping if the cancel is successful and the previous search has only one
     *         result; results mapping if the cancel is successful and the previous
     *         search has produced multiple results.
     * @throws java.lang.Exception for any uncaught errors.
     */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws Exception {
        // Handler to the Intact User.
        EditUserI user = getIntactUser(request);

        // Update the search cache to display the current object.
        AnnotatedObject annobj = user.getView().getAnnotatedObject();
        if ((annobj != null) && (annobj.getAc() != null)) {
            // Only update the persistent objects.
            user.updateSearchCache(annobj);

            // Release the lock.
            getLockManager().release(annobj.getAc());
        }
        cancelEdit(user);

        // Back to the search page.
        return mapping.findForward(RESULT);
    }

    // Subclasses can override this method to delay cancelling the edit session.
    protected void cancelEdit(EditUserI user) {
        // Cancel the current edit session.
        user.cancelEdit();
    }
}
