/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.action.experiment;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.application.editor.business.EditorService;
import uk.ac.ebi.intact.application.editor.struts.action.CommonDispatchAction;
import uk.ac.ebi.intact.application.editor.struts.view.experiment.ExperimentActionForm;
import uk.ac.ebi.intact.application.editor.util.DaoProvider;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.persistence.dao.InteractionDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Links to an interaction button from an experiment.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 *
 * @struts.action
 *      path="/int/link"
 *      name="expForm"
 *      input="edit.layout"
 *      scope="session"
 *
 * @struts.action-forward
 *      name="success"
 *      path="/do/int/fill/form"
 */
public class InteractionLinkAction extends CommonDispatchAction {

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws Exception {
        // Save the experiment first.
        ActionForward forward = save(mapping, form, request, response);

        // Return the forward for any non success.
        if (!forward.equals(mapping.findForward(SUCCESS))) {
            return forward;
        }
        // No errors. Linking starts from here.

        // Handler to the Intact User.
        EditUserI user = getIntactUser(request);

        // The AC of the interaction we are about to edit.
        String intAc = (String) ((ExperimentActionForm) form).getIntac();

        // The Intact helper to access the Interaction.
        InteractionDao interactionDao = DaoProvider.getDaoFactory().getInteractionDao();

        // The interaction we are about to edit.
        Interaction inter = interactionDao.getByAc(intAc);//(Interaction) helper.getObjectByAc(Interaction.class, intAc);

        // We must have this Interaction.
        assert inter != null;

        // Try to acquire the lock.
        ActionMessages errors = acquire(intAc, user.getUserName(), "err.interaction");
        if (errors != null) {
            saveErrors(request, errors);
            return mapping.getInputForward();
        }
        // Set the topic.
        user.setSelectedTopic(EditorService.getTopic(Interaction.class));

        // Set the interaction as the new view, don't release the previous view.
        user.setView(inter, false);

        // Update the form.
        return mapping.findForward(SUCCESS);
    }
}