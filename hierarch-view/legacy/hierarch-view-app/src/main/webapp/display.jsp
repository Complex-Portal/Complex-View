<%@ page language="java" %>

<!--
   - Copyright (c) 2002 The European Bioinformatics Institute, and others.
   - All rights reserved. Please see the file LICENSE
   - in the root directory of this distribution.
   -
   - Allows to submit a query without passing by the home page.
   -
   - @author Samuel Kerrien (skerrien@ebi.ac.uk)
   - @version $Id$
-->

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>

<html:html>

<head>
     <title>hierarchView: external request management</title>
</head>

<body bgcolor="white">

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
                                    Please wait, your request is being processed.
                                    <br />
                                    Graphical view of the interaction network centered on the
                                    Interactor AC: <%= request.getParameter ("AC") %>.
                                </font>
                            </strong>
                        </td>
                      <tr>
                    </table>
                </blockquote>
            </blockquote>
        </blockquote>

    <form action="<%=request.getContextPath()%>/display.do" method="post">
          <input type="hidden" name="host" />
          <input type="hidden" name="protocol" />
          <input type="hidden" name="AC"         value="<%= request.getParameter ("AC")     %>" />
          <input type="hidden" name="depth"      value="<%= request.getParameter ("depth")  %>" />
          <input type="hidden" name="method"     value="<%= request.getParameter ("method") %>" />
          <input type="hidden" name="network"    value="<%= request.getParameter ("network") %>" />
          <input type="hidden" name="singletons" value="<%= request.getParameter ("singletons") %>" />
    </form>

    <script language="JavaScript" type="text/javascript">
        <!--
            // gives the current host and protocol from the browser URL.
            document.forms[0].host.value     = window.location.host;
            document.forms[0].protocol.value = window.location.protocol;

            // submit the form automatically toward the entry page
            document.forms[0].submit();
        //-->
    </script>


</body>

</html:html>