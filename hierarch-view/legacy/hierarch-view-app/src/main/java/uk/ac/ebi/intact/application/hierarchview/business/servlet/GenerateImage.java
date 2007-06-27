/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.hierarchview.business.servlet;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import org.apache.log4j.Logger;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.image.ImageBean;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.util.Chrono;
import uk.ac.ebi.intact.business.IntactTransactionException;

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

    static Logger logger = Logger.getLogger (Constants.LOGGER_NAME);
    private final static String ERROR_MESSAGE = "Unable to produce the interaction network, please warn your administrator";

    /**
     * Servlet allowing to get image data, rasterize into JPEG and send
     * to the browser the image by taking care of the MIME type.
     */
    public void doGet (HttpServletRequest aRequest, HttpServletResponse aResponse)
            throws ServletException {
        OutputStream outputStream = null;

        IntactContext.getCurrentInstance().getDataContext().beginTransaction();

        try {
            // get the current user session
            HttpSession session = aRequest.getSession (false);

            if (session == null) {
                logger.error ("No session available, don't displays interaction network");
                return;
            }

            IntactUserI user = (IntactUserI) IntactContext.getCurrentInstance().getSession().getAttribute (Constants.USER_KEY);

            if (user == null) {
                aResponse.getOutputStream().print(ERROR_MESSAGE);
                logger.error ("No user in the session, don't displays interaction network");
                 return;
            }

            ImageBean imageBean = user.getImageBean();

            if (null == imageBean) {
                logger.error ("ImageBean in the session is null");
                return;
            }

            BufferedImage image = imageBean.getImageData();
            outputStream = new BufferedOutputStream(aResponse.getOutputStream(), 1024);

            // Send browser MIME type
            aResponse.setContentType("image/jpg");
            logger.info ("MIME type: image/jpg");

            // JPEG encoding
            JPEGEncodeParam  jpegEncodeParam  = null;
            jpegEncodeParam  = JPEGCodec.getDefaultJPEGEncodeParam (image);

            Chrono chrono = new Chrono ();
            chrono.start();

            /*
            *  Encode the produced image in JPEG.
            *   Quality range : 0.0 .. 1.0
            *   0.7  : high quality   (good compromise between file size and quality)
            *   0.5  : medium quality
            *   0.25 : low quality
            */
            jpegEncodeParam.setQuality (0.7F, false);
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(outputStream, jpegEncodeParam);
            encoder.encode(image);

            chrono.stop();
            logger.info("Time for rasterizing " + chrono);

            outputStream.close();

        }
        catch (IOException e) {
            logger.error ("Error during the image producing process", e);
            return;
        }
        finally {
            try {
                if (outputStream != null)
                {
                    outputStream.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        try {
            IntactContext.getCurrentInstance().getDataContext().commitTransaction();
        } catch (IntactTransactionException e) {
            e.printStackTrace();
        }
    } // doGet
}


