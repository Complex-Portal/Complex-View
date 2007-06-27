<%@ page language="java" %>

<!--
   - Copyright (c) 2002 The European Bioinformatics Institute, and others.
   - All rights reserved. Please see the file LICENSE
   - in the root directory of this distribution.
   -
   - This should be displayed in the content part of the IntAct layout,
   - it displays the highlightment sources.
   -
   - @author Samuel Kerrien (skerrien@ebi.ac.uk)
   - @version $Id$
-->

<%@ page import="uk.ac.ebi.intact.application.hierarchview.business.Constants,
                 uk.ac.ebi.intact.application.hierarchview.business.IntactUserI"%>
<%@ page import="uk.ac.ebi.intact.context.IntactContext" %>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>

<html:html>

<head>
    <base target="_top">
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="expires" content="-1">

    <%
        /**
         * Retreive user's data from the session
         */
        IntactUserI user = (IntactUserI) IntactContext.getCurrentInstance().getSession().getAttribute(Constants.USER_KEY);

        // Displays Http content if URL updated in the session
        if (user.hasSourceUrlToDisplay())
        {

    %>
            <script>
               setTimeout("top.contentFrame.selectedSourcetFrame.location = '<%= user.getSourceURL() %>'", 1000);
            </script>
    <%
       }
    %>

</head>

<body bgcolor="#FFFFFF" topmargin="0" leftmargin="0">
</body>
</html:html>
