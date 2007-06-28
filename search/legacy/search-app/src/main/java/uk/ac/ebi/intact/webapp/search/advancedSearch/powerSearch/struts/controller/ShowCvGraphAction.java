/*
* Copyright (c) 2002 The European Bioinformatics Institute, and others.
* All rights reserved. Please see the file LICENSE
* in the root directory of this distribution.
*/
package uk.ac.ebi.intact.webapp.search.advancedSearch.powerSearch.struts.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.webapp.search.advancedSearch.powerSearch.Constants;
import uk.ac.ebi.intact.webapp.search.advancedSearch.powerSearch.business.graphdraw.CvGraph;
import uk.ac.ebi.intact.webapp.search.advancedSearch.powerSearch.business.graphdraw.ImageBean;
import uk.ac.ebi.intact.webapp.search.business.IntactUserIF;
import uk.ac.ebi.intact.webapp.search.struts.framework.IntactBaseAction;
import uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This action is called to view the Cv DAG graphs. It creates the image and the corresponding image map and stores them
 * into an ImageBean.
 *
 * @author Anja Friedrichsen
 * @version $Id$
 */
public class ShowCvGraphAction extends IntactBaseAction {

    private static final Log logger = LogFactory.getLog(ShowCvGraphAction.class);

    // Basic Caching parameters
    private static final long CACHE_TIMEOUT = 5 * 60 * 1000; // 5 minutes
    private long lastImageGeneratedTime = -1;
    private HashMap cacheMap = new HashMap();

    private static Map requestedCVs = new HashMap( 8 );

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
     *
     * @throws IOException      ...
     * @throws ServletException ...
     */
    public ActionForward execute( ActionMapping mapping,
                                  ActionForm form,
                                  HttpServletRequest request,
                                  HttpServletResponse response )
            throws IOException, ServletException {

        logger.info( "in ShowCvGraphAction" );

        if ( lastImageGeneratedTime == -1 ) {
            lastImageGeneratedTime = System.currentTimeMillis();
        }

        long timeElapsed = System.currentTimeMillis() - lastImageGeneratedTime;
        boolean timeout = timeElapsed > CACHE_TIMEOUT;

        logger.info( "time elapsed: " + timeElapsed );
        logger.info( "timeout: " + timeout );

        // Session to access various session objects. This will create
        //a new session if one does not exist.
        HttpSession session = super.getSession( request );

        // Handle to the Intact User.
        IntactUserIF user = super.getIntactUser( session );
        if ( user == null ) {
            //just set up a new user for the session - if it fails, need to give up!
            user = super.setupUser( request );
            if ( user == null ) {
                return mapping.findForward( SearchConstants.FORWARD_FAILURE );
            }
        }

        DynaActionForm dyForm = (DynaActionForm) form;
        // retrieve the name of the CV which should be viewed
        String cvName = (String) dyForm.get( "cvName" );
        Class cvClass = null;
        if ( requestedCVs.keySet().contains( cvName ) ) {
            cvClass = (Class) requestedCVs.get( cvName );
        } else {
            try {
                // Class lookup
                cvClass = Class.forName( cvName );
                cvName = cvClass.getName().substring( cvClass.getName().lastIndexOf( "." ) + 1 );

                // cache it
                requestedCVs.put( cvName, cvClass );
            } catch ( ClassNotFoundException e ) {
                return mapping.findForward( SearchConstants.FORWARD_FAILURE );
            }
        }

        ImageBean imageBean = null;

        // create a new Imagebean with the image and the imageMap, if it is not in the cache
        if ( !cacheMap.containsKey( cvName ) || timeout ) {

            logger.info( "Generate the picture for " + cvName );

            CvGraph imageProducer = new CvGraph();
            imageBean = new ImageBean();

            // create the image and set it to the bean
            try {
                imageBean.setImageData( (BufferedImage) imageProducer.createImage( cvClass ) );
            } catch ( IntactException e ) {
                logger.error( "Could not produce image for " + cvClass, e );
                return mapping.findForward( SearchConstants.FORWARD_ERROR );
            }

            // set the corresponding map to the bean
            imageBean.setImageMap( imageProducer.getImageMap() );

            // set the cvName to the bean
            imageBean.setCvName( cvName );

            // cache the imageBean
            cacheMap.put( cvName, imageBean );
        } else {

            logger.info( "Use the cache" );
            imageBean = (ImageBean) cacheMap.get( cvName );
        }

        logger.info( "cvName: " + imageBean.getCvName() );
        logger.info( "ImageMapLength: " + imageBean.getImageMap().length() );

        getServlet().getServletContext().setAttribute( Constants.IMAGE_BEAN, imageBean );

        // forward to the corresponding jsp page, dependent on the cvName
        logger.info( "forward to display of " + imageBean.getCvName() );
        return mapping.findForward( SearchConstants.FORWARD_SHOW_CV_DAG );
    }
}