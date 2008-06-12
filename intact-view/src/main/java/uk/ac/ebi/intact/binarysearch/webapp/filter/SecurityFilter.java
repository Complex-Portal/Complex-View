/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.binarysearch.webapp.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.binarysearch.webapp.view.AdminBean;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Filter for the restricted (admin) area, that redirects to the login JSF page if the current
 * user is not identified
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class SecurityFilter implements Filter {

    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog(SecurityFilter.class);

    private final static String FILTER_APPLIED = SecurityFilter.class + ".security_filter_applied";

    public SecurityFilter() {
    }


    public void init(FilterConfig filterConfig) {

    }


    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws java.io.IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession();

        String requestedPage = req.getRequestURL().toString();

        if (log.isDebugEnabled()) log.debug("Checking request: " + requestedPage);

        if (request.getAttribute(FILTER_APPLIED) == null && requestedPage != null) {

            //check if the page requested is the login page or register page
            if ((!requestedPage.endsWith("login.xhtml"))) {
                //Requested page is not login.jsp therefore check for user logged in..
                //set the FILTER_APPLIED attribute to true
                request.setAttribute(FILTER_APPLIED, Boolean.TRUE);
                //Check that the session bean is not null and get the session bean property username.

                boolean authenticated = session.getAttribute(AdminBean.USER_AUTHENTICATED_TOKEN) != null;

                if (!authenticated) {
                    res.sendRedirect(req.getContextPath() + "/faces/admin/login.xhtml");
                    return;
                } else {
                    if (!requestedPage.endsWith("index_management.xhtml")) {
                        res.sendRedirect(req.getContextPath() + "/faces/admin/index_management.xhtml");
                        return;
                    }
                }
            }
        }

        //deliver request to next filter
        chain.doFilter(request, response);

    }

    public void destroy() {

    }
}