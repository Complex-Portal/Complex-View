<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<%
        String path = request.getScheme()+"://" +
                      request.getServerName()+":" +
                      request.getServerPort() +
                      request.getContextPath();
    %>

<form name="loginForm" action="<%=path%>/j_spring_security_check" method="POST">

    <table border="0" width="100%">

        <tr>
            <th align="right">
                Username
            </th>
            <td align="left">
                <input name="j_username" type="text" size="10" maxlength="16"/>
            </td>
        </tr>

        <tr>
            <th align="right">
                Password
            </th>
            <td align="left">
                <input name="j_password" type="password"
                        size="10" maxlength="16"/>
            </td>
        </tr>

        <!--
        <tr>
            <th align="right">
                Remember Me
            </th>
            <td align="left">
                <input type="checkbox" checked="true" disabled="true" />
            </td>
        </tr>
            -->
        <tr>
            <td align="left">
                <input type="submit" value="Login"/>
            </td>
        </tr>
    </table>
</form>
