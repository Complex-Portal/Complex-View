/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.hierarchview.struts.taglibs;

import org.apache.log4j.Logger;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.context.IntactContext;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

/**
 * That tag allows to restore the eventual user context of the user
 * from a cookie which has been created and updated by <code>SaveContextInCookieTag</code>.
 *
 * @see SaveContextInCookieTag
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */

public class RestoreContextFromCookieTag extends TagSupport {

    static Logger logger = Logger.getLogger (Constants.LOGGER_NAME);

    /**
     * Skip the body content.
     */
    public int doStartTag() throws JspTagException {
        return SKIP_BODY;
    }

    /**
     * Restore the context if needed.
     */
    public int doEndTag() throws JspException {

        logger.warn("Try to restore the last user context");

        final HttpSession session = pageContext.getSession();
        if (session == null) {
            logger.warn("Could not create a new session to restore the setting");
            return EVAL_PAGE;
        }

        final IntactUserI user = (IntactUserI) IntactContext.getCurrentInstance().getSession().getAttribute (Constants.USER_KEY);
        // no point to restore the environment if the user already exists
        if (user != null) {
            logger.info("The user exists, exit the restore procedure");
            return EVAL_PAGE;
        }

        Cookie cookies[] = ((HttpServletRequest) pageContext.getRequest()).getCookies();
        if (cookies == null) return EVAL_PAGE;


        String method = null;
        String depth  = null;
        String query  = null;

        // firstly cache the cookie content to have direct access instead of traversing the collection
        HashMap cookieCache = new HashMap (cookies.length);
        int i;
        for (i = 0; i < cookies.length; i++) {
            cookieCache.put (cookies[i].getName(), cookies[i].getValue());
        }

        depth  = (String) cookieCache.get ("DEPTH");
        method = (String) cookieCache.get ("METHOD");
        query  = (String) cookieCache.get ("QUERY");

        final String contextPath = ((HttpServletRequest) pageContext.getRequest()).getContextPath();
        String url = null;
        if (query != null && method != null && depth != null) {
            try {
                query = URLDecoder.decode (query, "UTF-8");
            } catch (UnsupportedEncodingException uee) {
                logger.error ("Unsupported encoding system");
                return EVAL_PAGE;
            }

            url = contextPath + "/display.jsp?AC="+ query +"&method="+ method +"&depth="+ depth;
        } else {
            // simply display the current page
            return EVAL_PAGE;
        }


        try {
            // save the URL to forward to
            pageContext.getRequest().setAttribute ("restoreUrl", url);
            session.setAttribute ("restoreUrl", url);

            String forwardUrl = null;
            if (url == null) {
                forwardUrl = contextPath;
                logger.info ("No query registered in the cookie");
            } else {
                // redirect to the forward page (with waiting message)
                forwardUrl = contextPath + "/pages/restoreContext.jsp";
            }

            logger.info("forward to: " + forwardUrl);

            ((HttpServletResponse) pageContext.getResponse()).sendRedirect(forwardUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return EVAL_PAGE;
    }
}