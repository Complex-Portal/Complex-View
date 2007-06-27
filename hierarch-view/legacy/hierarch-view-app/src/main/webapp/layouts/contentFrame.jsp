<%@ page language="java"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>

<!--
   - Copyright (c) 2002 The European Bioinformatics Institute, and others.
   - All rights reserved. Please see the file LICENSE
   - in the root directory of this distribution.
   -
   - Content of the content frame, it rely on the Tiles configuration.
   -
   - @author: Samuel Kerrien (skerrien@ebi.ac.uk)
   - @version: $Id$
   -
   - update: Alexandre Liban (aliban@ebi.ac.uk) | 13/08/2005
             some fixes on source code to allow the XHTML compatibility and improve the display
             ** XHTML/F ready **
-->

<%
    long timestamp = System.currentTimeMillis();
%>

<html:html>

<head>
    <base target="_top" />

    <meta http-equiv="cache-control" content="no-cache, no-store, must-revalidate" />
    <meta http-equiv="pragma" content="no-cache" />
    <meta http-equiv="expires" content="0" />
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/layouts/styles/hv.css" />
</head>

<frameset cols="63%,*" border=0>

   <frame src="<%=request.getContextPath()%>/layouts/graphFrame.jsp?<%= timestamp %>" name="FOO" />

   <frameset rows="45%,*">
      <frame src="<%=request.getContextPath()%>/layouts/sourceListFrame.jsp?<%= timestamp %>"     name="sourceListFrame" />
      <frame src="<%=request.getContextPath()%>/layouts/selectedSourceFrame.jsp?<%= timestamp %>" name="selectedSourcetFrame" />
   </frameset>

   <noframes>
   Your browser doesn't support frames.
   </noframes>

</frameset>

<body bgcolor="#FFFFFF" topmargin="0" leftmargin="0">

</body>
</html:html>
