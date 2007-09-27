/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.action;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.application.editor.struts.framework.AbstractEditorAction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This action is responsible for choosing the editor. The editor choice
 * is dependent on the selected topic.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 *
 * @struts.action
 *      path="/choose"
 *      validate="false"
 *
 * @struts.action-forward
 *      name="cv"
 *      path="/do/cv/fill/form"
 *
 * @struts.action-forward
 *      name="exp"
 *      path="/do/exp/fill/form"
 *
 * @struts.action-forward
 *      name="bs"
 *      path="/do/bs/fill/form"
 *
 * @struts.action-forward
 *      name="int"
 *      path="/do/int/fill/form"
 *
 * @struts.action-forward
 *      name="seq"
 *      path="/do/seq/fill/form"
 *
 * @struts.action-forward
 *      name="sm"
 *      path="/do/sm/fill/form"
 */
public class EditorChoiceAction extends AbstractEditorAction {

    /**
     * Process the specified HTTP request, and create the corresponding
     * HTTP response (or forward to another web component that will create
     * it). Return an ActionForward instance describing where and how
     * control should be forwarded, or null if the response has
     * already been completed.
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
        // Handler to the Intact User.
        EditUserI user = getIntactUser(request);

        // The topic to choose where to go.
        String topic = user.getSelectedTopic();
        if (topic.equals("BioSource")) {
            return mapping.findForward("bs");
        }
        if (topic.equals("Experiment")) {
            return mapping.findForward("exp");
        }
        if (topic.equals("Interaction")) {
            return mapping.findForward("int");
        }
        if (topic.equals("Protein") || topic.equals("NucleicAcid")) {
            return mapping.findForward("seq");
        }
        if(topic.equals("SmallMolecule")) {
            return mapping.findForward("sm");
        }
        // Must have selected a CV object to edit.
        return mapping.findForward("cv");
    }
}
