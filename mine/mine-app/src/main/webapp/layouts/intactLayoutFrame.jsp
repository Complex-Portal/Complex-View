<%@ page language="java"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld"  prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/intact.tld"       prefix="intact"%>

<%--
    Intact default look & feel layout with frames.
    It consists of a sidebar and a display area.
    The display area conatns the header, contents and the footer as shown below:
    +-----------------+
    | side | header   +
    | bar  | contents +
    |      | footer   +
    |-----------------+

    Author: Samuel Kerrien (skerrien@ebi.ac.uk)
    Version: $Id$
--%>

<%
    long timestamp = System.currentTimeMillis();
%>

<html:html>

<head>
    <title><tiles:getAsString name="title"/></title>

    <base target="_top">
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="expires" content="-1">

    <!-- IntAct dynamic application should not be indexed by search engines -->
    <meta name='ROBOTS' content='NOINDEX'>

    <link rel="stylesheet" type="text/css" href="layouts/styles/intact.css"/>
</head>

    <frameset cols="12%,*" border=0>

       <frame src="<%=request.getContextPath()%>/<tiles:getAsString name="sidebar"/>?<%= timestamp %>" name="sidebarFrame">

       <frameset rows="8%,*, 9%">
          <frame src="<%=request.getContextPath()%>/<tiles:getAsString name="header"/>?<%= timestamp %>"  name="headerFrame">
          <frame src="<%=request.getContextPath()%>/<tiles:getAsString name="content"/>?<%= timestamp %>" name="contentFrame">
          <frame src="<%=request.getContextPath()%>/layouts/footerFrame.jsp?<%= timestamp %>"  name="footerFrame">
       </frameset>

       <noframes>
       Your browser doesn't support frames.
       </noframes>

    </frameset>

<body bgcolor="#FFFFFF" topmargin="0" leftmargin="0">

<intact:saveErrors/>

</body>
</html:html>
