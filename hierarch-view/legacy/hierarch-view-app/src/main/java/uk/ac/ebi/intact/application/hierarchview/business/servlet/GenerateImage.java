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
import org.apache.solr.client.solrj.SolrQuery;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.data.LocalIndexDataSevice;
import uk.ac.ebi.intact.application.hierarchview.business.data.UserQuery;
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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;


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

    private static final int MAX_WIDTH = 1600;
    private static final int MAX_HEIGHT = 1200;

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

            String queryParam = aRequest.getParameter("query");
            String solrQueryParam = aRequest.getParameter("sq");

            UserQuery query = null;
            ImageBean imageBean = null;

            if (queryParam != null) {
                query = new UserQuery(queryParam);
            } else if (queryParam != null) {
                SolrQuery solrQuery = createSolrQueryFromURL(solrQueryParam);
                query = new UserQuery(solrQuery);
            } else {
                IntactUserI user = ( IntactUserI ) session.getAttribute( Constants.USER_KEY );
                imageBean = user.getImageBean();
            }

            if (imageBean == null) {
                // search and create image
                String widthParam = aRequest.getParameter("w");
                String heightParam = aRequest.getParameter("h");

                String appPath = "";
                String minePath = "";

                int width = 400;
                int height = 400;

                if (queryParam == null || solrQueryParam == null) {
                    throw new IllegalStateException("Parameters 'query' or 'sq' (solr query) are needed if the session does not exist (direct call to the servlet)");
                }

                if (widthParam != null) {
                    width = Integer.parseInt(widthParam);
                    width = Math.min(width, MAX_WIDTH);
                }

                if (heightParam != null) {
                    height = Integer.parseInt(heightParam);
                    height = Math.min(height, MAX_HEIGHT);
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

                DrawGraph imageProducer = new DrawGraph( network, appPath, minePath, height, width );
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

    private SolrQuery createSolrQueryFromURL(String solrQueryParam) throws UnsupportedEncodingException {
        String s = URLDecoder.decode(solrQueryParam, "UTF-8");

        String[] stokens = s.split("&");

        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setRows(HVNetworkBuilder.getMaxInteractions());

        for (String stoken : stokens) {
            String[] param = stoken.split("=");

            if (param[0].equals("q")) {
                solrQuery.setQuery(param[1]);
            } else if (param[0].equals("fq")) {
                solrQuery.addFilterQuery(param[1]);
            }
        }
        return solrQuery;
    }
}


