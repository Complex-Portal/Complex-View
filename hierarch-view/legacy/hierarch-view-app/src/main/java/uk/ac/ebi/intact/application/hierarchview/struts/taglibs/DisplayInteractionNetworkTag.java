/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.hierarchview.struts.taglibs;

import org.apache.log4j.Logger;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.graph.InteractionNetwork;
import uk.ac.ebi.intact.application.hierarchview.business.image.ImageBean;
import uk.ac.ebi.intact.application.hierarchview.highlightment.HighlightProteins;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.searchengine.CriteriaBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.ArrayList;
import java.util.Properties;

/**
 * That class allows to display in the browser the current interaction network
 * and the associated HTML MAP.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */

public class DisplayInteractionNetworkTag extends TagSupport {

    static Logger logger = Logger.getLogger (Constants.LOGGER_NAME);

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
            IntactUserI user = (IntactUserI) IntactContext.getCurrentInstance().getSession().getAttribute (Constants.USER_KEY);
            // Retrieve user's data
            if (user == null) {
                logger.error("User was null, exit the tag.");
                return EVAL_PAGE;
            }

            ImageBean imageBean   = user.getImageBean();
            String behaviour      = user.getBehaviour();
            InteractionNetwork in = user.getInteractionNetwork();
            int imageHeight       = imageBean.getImageHeight();
            int imageWidth        = imageBean.getImageWidth();

            /**
             * Apply an highlight if needed data are available
             */
            if (user.InteractionNetworkReadyToBeHighlighted()) {
                String methodClass = user.getMethodClass();
                HighlightProteins.perform (methodClass, behaviour, session, in) ;
            }

            /**
             *  Display only the picture if needed data are available
             */
            if (user.InteractionNetworkReadyToBeDisplayed()) {
                // Display the HTML code map
                pageContext.getOut().write (imageBean.getMapCode());

                // read the Graph.properties file
                String mapName = null;

                Properties properties = IntactUserI.GRAPH_PROPERTIES;;

                if (null != properties) {
                    mapName = properties.getProperty ("hierarchView.image.map.name");
                } else {
                    logger.error("Unable to load properties from " +
                                 uk.ac.ebi.intact.application.hierarchview.business.Constants.PROPERTY_FILE);
                }

                /*
                 * Prepare an identifier unique for the generated image name, it will allows
                 * to take advantage of the client side caching.
                 */
                InteractionNetwork network = user.getInteractionNetwork();

                ArrayList criterias = network.getCriteria();
                int max = criterias.size();
                StringBuffer sb = new StringBuffer( 256 );
                for (int i = 0; i < max; i++) {
                    sb.append ( ( (CriteriaBean) criterias.get(i) ).getQuery() ).append(',');
                }

                String queryString = sb.toString();
                int depth = user.getCurrentDepth();
                String method = user.getMethodClass();
                String key = user.getSelectedKey();
                String highlightContext = "";
                if (key != null) {
                    // a highlight has been requested
                    highlightContext = key;
                    // only relevant to add the behaviour if one is applied
                    highlightContext += behaviour;
                }

                String userContext = queryString + depth + method + highlightContext;

                String contextPath = ((HttpServletRequest) pageContext.getRequest()).getContextPath();
                /* The context parameter in the URL is also given to prevent some browser
                 * (eg. Netscape 4.7) to cache image wrongly.
                 * If the image link were /hierarchview/GenerateImage, netscape don't even
                 * call the servlet and display cached image.
                 */
                String randomParam = "&now=" + System.currentTimeMillis();
                String msg = "<p align=\"left\">\n"
                        + "  <center>"
                        + "     <img src=\""+ contextPath +"/GenerateImage?context="+ userContext
                        + randomParam
                        +"\" "
                        + "      usemap=\"#" + mapName +"\" width=\""+ imageWidth +"\" "
                        +        "height=\""+ imageHeight +"\"  border =\"0\" />"
                        + "     <br />"
                        + "  </center>"
                        + "</p>";

                pageContext.getOut().write( msg );
            }

        } catch (Exception ioe) {
            logger.error( "could not display interaction network", ioe );
            throw new JspException( "Error: could not display interaction network.", ioe );
        }

        return EVAL_PAGE; // the rest of the calling JSP is evaluated
    } // doEndTag
}