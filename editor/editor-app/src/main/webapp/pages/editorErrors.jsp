<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - The error page to display when the client side validation is turned off.
  --%>

<%@ page language="java"%>

<%-- Need this import for constant --%>
<%@ page import="org.apache.struts.action.ActionMessages,
                 uk.ac.ebi.intact.application.editor.struts.framework.util.EditorConstants"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>

<logic:messagesPresent name="<%=ActionMessages.GLOBAL_MESSAGE%>">
    <table width="100%" border="0" cellspacing="1" cellpadding="2">

        <%-- Error messages --%>
<%--        <html:messages id="msg" id="error">--%>
<%--            <tr class="tableRowEven">--%>
<%--                <td class="tableErrorCell"><bean:write name="error" filter="false"/></td>--%>
<%--            </tr>--%>
<%--        </html:messages>--%>

        <%-- Warning messages --%>
        <html:messages id="message" message="true">
            <tr class="tableRowEven">
                <td class="tableCell"><bean:write name="message" filter="false"/></td>
            </tr>
        </html:messages>
    </table>
</logic:messagesPresent>

<%-- Holds the warning type key --%>
<bean:define id="warn_key" value="<%=EditorConstants.SEVERE_WARN%>"/>

<%-- Severe warning messages --%>
<logic:messagesPresent name="<%=warn_key%>">
    <table width="100%" border="0" cellspacing="1" cellpadding="2">
            <tr>
                <td>
                    <span class="severe-warning">
                        <bean:write name="<%=warn_key%>" filter="false"/>
                    </span>
                </td>
            </tr>
    </table>
</logic:messagesPresent>
