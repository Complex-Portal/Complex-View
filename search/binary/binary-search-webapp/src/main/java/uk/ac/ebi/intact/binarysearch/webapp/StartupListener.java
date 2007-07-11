/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.binarysearch.webapp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.binarysearch.webapp.application.OlsBean;
import uk.ac.ebi.intact.binarysearch.webapp.util.WebappUtils;
import uk.ac.ebi.intact.util.ols.OlsUtils;
import uk.ac.ebi.intact.util.ols.Term;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Initializes the applications
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class StartupListener implements ServletContextListener {

    public static final Log log = LogFactory.getLog(StartupListener.class);

    private static final String DO_NOT_USE_OLS_INIT_PARAM = "psidev.DO_NOT_USE_OLS";

    /**
     * Notification that the web application initialization
     * process is starting.
     * All ServletContextListeners are notified of context
     * initialisation before any filter or servlet in the web
     * application is initialized.
     */
    public void contextInitialized(ServletContextEvent sce) {

        ServletContext ctx = sce.getServletContext();

        boolean doNotOls = false;
        String doNotOlsStr = ctx.getInitParameter(DO_NOT_USE_OLS_INIT_PARAM);
        if (doNotOlsStr != null) {
            doNotOls = Boolean.valueOf(doNotOlsStr);
        }

            if (doNotOls) {
                if (log.isWarnEnabled()) log.warn("OLS terms not loaded, as configured in the web.xml");
                    ctx.setAttribute(WebappUtils.INTERACTION_TYPE_TERM, null);
                    ctx.setAttribute(WebappUtils.INTERACTION_TYPES, Collections.EMPTY_LIST);
                    ctx.setAttribute(WebappUtils.DETECTION_METHOD_TERM, null);
                    ctx.setAttribute(WebappUtils.DETECTION_METHODS, Collections.EMPTY_LIST);
            } else {
                try {
                    WebappUtils.loadOlsTerms(ctx);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
    }

    /**
     * Notification that the servlet context is about to be shut down. All servlets
     * have been destroy()ed before any ServletContextListeners are notified of context
     * destruction.
     */
    public void contextDestroyed(ServletContextEvent sce) {
        log.debug("LogFactory.release and destroying application");
        LogFactory.release(Thread.currentThread().getContextClassLoader());
    }
}