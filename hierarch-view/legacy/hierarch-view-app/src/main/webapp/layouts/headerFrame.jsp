<%@ page language="java"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<%--
    Content of the header frame, it rely on the Tiles configuration.

    Author: Samuel Kerrien (skerrien@ebi.ac.uk)
    Version: $Id$
--%>

<html:html>

<head>
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="expires" content="-1">
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/layouts/styles/intact.css"/>
</head>

<body topmargin="0" leftmargin="0">

   <tiles:insert definition="intact.header.layout" ignore="true"/>

</body>
</html:html>
