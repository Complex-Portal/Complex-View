<%--
  Created by IntelliJ IDEA.
  User: maitesin
  Date: 13/03/2014
  Time: 14:43
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Complex Web Service</title>
</head>
<body>
<h2>Details method</h2>
** NOTE: We have to force the format with the parameter format in the query because we are using a web browser and is header sensitive, but if you do not give a header you will receive the answer in JSON (default format) **
This method is for query the data base. Then you have to specify an identifier in for query. Please find below different examples about that:
<ul>
    <li>Query for EBI-1163476<br><a href="<%= request.getContextPath() %>/details/EBI-1163476?format=json"><%= request.getContextPath() %>/details/EBI-1163476?format=json</a></li></ul>
</body>
</html>
