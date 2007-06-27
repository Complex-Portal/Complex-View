<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - The error page to display struts and JSP errors.
  --%>

<%@ page language="java" %>
<%@ page isErrorPage="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>

<h1><font color="red">Error</font></h1>

<%-- Struts errors --%>
<logic:messagesPresent>
    <ul>
         <html:messages id="error">
            <%-- If the filter is false, it prevent bean:write to convert HTML to text --%>
            <li><bean:write name="error" filter="false" /></li>
         </html:messages>
    </ul>
</logic:messagesPresent>

<%-- JSP errors (e.g., unable to get a list of species for the welcome.jsp --%>
<logic:messagesNotPresent>
    <h3>An unexcepted error occurred. Please report the following error to the Intact team</h3>
    <ul>
        <li><%=exception%></li>
    </ul>
</logic:messagesNotPresent>
</hr>