/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.action.feature;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import uk.ac.ebi.intact.application.editor.struts.framework.AbstractEditorAction;
import uk.ac.ebi.intact.application.editor.struts.framework.EditorFormI;
import uk.ac.ebi.intact.application.editor.struts.view.feature.DefinedFeatureBean;
import uk.ac.ebi.intact.application.editor.struts.view.feature.FeatureViewBean;
import uk.ac.ebi.intact.application.editor.struts.view.feature.RangeBean;
import uk.ac.ebi.intact.model.Feature;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This action is invoked when Clone button is selected. As a pre condition,
 * an AC must be provided.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 *
 * @struts.action
 *      path="/feature/undetermined/clone"
 *      name="featureForm"
 *      scope="session"
 *      validate="false"
 *
 * @struts.action-forward
 *      name="success"
 *      path="edit.layout"
 */
public class UndeterminedCloneAction extends AbstractEditorAction {

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws Exception {
        // The current view.
        FeatureViewBean view = (FeatureViewBean) getIntactUser(request).getView();

        // Reset the view.
        view.reset(Feature.class);

        // The defined feature from the view.
        DefinedFeatureBean dfb = view.getDefinedFeature();

        // Set the defined feature values to the bean.
        view.setShortLabel(dfb.getShortLabel());
        view.setFullName(dfb.getFullName());

        // The undetermined range.
        RangeBean rb = dfb.getDefinedRange();

        // Add a copy of the new range (as rb is shared among all users)
        view.addRange(new RangeBean(rb.getFromRange(), rb.getToRange(), rb.getLink()));

        // Update the form for the display.
        view.copyPropertiesTo((EditorFormI) form);

        // Update the form and display.
        return mapping.findForward(SUCCESS);
    }
}