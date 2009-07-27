/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.view.webapp.servlet;


import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import uk.ac.ebi.intact.view.webapp.controller.details.complex.DiagonalTextRenderer;
import uk.ac.ebi.intact.view.webapp.controller.details.complex.TableHeaderController;

import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.WritableRenderedImage;
import java.io.IOException;


/**
 * Servlet that outputs a table header based on given parameters.
 *
 *  <p>
 *  This class was kindly made available by the Microarray group.
 * </p>
 *
 * @author Pavel Kurnosov (pashky@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 */
public class TableHeaderServlet extends HttpServlet {

    private WebApplicationContext applicationContext;

    private static int stoi( String str, int def ) {
        try {
            return Integer.valueOf( str );
        } catch ( Exception e ) {
            return def;
        }
    }

    private static Color stoc( String str, Color def ) {
        try {
            return Color.decode( str );
        } catch ( Exception e ) {
            return def;
        }
    }

    @Override
    public void init( ServletConfig config) throws ServletException {
        super.init(config);

        ServletContext context = getServletContext();
        applicationContext = WebApplicationContextUtils.getWebApplicationContext(context);
    }

    protected void doGet( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException {
        int stepWidth = stoi( req.getParameter( "s" ), 26 );
        int maxHeight = stoi( req.getParameter( "h" ), -1 );
        int fontSize = stoi( req.getParameter( "fs" ), 11 );
        int lineHeight = stoi( req.getParameter( "lh" ), 15 );
        Color textColor = stoc( req.getParameter( "tc" ), Color.BLACK );
        Color lineColor = stoc( req.getParameter( "lc" ), Color.BLACK );

        String[] texts = null;
        if ( req.getParameter( "st" ) != null ) {
            TableHeaderController details = (TableHeaderController) applicationContext.getBean("tableHeaderBean");
            texts = details.getLabelArray();
        } else {
            texts = req.getParameterValues( "t" );
        }
        if ( texts == null )
            texts = new String[0];

        res.setContentType( "image/png" );
        WritableRenderedImage img = DiagonalTextRenderer.drawTableHeader( texts,
                                                                          stepWidth, maxHeight, fontSize, lineHeight,
                                                                          textColor, lineColor );
        ImageIO.write( img, "png", res.getOutputStream() );
    }
}
