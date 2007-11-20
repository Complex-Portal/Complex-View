/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.action.interaction;

import org.apache.struts.action.*;
import uk.ac.ebi.intact.application.editor.struts.framework.AbstractEditorDispatchAction;
import uk.ac.ebi.intact.application.editor.struts.view.AbstractEditBean;
import uk.ac.ebi.intact.application.editor.struts.view.interaction.ComponentBean;
import uk.ac.ebi.intact.application.editor.struts.view.interaction.InteractionActionForm;
import uk.ac.ebi.intact.application.editor.struts.view.interaction.InteractionViewBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * The action class for edit/save/delete a Protein.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 *
 * @struts.action
 *      path="/int/prot"
 *      name="intForm"
 *      input="edit.layout"
 *      scope="session"
 *      parameter="dispatchProtein"
 */
public class ProteinDispatchAction extends AbstractEditorDispatchAction {

    protected Map getKeyMethodMap() {
        Map map = new HashMap();
        map.put("int.interactors.button.edit", "edit");
        map.put("int.interactors.button.save", "save");
        map.put("int.interactors.button.delete", "delete");
        return map;
    }

    /**
     * Handles when Edit Protein button is pressed.
     */
    public ActionForward edit(ActionMapping mapping,
                              ActionForm form,
                              HttpServletRequest request,
                              HttpServletResponse response)
            throws Exception {
        // The form.
        InteractionActionForm intform = (InteractionActionForm) form;

        // The protein we are editing at the moment.
        ComponentBean cb = intform.getSelectedComponent();

        // We must have the protein bean.
        assert cb != null;

        // Must save this bean.
        cb.setEditState(AbstractEditBean.SAVE);

        // The current view of the edit session.
        InteractionViewBean view =
                (InteractionViewBean) getIntactUser(request).getView();

        // Update the form.
        return updateForm(mapping, intform, request, view);
    }

    /**
     * Handles when Save Protein button is pressed.
     */
    public ActionForward save(ActionMapping mapping,
                              ActionForm form,
                              HttpServletRequest request,
                              HttpServletResponse response)
            throws Exception {
        // The form.
        InteractionActionForm intform = (InteractionActionForm) form;

        // The current view of the edit session.
        InteractionViewBean view =
                (InteractionViewBean) getIntactUser(request).getView();

        // The component we are editing at the moment.
        ComponentBean cb = intform.getSelectedComponent();

        // We must have the protein bean.
        assert cb != null;

        // Must define a role for the Protein.
        if (cb.getExpRole() == null) {
            ActionMessages errors = new ActionMessages();
            errors.add("int.prot.role",
                    new ActionMessage("error.int.interact.edit.role"));
            saveErrors(request, errors);
            cb.setEditState(AbstractEditBean.ERROR);

            // Set the anchor for the page to scroll.
            setAnchor(request, intform);

            // Display the error in the editor.
            return mapping.getInputForward();
        }
        if (cb.getBioRole() == null) {
            log.debug("cb.getBioRole() = " + cb.getBioRole());

            ActionMessages errors = new ActionMessages();
            errors.add("int.prot.role",
                    new ActionMessage("error.int.interact.edit.biorole"));
            saveErrors(request, errors);
            cb.setEditState(AbstractEditBean.ERROR);

            // Set the anchor for the page to scroll.
            setAnchor(request, intform);

            // Display the error in the editor.
            return mapping.getInputForward();
        }
        // The protein to update.
        view.addPolymerToUpdate(cb);

        // Back to the view mode.
        cb.setEditState(AbstractEditBean.VIEW);

        // Remove the unsaved proteins.
        view.removeUnsavedProteins();

        // Update the form.
        return updateForm(mapping, intform, request, view);
    }

    /**
     * Handles when Delete Protein button is pressed.
     */
    public ActionForward delete(ActionMapping mapping,
                                ActionForm form,
                                HttpServletRequest request,
                                HttpServletResponse response)
            throws Exception {
        // The form.
        InteractionActionForm intform = (InteractionActionForm) form;

        // The current view of the edit session.
        InteractionViewBean view =
                (InteractionViewBean) getIntactUser(request).getView();

        // Delete this Protein from the view.
        view.delPolymer(intform.getDispatchIndex());

        // Update the form.
        return updateForm(mapping, intform, request, view);
    }

    private ActionForward updateForm(ActionMapping mapping,
                                           InteractionActionForm form,
                                           HttpServletRequest request,
                                           InteractionViewBean view) {
        // Refresh the form with updated components.
        form.setComponents(view.getComponents());

        // Set the anchor if necessary.
        setAnchor(request, form);

        // Update the form.
        return mapping.getInputForward();
    }
}