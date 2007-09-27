/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.action;

import org.apache.struts.action.*;
import uk.ac.ebi.intact.application.editor.struts.framework.AbstractEditorAction;
import uk.ac.ebi.intact.application.editor.struts.framework.EditorFormI;
import uk.ac.ebi.intact.application.editor.struts.framework.util.AbstractEditViewBean;
import uk.ac.ebi.intact.application.editor.struts.view.AbstractEditBean;
import uk.ac.ebi.intact.application.editor.struts.view.CommentBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Dispatcher action which dispatches according to the button title. This
 * class handles events for edit, save and delete of an annotation.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 *
 * @struts.action
 *      path="/cv/annot/submit"
 *      name="cvForm"
 *      input="edit.layout"
 *      scope="session"
 *      validate="false"
 *
 * @struts.action
 *      path="/exp/annot/submit"
 *      name="expForm"
 *      input="edit.layout"
 *      scope="session"
 *      validate="false"
 *
 * @struts.action
 *      path="/bs/annot/submit"
 *      name="bsForm"
 *      input="edit.layout"
 *      scope="session"
 *      validate="false"
 *
 * @struts.action
 *      path="/int/annot/submit"
 *      name="intForm"
 *      input="edit.layout"
 *      scope="session"
 *      validate="false"
 *
 * @struts.action
 *      path="/seq/annot/submit"
 *      name="seqForm"
 *      input="edit.layout"
 *      scope="session"
 *      validate="false"
 */
public class AnnotationDispatchAction extends AbstractEditorAction {

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

        if (hasPressedButton(editorForm, request, "annotations.button.edit")) {
            fwd =  edit(mapping, form, request, response);
        }
        else if (hasPressedButton(editorForm, request, "annotations.button.save")) {
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
     * Action for editing the selected annotation.
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

        // The annotation we are editing at the moment.
        CommentBean cb = editorForm.getSelectedAnnotation();

        // Must save this bean.
        cb.setEditState(AbstractEditBean.SAVE);

        // Back to the edit form.
        return mapping.getInputForward();
    }

    /**
     * Action for deleting the selected annotation.
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
        // The edit form.
        EditorFormI editorForm = (EditorFormI) form;

        // The annotation we are about to delete.
        CommentBean cb = editorForm.getSelectedAnnotation();

        // The current view of the edit session.
        AbstractEditViewBean view = getIntactUser(request).getView();
        view.delAnnotation(cb);

        // Back to the edit form.
        return mapping.getInputForward();
    }

    /**
     * Action for saving an edited annotation.
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
        // The edit form.
        EditorFormI editorForm = (EditorFormI) form;

        // The annotation we are editing at the moment.
        CommentBean cb = editorForm.getSelectedAnnotation();

        // The current view of the edit session.
        AbstractEditViewBean view = getIntactUser(request).getView();

        // Does this bean exist in the current view?
        if (view.annotationExists(cb)) {
            // Mark the bean as error.
            cb.setEditState(AbstractEditBean.ERROR);

            ActionMessages errors = new ActionMessages();
            errors.add("annotation.exists",
                    new ActionMessage("error.edit.annotation.exists"));
            // Save the errors to display later
            saveErrors(request, errors);

            // Set the anchor
            setAnchor(request, editorForm);

            // Back to the edit form
            return mapping.getInputForward();
        }

        // Save the annotation in the view.
        view.saveAnnotation(cb);

        // Back to the view mode.
        cb.setEditState(AbstractEditBean.VIEW);

        // Back to the edit form.
        return mapping.getInputForward();
    }
}
