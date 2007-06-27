<%@ page language="java"%>

<%@ page import="uk.ac.ebi.intact.application.hierarchview.business.Constants,
                 uk.ac.ebi.intact.application.hierarchview.business.IntactUserI,
                 uk.ac.ebi.intact.application.hierarchview.business.graph.InteractionNetwork"%>
<%@ page import="uk.ac.ebi.intact.context.IntactContext" %>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>

<!--
   - Copyright (c) 2002 The European Bioinformatics Institute, and others.
   - All rights reserved. Please see the file LICENSE
   - in the root directory of this distribution.
   -
   - Forward to the search application to display the current context.
   -
   - @author Samuel Kerrien (skerrien@ebi.ac.uk)
   - @version $Id$
-->

<html:html>

<head>
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="expires" content="-1">
<%
    /**
     * Retreive user's data from the session
     */
    IntactUserI user = (IntactUserI) IntactContext.getCurrentInstance().getSession().getAttribute(Constants.USER_KEY);
    int maxInteractor = InteractionNetwork.getMaxCentralProtein();
    // Displays Http content if URL updated in the session -->
    String searchUrl = user.getSearchUrl();
    if (searchUrl != null)
    {
%>
       <META HTTP-EQUIV="REFRESH" CONTENT="1; URL=<%= searchUrl %>">
<%
    }
%>

</head>

<body bgcolor="#FFFFFF" topmargin="0" leftmargin="0">

<%
    if (searchUrl != null) {
%>
        <blockquote>
            <blockquote>
                <blockquote>
                    <table>
                      <tr>
                        <td>
                            <img src="<%=request.getContextPath()%>/images/clockT.gif" border="0" />
                        </td>
                        <td>
                            <strong>
                                <font color="#000080">
                                    Your request gave too many results, hierarchView is configured to handle a
                                    maximum of <%= maxInteractor %> interactor<%= (maxInteractor > 1 ? "s" : "") %>
                                    simultaneously.
                                    <br>
                                    Please wait whilst your query is forwarded to the search application.
                                    <br>
                                    If the screen is not refreshed, please click
                                    <a href="<%= searchUrl %>" target="_top">here</a>.
                                </font>
                            </strong>
                        </td>
                      <tr>
                    </table>
                </blockquote>
            </blockquote>
        </blockquote>
<%
    } else {
%>
        <%-- no URL for search, error message --%>
        The search access is not properly configured, please warn your administrator.<br>
        click <a href="<%=request.getContextPath()%>" target="_top">here</a> to go to the
        hierarchView home page.
<%
    }
%>
</body>
</html:html>
