/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.action.interaction;

import org.apache.struts.action.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.application.editor.struts.framework.AbstractEditorDispatchAction;
import uk.ac.ebi.intact.application.editor.struts.framework.EditorFormI;
import uk.ac.ebi.intact.application.editor.struts.view.feature.FeatureBean;
import uk.ac.ebi.intact.application.editor.struts.view.interaction.InteractionViewBean;
import uk.ac.ebi.intact.application.editor.struts.view.interaction.ComponentBean;
import uk.ac.ebi.intact.model.Interaction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * The action class to handle events related to delete/link/unlink of a feature.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 *
 * @struts.action
 *      path="/int/feature/link"
 *      name="intForm"
 *      input="edit.layout"
 *      scope="session"
 *      parameter="dispatch"
 */
public class FeatureLinkAction extends AbstractEditorDispatchAction {
    private static final Log log = LogFactory.getLog(FeatureLinkAction.class);

    protected Map getKeyMethodMap() {
        Map map = new HashMap();
        map.put("int.proteins.button.feature.delete", "delete");
        map.put("int.proteins.button.feature.link", "link");
        map.put("int.proteins.button.feature.unlink", "unlink");
        return map;
    }

    /**
     * Handles when Delete Feature button is pressed.
     */
    public ActionForward delete(ActionMapping mapping,
                                ActionForm form,
                                HttpServletRequest request,
                                HttpServletResponse response)
            throws Exception {

        log.debug("\n\n\n\n\nFeatureLinkAction.delete");

        // Handler to the Intact User.
        EditUserI user = getIntactUser(request);

        // The current view to delete features.
        InteractionViewBean view = (InteractionViewBean) user.getView();

        // The features we are about to delete.
        List beans = view.getFeaturesToDelete();

        // Delete the features.
        for (Iterator iter = beans.iterator(); iter.hasNext();) {
            FeatureBean fb = (FeatureBean) iter.next();
            view.delFeature(fb);
            // No longer checked.
            fb.setChecked(false);
        }
      
        return updateForm(mapping, form, request);
    }

    /**
     * Handles when Link Feature button is pressed.
     */
    public ActionForward link(ActionMapping mapping,
                              ActionForm form,
                              HttpServletRequest request,
                              HttpServletResponse response)
            throws Exception {
        // The current view to link/unlink features.
        InteractionViewBean view =
                (InteractionViewBean) getIntactUser(request).getView();

        // The two Features to link.
        FeatureBean[] fbs = view.getFeaturesForLink();

        // Set the checked boxes off.
        fbs[0].setChecked(false);
        fbs[1].setChecked(false);

        // Check for any error Features.
        if (fbs[0].isError() || fbs[1].isError()) {
            ActionMessages errors = new ActionMessages();
            errors.add("feature.link", new ActionMessage("error.int.feature"));
            saveErrors(request, errors);
            return updateForm(mapping, form, request);
        }

        // Check if they already have bound domains.
        if (fbs[0].hasBoundDomain() || fbs[1].hasBoundDomain()) {
            ActionMessages errors = new ActionMessages();
            errors.add("feature.link",
                    new ActionMessage("error.int.feature.link.error"));
            saveErrors(request, errors);
        }
        else {
            // Link two features.
            view.addFeatureLink(fbs[0], fbs[1]);
        }
        return updateForm(mapping, form, request);
    }

    /**
     * Handles when Unlink Feature button is pressed.
     */
    public ActionForward unlink(ActionMapping mapping,
                                ActionForm form,
                                HttpServletRequest request,
                                HttpServletResponse response)
            throws Exception {
        // The current view to link/unlink features.
        InteractionViewBean view =
                (InteractionViewBean) getIntactUser(request).getView();

        // The Feature to unlink.
        FeatureBean fb = view.getFeatureForUnlink();

        // Set the checked box off.
        fb.setChecked(false);

        // Check for any error Features.
        if (fb.isError()) {
            ActionMessages errors = new ActionMessages();
            errors.add("feature.link", new ActionMessage("error.int.feature"));
            saveErrors(request, errors);
            return updateForm(mapping, form, request);
        }

        // Check if they already have bound domains.
        if (!fb.hasBoundDomain()) {
            ActionMessages errors = new ActionMessages();
            errors.add("feature.link",
                    new ActionMessage("error.int.feature.unlink.error"));
            saveErrors(request, errors);
        }
        else {
            // This feature is linked.
            view.addFeatureToUnlink(fb);
        }
        return updateForm(mapping, form, request);
    }

    // Encapsulate common functionality
    private ActionForward updateForm(ActionMapping mapping,
                                     ActionForm form,
                                     HttpServletRequest request) {
        // Set the anchor if necessary.
        setAnchor(request, (EditorFormI) form);

        // Update the form.
        return mapping.getInputForward();
    }
}