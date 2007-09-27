/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.action.feature;

import org.apache.struts.action.*;
import org.apache.struts.util.MessageResources;
import uk.ac.ebi.intact.application.editor.struts.framework.AbstractEditorAction;
import uk.ac.ebi.intact.application.editor.struts.view.AbstractEditBean;
import uk.ac.ebi.intact.application.editor.struts.view.feature.FeatureActionForm;
import uk.ac.ebi.intact.application.editor.struts.view.feature.FeatureViewBean;
import uk.ac.ebi.intact.application.editor.struts.view.feature.RangeBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The action class for editing a Range (edit/delete/save).
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 *
 * @struts.action
 *      path="/feature/range"
 *      name="featureForm"
 *      input="edit.layout"
 *      scope="session"
 */
public class RangeDispatchAction extends AbstractEditorAction {

    /**
     * This method dispatches calls to various methods using the 'dispatch' parameter.
     *
     * @param mapping  - The <code>ActionMapping</code> used to select this instance
     * @param form     - The optional <code>ActionForm</code> bean for this request (if any)
     * @param request  - The HTTP request we are processing
     * @param response - The HTTP response we are creating
     * @return - represents a destination to which the action servlet,
     *         <code>ActionServlet</code>, might be directed to perform a RequestDispatcher.forward()
     *         or HttpServletResponse.sendRedirect() to, as a result of processing
     *         activities of an <code>Action</code> class
     */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws Exception {
        // The form.
        FeatureActionForm featureForm = (FeatureActionForm) form;

        // The current view of the edit session.
        FeatureViewBean view =
                (FeatureViewBean) getIntactUser(request).getView();

        // The range we are editing at the moment.
        RangeBean bean = featureForm.getSelectedRange();

        // The command associated with the dispatch
        String cmd = featureForm.getDispatch();

        // Message resources to access button labels.
        MessageResources msgres = getResources(request);

        if (cmd.equals(msgres.getMessage("feature.range.button.edit"))) {
            // Set the state to save this bean.
            bean.setEditState(AbstractEditBean.SAVE);
        }
        else if (cmd.equals(msgres.getMessage("feature.range.button.save"))) {
            // Does the range exist in the current ranges?
            if (view.rangeExists(bean)) {
                // Mark the bean as error.
                bean.setEditState(AbstractEditBean.ERROR);

                // The errors to display.
                ActionMessages errors = new ActionMessages();
                errors.add("feature.range.exists",
                        new ActionMessage("error.feature.range.exists"));
                saveErrors(request, errors);
                // Display the errors.
                return mapping.getInputForward();
            }
            // Save the bean in the view.
            view.saveRange(bean);

            // Back to the view mode.
            bean.setEditState(AbstractEditBean.VIEW);
        }
        else {
            // Default is to delete a range.
            view.delRange(bean);
        }
        // Update the form.
        return mapping.getInputForward();
    }
}