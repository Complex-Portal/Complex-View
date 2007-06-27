/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.statisticView.struts.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import uk.ac.ebi.intact.application.statisticView.business.util.Constants;
import uk.ac.ebi.intact.application.statisticView.struts.view.FilterForm;
import uk.ac.ebi.intact.application.statisticView.struts.view.IntactStatisticsBean;
import uk.ac.ebi.intact.application.statisticView.struts.view.ViewBeanFactory;
import uk.ac.ebi.intact.business.IntactException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * User: Michael Kleen mkleen@ebi.ac.uk Date: Mar 17, 2005 Time: 1:47:53 PM
 * <p/>
 * This class provides the actions required to carry out the filter request from the sidebar for the web-based
 * interface. The filter criteria are obtained from a Form object and then the search is carried out. The class
 * generates the specific Charts as pngs and store them in the tomcat /temp folder. After the http session from the user
 * is expired the .png file will automatically deleted.  *
 */
public class IntactStatisticsAction extends Action {

    private static final Log logger = LogFactory.getLog( IntactStatisticsAction.class );

    /**
     * Process the specified HTTP request, and create the corresponding HTTP response (or forward to another web
     * component that will create it). Return an ActionForward instance describing where and how control should be
     * forwarded, or null if the response has already been completed.
     *
     * @param mapping  - The <code>ActionMapping</code> used to select this instance
     * @param form     - The optional <code>ActionForm</code> bean for this request (if any)
     * @param request  - The HTTP request we are processing
     * @param response - The HTTP response we are creating
     *
     * @return - represents a destination to which the controller servlet, <code>ActionServlet</code>, might be directed
     *         to perform a RequestDispatcher.forward() or HttpServletResponse.sendRedirect() to, as a result of
     *         processing activities of an <code>Action</code> class
     */
    public ActionForward execute( ActionMapping mapping,
                                  ActionForm form,
                                  HttpServletRequest request,
                                  HttpServletResponse response
    ) {
        logger.debug( "Enterning execution of IntactStatisticsAction" );

        // get the session
        HttpSession session = request.getSession();

        IntactStatisticsBean intactBean = null;
        String start = null;
        String stop = null;

        FilterForm filterForm = ( FilterForm ) form;

        // get the date to search for
        if ( null != form ) {
            // read start values from the form
            start = filterForm.getStart();
            if ( start != null ) {
                logger.debug( "START: " + start );
                request.setAttribute( "start", start );
            }
            // read stop values from the form
            stop = filterForm.getStop();
            if ( stop != null ) {
                logger.debug( "STOP: " + stop );
                request.setAttribute( "stop", stop );
            }
        }

        try {
            // receive the viewbean for the specific
            logger.debug( "Creating ViewBean factory" );
            ViewBeanFactory chartFactory = new ViewBeanFactory( request.getContextPath() );

            logger.debug( "Creating ViewBean" );
            intactBean = chartFactory.createViewBean( start, stop, session );

        } catch ( IntactException e ) {
            // forward to an error page if something went wrong
            logger.error( "Error when loading the statistics data.", e );
            return mapping.findForward( "error" );
        } catch ( IOException ioe ) {
            // forward to an error page if something went wrong
            logger.error( "Error when saving the charts on disk.", ioe );
            return mapping.findForward( "error" );
        } catch ( Throwable e ) {
            logger.error( "Exception creating view bean", e );
            return mapping.findForward( "error" );
        }

        // put the databean to the request and forward to the jsp
        request.setAttribute( "intactbean", intactBean );

        logger.debug( "Redirecting to result page" );

        return mapping.findForward( Constants.FORWARD_RESULT_PAGE );
    }
}