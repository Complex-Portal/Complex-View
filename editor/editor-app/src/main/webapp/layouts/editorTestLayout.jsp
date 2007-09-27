<%@ page language="java"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>

<%--
    The layout for the test editor pages. Identical to the intactLayout except
    for changes to:

    1. onload Javascript for loading the page. So, the onload method is only
    called upon loading an edit page.

    2. Title is hardcoded as Editor.
    3. Content is always present.

    Author: Sugath Mudali (smudali@ebi.ac.uk)
    Version: $Id$
--%>

<html:html>
<head>
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="expires" content="-1">
    <title>Editor</title>
    <!-- Don't put the base tag here; let the browser sort out the URLs -->
    <link rel="stylesheet" type="text/css"
        href="<%=request.getContextPath()%>/layouts/styles/intact.css"/>
</head>

<script language="JavaScript" type="text/javascript">
    // The Javascript function to go to a page anchor.
    function goToAnchor() {
        // Here we are assuming that the anchor is in the first form.
        var anchor = document.forms[0].anchor.value;
        //window.alert(anchor);
        if (anchor.length != 0) {
           // Only go to the anchor if it has been set up.
           location.href='#' + anchor;
           document.forms[0].anchor.value = '';
        }
    }
</script>

<body bgcolor="#FFFFFF" topmargin="0" leftmargin="0" onload="goToAnchor();">
<table border="0" height="100%" width="100%" cellspacing="5">

<tr>
    <%-- Sidebar section --%>
    <td bgcolor="#D8BFD" width='113' valign='top' align='left'>
        <tiles:insert attribute="sidebar"/>
    </td>

    <td valign="top" height="100%" width="*">
        <table width="100%" height="100%">

            <%-- Application Header section --%>
            <tr>
                <td valign="top" height="5%">
                    <tiles:insert attribute="header"/>
                </td>

            </tr>
            <tr>
                <td height="3" background="<%=request.getContextPath()%>/images/hor.gif"></td>
            </tr>
            <%-- Content section --%>
            <tr>
                <td valign="top" height="*">
                    <tiles:insert attribute="content"/>
                </td>
            </tr>

            <%-- The footer --%>
            <tr>
                <td valign="bottom" height="10%">
                    <tiles:insert attribute="footer"/>
                </td>
            </tr>
        </table>
    </td>
</tr>
</table>
</body>
</html:html>
