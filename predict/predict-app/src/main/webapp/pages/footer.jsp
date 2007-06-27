<%@ page language="java"%>

<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - The common footer for all the Intact web applications.
--%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean"%>

<hr>
<span class="footer">
<table width="100%">
<tr>
    <td>
        Please send any questions or suggestions to
            <a href="mailto:intact-help@ebi.ac.uk">intact-help@ebi.ac.uk</a>
        <br/>
        <%-- commented during the reorg to maven
        Last modified: <i><%@ include file="../last-modified.txt" %></i>
        <br/>
        --%>
        Version: <i><bean:message bundle="buildInfo" key="build.version"/></i>
    </td>
    <td class="tableCell">
        <img border="0" align="right" src="<%=request.getContextPath()%>/images/struts-power.gif">
    </td>
</tr>
</table>
</span>