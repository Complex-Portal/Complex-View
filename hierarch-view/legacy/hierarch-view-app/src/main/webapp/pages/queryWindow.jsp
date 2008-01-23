<%--
    Copyright (c) 2002 The European Bioinformatics Institute, and others.
    All rights reserved. Please see the file LICENSE in the root directory of this distribution.

  @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
  @version $Id
--%>

<head>
    <%--    <html:base target="_top"/>--%>
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="expires" content="-1">
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/layouts/styles/intact.css"/>
</head>

  <%
      String query = (String)session.getAttribute( "queryToDisplay" );
      query = query.replace( " [ EXPAND ] ", (String)session.getAttribute( "additionalQueryToDisplay" ) );      
  %>

<table border="0" cellspacing="3" cellpadding="3" width="100%" height="100%">
    <tr>
        <td>
            <%= query %>
        </td>
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