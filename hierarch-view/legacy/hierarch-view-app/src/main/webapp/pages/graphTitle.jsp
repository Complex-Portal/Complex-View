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

<%@ page import="uk.ac.ebi.intact.application.hierarchview.business.Constants,
                 uk.ac.ebi.intact.application.hierarchview.business.IntactUserI,
                 uk.ac.ebi.intact.application.hierarchview.business.graph.Network,
                 uk.ac.ebi.intact.context.IntactContext" %>
<%@ page import="uk.ac.ebi.intact.service.graph.Node" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Iterator" %>

<head>
    <%--    <html:base target="_top"/>--%>
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="expires" content="-1">
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/layouts/styles/intact.css"/>
</head>

<%
    /**
     * Retreive user's data from the session
     */
    IntactUserI user = ( IntactUserI ) IntactContext.getCurrentInstance().getSession().getAttribute( Constants.USER_KEY );
    if ( user.getSearchUrl() == null ) return;

    Network in = user.getInteractionNetwork();
    if ( in == null ) return;

    StringBuffer contextToDisplay = new StringBuffer( 256 );
    StringBuffer infoToDisplay = null;
    StringBuffer errorToDisplay = null;

    // If there are nodes and edges display: <Result: x molecules, y interactions.>
    if ( in.getNodes() != null && in.getEdges() != null ) {
        infoToDisplay = new StringBuffer( 256 );

        infoToDisplay.append( "Result: " );
        infoToDisplay.append( "<strong>" ).append( in.getNodes().size() ).append( "</strong> molecules, " );
        infoToDisplay.append( "<strong>" ).append( in.getEdges().size() ).append( "</strong> interactions." );
    }

    if ( user.getMinePath() == null ) {
        contextToDisplay.append( "Query: " );
    } else {
        contextToDisplay.append( "This is the minimal connecting network for " );
    }

    contextToDisplay.append( "<strong>" );
    if ( in.getCentralNodes() != null && !in.getCentralNodes().isEmpty() ) {

        Collection<Node> centralNodes = in.getCentralNodes();
        Iterator<Node> iterator = centralNodes.iterator();
        int index = 0;
        int max = 6;
        while ( iterator.hasNext() && index < max ) {
            Node node = iterator.next();
            contextToDisplay.append( "<a href=\"" );
            contextToDisplay.append( user.getSearchUrl( node.getId(), false ) );
            contextToDisplay.append( "\" target=\"_blank\">" );
            contextToDisplay.append( node.getLabel() );
            contextToDisplay.append( "</a>" );
            if ( iterator.hasNext() ) {
                contextToDisplay.append( ", " );
            }
            index++;
        }

        if ( centralNodes.size() > max ) {
            contextToDisplay.append( " ... " );
        }

    } else {
        String query = user.getQueryString();
        if ( query == null ) return;
        contextToDisplay.append( query );
    }
    contextToDisplay.append( "</strong>" );

    if ( user.hasErrorMessage() ) {
        errorToDisplay = new StringBuffer();

        errorToDisplay.append( "<strong><font color=\"red\">" );
        errorToDisplay.append( user.getErrorMessage() );
        errorToDisplay.append( "</font></strong>" );
        user.clearErrorMessage();
    }
%>

<table border="0" cellspacing="3" cellpadding="3" width="100%" height="100%">

    <tr>
        <td>
            <%= contextToDisplay.toString() %>
        </td>
    </tr>
    <% if ( infoToDisplay != null ) { %>
    <tr>
        <td>
            <%= infoToDisplay.toString() %>
        </td>
    </tr>
    <% }
        if ( errorToDisplay != null ) { %>
    <tr>
        <td>
            <%= errorToDisplay.toString() %>
        </td>
    </tr>
    <% } %>
</table>