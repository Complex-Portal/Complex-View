/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.action;

import org.apache.struts.action.*;
import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.application.editor.struts.framework.AbstractEditorAction;
import uk.ac.ebi.intact.application.editor.struts.framework.EditorFormI;
import uk.ac.ebi.intact.application.editor.struts.framework.util.AbstractEditViewBean;
import uk.ac.ebi.intact.application.editor.struts.view.AbstractEditBean;
import uk.ac.ebi.intact.application.editor.struts.view.XreferenceBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Dispatcher action which dispatches according to 'dispatch' parameter. This
 * class handles events for edit, save and delete of an Xref.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 *
 * @struts.action
 *      path="/cv/xref/submit"
 *      name="cvForm"
 *      input="edit.layout"
 *      scope="session"
 *      validate="false"
 *
 * @struts.action
 *      path="/exp/xref/submit"
 *      name="expForm"
 *      input="edit.layout"
 *      scope="session"
 *      validate="false"
 *
 * @struts.action
 *      path="/bs/xref/submit"
 *      name="bsForm"
 *      input="edit.layout"
 *      scope="session"
 *      validate="false"
 *
 * @struts.action
 *      path="/int/xref/submit"
 *      name="intForm"
 *      input="edit.layout"
 *      scope="session"
 *      validate="false"
 *
 * @struts.action
 *      path="/feature/xref/submit"
 *      name="featureForm"
 *      input="edit.layout"
 *      scope="session"
 *      validate="false"
 *
 * @struts.action
 *      path="/seq/xref/submit"
 *      name="seqForm"
 *      input="edit.layout"
 *      scope="session"
 *      validate="false"
 */
public class XrefDispatchAction extends AbstractEditorAction {

    /**
     * This method dispatches calls to various methods using the 'dispatch' parameter.
     *
     * @param mapping - The <code>ActionMapping</code> used to select this instance
     * @param form - The optional <code>ActionForm</code> bean for this request (if any)
     * @param request - The HTTP request we are processing
     * @param response - The HTTP response we are creating
     *
     * @return - represents a destination to which the action servlet,
     * <code>ActionServlet</code>, might be directed to perform a RequestDispatcher.forward()
     * or HttpServletResponse.sendRedirect() to, as a result of processing
     * activities of an <code>Action</code> class
     */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws Exception {
        // The editor form.
        EditorFormI editorForm = (EditorFormI) form;

        // The action forward to return
        ActionForward fwd = null;

        if (hasPressedButton(editorForm, request, "xrefs.button.edit")) {
            fwd = edit(mapping, form, request, response);
        }
        else if (hasPressedButton(editorForm, request, "xrefs.button.save")) {
            fwd = save(mapping, form, request, response);
        }
        else {
            // default is delete.
            fwd = delete(mapping, form, request, response);
        }
        // Set anchor if necessary.
        setAnchor(request, editorForm);
        return fwd;
    }

    /**
     * Action for editing the selected xref.
     *
     * @param mapping the <code>ActionMapping</code> used to select this instance
     * @param form the optional <code>ActionForm</code> bean for this request
     * (if any).
     * @param request the HTTP request we are processing
     * @param response the HTTP response we are creating
     * @return failure mapping for any errors in updating the CV object; search
     * mapping if the update is successful and the previous search has only one
     * result; results mapping if the update is successful and the previous
     * search has produced multiple results.
     * @throws Exception for any uncaught errors.
     */
    public ActionForward edit(ActionMapping mapping,
                                ActionForm form,
                                HttpServletRequest request,
                                HttpServletResponse response)
            throws Exception {
        // The editor form.
        EditorFormI editorForm = (EditorFormI) form;

        // The xref we are editing at the moment.
        XreferenceBean xb = editorForm.getSelectedXref();

        // Must save this bean.
        xb.setEditState(AbstractEditBean.SAVE);

        // Back to the edit form.
        return mapping.getInputForward();
    }

    /**
     * Action for deleting the selected xref.
     *
     * @param mapping the <code>ActionMapping</code> used to select this instance
     * @param form the optional <code>ActionForm</code> bean for this request
     * (if any).
     * @param request the HTTP request we are processing
     * @param response the HTTP response we are creating
     * @return failure mapping for any errors in deleting the CV object; search
     * mapping if the delete is successful and the previous search has only one
     * result; results mapping if the delete is successful and the previous
     * search has produced multiple results.
     * @throws Exception for any uncaught errors.
     */
    public ActionForward delete(ActionMapping mapping,
                                ActionForm form,
                                HttpServletRequest request,
                                HttpServletResponse response)
            throws Exception {
        // The editor form.
        EditorFormI editorForm = (EditorFormI) form;

        // The xref we are about to delete.
        XreferenceBean xb = editorForm.getSelectedXref();

        // The current view of the edit session.
        AbstractEditViewBean view = getIntactUser(request).getView();

        // Delete from the view.
        view.delXref(xb);

        // Back to the edit form.
        return mapping.getInputForward();
    }

    /**
     * Action for saving an edited xref.
     *
     * @param mapping the <code>ActionMapping</code> used to select this instance
     * @param form the optional <code>ActionForm</code> bean for this request
     * (if any).
     * @param request the HTTP request we are processing
     * @param response the HTTP response we are creating
     * @return failure mapping for any errors in cancelling the CV object; search
     * mapping if the cancel is successful and the previous search has only one
     * result; results mapping if the cancel is successful and the previous
     * search has produced multiple results.
     * @throws Exception for any uncaught errors.
     */
    public ActionForward save(ActionMapping mapping,
                                ActionForm form,
                                HttpServletRequest request,
                                HttpServletResponse response)
            throws Exception {
        // The editor form.
        EditorFormI editorForm = (EditorFormI) form;

        // The xref we are about to save.
        XreferenceBean xb = editorForm.getSelectedXref();

        // Handler to the EditUser.
        EditUserI user = getIntactUser(request);

        // The current view of the edit session.
        AbstractEditViewBean view = user.getView();

        // Does this bean exist in the current view?
        if (view.xrefExists(xb)) {
            // Mark the bean as error.
            xb.setEditState(AbstractEditBean.ERROR);

            // The errors to display.
            ActionMessages errors = new ActionMessages();
            errors.add("xref.exists", new ActionMessage("error.edit.xref.exists"));
            // Save the errors to display later
            saveErrors(request, errors);

            // Set the anchor
            setAnchor(request, editorForm);

            // Back to the edit form
            return mapping.getInputForward();
        }

        // For Go database, set values from the Go server.
        if (xb.getDatabase().equals("go")) {
            ActionMessages errors = xb.setFromGoServer(user.getGoProxy());
            // Non null error indicates errors.
            if (errors != null) {
                saveErrors(request, errors);
                // Display the errors in the input page.
                return mapping.getInputForward();
            }
        }
        // Save the bean in the view.
        view.saveXref(xb);

        // Back to the view mode again.
        xb.setEditState(AbstractEditBean.VIEW);

        // Back to the edit form.
        return mapping.getInputForward();
    }
}
