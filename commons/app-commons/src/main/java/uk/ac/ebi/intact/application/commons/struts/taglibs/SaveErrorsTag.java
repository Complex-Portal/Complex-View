/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.commons.struts.taglibs;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.Globals;

/**
 * That class allows to save in the session scope any ActionErrors
 * available in the request scope (in pageRequest)
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */

public class SaveErrorsTag extends TagSupport {

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

        ActionErrors errors = (ActionErrors) pageContext.findAttribute (Globals.ERROR_KEY);
        ActionMessages messages = (ActionMessages) pageContext.findAttribute (Globals.MESSAGE_KEY);
        HttpSession session = pageContext.getSession();

        if ( null != errors ) {
            session.setAttribute(Globals.ERROR_KEY, errors);
        }

        if ( null != messages ) {
            session.setAttribute(Globals.MESSAGE_KEY, messages);
        }

        return EVAL_PAGE; // the rest of the calling JSP is evaluated
    }
}