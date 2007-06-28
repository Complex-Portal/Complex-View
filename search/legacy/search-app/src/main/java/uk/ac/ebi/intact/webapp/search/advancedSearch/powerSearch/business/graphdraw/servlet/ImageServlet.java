/*
* Copyright (c) 2002 The European Bioinformatics Institute, and others.
* All rights reserved. Please see the file LICENSE
* in the root directory of this distribution.
*/
package uk.ac.ebi.intact.webapp.search.advancedSearch.powerSearch.business.graphdraw.servlet;

import com.sun.jimi.core.Jimi;
import com.sun.jimi.core.JimiException;
import com.sun.jimi.core.JimiWriter;
import org.apache.log4j.Logger;
import uk.ac.ebi.intact.webapp.search.advancedSearch.powerSearch.business.graphdraw.ImageBean;
import uk.ac.ebi.intact.webapp.search.business.Constants;
import uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Thas class provides a method to create an Image on the fly.
 *
 * @author Anja Friedrichsen
 * @version $Id$
 */
public class ImageServlet extends HttpServlet {

    private static Logger logger = Logger.getLogger( Constants.LOGGER_NAME );

    public void doGet( HttpServletRequest request, HttpServletResponse response )
            throws IOException, ServletException {

        ImageBean imageBean = (ImageBean) getServletContext().getAttribute( SearchConstants.IMAGE_BEAN );
        BufferedImage image = imageBean.getImageData();

        // encode the image and put it on the stream
        try {
            JimiWriter writer = Jimi.createJimiWriter( "image/png", response.getOutputStream() );
            writer.setSource( image );
            writer.putImage( response.getOutputStream() );
        } catch ( JimiException je ) {
            throw new IOException( je.getMessage() );
        }

        if ( logger.isInfoEnabled() ) {
            // Get current size of heap in bytes
            long heapSize = Runtime.getRuntime().totalMemory();

            // Get maximum size of heap in bytes. The heap cannot grow beyond this size.
            // Any attempt will result in an OutOfMemoryException.
            long heapMaxSize = Runtime.getRuntime().maxMemory();

            // Get amount of free memory within the heap in bytes. This size will increase
            // after garbage collection and decrease as new objects are created.
            long heapFreeSize = Runtime.getRuntime().freeMemory();

            logger.info( "in Servlet: current size of heap: " + heapSize );
            logger.info( "maximum heap size: " + heapMaxSize );
            logger.info( "free heap size: " + heapFreeSize );
        }

        response.setContentType( "image/png" );
    }
}
