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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * That class allows to clear from the request scope (in pageRequest)
 * any errors collected by struts.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */

public class ClearErrorsTag extends TagSupport {

    /**
     * Skip the body content.
     */
    public int doStartTag() throws JspTagException {
        return SKIP_BODY;
    }

    /**
     * Clear any Struts errors stored in the pageContext.
     */
    public int doEndTag() throws JspException {

        ActionErrors errors = (ActionErrors) pageContext.findAttribute(Globals.ERROR_KEY);
        if ( null != errors ) {
            errors.clear();
            pageContext.removeAttribute(Globals.ERROR_KEY);
        }

        ActionMessages messages = (ActionMessages) pageContext.findAttribute (Globals.MESSAGE_KEY);
        if ( null != messages ) {
            messages.clear();
            pageContext.removeAttribute(Globals.MESSAGE_KEY);
        }

        return EVAL_PAGE;
    }

}
