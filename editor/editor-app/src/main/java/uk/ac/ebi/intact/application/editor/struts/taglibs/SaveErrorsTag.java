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

        ActionMessages messages = null;

        if (pageContext.findAttribute (Globals.MESSAGE_KEY) instanceof ActionMessages) {
            messages = (ActionMessages) pageContext.findAttribute (Globals.MESSAGE_KEY);
        }


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
