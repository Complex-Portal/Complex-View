<%@ page import="java.util.*" %>
<%@ page import="java.lang.*" %>

<html>
	<head>
		<title> ERROR </title>
	</head>

	<body>
		<center>
		<h2><FONT COLOR="#FF6600">	ERROR:  <FONT></h2>
    	 
		<p><FONT COLOR="#FF6600">
				<%
					String type = (String)request.getAttribute("ErrorType");
					
					if (type.equals("Exception")){
						Exception exception = (Exception)request.getAttribute("ErrorMessage");
						out.println(exception.getMessage());
					}
					if (type.equals("SimpleError")){
						String message = (String)request.getAttribute("ErrorMessage");
						out.println(message);
					}
				%>
		<FONT></p>
		</center>
		
	</body>
</html>