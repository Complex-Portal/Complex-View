/*
Copyright (c) 2002-2005 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.action.feature;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import uk.ac.ebi.intact.application.editor.struts.action.AbstractSubmitAction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * The submitter action for the Feature editor.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 *
 * @struts.action
 *      path="/featureDispatch"
 *      name="featureForm"
 *      scope="session"
 *      validate="false"
 *
 * @struts.action-forward
 *      name="submit"
 *      path="/do/feature/submit"
 *
 * @struts.action-forward
 *      name="cancel"
 *      path="/do/feature/cancel"
 *
 * @struts.action-forward
 *      name="xref"
 *      path="/do/feature/xref/submit"
 *
 * @struts.action-forward
 *      name="feature.mutation"
 *      path="/do/feature/mutation"
 *
 * @struts.action-forward
 *      name="feature.undetermined.clone"
 *      path="/do/feature/undetermined/clone"
 *
 * @struts.action-forward
 *      name="feature.range"
 *      path="/do/feature/range"
 *
 * @struts.action-forward
 *      name="feature.range.new"
 *      path="/do/feature/range/new"
 */
public class FeatureSubmitAction extends AbstractSubmitAction {

    private static final Map ourButtonToAction = new HashMap();

    static {
        // Resource bundle to access the message resources to set keys.
        ResourceBundle rb = ResourceBundle.getBundle(
                "uk.ac.ebi.intact.application.editor.MessageResources");

        ourButtonToAction.put(rb.getString("feature.mutation.toggle.button"),
                "feature.mutation");
        ourButtonToAction.put(rb.getString("feature.undetermined.clone.button"),
                "feature.undetermined.clone");
        ourButtonToAction.put(rb.getString("feature.range.button.add"),
                "feature.range.new");
        ourButtonToAction.put(rb.getString("feature.range.button.edit"),
                "feature.range");
        ourButtonToAction.put(rb.getString("feature.range.button.save"),
                "feature.range");
        ourButtonToAction.put(rb.getString("feature.range.button.delete"),
                "feature.range");
    }

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws Exception {
        return super.execute(mapping, form, request, ourButtonToAction);
    }
}
