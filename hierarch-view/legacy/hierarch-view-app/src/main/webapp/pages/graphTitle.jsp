<%@ page language="java" %>

<!--
- Copyright (c) 2002 The European Bioinformatics Institute, and others.
- All rights reserved. Please see the file LICENSE
- in the root directory of this distribution.
-
- hierarchView graph title page
- This should be displayed in the content part of the IntAct layout,
- it displays the interaction network title.
-
- @author Samuel Kerrien (skerrien@ebi.ac.uk)
- @version $Id$
-->

<%@ taglib uri="http://www.ebi.ac.uk/intact/hierarch-view" prefix="hierarchView" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<head>
    <%--    <html:base target="_top"/>--%>
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="expires" content="-1">
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/layouts/styles/intact.css"/>
</head>

<hierarchView:displayGraphTitle/>

<%
    String shortQuery = ( String ) session.getAttribute( "queryToDisplay" );
    String longQuery = (String) session.getAttribute( "additionalQueryToDisplay" );
%>

<table border="0" cellspacing="3" cellpadding="3" width="100%" height="100%">
    <tr>
        <td>
            <%= shortQuery%>
            </td>

            <%--<form name="Test" action="">--%>
                <%--<input type="td" name="Ausgabe">--%>
                <%--<!--<input type="text" name="Ausgabe">-->--%>
                <%--<input type="button" value="EXPAND" onclick="this.form.Ausgabe.value = 'something'">--%>
            <%--</form>--%>


    </tr>
    <% String infoToDisplay = ( String ) session.getAttribute( "infoToDisplay" );
        if ( infoToDisplay != null ) { %>
    <tr>
        <td>
            <%= infoToDisplay %>
        </td>
    </tr>
    <% }
        String errorToDisplay = ( String ) session.getAttribute( "errorToDisplay" );
        if ( errorToDisplay != null ) { %>
    <tr>
        <td>
            <%=errorToDisplay %>
        </td>
    </tr>
    <% } %>
</table>
