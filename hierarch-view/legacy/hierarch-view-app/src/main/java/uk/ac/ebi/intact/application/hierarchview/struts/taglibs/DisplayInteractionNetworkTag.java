/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.hierarchview.struts.taglibs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.graph.HVNetworkBuilder;
import uk.ac.ebi.intact.application.hierarchview.business.graph.Network;
import uk.ac.ebi.intact.application.hierarchview.business.image.ImageBean;
import uk.ac.ebi.intact.application.hierarchview.highlightment.HighlightInteractions;
import uk.ac.ebi.intact.application.hierarchview.highlightment.HighlightProteins;
import uk.ac.ebi.intact.application.hierarchview.highlightment.source.edge.EdgeHighlightmentSource;
import uk.ac.ebi.intact.application.hierarchview.highlightment.source.node.NodeHighlightmentSource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.Properties;

/**
 * That class allows to display in the browser the current interaction network
 * and the associated HTML MAP.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */

public class DisplayInteractionNetworkTag extends TagSupport {

    private static final Log logger = LogFactory.getLog( DisplayInteractionNetworkTag.class );

    /**
     * Skip the body content.
     */
    public int doStartTag() throws JspTagException {

        return SKIP_BODY;
    } // doStartTag


    /**
     * Called when the JSP encounters the end of a tag.
     */
    public int doEndTag() throws JspException {
        HttpSession session = pageContext.getSession();

        try {
            IntactUserI user = ( IntactUserI ) session.getAttribute( Constants.USER_KEY );
            // Retrieve user's data
            if ( user == null ) {
                logger.error( "User was null, exit the tag." );
                return EVAL_PAGE;
            }

            String behaviour = user.getBehaviour();
            Network in = user.getInteractionNetwork();
            /**
             * Apply an highlight if needed data are available
             */
            if ( user.InteractionNetworkReadyToBeHighlighted() ) {

                String methodLabel = user.getMethodLabel();

                if ( HVNetworkBuilder.NODE_SOURCES.contains( methodLabel ) ) {
                    NodeHighlightmentSource nodeHighlightmentSource = NodeHighlightmentSource.getHighlightmentSourceBySourceKey( methodLabel );
                    HighlightProteins.perform( nodeHighlightmentSource, behaviour, session, in );
                }
                if ( HVNetworkBuilder.EDGE_SOURCES.contains( methodLabel ) ) {
                    EdgeHighlightmentSource edgeHighlightmentSource = EdgeHighlightmentSource.getHighlightmentSourceBySourceKey( methodLabel );
                    HighlightInteractions.perform( edgeHighlightmentSource, behaviour, session, in );
                }
            }

            /**
             *  Display only the picture if needed data are available
             */
            if ( user.InteractionNetworkReadyToBeDisplayed() ) {

                ImageBean imageBean = user.getImageBean();
                int imageHeight = imageBean.getImageHeight();
                int imageWidth = imageBean.getImageWidth();

                // Display the HTML code map
                pageContext.getOut().write( imageBean.getMapCode() );

                // read the Graph.properties file
                String mapName = null;

                Properties properties = IntactUserI.GRAPH_PROPERTIES;

                if ( null != properties ) {
                    mapName = properties.getProperty( "hierarchView.image.map.name" );
                } else {
                    logger.error( "Unable to load properties from " + Constants.PROPERTY_FILE );
                }

                /*
                 * Prepare an identifier unique for the generated image name, it will allows
                 * to take advantage of the client side caching.
                 */
                Network network = user.getInteractionNetwork();
                int depth = 0;
                if (network != null) {
                    depth = network.getCurrentDepth();
                }
                String method = user.getMethodClass();
                String key = user.getClickedKey();
                String highlightContext = "";
                if ( key != null ) {
                    // a highlight has been requested
                    highlightContext = key;
                    // only relevant to add the behaviour if one is applied
                    highlightContext += behaviour;
                }

                String userContext = user.getQueryString() + depth + method + highlightContext;

                String contextPath = ( ( HttpServletRequest ) pageContext.getRequest() ).getContextPath();
                /* The context parameter in the URL is also given to prevent some browser
                 * (eg. Netscape 4.7) to cache image wrongly.
                 * If the image link were /hierarchview/GenerateImage, netscape don't even
                 * call the servlet and display cached image.
                 */
                String randomParam = "&now=" + System.currentTimeMillis();
                String msg = "<p align=\"left\">\n"
                             + "  <center>"
                             + "     <img src=\"" + contextPath + "/GenerateImage?context=" + userContext
                             + randomParam
                             + "\" "
                             + "      usemap=\"#" + mapName + "\" width=\"" + imageWidth + "\" "
                             + "height=\"" + imageHeight + "\"  border =\"0\" />"
                             + "     <br />"
                             + "  </center>"
                             + "</p>";

                pageContext.getOut().write( msg );
            }

        } catch ( Exception ioe ) {
            logger.error( "could not display interaction network", ioe );
            throw new JspException( "Error: could not display interaction network.", ioe );
        }

        return EVAL_PAGE; // the rest of the calling JSP is evaluated
    } // doEndTag
}