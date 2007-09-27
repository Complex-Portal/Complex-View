/*
Copyright (c) 2002-2005 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.action.biosrc;

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
 *      path="/bsDispatch"
 *      name="bsForm"
 *      scope="session"
 *      validate="false"
 *
 * @struts.action-forward
 *      name="submit"
 *      path="/do/bs/submit"
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
 *      path="/do/bs/annot/submit"
 *
 * @struts.action-forward
 *      name="xref"
 *      path="/do/bs/xref/submit"
 *
 * @struts.action-forward
 *      name="taxid"
 *      path="/do/bs/taxid"
 */
public class BioSourceSubmitAction extends AbstractSubmitAction {

    private static final Map ourButtonToAction = new HashMap();

    static {
        // Resource bundle to access the message resources to set keys.
        ResourceBundle rb = ResourceBundle.getBundle(
                "uk.ac.ebi.intact.application.editor.MessageResources");

        ourButtonToAction.put(rb.getString("biosource.button.taxid"), "taxid");
    }

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws Exception {
        return super.execute(mapping, form, request, ourButtonToAction);
    }
}
