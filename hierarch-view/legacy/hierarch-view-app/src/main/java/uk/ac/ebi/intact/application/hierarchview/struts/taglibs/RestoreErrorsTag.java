/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.hierarchview.struts.taglibs;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionMessages;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * That class allows to restore in the request scope any errors saved
 * in the session scope via the tag <code>&lt;intact:saveErrors&gt;</code>.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @see SaveErrorsTag
 */

public class RestoreErrorsTag extends TagSupport {

    /**
     * Skip the body content.
     */
    public int doStartTag() throws JspTagException {
        return SKIP_BODY;
    }


    /**
     * In case some errors (ActionErrors) are discovered, let's save them in the
     * session for later displaying. The page in charge of the displaying will
     * have to check it any errors are stored and need to be displayed.
     *
     * @throws JspException
     */
    public int doEndTag() throws JspException {

        HttpSession session = pageContext.getSession();

        ActionMessages errors = ( ActionMessages ) session.getAttribute( Globals.ERROR_KEY );
        if ( null != errors ) {
            pageContext.setAttribute( Globals.ERROR_KEY, errors );
            session.removeAttribute( Globals.ERROR_KEY );
        }

        ActionMessages messages = ( ActionMessages ) session.getAttribute( Globals.MESSAGE_KEY );
        if ( null != messages ) {
            pageContext.setAttribute( Globals.MESSAGE_KEY, messages );
            session.removeAttribute( Globals.MESSAGE_KEY );
        }

        return EVAL_PAGE; // the rest of the calling JSP is evaluated
    }
}