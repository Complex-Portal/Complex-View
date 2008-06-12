<%@ page import="java.io.File" %>
<%@ page session="false"%>
<%
    String configFile = request.getSession().getServletContext().getInitParameter("psidev.DEFAULT_CONFIG_FILE");

    if (configFile != null && new File(configFile).exists()) {
        response.sendRedirect("pages/binarysearch/binarysearch.xhtml");
    } else {
        response.sendRedirect("pages/binarysearch/first_time_config.xhtml");
    }

%>