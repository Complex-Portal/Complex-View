<%@ page language="java"  %>
<%@ page buffer="none"    %>
<%@ page autoFlush="true" %>

<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<!--
    The layout for the side bar, which consists of intact logo, input dialog box
    and menu. Except for the logo, other two components are optional.

    Author: Sugath Mudali (smudali@ebi.ac.uk)
    Version: $Id$
-->

<table width="100%" border="0" cellspacing="0" cellpadding="0">

    <%-- Sidebar logo --%>
	<tr>
        <td bgcolor="#boc4de" valign="top" align="left" width="113" height="75">
            <img src="<%=request.getContextPath()%>/images/logo_intact.gif" border="0">
        </td>
    </tr>

    <!-- The input dialog box -->
    <tr>
        <td>
            <tiles:insert attribute="input-dialog" ignore="true"/>
        </td>
    </tr>

    <%-- Sidebar menu links --%>
    <tr>
		<td>
            <tiles:insert attribute="menu" ignore="true"/>
        </td>
	</tr>
</table>