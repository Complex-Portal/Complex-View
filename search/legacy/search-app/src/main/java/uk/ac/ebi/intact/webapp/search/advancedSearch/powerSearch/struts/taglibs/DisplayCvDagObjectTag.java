/*
* Copyright (c) 2002 The European Bioinformatics Institute, and others.
* All rights reserved. Please see the file LICENSE
* in the root directory of this distribution.
*/
package uk.ac.ebi.intact.webapp.search.advancedSearch.powerSearch.struts.taglibs;

import org.apache.log4j.Logger;
import uk.ac.ebi.intact.webapp.search.advancedSearch.powerSearch.business.graphdraw.ImageBean;
import uk.ac.ebi.intact.webapp.search.business.Constants;
import uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * This class builds the HTML code which is needed to view the CvDag graphs.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk), Anja Friedrichsen
 * @version $Id$
 */
public class DisplayCvDagObjectTag extends TagSupport {

    static Logger logger = Logger.getLogger( Constants.LOGGER_NAME );


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

        ImageBean imageBean = (ImageBean) pageContext.getServletContext().getAttribute( SearchConstants.IMAGE_BEAN );

        // Display the HTML code map
        try {
            pageContext.getOut().write( "<map name='graph'>" + imageBean.getImageMap() + "<map>" );

            String cvName = imageBean.getCvName();
            String contextPath = ( (HttpServletRequest) pageContext.getRequest() ).getContextPath();
            /* The context parameter in the URL is also given to prevent some browser (eg. Netscape 4.7) to cache
             * image wrongly. If the image link were /hierarchView/GenerateImage, netscape don't even call the
             * servlet and display cached image.
             */
            String msg = "<p align=\"left\">\n"
                         + "  <center>"
                         + "     <img src=\"" + contextPath + "/image?" + cvName + "\""
                         + "      USEMAP=\"#graph\" border =\"0\">"
                         + "     <br>"
                         + "  </center>"
                         + "</p>";

            pageContext.getOut().write( msg );

        } catch ( IOException e ) {
            e.printStackTrace();
        }

        return EVAL_PAGE;
    }
}