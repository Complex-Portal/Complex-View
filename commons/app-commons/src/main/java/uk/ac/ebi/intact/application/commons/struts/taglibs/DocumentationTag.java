/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.commons.struts.taglibs;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspException;
import java.io.IOException;

/**
 * Allows to display a javascript link pointing on the intact documentation directly on the right section.
 * This tag is callin a JSP page which is supposed to display the right section
 * thanks to the <b>section</b> parameter.<br>
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class DocumentationTag extends TagSupport {

    public static final String SESSION_KEY = "INTACT_DOCUMENTATION_PRESENT_TESTED";

    public static final int INFORMATIONAL = 1;
    public static final int SUCCESSFUL    = 2;
    public static final int REDIRECTION   = 3;
    public static final int ERROR         = 4;
    public static final int SERVER_ERROR  = 5;

    private static final String DEFAULT_TITLE = "<sup><b><font color=\"red\">?</font></b><sup></a>";

    private static final String PAGE      = "/intact/displayDoc.jsp";
    private static final String URL_BEGIN = "<a name=\"#\" onClick=\"w=window.open('";
    private static final String URL_MID   = PAGE + "?section=";
    private static final String URL_END   = "', 'helpwindow', " +
                                            "'width=800,height=500,toolbar=no,directories=no,menu bar=no,scrollbars=yes,resizable=yes');"+
                                            "w.focus();\">";

    /*
     * Tag attribute:
     * The name of the documentation section the user want to reach.
     */
    private String section;

    /**
     * The title attribute; defaults to the DEFAULT_TITLE if none
     * specified.
     */
    private String title = DEFAULT_TITLE;

    // Static methods

    /**
     * Returns HTML code snippet for use in non JSP pages.
     * @param docURL the URL path to the URL
     * @param section the section to go to; could be null if there is no
     * section associated with it.
     * @param title the title to display as the link. For e.g., [?]
     * @return the HTML constructed from parameters; this value could be
     * displayed in a HTML page directly.
     */
    public static String getHtmlVersion(String docURL, String section, String title) {
        // The buffer to construct the string to return.
        StringBuffer sb = new StringBuffer (256);

        sb.append (URL_BEGIN);
        sb.append(docURL);
        sb.append ("?section=");
        if (section != null) {
            sb.append (section);
        }
        sb.append (URL_END);
        sb.append(title);
        return sb.toString();
    }

    /**
     * The path to the JSP documentation page.
     * @return the relative path (from root of the server) of the JSP documentation page.
     */
    public static String getJspPath() {
        return PAGE;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    /**
     * Skip the body content.
     */
    public int doStartTag() throws JspTagException {
        return SKIP_BODY;
    }


    /**
     * Display the link to the documentation page with eventually the section.
     */
    public int doEndTag() throws JspException {

        StringBuffer sb = new StringBuffer (256);

        sb.append (URL_BEGIN);

        // Assumes that the intact application is on the same server
        // add current server path

        sb.append (URL_MID);
        if (section != null) sb.append (section);
        sb.append (URL_END);
        sb.append(this.title);

        try {
            pageContext.getOut().write (sb.toString());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return EVAL_PAGE; // the rest of the calling JSP is evaluated
    }

    public void release () {}
}
