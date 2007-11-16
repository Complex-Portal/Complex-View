<%@ page import="org.apache.commons.lang.exception.ExceptionUtils" %>
<%@ page import="uk.ac.ebi.intact.application.editor.struts.framework.util.EditorExceptionHandler" %>
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

    %>

    <a id="displayMore" href="#" onclick="toggle('exception')">Display more information</a>
    <a id="hide" href="#" onclick="toggle('exception')" style="display:none;">Hide information</a>

    <div id="exception" style="color:gray; display:none;">
        <%
                out.println(ExceptionUtils.getFullStackTrace(t));
            }
        %>


    </div>

</logic:messagesPresent>
</hr>