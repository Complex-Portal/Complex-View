/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.action;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import uk.ac.ebi.intact.application.editor.struts.framework.AbstractEditorAction;
import uk.ac.ebi.intact.application.editor.struts.framework.EditorFormI;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Dispatcher action which dispatches to various submit actions.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public abstract class AbstractSubmitAction extends AbstractEditorAction {

    private static final Map ourButtonToAction = new HashMap();

    static {
        // Resource bundle to access the message resources to set keys.
        ResourceBundle rb = ResourceBundle.getBundle(
                "uk.ac.ebi.intact.application.editor.MessageResources");

        // Actions Common to all the editors.
        ourButtonToAction.put(rb.getString("button.submit"), "submit");
        ourButtonToAction.put(rb.getString("button.save.continue"), "submit");
        ourButtonToAction.put(rb.getString("button.clone"), "submit");

        ourButtonToAction.put(rb.getString("button.cancel"), "cancel");
        ourButtonToAction.put(rb.getString("button.delete"), "delete");

        // Actions related to annotations.
        ourButtonToAction.put(rb.getString("annotations.button.add"), "submit");
        ourButtonToAction.put(rb.getString("annotations.button.edit"), "annotation");
        ourButtonToAction.put(rb.getString("annotations.button.save"), "annotation");
        ourButtonToAction.put(rb.getString("annotations.button.delete"), "annotation");

        // Actions related to xrefs.
        ourButtonToAction.put(rb.getString("xrefs.button.add"), "submit");
        ourButtonToAction.put(rb.getString("xrefs.button.edit"), "xref");
        ourButtonToAction.put(rb.getString("xrefs.button.save"), "xref");
        ourButtonToAction.put(rb.getString("xrefs.button.delete"), "xref");

        ourButtonToAction.put(rb.getString("exp.button.accept"),"submit");
        ourButtonToAction.put(rb.getString("exp.button.review"),"submit");
    }

    /**
     * Action for submitting the edit form.
     * @param mapping the <code>ActionMapping</code> used to select this instance
     * @param form the optional <code>ActionForm</code> bean for this request
     * (if any).
     * @param request the HTTP request we are processing
     * @param map the alternative map to search if the dispatch variable is not
     * found in the primary map.
     * @return failure mapping for any errors in updating the CV object; search
     * mapping if the update is successful and the previous search has only one
     * result; results mapping if the update is successful and the previous
     * search has produced multiple results.
     * @throws Exception for any uncaught errors.
     */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 Map map)
            throws Exception {
//        LOGGER.debug("At the beginning of AbstractSubmitAction");
//        for (Enumeration e = request.getParameterNames(); e.hasMoreElements();) {
//            String para = (String) e.nextElement();
//            LOGGER.debug("parameter: " + para + " - " + request.getParameter(para));
//        }
        // Cast the form.
        EditorFormI editorForm = (EditorFormI) form;
        //
        getIntactUser(request).getView().copyPropertiesFrom(editorForm);
        // The dispatch value holds the button label.
        String dispatch = editorForm.getDispatch();
        LOGGER.debug("Dispatch received " + dispatch);

        ActionForward forward = null;

        // The action path from the map.
        String path = (String) ourButtonToAction.get(dispatch);
        if (path != null) {
            forward = mapping.findForward(path);
        }
        else {
            // Use the supplied map.
            path = (String) map.get(dispatch);
            if (path != null) {
                forward = mapping.findForward(path);
            }
        }
        return forward;
    }
}
