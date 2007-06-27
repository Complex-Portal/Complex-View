/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.hierarchview.struts.taglibs;

import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.context.IntactContext;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * That class allow to initialize properly the HTTPSession object
 * with what will be neededlater by the user of the web application.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */

public class DisplayHttpContentTag extends TagSupport {

    /**
     * Skip the body content.
     */
    public int doStartTag() throws JspTagException {
        return SKIP_BODY;
    } // doStartTag


    /**
     * Called when the JSP encounters the end of a tag. This will create the
     * option list.
     */
    public int doEndTag() throws JspException {
        HttpSession session = pageContext.getSession();

        try {
            IntactUserI user = (IntactUserI) IntactContext.getCurrentInstance().getSession().getAttribute (Constants.USER_KEY);
            String urlStr = user.getSourceURL();

            if (urlStr == null) {
                // nothing to display
                return EVAL_PAGE;
            }

            URL url = null;
            try {
                url = new URL (urlStr);
            } catch (MalformedURLException me) {
                String decodedUrl = URLDecoder.decode (urlStr, "UTF-8");
                pageContext.getOut().write ("The source is malformed : <a href=\"" + decodedUrl +
                                             "\" target=\"_blank\">" + decodedUrl + "</a>" );
                return EVAL_PAGE;
            }

            // Retrieve the content of the URL
            StringBuffer httpContent = new StringBuffer();
            httpContent.append ("<!-- URL : " + urlStr + "-->");
            String tmpLine;
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                while ((tmpLine = reader.readLine()) != null) {
                    httpContent.append(tmpLine);
                }
                reader.close();
            } catch (IOException ioe) {
                 user.resetSourceURL();
                String decodedUrl = URLDecoder.decode (urlStr, "UTF-8");
                 pageContext.getOut().write ("Unable to display the source at : <a href=\"" + decodedUrl +
                                             "\" target=\"_blank\">" + decodedUrl + "</a>" );
                 return EVAL_PAGE;
            }

            // return the content to the browser
            pageContext.getOut().write (httpContent.toString());

        } catch (Exception e) {
            e.printStackTrace();
            throw new JspException ("Error when trying to get HTTP content");
        }

       return EVAL_PAGE;  // the rest of the calling JSP is evaluated
    } // doEndTag
}