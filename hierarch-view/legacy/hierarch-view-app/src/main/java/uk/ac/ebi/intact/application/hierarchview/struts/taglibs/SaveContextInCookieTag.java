/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.hierarchview.struts.taglibs;

import org.apache.log4j.Logger;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.graph.InteractionNetwork;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.searchengine.CriteriaBean;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * <p>
 * That tag allows to save in a cookie (stored in the client side)
 * all needed data to restore later the user context.
 * It stores the selected protein, the method and the current depth
 * of the graph.
 * </p>
 * <p>
 * Be aware that the cookie specification allows to store a maximum of 20 cookies per session,
 * the length of a cookie including name and content shouldn't exceed 4Kbytes.
 * cf. http://wp.netscape.com/newsref/std/cookie_spec.html
 * </p>
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class SaveContextInCookieTag extends TagSupport {

    static Logger logger = Logger.getLogger (Constants.LOGGER_NAME);

    private final static int KEEP_UNTIL_BROWSER_IS_CLOSED = -1;

    /**
     * Create/update a persistent cookie
     *
     * @param name name the name of the cookie to create
     * @param value the associated value
     */
    private void saveCookie (String applicationPath, String name, String value) {
        Cookie cookie = new Cookie (name, value);
        cookie.setPath (applicationPath);
        cookie.setMaxAge(KEEP_UNTIL_BROWSER_IS_CLOSED);
        ((HttpServletResponse) pageContext.getResponse()).addCookie(cookie);

        logger.info (name + " saved in the cookie ("+ applicationPath +") with value: " + value);
    }

    /**
     * For Debugging Purpose.
     * display the content of the current user cookie.
     */
    public void displayCookieContent () {
        Cookie cookies[] = ((HttpServletRequest) pageContext.getRequest()).getCookies();
        if (cookies == null) {
            logger.info("The cookie contains no data.");
            return;
        }

        Cookie aCookie = null;
        logger.info("The cookie contains :");
        for (int i = 0; i < cookies.length; i++) {
            aCookie = cookies[i];
            logger.info ( aCookie.getName() + " = " + aCookie.getValue() );
        }
    }

    /**
     * Skip the body content.
     */
    public int doStartTag() throws JspTagException {
        return SKIP_BODY;
    }

    /**
     * Save the hierarchview search context in a cookie
     */
    public int doEndTag() throws JspException {

        HttpSession session = pageContext.getSession();
        if (session == null) return EVAL_PAGE;

        IntactUserI user = (IntactUserI) IntactContext.getCurrentInstance().getSession().getAttribute (Constants.USER_KEY);
        if (user == null) return EVAL_PAGE;

        InteractionNetwork network = user.getInteractionNetwork();
        if (network == null) return EVAL_PAGE;

        displayCookieContent ();

        // get the application path
        HttpServletRequest request = ((HttpServletRequest) pageContext.getRequest());
        String applicationPath = request.getContextPath();

        // save our context
        ArrayList criterias = network.getCriteria();
        int max = criterias.size();
        StringBuffer sb = new StringBuffer( 128 );
        for (int i = 0; i < max; i++) {
            sb.append ( ( (CriteriaBean) criterias.get(i) ).getQuery() ).append(',');
        }

        // save queries without the last comma
        String queryString = null;
        if( sb.length() > 0 ) {
            try {
                queryString = URLEncoder.encode( sb.substring( 0, sb.length() - 1 ), "UTF-8" );
            } catch (UnsupportedEncodingException uee) {
                logger.error ("Unsupported encoding system");
                return EVAL_PAGE;
            }
        }

        saveCookie (applicationPath, "QUERY",  queryString);
        saveCookie (applicationPath, "DEPTH",  ""+user.getCurrentDepth());
        saveCookie (applicationPath, "METHOD", user.getMethodLabel());

        return EVAL_PAGE;
    }
}