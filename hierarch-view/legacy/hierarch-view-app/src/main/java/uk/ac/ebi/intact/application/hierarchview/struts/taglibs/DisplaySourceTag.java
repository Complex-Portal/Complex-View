/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */

package uk.ac.ebi.intact.application.hierarchview.struts.taglibs;

import org.apache.log4j.Logger;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.graph.Network;
import uk.ac.ebi.intact.application.hierarchview.highlightment.source.HighlightmentSource;
import uk.ac.ebi.intact.application.hierarchview.struts.StrutsConstants;
import uk.ac.ebi.intact.application.hierarchview.struts.view.utils.SourceBean;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.service.graph.Node;
import uk.ac.ebi.intact.service.graph.binary.InteractorVertex;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * That class allow to initialize properly in the session the sources to display according to the current central
 * protein.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */

public class DisplaySourceTag extends TagSupport {

    private static Logger logger = Logger.getLogger( Constants.LOGGER_NAME );

    /**
     * Skip the body content.
     */
    public int doStartTag() throws JspTagException {
        return SKIP_BODY;
    } // doStartTag

    private Collection<CrossReference> getXRefFromCentralNodes( List<Node> centralNodes ) {
        Collection<CrossReference> xRefs;

        /*
         * if the graph was built with the help of the mine database table the
         * xref are fetched directly from the central nodes.
         * 
         * the collection which stores the xrefs stores actually a map (Map
         * <String, Collection <String> >). which maps for each source all xrefs
         * of that related source.
         */

        xRefs = new ArrayList( 50 );

        for ( Node centralNode : centralNodes ) {
            // reload interactor
            logger.debug( "Reloading node: " + centralNode.getId() );

            Collection<CrossReference> xRefs2 = ( ( InteractorVertex ) centralNode ).getProperties();
            for ( CrossReference aXref : xRefs2 ) {
                if ( !xRefs.contains( aXref ) ) {
                    xRefs.add( aXref );
                }
            }
        }

        return xRefs;
    }

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

            // BasicGraphI interactor = in.getCentralProtein();
            List<Node> centrals = in.getCentralNodes();
            // logger.info( "Central protein AC: " + interactor.getAc() );
            if ( logger.isInfoEnabled() )
                logger.info( centrals.size() + " central protein(s) referenced in the interaction network." );

            // collect the xrefs from the central node
            Collection<CrossReference> xRefs = getXRefFromCentralNodes( centrals );

            String queryString = user.getQueryString();
            String method_class = user.getMethodClass();

            if ( null != queryString ) {
                // get the implementation class of the selected source
                HighlightmentSource source = HighlightmentSource.getHighlightmentSource( method_class );

                if ( null == source ) {
                    pageContext.getOut().write( "An error occured when trying to retreive source.<br />" );
                    logger.error( "Error when trying to load the source class: " + method_class );
                } else {
                    logger.info( "Display highlight source items for query = " + queryString + " SourceClass = " + method_class );

                    List urls;

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
                        System.out.println( "CONTEXT PATH: " + contextPath );
                        logger.info( "CONTEXT PATH : " + contextPath );

                        String applicationPath = protocolAndHost + contextPath;
                        System.out.println( "APPLICATION PATH: " + applicationPath );
                        logger.info( "APPLICATION PATH: " + applicationPath );

                        Collection<String> selectedKeys = user.getKeys();
                        String theClickedKeys = user.getSelectedKey();
                        String selectedKeysType = user.getSelectedKeyType();
                        String tabType = ( String ) session.getAttribute( "tabType" );
                        logger.info( "selectedKeys=" + selectedKeys + " | theClickedKeys=" + theClickedKeys
                                     + " | selectedKeysType=" + selectedKeysType + " | tabType=" + tabType );

                        Collection<String> allSelectedKeys = new ArrayList();

                        if ( selectedKeys != null &&
                             selectedKeysType != null
                             && tabType != null
                             && ( ( "All".equals( tabType ) ) || ( selectedKeysType.toLowerCase().equals( tabType.toLowerCase() ) ) ) ) {
                            allSelectedKeys.addAll( selectedKeys );
                        } else {
                            user.setKeys( null );
                            user.setSelectedKey( "null" );
                            user.setSelectedKeyType( "null" );
                        }

                        if ( theClickedKeys != null
                             && tabType != null
                             && selectedKeysType != null
                             && ( ( "All".equals( tabType ) )
                                  ||
                                  ( selectedKeysType.toLowerCase().equals( tabType.toLowerCase() ) ) ) ) {
                            allSelectedKeys.add( theClickedKeys );
                        } else {
                            user.setKeys( null );
                            user.setSelectedKey( "null" );
                            user.setSelectedKeyType( "null" );
                        }

                        logger.info( "xRefs=" + xRefs + " | allSelectedKeys=" + allSelectedKeys );

                        urls = source.getSourceUrls( xRefs, allSelectedKeys, applicationPath, user );
                    }
                    catch ( IntactException ie ) {
                        String msg = "ERROR<br />The hierarchview system is not properly configured. Please warn your administrator.<br />" + ie;
                        pageContext.getOut().write( msg );
                        return EVAL_PAGE;
                    }

                    /**
                     * We store the source collection in the session in order to
                     * display it with a dedicated tag (display:*).
                     */
                    session.setAttribute( "sources", urls );

                    if ( urls.size() == 1 ) {
                        // only one source element, let's display automatically the relevant page.
                        SourceBean url = ( SourceBean ) urls.get( 0 );
                        String absoluteUrl = url.getSourceBrowserGraphUrl(); // Value();
                        user.setSourceURL( absoluteUrl );
                    } else {
                        user.setSourceURL( null );
                    }
                } // else
            } // if

        }
        catch ( Exception ioe ) {
            ioe.printStackTrace();
            throw new JspException( "Fatal error: could not display protein associated source. <em>" + ioe + "</em>." );
        }
        return EVAL_PAGE;
    } // doEndTag
}