/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.hierarchview.business.servlet;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.data.LocalIndexDataSevice;
import uk.ac.ebi.intact.application.hierarchview.business.graph.HVNetworkBuilder;
import uk.ac.ebi.intact.application.hierarchview.business.graph.Network;
import uk.ac.ebi.intact.application.hierarchview.business.image.DrawGraph;
import uk.ac.ebi.intact.application.hierarchview.business.image.ImageBean;
import uk.ac.ebi.intact.util.Chrono;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Purpose: <br>
 * For a bean given in the session, forward an image to be displayed.
 * The image format is parameterized in the Graph.properties file.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */

public class GenerateImage extends HttpServlet {

    private static final Log logger = LogFactory.getLog( GenerateImage.class );

    private final static String ERROR_MESSAGE = "Unable to produce the interaction network, please warn your administrator";

    /**
     * Servlet allowing to get image data, rasterize into JPEG and send
     * to the browser the image by taking care of the MIME type.
     */
    public void doGet( HttpServletRequest aRequest, HttpServletResponse aResponse )
            throws ServletException {
        OutputStream outputStream = null;

        try {
            // get the current user session
            HttpSession session = aRequest.getSession( false );

            ImageBean imageBean;

            if ( session != null ) {
                IntactUserI user = ( IntactUserI ) session.getAttribute( Constants.USER_KEY );
                imageBean = user.getImageBean();
            } else {
                // search and create image
                String query = aRequest.getParameter("query");
                String widthParam = aRequest.getParameter("w");
                String heightParam = aRequest.getParameter("h");

                String appPath = "";
                String minePath = "";

                int width = 400;
                int height = 400;

                if (query == null) {
                    throw new IllegalStateException("Parameter 'query' is needed if the session does not exist (direct call to the servlet)");
                }

                if (widthParam != null) {
                    width = Integer.parseInt(widthParam);
                }

                if (heightParam != null) {
                    height = Integer.parseInt(heightParam);
                }

                LocalIndexDataSevice dataService = new LocalIndexDataSevice();

                Network network;

                try {
                    HVNetworkBuilder builder = new HVNetworkBuilder(dataService);
                    network = builder.buildBinaryGraphNetwork(query);
                } catch (Exception e) {
                   throw new ServletException("Problem creating network using query: "+query, e);
                }

                String dataTlp = network.exportTlp();
                network.importDataToImage(dataTlp);

                DrawGraph imageProducer = new DrawGraph( network, "", "", height, width );
                imageProducer.draw();
                imageBean = imageProducer.getImageBean();
            }


            if ( null == imageBean ) {
                throw new IllegalStateException( "ImageBean is null" );
            }

            BufferedImage image = imageBean.getImageData();
            outputStream = new BufferedOutputStream( aResponse.getOutputStream(), 1024 );

            // Send browser MIME type
            aResponse.setContentType( "image/jpg" );
            logger.info( "MIME type: image/jpg" );

            // JPEG encoding
            JPEGEncodeParam jpegEncodeParam;
            jpegEncodeParam = JPEGCodec.getDefaultJPEGEncodeParam( image );

            Chrono chrono = new Chrono();
            chrono.start();

            /*
            *  Encode the produced image in JPEG.
            *   Quality range : 0.0 .. 1.0
            *   0.7  : high quality   (good compromise between file size and quality)
            *   0.5  : medium quality
            *   0.25 : low quality
            */
            jpegEncodeParam.setQuality( 0.7F, false );
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder( outputStream, jpegEncodeParam );
            encoder.encode( image );

            chrono.stop();
            logger.info( "Time for rasterizing " + chrono );

            outputStream.close();

        }
        catch ( IOException e ) {
            logger.error( "Error during the image producing process", e );
            return;
        }
        finally {
            try {
                if ( outputStream != null ) {
                    outputStream.close();
                }
            } catch ( IOException ioe ) {
                ioe.printStackTrace();
            }
        }
    } // doGet
}


