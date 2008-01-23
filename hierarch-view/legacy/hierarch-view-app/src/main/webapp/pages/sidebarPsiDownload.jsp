<%@ page language="java" %>

<!--
- Copyright (c) 2002 The European Bioinformatics Institute, and others.
- All rights reserved. Please see the file LICENSE
- in the root directory of this distribution.
-
- This layout displays the PSI download button.
- This is displayed only if the user has already requested the display
- of an interaction network.
-
- @author Samuel Kerrien (skerrien@ebi.ac.uk)
- @version $Id$
-->

<%@ page import="org.apache.taglibs.standard.lang.jpath.encoding.HtmlEncoder,
                 uk.ac.ebi.intact.application.hierarchview.business.Constants,
                 uk.ac.ebi.intact.application.hierarchview.business.IntactUserI,
                 uk.ac.ebi.intact.context.IntactContext,
                 uk.ac.ebi.intact.service.graph.Node" %>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://ebi.ac.uk/intact/commons" prefix="intact" %>

<%
    /**
     * Retreive user's data from the session
     */
    IntactUserI user = ( IntactUserI ) IntactContext.getCurrentInstance().getSession().getAttribute( Constants.USER_KEY );

    if ( user == null ) {
        // no user in the session, don't display anything
        return;
    }

    if ( user.InteractionNetworkReadyToBeDisplayed() ) {
        String graph2mif = IntactUserI.GRAPH2MIF_PROPERTIES.getProperty( "graph2mif.url" );
        StringBuffer ac = new StringBuffer( 32 );
        for ( Node node : user.getInteractionNetwork().getCentralNodes() ) {
            ac.append( node.getId() ).append( "%2C" ); // %2C <=> ,
        }
        int l = ac.length();

        if ( l > 0 )
            ac.delete( l - 3, l ); // the 3 last caracters (%2C)
        if ( l == 0 )
            ac.append( user.getQueryString() );
        String url = graph2mif + "?ac=" + ac.toString()
                     + "&depth=1&strict=false";

        String url25 = url + "&version=" + HtmlEncoder.encode( "2.5" );
        String mitab = request.getContextPath() + "/MitabExport";
        
        user.setExportUrl(url25);        
%>

<hr>

<table width="100%">
    <tr>
        <th colspan="2">
            <div align="left">
                <strong><bean:message key="sidebar.psi.section.title"/></strong>
                <intact:documentation section="hierarchView.PPIN.download"/>
            </div>
        </th>
    </tr>

    <tr>
        <td valign="bottom" align="center">
            <nobr>
                <img border="0" src="<%= request.getContextPath() %>/images/psi25.png"
                     alt="PSI-MI 2.5 Download"
                     onclick="var w=window.open('<%= url25 %>');w.focus();">
            </nobr>
        </td>

    </tr>
    <tr>
        <td valign="bottom" align="center">
            <nobr>
                <img border="0" src="<%= request.getContextPath() %>/images/mitab.png"
                     alt="PSI-MI TAB Download"
                     onclick="var w=window.open('<%= mitab %>');w.focus();">
            </nobr>
        </td>
</table>

<%
    } // if InteractionNetworkReady
%>