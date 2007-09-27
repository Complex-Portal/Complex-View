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
import uk.ac.ebi.intact.application.editor.struts.view.feature.FeatureViewBean;
import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.application.editor.util.DaoProvider;
import uk.ac.ebi.intact.model.CvFeatureType;
import uk.ac.ebi.intact.persistence.dao.CvObjectDao;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This action is invoked when Mutation Toggle button is selected.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 *
 * @struts.action
 *      path="/feature/mutation"
 *      name="featureForm"
 *      scope="session"
 *      validate="false"
 *
 * @struts.action-forward
 *      name="success"
 *      path="/do/feature/fill/form"
 */
public class MutationToggleAction extends AbstractEditorAction {

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws Exception {
        // The current user.
        EditUserI user = getIntactUser(request);

        // The current view.
        FeatureViewBean view = (FeatureViewBean) user.getView();

        if (view.isInMutationMode()) {
            // Going from Mutation -> Normal mode.

            // Reset the short label (was set to a dummy default value) and the
            // feature type was set to hotspot
            view.setShortLabel("");
            view.setCvFeatureType(null);
        }
        else {
            // Going from Normal -> Mutation editor.

            // Set this dummy value to get through the validation.
            view.setShortLabel("xyz");

            // The helper to access the feature type.
            CvObjectDao<CvFeatureType> cvObjectDao = DaoProvider.getDaoFactory().getCvObjectDao(CvFeatureType.class);
            // Preset the CvFeature type.
            CvFeatureType featureType = cvObjectDao.getByShortLabel("hotspot");

            // We shouldn't be getting a null object. But.... just in case.
            if (featureType != null) {
                view.setCvFeatureType("hotspot");
            }
        }
        // Reset full name and identification as they are common to both.
        view.setFullName("");
        view.setCvFeatureIdentification(null);

        // Toggle between modes.
        view.toggleEditMode();

        // Update the form and display.
        return mapping.findForward(SUCCESS);
    }
}