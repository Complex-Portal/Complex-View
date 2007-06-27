<%@ page language="java"%>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>

<!--
   - Copyright (c) 2002 The European Bioinformatics Institute, and others.
   - All rights reserved. Please see the file LICENSE
   - in the root directory of this distribution.
   -
   - Try to restore the last user query.
   -
   - @author Samuel Kerrien (skerrien@ebi.ac.uk)
   - @version $Id$
-->

<html:html>

<head>
    <base target="_top">
    <meta http-equiv="cache-control" content="no-cache" />
    <meta http-equiv="pragma" content="no-cache" />
    <meta http-equiv="expires" content="-1" />
<%
    String url = (String) session.getAttribute ("restoreUrl");
    if (url != null) {
        // delete the URL in the session
        session.removeAttribute ("restoreUrl");
%>
        <script>
           setTimeout("top.location = '<%= url %>'", 1000);
        </script>
<%
    }
%>

</head>

<body bgcolor="#FFFFFF" topmargin="0" leftmargin="0">

<%
    if (url != null) {
%>

<blockquote>
    <blockquote>
        <blockquote>
            <table>
              <tr>
                <td>
                    <img src="<%=request.getContextPath()%>/images/clockT.gif" border="0" />
                </td>
                <td>
                    <strong>
                        <font color="#000080">
                            A long inactivity has been detected, please wait whilst reloading your last query.<br>
                            If the screen is not refreshed, please click <a href="<%= url %>" target="_top">here</a>.
                        </font>
                    </strong>
                </td>
              <tr>
            </table>
        </blockquote>
    </blockquote>
</blockquote>

<%
    } else {
%>
    <%-- no URL for restoring the last context, error message --%>
    Unable to restore your last application context,<br />
    please go to the hierarchView <a href="<%=request.getContextPath()%>" target="_top">home page</a>.
<%
    }
%>
</body>
</html:html>
