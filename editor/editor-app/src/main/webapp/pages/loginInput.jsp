<%@ page import="uk.ac.ebi.intact.application.editor.struts.security.LoginAction" %>
<%@ page import="uk.ac.ebi.intact.util.DesEncrypter" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>

<%--
    Defines the login input dialog window.
    Author: Sugath Mudali (smudali@ebi.ac.uk)
    Version: $Id$
--%>



<html:form action="/login" focus="username" onsubmit="return validateLoginForm(this)">

    <bean:cookie id="uname" name="editor_username" value=""/>
    <bean:cookie id="pword" name="editor_password" value=""/>

    <%
        DesEncrypter encrypter = new DesEncrypter(LoginAction.secretKey());
        String cookieUserName = encrypter.decrypt(uname.getValue());
        String cookiePassword = encrypter.decrypt(pword.getValue());
    %>

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
                <html:text property="username" value="<%=cookieUserName%>" size="10" maxlength="16"/>
            </td>
        </tr>

        <tr>
            <th align="right">
                <bean:message key="loginForm.label.password"/>
            </th>
            <td align="left">
                <html:password property="password" value="<%=cookiePassword%>"
                        size="10" maxlength="16" redisplay="false"/>
            </td>
        </tr>

        <tr>
            <th align="right">
                <bean:message key="loginForm.label.rememberme" />
            </th>
            <td align="left">
                <html:checkbox property="rememberMe"/>
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
