<%@ page import="uk.ac.ebi.intact.application.hierarchview.business.Constants,
                 uk.ac.ebi.intact.application.hierarchview.business.IntactUserI,
                 uk.ac.ebi.intact.context.IntactContext"%>
<%@ page language="java"%>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>


<!--
   - Copyright (c) 2002 The European Bioinformatics Institute, and others.
   - All rights reserved. Please see the file LICENSE
   - in the root directory of this distribution.
   -
   - Content of the sources list frame, it rely on the Tiles configuration.
   -
   - @author: Samuel Kerrien (skerrien@ebi.ac.uk) & Alexandre Liban (aliban@ebi.ac.uk)
   - @version: $Id$
-->


<html:html>

<head>
    <base target="_top" />

    <meta http-equiv="cache-control" content="no-cache, no-store, must-revalidate" />
    <meta http-equiv="pragma" content="no-cache" />
    <meta http-equiv="expires" content="-1" />

<%
    // check if the session is still opened
    IntactUserI user = (IntactUserI) IntactContext.getCurrentInstance().getSession().getAttribute(Constants.USER_KEY);

    boolean checkSession = true;

    if (null == user)
    {
        checkSession = false;
    }
%>

    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/layouts/styles/hv.css" />
</head>

<%
    // session is opened
    if( checkSession ) {
%>

    <body bgcolor="#FFFFFF" topmargin="0" leftmargin="0">

        <table border="1" cellpadding="2" cellspacing="0" width="100%" height="100%">

              <tr>
                     <td width="40%" height="15" valign="top">
                            <!-- Top Right cell: Display the highligh tool title -->
                            <tiles:insert definition="highlightTitle" />
                     </td>
              </tr>

              <tr>
                     <td width="40%" valign="top">
                            <!-- Bottom Right cell: Display the highligh tool -->
                            <tiles:insert definition="highlight" />
                     </td>
              </tr>

        </table>

    </body>

<%
    }

    // session is closed : the whole page is reloaded (activation of restoreContext)
    else {
%>

    <body bgcolor="#FFFFFF" topmargin="0" leftmargin="0" onLoad="javascript:parent.top.location.reload();" />

<%
    }
%>

</html:html>
