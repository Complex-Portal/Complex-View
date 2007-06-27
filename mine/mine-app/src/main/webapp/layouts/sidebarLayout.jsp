<%@ page language="java"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/intact.tld"      prefix="intact"%>

<!--
    The layout for the side bar, which consists of intact logo, contents and the footer.
    and menu.

    Author: Sugath Mudali (smudali@ebi.ac.uk)
    Version: $Id: sidebarLayout.jsp,v 1.2 2003/08/29 22:24:42 hhe Exp $
-->

<table width="100%" height="100%">
    <tr>
        <td valign="top" height="*">
            <table width="100%" border="0" cellspacing="0" cellpadding="0">

                <%-- Sidebar logo --%>
                <tr>
                    <td valign="top" align="left" width="113" height="75">
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
        </td>
    </tr>

    <%-- Sidebar footer --%>
    <tr>
        <span class="footer">
            <td valign="top" height="5%">
                <tiles:insert attribute="footer" ignore="true"/>
            </td>
        </span>
    </tr>
</table>