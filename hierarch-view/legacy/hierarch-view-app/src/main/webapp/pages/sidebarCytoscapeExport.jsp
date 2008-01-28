<%@ page language="java" %>
<!--
- Copyright (c) 2008 The European Bioinformatics Institute, and others.
- All rights reserved. Please see the file LICENSE
- in the root directory of this distribution.
-->
<%@ page import="uk.ac.ebi.intact.application.hierarchview.business.Constants,
                 uk.ac.ebi.intact.application.hierarchview.business.IntactUserI" %>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://ebi.ac.uk/intact/commons" prefix="intact" %>

<%--
  @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
  @version $Id
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    /**
     * Retreive user's data from the session
     */
    IntactUserI user = ( IntactUserI ) session.getAttribute( Constants.USER_KEY );

    if ( user == null ) {
        // no user in the session, don't display anything
        return;
    }

    if ( user.InteractionNetworkReadyToBeDisplayed() ) {
        String cytoscape = request.getContextPath() + "/CytoscapeExport";
%>


<hr>

<table width="100%">
    <tr>
        <th colspan="2">
            <div align="left">
                <strong><bean:message key="sidebar.cytoscape.section.title"/></strong>
                <intact:documentation section="hierarchView.PPIN.download"/>
            </div>
        </th>
    </tr>

    <tr>
        <td valign="bottom" align="center">
            <nobr>
                <img border="0" src="<%= request.getContextPath() %>/images/cytoscape.png"
                     alt="Cytoscape Export"
                     onclick="var w=window.open('<%= cytoscape %>');w.focus();">
            </nobr>
        </td>
    </tr>
</table>

<%
    } // if InteractionNetworkReady
%>