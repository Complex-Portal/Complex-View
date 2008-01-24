<%--
    Copyright (c) 2002 The European Bioinformatics Institute, and others.
    All rights reserved. Please see the file LICENSE in the root directory of this distribution.

  @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
  @version $Id
--%>

<head>    
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="expires" content="-1">
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/layouts/styles/intact.css"/>
</head>


<table border="0" cellspacing="3" cellpadding="3">
    <tr>
        <td>
            <%= (String)session.getAttribute( "additionalQueryToDisplay" ) %>
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