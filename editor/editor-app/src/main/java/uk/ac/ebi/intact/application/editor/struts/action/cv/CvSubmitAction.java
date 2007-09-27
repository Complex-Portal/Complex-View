/*
Copyright (c) 2002-2005 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.action.cv;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import uk.ac.ebi.intact.application.editor.struts.action.AbstractSubmitAction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

/**
 * The submitter action for the CV editor.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 *
 * @struts.action
 *      path="/cvDispatch"
 *      name="cvForm"
 *      scope="session"
 *      validate="false"
 *
 * @struts.action-forward
 *      name="submit"
 *      path="/do/cv/submit"
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
 *      path="/do/cv/annot/submit"
 *
 * @struts.action-forward
 *      name="xref"
 *      path="/do/cv/xref/submit"
 */
public class CvSubmitAction extends AbstractSubmitAction {

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws Exception {
        return super.execute(mapping, form, request, Collections.EMPTY_MAP);
    }
}
