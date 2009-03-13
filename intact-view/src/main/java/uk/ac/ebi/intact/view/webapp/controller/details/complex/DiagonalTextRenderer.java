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
package uk.ac.ebi.intact.view.webapp.controller.details.complex;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.HashMap;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.font.TextLayout;
import java.awt.image.WritableRenderedImage;
import java.awt.image.BufferedImage;


/**
 * Based on a list of Strings, draw a table header where each string is a label angled at 45 degrees. 
 *
 * <p>
 *  This class was kindly made available by the Microarray group.
 * </p>
 *
 * @author Pavel Kurnosov (pashky@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 */
public class DiagonalTextRenderer {

    protected final Log log = LogFactory.getLog( getClass() );

    private static final Map hints = new HashMap();

    static {
        hints.put( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
        hints.put( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
        hints.put( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
        hints.put( RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE );
        hints.put( RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY );
    }

    public static WritableRenderedImage drawTableHeader( final String[] texts,
                                                         int stepWidth, int maxHeight, int fontSize, int lineHeight,
                                                         Color textColor, Color lineColor ) {

        String[] txts = texts.clone();
        BufferedImage img = new BufferedImage( 10, 10, BufferedImage.TYPE_BYTE_INDEXED );

        Font font = new Font( "Default", Font.PLAIN, fontSize );

        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHints( hints );

        int maxW = 0;
        int maxH = 0;
        double cs = 0.707106781186548;
        for ( int i = 0; i < txts.length; ++i ) {
            int bw;
            while ( true ) {
                TextLayout layout = new TextLayout( txts[i], font, g2d.getFontRenderContext() );
                bw = ( int ) Math.round( layout.getBounds().getWidth() * cs );
                if ( maxHeight > 0 && bw > maxHeight - 20 && txts[i].length() > 3 )
                    txts[i] = txts[i].replaceAll( ".(\\.\\.\\.)?$", "..." );
                else
                    break;
            }
            if ( maxH < bw )
                maxH = bw;
            if ( maxW < stepWidth * i + bw )
                maxW = stepWidth * i + bw;
        }

        g2d.dispose();

        int width = maxW + stepWidth + 20;
        int height = maxHeight;
        img = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );

        g2d = img.createGraphics();
        g2d.setComposite( AlphaComposite.getInstance( AlphaComposite.CLEAR ) );
        g2d.fillRect( 0, 0, width, height );
        g2d.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC ) );

        g2d.setRenderingHints( hints );
        g2d.setPaint( Color.black );
        g2d.setStroke( new BasicStroke( 1.0f ) );

        int x = 0;
        for ( int i = 0; i < texts.length; ++i ) {
            String s = txts[i];
            TextLayout layout = new TextLayout( txts[i], font, g2d.getFontRenderContext() );

            AffineTransform saveAT = g2d.getTransform();
            g2d.translate( x + 9, height - 5 );
            g2d.rotate( -Math.PI / 4 );

            g2d.setColor( Color.WHITE );
            g2d.fillRect( ( int ) ( layout.getBounds().getMinX() ) - 4,
                          ( int ) ( layout.getBounds().getMinY() ) - 4,
                          ( int ) ( layout.getBounds().getWidth() ) + 8,
                          ( int ) ( layout.getBounds().getHeight() ) + 8 );

            g2d.setColor( textColor );
            layout.draw( g2d, 0, 0 );
            g2d.setTransform( saveAT );

            if ( lineHeight > 0 ) {
                g2d.setColor( lineColor );
                g2d.drawLine( x,
                              height - lineHeight,
                              x,
                              height );

                if ( i > 0 ) {
                    int shift = height - lineHeight;

                    if ( x + shift > texts.length * stepWidth )
                        shift = texts.length * stepWidth - x;

                    g2d.drawLine( x, height - lineHeight,
                                  x + shift,
                                  height - lineHeight - shift );
                }
            }

            x += stepWidth;
        }
        if ( lineHeight > 0 ) {
            g2d.setColor( lineColor );
            g2d.drawLine( x,
                          height - lineHeight,
                          x,
                          height );
        }

        g2d.dispose();

        return img;
    }
}