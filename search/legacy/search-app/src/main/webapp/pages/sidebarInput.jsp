<%@ page language="java"  %>
<%@ page buffer="none"    %>
<%@ page autoFlush="true" %>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>

<%--
    This layout displays the search box to search the CV database.
    Author: Sugath Mudali (smudali@ebi.ac.uk)
    Version: $Id$
--%>

<html:form action="/search" focus="searchString">
    <table>
        <tr>
            <td>
                <html:text styleId="searchId" property="searchString" size="16"/>
             </td>
        </tr>
        <tr>
            <td>
                <html:submit titleKey="sidebar.button.search.title">
                    <bean:message key="sidebar.button.search"/>
                </html:submit>
                &nbsp;
                <a onclick="var box = document.getElementById('searchId'); box.value=''; box.focus()" href="#">Clean</a>
            </td>
        </tr>
    </table>
</html:form>
