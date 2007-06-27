<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/intact.tld"      prefix="intact"%>

<%--
    Intact default look & feel layout for the header.
    Author: Sugath Mudali (smudali@ebi.ac.uk)
    Version: $Id: headerLayout.jsp,v 1.3 2003/08/29 22:24:42 hhe Exp $
--%>
 <table width="100%" height="100%">

            <%-- Application Header section --%>
            <tr>
                <td valign="top" height="5%">
                    <span class="header"><tiles:getAsString name="header.title"/></span>
                    <intact:documentation section="mine" />
                </td>
            </tr>

            <tr>
                <td height="1" background="<%=request.getContextPath()%>/images/hor.gif"></td>
            </tr>

        </table>
