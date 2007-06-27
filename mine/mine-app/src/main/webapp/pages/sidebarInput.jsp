<%@ page language="java"  %>

<%--
    This layout displays the search box to search the CV database.
    Author: Andreas Groscurth (groscurt@ebi.ac.uk)
    Version: $Id: sidebarInput.jsp,v 1.3 2003/08/29 17:01:47 skerrien Exp $
--%>

<%@ page import="uk.ac.ebi.intact.application.mine.business.IntactUserI,
				uk.ac.ebi.intact.application.mine.business.Constants" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/intact.tld"      prefix="intact"%>

<% IntactUserI user = (IntactUserI)session.getAttribute(Constants.USER);
   String search = user.getSearch();
%>
<form action="<%=request.getContextPath()%>/do/search" focus="AC">
    <table>
     <tr>
        <th align="left"><bean:message key="sidebar.search.title"/>
		<intact:documentation section="mine.search" /></th>
     </tr>
     <tr>
        <td><html:text property="AC" value="<%= search %>" size="12"/></td>
     </tr>
     <tr>
        <td><html:submit property="action" titleKey="sidebar.search.button.submit.title">
                  <bean:message key="sidebar.search.button.submit"/>
            </html:submit>
		</td>
      </tr>
    </table>
</form>
