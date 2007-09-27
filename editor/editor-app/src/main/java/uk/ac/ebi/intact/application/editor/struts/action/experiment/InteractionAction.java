/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.action.experiment;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import uk.ac.ebi.intact.application.editor.struts.framework.AbstractEditorAction;
import uk.ac.ebi.intact.application.editor.struts.view.experiment.ExperimentActionForm;
import uk.ac.ebi.intact.application.editor.struts.view.experiment.ExperimentViewBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Invoked when Delete/Edit Interaction button is pressed to delete/edit
 * an Interaction.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 *
 * @struts.action path="/interaction"
 *      name="expForm"
 *      input="edit.layout"
 *      scope="session"
 *      validate="false"
 *
 * @struts.action-forward name="editInt"
 *      path="/do/int/link"
 */
public class InteractionAction extends AbstractEditorAction {

    // Override the execute method to
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws Exception {
        // The dyna form.
        ExperimentActionForm expform = (ExperimentActionForm) form;

        // Are we editing an Interaction?
        if (expform.getDispatch().equals(getResources(request).getMessage(
                "exp.int.button.edit"))) {
            // Pass the control to another action to edit an interaction.
            return mapping.findForward("editInt");
        }
        // default is to delete an interaction from the experiment.

        // The current view of the edit session.
        ExperimentViewBean view = (ExperimentViewBean) getIntactUser(request).getView();

        // Delete the selected interaction.
        view.delInteraction(expform.getIntac());

        // Back to the input page.
        return mapping.getInputForward();
    }
}