/*
Copyright (c) 2002-2005 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.action.experiment;

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
 * The submitter action for the CV editor.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 *
 * @struts.action
 *      path="/expDispatch"
 *      name="expForm"
 *      scope="session"
 *      validate="false"
 *
 * @struts.action-forward
 *      name="submit"
 *      path="/do/exp/submit"
 *
 * @struts.action-forward
 *      name="cancel"
 *      path="/do/cancel"
 *
 * @struts.action-forward
 *      name="delete"
 *      path="/do/delete"
 *
 * @struts.action-forward
 *      name="annotation"
 *      path="/do/exp/annot/submit"
 *
 * @struts.action-forward
 *      name="xref"
 *      path="/do/exp/xref/submit"
 *
 * @struts.action-forward
 *      name="interaction"
 *      path="/do/interaction"
 *
 * @struts.action-forward
 *      name="exp.autocomp"
 *      path="/do/exp/autocomp"
 *
 * @struts.action-forward
 *      name="exp.int.hold"
 *      path="/do/exp/int/hold"
 *
 * @struts.action-forward
 *      name="exp.int.search"
 *      path="/do/exp/int/search"
 */
public class ExperimentSubmitAction extends AbstractSubmitAction {

    private static final Map ourButtonToAction = new HashMap();

    static {
        // Resource bundle to access the message resources to set keys.
        ResourceBundle rb = ResourceBundle.getBundle(
                "uk.ac.ebi.intact.application.editor.MessageResources");

        ourButtonToAction.put(rb.getString("exp.int.button.edit"), "interaction");
        ourButtonToAction.put(rb.getString("exp.int.button.del"), "interaction");
        ourButtonToAction.put(rb.getString("exp.int.button.add"), "exp.int.hold");
        ourButtonToAction.put(rb.getString("exp.int.button.hide"), "exp.int.hold");
        ourButtonToAction.put(rb.getString("exp.int.button.recent"), "exp.int.search");
        ourButtonToAction.put(rb.getString("exp.int.button.search"),"exp.int.search");
        ourButtonToAction.put(rb.getString("exp.button.autocompletion"),"exp.autocomp");

    }

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws Exception {
        return super.execute(mapping, form, request, ourButtonToAction);
    }
}
