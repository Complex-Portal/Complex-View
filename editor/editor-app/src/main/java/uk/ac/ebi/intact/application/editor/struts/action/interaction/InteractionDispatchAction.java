/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.action.interaction;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.application.editor.struts.action.CommonDispatchAction;
import uk.ac.ebi.intact.application.editor.struts.view.experiment.ExperimentViewBean;
import uk.ac.ebi.intact.application.editor.struts.view.interaction.InteractionViewBean;
import uk.ac.ebi.intact.application.editor.struts.framework.util.AbstractEditViewBean;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

/**
 * An action to handle when an Interaction is submitted. This action overrides
 * the submit method of the super to analyze the next cause of action
 * to take. If there are no errors, the user is returned to the experiment
 * editor only if we got to the interaction editor from an experiment.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 *
 * @struts.action
 *      path="/int/submit"
 *      name="intForm"
 *      input="edit.layout"
 *      scope="session"
 *      parameter="dispatch"
 *
 * @struts.action-forward
 *      name="success"
 *      path="/do/display"
 *
 * @struts.action-forward
 *      name="experiment"
 *      path="/do/exp/fill/form"
 *
 * @struts.action-forward
 *      name="reload"
 *      path="/do/int/fill/form"
 */
public class InteractionDispatchAction extends CommonDispatchAction {

    // Override to provide a way to get back to the experiment editor when
    // an interaction is submitted.
    public ActionForward submit(ActionMapping mapping,
                                ActionForm form,
                                HttpServletRequest request,
                                HttpServletResponse response)
            throws Exception {
        log.debug("\n\n\n\nInteractionDispatchAction.submit");

        // Submit the form. Analyze the forward path.
        ActionForward forward = submitForm(mapping, form, request, true);

        // Return the forward if it isn't a success.
        if (!forward.equals(mapping.findForward(SUCCESS))) {
            return forward;
        }
        // Handler to the user.
        EditUserI user = getIntactUser(request);

        // The current view.
        InteractionViewBean view = (InteractionViewBean) user.getView();

        // Do we have to return to the experiment editor?
        if (user.hasPreviousView()) {
            // The experiment we going back to.
            ExperimentViewBean expView = (ExperimentViewBean) user.popPreviousView();

            // Update the experiment-Interaction view.
            expView.updateInteractionRow((Interaction) view.getAnnotatedObject());

            // Set the tpdated view.
            user.setView(expView);

            // Return to the experiment editor.
            return mapping.findForward(EXP);
        }
        // Update the search cache.
        user.updateSearchCache(view.getAnnotatedObject());

        // Add the current edited object to the recent list.
        view.addToRecentList(user);

        // Only show the submitted record.
        return mapping.findForward(RESULT);
    }

    // Override to add the saved interaction to the experiment if necessary.
    public ActionForward save(ActionMapping mapping,
                              ActionForm form,
                              HttpServletRequest request,
                              HttpServletResponse response)
            throws Exception {
        // Submit the form. Analyze the forward path.
        ActionForward forward = super.save(mapping, form, request, response);

        // Return the forward if it isn't a success.
        if (!forward.equals(mapping.findForward(SUCCESS))) {
            return forward;
        }
        // Handler to the user.
        EditUserI user = getIntactUser(request);

//  interactionViewBean.removeAllComponentsToUpdate();
//        Interaction interaction = (Interaction) user.getView().getAnnotatedObject();
//        // We reset the view with the saved interaction so that the ac are reset as well.
//        // !!!!BE CAREFULL !!!! when you reset the view all the isSelected boolean are reset to false, so you won't know anymore
//        // if something has been selected.
//        user.getView().reset(interaction);

        // Do we have to return to the experiment editor?
        if (user.hasPreviousView()) {

            AbstractEditViewBean view = user.peekPreviousView();

            if (ExperimentViewBean.class.isAssignableFrom(view.getClass())){

                // The experiment we will be going back to.
                ExperimentViewBean expView = (ExperimentViewBean) user.peekPreviousView();

                // Update the experiment-Interaction view.
                expView.updateInteractionRow((Interaction) user.getView().getAnnotatedObject());

                log.info("The previous view of this Interaction was no an ExperimentView but a "
                        + view.getClass().getSimpleName());
            }
        }
        return forward;
    }
}
