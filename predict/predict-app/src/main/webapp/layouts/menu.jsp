<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ page import="java.util.Iterator" %>

<%-- Menu Layout
  This layout render a menu with links.
  It takes as parameter the title, and a list of items. Each item is a bean with following properties :
  value, href, icon, tooltip.
  @param title Menu title
  @param items list of items. Items are beans whith following properties :

  Note: on any links begining with '/' we add the server URL in the front.

  Part of tiles distribution.
 --%>

<%-- Push tiles attributes in page context --%>
<tiles:importAttribute />

<%
    // Cache it as it is used in many places.
    String serverPath = request.getContextPath();
%>

<table>

    <logic:present name="title">
        <tr>
          <th colspan=2>
            <div align="left"><strong><tiles:getAsString name="title"/></strong></div>
          </th>
        </tr>
    </logic:present>

    <%-- iterate on items list --%>
    <logic:iterate id="item" name="items" type="org.apache.struts.tiles.beans.MenuItem" >
        <%  // Add server url if link start with "/"
            String link = item.getLink();
            if (link.startsWith("/")) {
                // remove the application name from the path
                link = serverPath.substring (0, serverPath.lastIndexOf('/')) + link;
            }
        %>

        <tr>
          <td width="10" valign="top" ></td>
          <td valign="top"  >
             <font size="-1"><a href="<%=link%>">
                <logic:notPresent name="item" property="icon"><%= item.getValue() %></logic:notPresent>

                <logic:present name="item" property="icon">

                    <%
                        // Add site url if link start with "/"
                        String icon = item.getIcon();
                        if (icon.startsWith("/"))
                           icon = serverPath + icon;
                    %>

                    <img src='<%= serverPath %><bean:write name="item" property="icon" scope="page"/>'
                         alt='<bean:write name="item" property="tooltip" scope="page" ignore="true"/>' />

                </logic:present></a>
             </font>
          </td>
        </tr>

    </logic:iterate>

</table>
