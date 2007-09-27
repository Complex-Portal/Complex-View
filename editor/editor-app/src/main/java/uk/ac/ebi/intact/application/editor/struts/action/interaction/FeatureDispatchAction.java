/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.action.interaction;

import org.apache.struts.action.*;
import org.apache.log4j.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.application.editor.struts.action.CommonDispatchAction;
import uk.ac.ebi.intact.application.editor.struts.view.feature.FeatureBean;
import uk.ac.ebi.intact.application.editor.struts.view.feature.FeatureViewBean;
import uk.ac.ebi.intact.application.editor.struts.view.interaction.ComponentBean;
import uk.ac.ebi.intact.application.editor.struts.view.interaction.InteractionActionForm;
import uk.ac.ebi.intact.application.editor.struts.view.interaction.InteractionViewBean;
import uk.ac.ebi.intact.application.editor.struts.framework.util.AbstractEditViewBean;
import uk.ac.ebi.intact.application.editor.util.DaoProvider;
import uk.ac.ebi.intact.model.Feature;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.persistence.dao.FeatureDao;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

/**
 * The action class to handle events related to add/edit a
 * Feature from an Interaction editor.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 *
 * @struts.action
 *      path="/int/feature"
 *      name="intForm"
 *      input="edit.layout"
 *      scope="session"
 *      parameter="dispatchFeature"
 *
 * @struts.action-forward
 *      name="success"
 *      path="/do/feature/fill/form"
 */
public class FeatureDispatchAction extends CommonDispatchAction {
    private static final Log log = LogFactory.getLog(FeatureDispatchAction.class);

    protected Map getKeyMethodMap() {
        Map map = new HashMap();
        map.put("int.proteins.button.feature.edit", "edit");
        map.put("int.proteins.button.feature.add", "add");
        return map;
    }

    /**
     * Handles when Edit Feature button is pressed.
     */
    public ActionForward edit(ActionMapping mapping,
                              ActionForm form,
                              HttpServletRequest request,
                              HttpServletResponse response)
            throws Exception {
        // I have to get the selected feature now. As after the super.save() the view is reset with the saved
        // annotatedObject, and during this reset the isSelected boolean got set to false.
        // So : make sure the getSelectedFeature is call before super.save().
        // Handler to the Intact User.
        EditUserI user = getIntactUser(request);
        // The feature we are about to edit.
        FeatureBean fb = ((InteractionViewBean) user.getView()).getSelectedFeature();
        Feature feature = fb.getFeature();

        //If the feature, has no ac, it means that the interaction has been saved and not yet cloned. In that case, the
        //user should first Save And Continue before trying to edit a feature. So we just send a message to the user in 
        //that sens.
        if(feature.getAc() == null){
            InteractionActionForm intform = (InteractionActionForm) form;
            ActionMessages errors = new ActionMessages();
            errors.add("int.unsaved.clone", new ActionMessage("error.clone.unsaved"));
            // Save the errors to display later
            saveErrors(request, errors);
            // Set the anchor
            setAnchor(request, intform);
            // Back to the edit form
            return mapping.getInputForward();
            
        }

        // Save the interaction first.
        ActionForward forward = super.save(mapping, form, request, response);
        // Don not proceed if the inteaction wasn't saved successfully first.
        if (!forward.equals(mapping.findForward(SUCCESS))) {
            return forward;
        }
        // Linking to the feature editor starts from here.
        // Set the selected topic as other operation use it for various tasks.
        user.setSelectedTopic("Feature");

        FeatureDao featureDao = DaoProvider.getDaoFactory().getFeatureDao();
        feature = featureDao.getByAc(feature.getAc());

        // Set the new object as the current edit object, don't release the pre view
        user.setView(feature, false);

        return forward;
    }

    /**
     * Handles when Add Feature button is pressed.
     */
    public ActionForward add(ActionMapping mapping,
                             ActionForm form,
                             HttpServletRequest request,
                             HttpServletResponse response)
            throws Exception {

        log.debug("\n\n\n\n\nFeatureDispatchAction.add");
        // The form.
        InteractionActionForm intform = (InteractionActionForm) form;
        
        // The selected component from the form.
        ComponentBean selectedComp = intform.getSelectedComponent();
        log.debug("The selected component shortlabel is : " + selectedComp.getFullName());
        
        // Save the interaction first.
        ActionForward forward = super.save(mapping, form, request, response);

        // Don not proceed if the inteaction wasn't saved successfully first.
        if (!forward.equals(mapping.findForward(SUCCESS))) {
            return forward;
        }
        // Linking to the feature editor starts from here.

        // Handler to the Intact User.
        EditUserI user = getIntactUser(request);

        InteractionViewBean interactionViewBean = (InteractionViewBean) user.getView();

        // Set the selected topic as other operation use it for various tasks.
        user.setSelectedTopic("Feature");

        // Set the new object as the current edit object, don't release the pre view
        user.setView(Feature.class, false);

        // The feature view bean.
        FeatureViewBean featureView = (FeatureViewBean) user.getView();


        // The component for the feature.
        featureView.setComponent(selectedComp.getComponent());
        return mapping.findForward(SUCCESS);
    }
}