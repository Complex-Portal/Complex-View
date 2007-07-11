/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.binarysearch.webapp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.binarysearch.webapp.application.OlsBean;
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

    public static final String INTERACTION_TYPE_TERM = ServletContextListener.class + ".INTERACTION_TYPE_TERM";
    public static final String INTERACTION_TYPES = ServletContextListener.class + ".INTERACTION_TYPES";
    public static final String DETECTION_METHOD_TERM = ServletContextListener.class + ".DETECTION_METHOD_TERM";
    public static final String DETECTION_METHODS = ServletContextListener.class + ".DETECTION_METHODS";

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
                    ctx.setAttribute(INTERACTION_TYPE_TERM, null);
                    ctx.setAttribute(INTERACTION_TYPES, Collections.EMPTY_LIST);
                    ctx.setAttribute(DETECTION_METHOD_TERM, null);
                    ctx.setAttribute(DETECTION_METHODS, Collections.EMPTY_LIST);
            } else {
                try {
                    if (log.isInfoEnabled()) log.info("Loading OLS terms using the Web Service");

                    if (log.isDebugEnabled()) log.debug("\tLoading Interaction Types...");
                    Term interactionTypeTerm = OlsUtils.getMiTerm("MI:0190");

                    if (log.isDebugEnabled()) log.debug("\tLoading Interaction Detection Methods...");
                    Term detectionMethodTerm = OlsUtils.getMiTerm("MI:0001");

                    List<Term> interactionTypeTerms = OlsBean.childrenFor(interactionTypeTerm, new ArrayList<Term>());
                    List<Term> detectionMethodTerms = OlsBean.childrenFor(detectionMethodTerm, new ArrayList<Term>());

                    ctx.setAttribute(INTERACTION_TYPE_TERM, interactionTypeTerm);
                    ctx.setAttribute(INTERACTION_TYPES, interactionTypeTerms);
                    ctx.setAttribute(DETECTION_METHOD_TERM, detectionMethodTerm);
                    ctx.setAttribute(DETECTION_METHODS, detectionMethodTerms);
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