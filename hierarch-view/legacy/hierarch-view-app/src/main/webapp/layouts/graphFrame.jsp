<%@ page language="java"%>
<%@ taglib uri="http://struts.apache.org/tags-html"  prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.ebi.ac.uk/intact/hierarch-view" prefix="hierarchView" %>

<!--
   - Copyright (c) 2002 The European Bioinformatics Institute, and others.
   - All rights reserved. Please see the file LICENSE
   - in the root directory of this distribution.
   -
   - Content of the graph frame, it rely on the Tiles configuration.
   -
   - @author: Samuel Kerrien (skerrien@ebi.ac.uk)
   - @version: $Id$
-->

    <!--
        Save current context (before the HTML header)
    -->
    <hierarchView:saveContextInCookie/>


<html:html>

<head>
    <base target="_top">

    <meta http-equiv="cache-control" content="no-cache, no-store, must-revalidate">
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="expires" content="-1">
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/layouts/styles/intact.css"/>
</head>

<body bgcolor="#FFFFFF" topmargin="0" leftmargin="0">

    <table border="1" cellpadding="3" cellspacing="0" width="100%" height="100%">

          <tr>
                 <td width="60%" valign="top">
                      <!-- Top Left cell: displays the interaction network title -->
                      <tiles:insert definition="hierarchView.graphTitle.layout" ignore="true"/>
                 </td>
          </tr>

          <tr>
                 <td width="40%" valign="top">
                       <!-- Bottom Left cell: displays the interaction network -->
                       <tiles:insert definition="hierarchView.graph.layout" ignore="true"/>
                 </td>
          </tr>

    </table>

</body>
</html:html>
