<%@ page import="org.apache.commons.lang.exception.ExceptionUtils" %>
<%@ page import="org.joda.time.DateTime" %>
<%@ page import="uk.ac.ebi.intact.application.editor.struts.framework.util.EditorExceptionHandler" %>
<%@ page import="uk.ac.ebi.intact.context.IntactContext" %>
<%@ page import="java.util.Properties" %>
<%@ page import="uk.ac.ebi.intact.application.editor.business.EditorService" %>
<%@ page import="java.util.Date" %>
<!--
- Author: Sugath Mudali (smudali@ebi.ac.uk)
- Version: $Id: error.jsp,v 1.5 2003/03/27 17:34:08 skerrien Exp $
- Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
- All rights reserved. Please see the file LICENSE in the root directory of
- this distribution.
-->

<%--
  - Displays an error message stored in the struts framework.
  --%>

<%@ page language="java" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>

<h1><font color="red">Error</font></h1>
You must correct the following error(s) before proceeding:
<logic:messagesPresent>
    <ul>
        <html:errors/>
            <%-- If the filter is false, it prevent bean:write to convert HTML to text --%>
            <%--<li><bean:write name="msg" filter="false" /></li>--%>
            <%--</html:errors>--%>
    </ul>

    <script type="text/javascript">
        function toggle(a) {
            var e = document.getElementById(a);
            if (!e)return true;
            if (e.style.display == "none") {
                e.style.display = "block"
                document.getElementById('displayMore').style.display = 'none';
                document.getElementById('hide').style.display = 'block';
            } else {
                e.style.display = "none"
                document.getElementById('displayMore').style.display = 'block';
                document.getElementById('hide').style.display = 'none';
            }
            return true;

        }
    </script>

    <%
        Throwable t = (Throwable) request.getAttribute(EditorExceptionHandler.EXCEPTION_PARAM);

        if (t != null) {
            final String fullStackTrace = ExceptionUtils.getFullStackTrace(t);

    %>

    <!--<p>If you want to report this bug to the developers, display the information below and send the whole stack trace to them.<p/>-->
    <%--<a id="displayMore" href="#" onclick="toggle('exception')" style="display:none;">Display more information</a>--%>
    <%--<a id="hide" href="#" onclick="toggle('exception')" >Hide information</a>--%>

    <%
        String jira = "http://www.ebi.ac.uk/interpro/internal-tools/jira-intact";
        int pid = 10181;
        int priority = 3;
        String summary = "Editor Exception: "+fullStackTrace.split("\n")[0];
        String reporter = IntactContext.getCurrentInstance().getUserContext().getUserId().toLowerCase();
        int component = 10437;

        Properties props = new Properties();
        props.load(EditorService.class.getResourceAsStream("/uk/ac/ebi/intact/application/editor/BuildInfo.properties"));

        String version = props.getProperty("build.version");
        String build = props.getProperty("buildNumber");

        String description = "<Please, explain what you were doing here>%0D%0D---- Stack Trace ---%0D%0D"+
                             new Date()+"%0DEditor Version: "+version+
                             "%0DCore Version: "+props.getProperty("core.version")+
                             "%0DBridges Version: "+props.getProperty("bridges.version")+
                             "%0DBuild: "+props.getProperty("buildNumber")+
                             "%0D%0D"+fullStackTrace;


        String url = jira+"/secure/CreateIssueDetails!init.jspa?pid="+pid+"&issuetype=1&priority="+priority+"&summary="+summary+"&reporter="+reporter+"&components="+component+"&description="+description+"&customfield_10010="+build;
    %>

    <p>
    <a href="<%=url%>">Report to JIRA</a> (use version: <b><%=version%></b> in the <i>Affects version/s</i> field when reporting)
    </p>

    <div id="exception" style="padding: 10px; color:black; border: thin solid gray; font-family: 'Courier New', Courier, Sans-serif; background-color: #DEDEDE">
        <%

                out.println("["+new DateTime()+"] "+ fullStackTrace);
            }
        %>


    </div>

</logic:messagesPresent>
</hr>