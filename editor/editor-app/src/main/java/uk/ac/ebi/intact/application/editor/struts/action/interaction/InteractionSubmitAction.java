/*
Copyright (c) 2002-2005 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.action.interaction;

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
 * The submitter action for the Interaction editor.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 *
 * @struts.action
 *      path="/intDispatch"
 *      name="intForm"
 *      scope="session"
 *      validate="false"
 *
 * @struts.action-forward
 *      name="submit"
 *      path="/do/int/submit"
 *
 * @struts.action-forward
 *      name="cancel"
 *      path="/do/int/cancel"
 *
 * @struts.action-forward
 *      name="delete"
 *      path="/do/int/delete"
 *
 * @struts.action-forward
 *      name="annotation"
 *      path="/do/int/annot/submit"
 *
 * @struts.action-forward
 *      name="xref"
 *      path="/do/int/xref/submit"
 *
 * @struts.action-forward
 *      name="int.exp.del"
 *      path="/do/int/exp/del"
 *
 * @struts.action-forward
 *      name="int.exp.hold"
 *      path="/do/int/exp/hold"
 *
 * @struts.action-forward
 *      name="int.exp.search"
 *      path="/do/int/exp/search"
 *
 * @struts.action-forward
 *      name="int.prot"
 *      path="/do/int/prot"
 *
 * @struts.action-forward
 *      name="int.prot.search"
 *      path="/do/int/prot/search"
 *
 * @struts.action-forward
 *      name="int.feature"
 *      path="/do/int/feature"
 *
 * @struts.action-forward
 *      name="int.feature.link"
 *      path="/do/int/feature/link"
 */
public class InteractionSubmitAction extends AbstractSubmitAction {

    private static final Map ourButtonToAction = new HashMap();

    static {
        // Resource bundle to access the message resources to set keys.
        ResourceBundle rb = ResourceBundle.getBundle(
                "uk.ac.ebi.intact.application.editor.MessageResources");

        ourButtonToAction.put(rb.getString("int.exp.button.del"), "int.exp.del");
        ourButtonToAction.put(rb.getString("int.exp.button.add"), "int.exp.hold");
        ourButtonToAction.put(rb.getString("int.exp.button.hide"), "int.exp.hold");
        ourButtonToAction.put(rb.getString("int.exp.button.recent"), "int.exp.search");
        ourButtonToAction.put(rb.getString("int.exp.button.search"), "int.exp.search");

        // Actions related to edit/save/delete of proteins.
        ourButtonToAction.put(rb.getString("int.interactors.button.edit"), "int.prot");
        ourButtonToAction.put(rb.getString("int.interactors.button.save"), "int.prot");
        ourButtonToAction.put(rb.getString("int.interactors.button.delete"), "int.prot");

        // Related to add/edit feature.
        ourButtonToAction.put(rb.getString("int.proteins.button.feature.add"), "int.feature");
        ourButtonToAction.put(rb.getString("int.proteins.button.feature.edit"), "int.feature");

        // Feature delete/link/unlink.
        ourButtonToAction.put(rb.getString("int.proteins.button.feature.delete"), "int.feature.link");
        ourButtonToAction.put(rb.getString("int.proteins.button.feature.link"), "int.feature.link");
        ourButtonToAction.put(rb.getString("int.proteins.button.feature.unlink"), "int.feature.link");

        // Actions related protein search.
        ourButtonToAction.put(rb.getString("int.interactors.button.search"), "int.interact.search");
    }

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws Exception {
        return super.execute(mapping, form, request, ourButtonToAction);
    }
}