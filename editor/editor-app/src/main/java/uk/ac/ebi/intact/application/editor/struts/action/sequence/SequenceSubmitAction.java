/*
Copyright (c) 2002-2005 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.action.sequence;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import uk.ac.ebi.intact.application.editor.struts.action.AbstractSubmitAction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

/**
 * The submitter action for the Sequence editor.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 *
 * @struts.action
 *      path="/seqDispatch"
 *      name="seqForm"
 *      scope="session"
 *      validate="false"
 *
 * @struts.action-forward
 *      name="submit"
 *      path="/do/seq/submit"
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
 *      path="/do/seq/annot/submit"
 *
 * @struts.action-forward
 *      name="xref"
 *      path="/do/seq/xref/submit"
 */
public class SequenceSubmitAction extends AbstractSubmitAction {

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws Exception {
        return super.execute(mapping, form, request, Collections.EMPTY_MAP);
    }
}
