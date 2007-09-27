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
<%@ taglib uri="/WEB-INF/tld/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>

<h1><font color="red">Validation Error</font></h1>
You must correct the following error(s) before proceeding:
<logic:messagesPresent>
    <ul>
         <html:messages id="msg">
            <%-- If the filter is false, it prevent bean:write to convert HTML to text --%>
            <li><bean:write name="msg" filter="false" /></li>
         </html:messages>
    </ul>
</logic:messagesPresent>
</hr>