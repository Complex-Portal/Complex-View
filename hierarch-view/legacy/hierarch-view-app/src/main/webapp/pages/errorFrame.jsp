<!--
  - Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -
  - Displays an error message stored in the struts framework
  -
  - @author Samuel Kerrien (skerrien@ebi.ac.uk)
  - @version $Id$
-->

<%@ page language="java" %>
<%@ taglib uri="http://struts.apache.org/tags-html"  prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean"  prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://ebi.ac.uk/intact/commons"       prefix="intact" %>
<%@ taglib uri="http://www.ebi.ac.uk/intact/hierarch-view" prefix="hierarchView" %>

<!-- Restore the last context -->
<hierarchView:restoreContextFromCookie/>


<html:html>

<head>
<%--    <html:base target="_top"/>--%>
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="expires" content="-1">
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/layouts/styles/intact.css"/>
</head>

<body bgcolor="#FFFFFF" topmargin="0" leftmargin="0">

<%-- restore eventual errors inorder to be displayed --%>
<intact:restoreErrors/>

<h1><font color="red">Validation Error</font></h1>
You must correct the following error(s) before proceeding:
<logic:messagesPresent>
    <ul>
         <html:messages id="error">
            <%-- If the filter is false, it prevent bean:write to convert HTML to text --%>
            <li><bean:write name="error" filter="false" /></li>
         </html:messages>
    </ul>
</logic:messagesPresent>
</hr>

<%-- Clear errors to avoid their accumulation --%>
<intact:clearErrors/>

</body>
</html:html>