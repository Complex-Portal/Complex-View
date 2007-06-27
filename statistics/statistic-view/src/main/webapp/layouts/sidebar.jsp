<%@ page language="java"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>

<!--
    The layout for the side bar, which consists of intact logo, input dialog box
    and menu. Except for the logo, other two components are optional.

    Author: Sugath Mudali (smudali@ebi.ac.uk)
    Version: $Id$
-->

<table width="100%" border="0" cellspacing="0" cellpadding="0">

    <%-- Sidebar logo --%>
	<tr>
        <td bgcolor="#cccccc" valign="top" align="left" width="113" height="75">
            <img src="<%=request.getContextPath()%>/images/logo_intact.gif" border="0">
        </td>
    </tr>

    <!-- filter -->
    <tr>
        <td>
            <tiles:insert attribute="filter" ignore="true"/>
        </td>
    </tr>

    <%-- Sidebar menu links --%>
    <tr>
		<td>
            <tiles:insert attribute="menu" ignore="true"/>
        </td>
	</tr>

    <%-- The footer --%>
    <%-- Sidebar footer --%>
    <tr>
        <span class="footer">
            <td valign="top" height="5%">
                <tiles:insert attribute="footer" ignore="true"/>
            </td>
        </span>
    </tr>
</table>