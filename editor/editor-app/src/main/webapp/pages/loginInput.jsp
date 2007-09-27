<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>

<%--
    Defines the login input dialog window.
    Author: Sugath Mudali (smudali@ebi.ac.uk)
    Version: $Id$
--%>

<html:form action="/login" focus="username" onsubmit="return validateLoginForm(this)">

    <%-- Append ac and search class parameters if they are present --%>
    <c:if test="${not empty param.ac and not empty param.type}">
        <html:hidden property="ac" value="<%=request.getParameter(\"ac\")%>"/>
        <html:hidden property="type" value="<%=request.getParameter(\"type\")%>"/>
    </c:if>

    <table border="0" width="100%">

        <tr>
            <th align="right">
                <bean:message key="loginForm.label.username"/>
            </th>
            <td align="left">
                <html:text property="username" size="10" maxlength="16"/>
            </td>
        </tr>

        <tr>
            <th align="right">
                <bean:message key="loginForm.label.password"/>
            </th>
            <td align="left">
                <html:password property="password"
                        size="10" maxlength="16" redisplay="false"/>
            </td>
        </tr>

        <tr>
            <td align="left">
                <html:submit property="submit">
                    <bean:message key="loginForm.button.login"/>
                </html:submit>
            </td>
        </tr>
    </table>
</html:form>
<html:javascript formName="loginForm"/>
