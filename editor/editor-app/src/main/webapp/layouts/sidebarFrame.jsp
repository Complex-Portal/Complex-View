<%@ page language="java"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<%--
    Content of the sidebar frame, it rely on the Tiles configuration.

    Author: Samuel Kerrien (skerrien@ebi.ac.uk)
    Version: $Id$
--%>

<html:html>

<head>
    <base target="_top">
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="expires" content="-1">
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/layouts/styles/intact.css"/>
</head>

<body bgcolor="#cccccc" topmargin="0" leftmargin="0">

   <tiles:insert definition="intact.sidebar.layout" ignore="true"/>

</body>
</html:html>
