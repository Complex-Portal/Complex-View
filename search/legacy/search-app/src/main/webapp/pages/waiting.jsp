<%@ page language="java" %>
<%@ page import="uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants" %>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<%--
    Intact default look & feel layout. It consists of a sidebar and a display
    area. The display area conatns the header, contents and the footer as
    shown below:
    +---------------------+
    | Organization header +
    |---------------------+
    | side | header       +
    | bar  | contents     +
    |      | footer       +
    |---------------------+

    Author: Christian Kohler, Samuel Kerrien
    Version: $Id$
--%>

<html:html>
<head>
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="expires" content="-1">

    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/layouts/styles/intact.css"/>

    <style type="text/css">
        .bgboxbody {
            background-color: #EEEEEE;
        }

        .border2 {
            border-width: 1px;
            border-style: solid;
            border-color: #188429;
        }

    </style>

    <%
        // Collecting URL to forward to.
        String url = (String) request.getAttribute(SearchConstants.WAITING_URL);
        if (url == null) {
            url = "Could not find a URL to forward to.";
        }

        // Collect message to display on the waiting page.
        String message = (String) request.getAttribute(SearchConstants.WAITING_MSG);
        if (message == null) {
            message = "";
        }

        // Collect waiting delay before redirect.
        Integer time = (Integer) request.getAttribute( SearchConstants.WAITING_TIME );
        if (time == null) {
            time = SearchConstants.DEFAULT_WAITING_TIME;
        }

        // Collect waiting delay before redirect.
        Boolean doNotForward = (Boolean) request.getAttribute( SearchConstants.DO_NOT_FORWARD );
        if (doNotForward == null) {
            doNotForward = Boolean.FALSE;;
        }
    %>

    <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">

    <%
        // if there is NO request for NO forward, then apply meta tag.
       if( doNotForward == Boolean.FALSE ) {
    %>
    <meta http-equiv="refresh" content="<%= time %>; url=<%= url %>">
    <%
        }
    %>

    <!-- IntAct dynamic application should not be indexed by search engines -->
    <meta name='ROBOTS' content='NOINDEX'>

    <title>Please wait while we are processing your request...</title>

    <!-- Don't put the base tag here; let the browser sort out the URLs -->
    <link rel="stylesheet" type="text/css"
          href="<%=request.getContextPath()%>/layouts/styles/intact.css"/>

    <!-- Needed to setup Tooltip for buttonBar.html. -->
    <script type="text/javascript" src="<%= request.getContextPath()%>/layouts/overLIB/overlib.js"></script>

</head>

<body bgcolor="#FFFFFF" topmargin="0" leftmargin="0">
<table border="0" height="100%" width="100%" cellspacing="5">


    <tr>
            <%-- Sidebar section --%>
        <%-- error does not affect application !--%>
        <td bgcolor="#cccccc" width='113' valign='top' align='left'>
            <tiles:insert attribute="sidebar"/>
        </td>


        <td valign="top" height="100%" width="*">
            <table width="100%" height="100%">

                    <%-- Application Header section --%>
                <tr>
                    <%-- error does not affect application !--%>
                    <td valign="top" height="5%">
                        <tiles:insert attribute="header"/>
                    </td>

                </tr>
                <tr>
                    <td height="3" background="<%=request.getContextPath()%>/images/hor.gif"></td>
                </tr>
                    <%-- Content section --%>
                <tr>
                    <td valign="top" height="*">
                        <!-- No errors if none specified -->

                            <%--
                                Waiting Page is called and displayed twice:
                                1. forwarded to waiting.jsp by WaitingInterProSearchAction.java
                                        Page to first display a summary of all the Protein-Intact ACs, when a user
                                        selects Proteins, Interactions or Experiments on the IntAct Search-application.

                                2. forwarded to waiting.jsp by InterProSearchAction.java
                                        Secondly, it will show a list of all found corresponding UniProtKB IDs.
                                        Also error/warning messages will be displayed.
                                        Finally it will then be forwarded to the corresponding entries on the InterPro-website.
                            --%>

                        <br><br><br><br>

                        <center>

                            <table class="border2" border="0" cellpadding="15" cellspacing="0">
                                <tbody>
                                    <font face="helvetica, arial" color="#003063">
                                        <tr>
                                            <td class="bgboxbody">
                                                <center>
                                                    <b><big>Please wait while we are processing your request...</big>
                                                    </b>
                                                    <br>
                                                    <br>
                                                    <img src="<%= request.getContextPath() %>/images/wait_ball_green.gif">
                                                </center>
                                            </td>
                                            <tr>
                                                <td class="bgboxbody">
                                                    <%= message %>
                                                </td>
                                            </tr>

                                        </tr>
                                    </font>
                                </tbody>
                            </table>
                        </center>
                    </td>
                </tr>

                    <%-- The footer --%>
                <tr>
                    <%-- error does not affect application !--%>
                    <td valign="bottom" height="10%">
                        <tiles:insert attribute="footer"/>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>
</body>
</html:html>
