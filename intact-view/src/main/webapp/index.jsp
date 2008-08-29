<%@ page import="java.io.File" %>
<%@ page session="false"%>
<%
    String configFile = request.getSession().getServletContext().getInitParameter("intact.DEFAULT_CONFIG_FILE");

    if (configFile != null && new File(configFile).exists()) {
        response.sendRedirect("main.xhtml");
    } else {
        response.sendRedirect("first_time_config.xhtml");
    }

%>