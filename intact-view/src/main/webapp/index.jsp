<%--<%@ page import="java.io.File" %>--%>
<%--<%@ page import="java.util.Enumeration" %>--%>
<%--<%@ page import="uk.ac.ebi.intact.view.webapp.controller.application.UserSessionConfig" %>--%>
<%--<%@ page session="true"%>--%>
<%--<%--%>

//    UserSessionConfig userSessionConfig = (UserSessionConfig) request.getSession().getAttribute("userSessionConfig");
//
//    if (userSessionConfig == null) {
//        Enumeration e = request.getSession().getAttributeNames();
//        while (e.hasMoreElements()) {
//            Object o =  e.nextElement();
//            System.out.println("SESSION: "+o);
//        }
//        throw new ServletException("UserSessionConfig was null. Check the attribute name.");
//    }
//
//    String configFile = userSessionConfig.getIntactViewConfiguration().getConfigFile();
//
//
//    if (configFile != null && new File(configFile).exists()) {
//        response.sendRedirect("main.xhtml");
//    } else {
//        response.sendRedirect("first_time_config.xhtml");
//    }
//
<%--%>--%>

<jsp:forward page="main.xhtml"/>