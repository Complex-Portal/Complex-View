/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.editor.struts.action.sm;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

import uk.ac.ebi.intact.application.editor.struts.action.AbstractSubmitAction;

/**
 * TODO comment it.
 *
 * @author Catherine Leroy (cleroy@ebi.ac.uk)
 * @version $Id$
 */

/**
 * @struts.action
 *      path="/smDispatch"
 *      name="cvForm"
 *      scope="session"
 *      validate="false"
 *
 * @struts.action-forward
 *      name="submit"
 *      path="/do/sm/submit"
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
 *      path="/do/sm/annot/submit"
 *
 * @struts.action-forward
 *      name="xref"
 *      path="/do/sm/xref/submit"
 */

public class SmSubmitAction extends AbstractSubmitAction {

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws Exception {
        return super.execute(mapping, form, request, Collections.EMPTY_MAP);
    }
}
