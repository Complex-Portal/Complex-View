/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.statisticView.webapp;

import com.keypoint.PngEncoder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.JFreeChart;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>03-Aug-2006</pre>
 */
public class ChartProviderServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog( ChartProviderServlet.class );

    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException,
                                                                                         IOException {

        String chartName = request.getParameter( "name" );

        ChartSessionInfo chartSessionInfo = ( ChartSessionInfo ) request.getSession().getAttribute( chartName );

        if ( chartSessionInfo == null ) {
            throw new NullPointerException( "No chart with name in the session: " + chartName );
        }

        // get the chart from storage
        JFreeChart chart = chartSessionInfo.getChart();
        // set the content type so the browser can see this as it is
        response.setContentType( "image/png" );

        log.debug( "Creating buffered chart: " + chartName );

        // send the picture
        BufferedImage buf = chart.createBufferedImage( chartSessionInfo.getHeight(),
                                                       chartSessionInfo.getWidth(),
                                                       chartSessionInfo.getChartRenderingInfo() );
        PngEncoder encoder = new PngEncoder( buf, false, 0, 9 );
        byte[] bytes = encoder.pngEncode();

        if ( log.isDebugEnabled() ) {
            log.debug( "Encoded image: " + bytes.length + " byte(s)" );
        }

        response.getOutputStream().write( bytes );
    }
}