/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.application.editor.struts.taglibs;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessages;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * That class allows to restore in the request scope any errors saved
 * in the session scope via the tag <code>&lt;intact:saveErrors&gt;</code>.
 *
 * @see SaveErrorsTag
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
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

        ActionErrors errors = (ActionErrors) session.getAttribute (Globals.ERROR_KEY);
        if ( null != errors ) {
            pageContext.setAttribute (Globals.ERROR_KEY, errors);
            session.removeAttribute (Globals.ERROR_KEY);
        }

        ActionMessages messages = (ActionMessages) session.getAttribute (Globals.MESSAGE_KEY);
        if ( null != messages ) {
            pageContext.setAttribute (Globals.MESSAGE_KEY, messages);
            session.removeAttribute (Globals.MESSAGE_KEY);
        }

        return EVAL_PAGE; // the rest of the calling JSP is evaluated
    }
}