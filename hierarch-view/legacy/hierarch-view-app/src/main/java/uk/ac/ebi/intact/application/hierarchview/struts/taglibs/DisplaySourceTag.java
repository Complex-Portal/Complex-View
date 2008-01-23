/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */

package uk.ac.ebi.intact.application.hierarchview.struts.taglibs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.graph.HVNetworkBuilder;
import uk.ac.ebi.intact.application.hierarchview.business.graph.Network;
import uk.ac.ebi.intact.application.hierarchview.highlightment.source.HighlightmentSource;
import uk.ac.ebi.intact.application.hierarchview.highlightment.source.edge.EdgeHighlightmentSource;
import uk.ac.ebi.intact.application.hierarchview.highlightment.source.node.NodeHighlightmentSource;
import uk.ac.ebi.intact.application.hierarchview.struts.StrutsConstants;
import uk.ac.ebi.intact.application.hierarchview.struts.view.utils.SourceBean;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;


/**
 * That class allow to initialize properly in the session the sources to display according
 * to the proteins.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */

public class DisplaySourceTag extends TagSupport {

    private static final Log logger = LogFactory.getLog( DisplaySourceTag.class );

    /**
     * Skip the body content.
     */
    public int doStartTag() throws JspTagException {
        return SKIP_BODY;
    } // doStartTag

    /**
     * Called when the JSP encounters the end of a tag. This will create the option list.
     */
    public int doEndTag() throws JspException {
        HttpSession session = pageContext.getSession();

        try {
            IntactUserI user = ( IntactUserI ) IntactContext.getCurrentInstance().getSession().getAttribute( Constants.USER_KEY );
            if ( user == null ) {
                logger.error( "No existing session" );
                return EVAL_PAGE;
            }

            Network in = user.getInteractionNetwork();
            if ( in == null ) {
                logger.error( "No existing interaction network" );
                return EVAL_PAGE;
            }

            try {
                ServletRequest request = pageContext.getRequest();
                String host = ( String ) session.getAttribute( StrutsConstants.HOST );
                String protocol = ( String ) session.getAttribute( StrutsConstants.PROTOCOL );

                String protocolAndHost = "";
                if ( host != null && protocol != null ) {
                    protocolAndHost = protocol + "//" + host;
                }

                HttpServletRequest httpRequest = ( HttpServletRequest ) request;

                String contextPath = httpRequest.getContextPath();
                logger.debug( "CONTEXT PATH : " + contextPath );

                String applicationPath = protocolAndHost + contextPath;
                logger.debug( "APPLICATION PATH: " + applicationPath );

                Collection<String> selectedKeys = user.getSelectedKeys();
                String clickedKey = user.getClickedKey();
                String selectedKeysType = user.getSelectedKeyType();
                String tabType = ( String ) session.getAttribute( "tabType" );

                logger.info( "clickedKey=" + clickedKey + " | selectedKeys=" + selectedKeys
                             + " | selectedKeysType=" + selectedKeysType + " | tabType=" + tabType );

                String queryString = user.getQueryString();
                if ( null != queryString ) {

                    session.setAttribute( "sources", new ArrayList<SourceBean>() );

                    for ( String method_label : HVNetworkBuilder.ALL_SOURCES ) {

                        HighlightmentSource source = getSourceClass( method_label );

                        List<SourceBean> urls = source.getSourceUrls( in, selectedKeys, applicationPath );

                        /**
                         * We store the source collection in the session in order to
                         * display it with a dedicated tag (display:*).
                         */
                        List<SourceBean> sourceUrls = ( ArrayList ) session.getAttribute( "sources" );
                        if ( sourceUrls != null ) {
                            urls.addAll( sourceUrls );
                        }
                        session.setAttribute( "sources", urls );
                    }
                }
            }
            catch ( IntactException ie ) {
                String msg = "ERROR<br />The hierarchview system is not properly configured. Please warn your administrator.<br />" + ie;
                pageContext.getOut().write( msg );
                return EVAL_PAGE;
            }


        } catch ( Exception ioe ) {
            ioe.printStackTrace();
            throw new JspException( "Fatal error: could not display protein associated source. <em>" + ioe + "</em>." );
        }

        return EVAL_PAGE;
    } // doEndTag


    private HighlightmentSource getSourceClass( String method_label ) throws IOException {

        String method_class = null;

        // read the highlighting.proterties file
        Properties properties = IntactUserI.HIGHLIGHTING_PROPERTIES;

        HighlightmentSource source = null;
        // get the implementation class of the selected source
        if ( HVNetworkBuilder.NODE_SOURCES.contains( method_label ) ) {
            if ( null != properties ) {
                method_class = properties.getProperty( "highlightment.source.node." + method_label + ".class" );
            }
            source = NodeHighlightmentSource.getHighlightmentSource( method_class );
        }
        if ( HVNetworkBuilder.EDGE_SOURCES.contains( method_label ) ) {
            if ( null != properties ) {
                method_class = properties.getProperty( "highlightment.source.edge." + method_label + ".class" );
            }
            source = EdgeHighlightmentSource.getHighlightmentSource( method_class );
        }
        if ( method_class == null ) {
            logger.error( "Error to get MethodClass " + method_class + " from Properties " + properties );
        }

        if ( null == source ) {
            pageContext.getOut().write( "An error occured when trying to retreive source.<br />" );
            logger.error( "Error when trying to load the class: " + method_class );
        } else {
            logger.debug( "Display highlight source items SourceClass = " + method_class );
        }

        return source;
    }


}