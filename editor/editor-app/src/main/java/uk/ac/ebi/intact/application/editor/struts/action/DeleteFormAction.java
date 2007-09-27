/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.action;

import org.apache.struts.action.*;
import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.application.editor.struts.framework.AbstractEditorAction;
import uk.ac.ebi.intact.application.editor.struts.framework.util.AbstractEditViewBean;
import uk.ac.ebi.intact.business.IntactException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This action handles the event when the user clicks on the Delete button to
 * delete the current edit record.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class DeleteFormAction extends AbstractEditorAction {

    /**
     * Action for cancelling changes to the current edit.
     * @param mapping the <code>ActionMapping</code> used to select this instance
     * @param form the optional <code>ActionForm</code> bean for this request
     * (if any).
     * @param request the HTTP request we are processing
     * @param response the HTTP response we are creating
     * @return failure mapping for any errors in cancelling the CV object; search
     * mapping if the cancel is successful and the previous search has only one
     * result; results mapping if the cancel is successful and the previous
     * search has produced multiple results.
     * @throws Exception for any uncaught errors.
     */
    public ActionForward execute(ActionMapping mapping,
                                ActionForm form,
                                HttpServletRequest request,
                                HttpServletResponse response)
            throws Exception {
        // Handler to the Intact User.
        EditUserI user = getIntactUser(request);

        // The current view.
        AbstractEditViewBean view = user.getView();

        // Remove this current bean from the recent lists.
        view.removeFromRecentList(user);


        try {
            // No need to delete if the object is not yet persisted.
            if ( view.getAnnotatedObject() == null ||
                 (view.getAnnotatedObject() != null && view.getAnnotatedObject().getAc() == null)) {
                // Back to the search page.
                return mapping.findForward(SEARCH);
            }
            // Delete the object we are editing at the moment.
            user.delete();
        }
        catch (IntactException ie1) {
            // Log the stack trace.
            LOGGER.error("", ie1);
//            try {
                user.rollback();
//            }
//            catch (IntactException ie2) {
//                LOGGER.error("Error trying to do a rollback", ie2);
//                // Oops! Problems with rollback; ignore this as this
//                // error is reported via the main exception (ie1).
//            }
            // Error with deleting the object.
            ActionMessages errors = new ActionMessages();
            errors.add(EDITOR_ERROR, new ActionMessage("error.delete",
                    ie1.getNestedMessage()));
            saveErrors(request, errors);
            return mapping.findForward(FAILURE);
        }
        finally {
            // Release the lock.
            getLockManager().release(view.getAc());
            releaseView(user);
        }
        // Back to the search page.
        return mapping.findForward(SEARCH);
    }

    // Subclasses may override this method to release the view later.
    protected void releaseView(EditUserI user) {
        // Release the view back to the pool.
        user.releaseView();
    }
}
