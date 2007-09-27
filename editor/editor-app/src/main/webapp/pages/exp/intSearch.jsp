<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - The page to search for Interactions.
  --%>

<%@ page language="java"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/intact.tld" prefix="intact"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>

<%-- Need this to access the interaction limit. --%>
<jsp:useBean id="service" scope="application"
    beanName="uk.ac.ebi.intact.application.editor.business.EditorService"
    type="uk.ac.ebi.intact.application.editor.business.EditorService"/>

<%-- A message to indicate that current interactions equal to max allowed --%>
<c:if test="${user.view.numberOfInteractions eq service.interactionLimit}">
    <div class="warning">
        <bean:message key="message.ints.limit.search"
            arg0='<%=service.getResource("exp.interaction.limit")%>'/>
    </div>
</c:if>

<%-- Only display the table if the interactions are less (<) than the allowed limit --%>
<c:if test="${user.view.numberOfInteractions lt service.interactionLimit}">

    <%-- The anchor name for this page --%>
    <a name="exp.int.search"/>

    <table width="50%" border="0" cellspacing="1" cellpadding="2">
        <tr class="tableRowHeader">
            <th class="tableCellHeader" width="10%" colspan="2">
                <bean:message key="label.action"/>
            </th>
            <th class="tableCellHeader" width="30%"/>
            <th>
                <%-- This NEED to be FIXED --%>
                <intact:documentation section="editor.int.experiments"/>
            </th>
        </tr>
        <tr class="tableRowEven">
            <td class="tableCell">
                <html:submit titleKey="exp.int.button.recent.titleKey"
                    property="dispatch">
                    <bean:message key="exp.int.button.recent"/>
                </html:submit>
            </td>
            <td class="tableCell">
                <html:submit titleKey="exp.int.button.search.titleKey"
                    property="dispatch">
                    <bean:message key="exp.int.button.search"/>
                </html:submit>
            </td>
            <td class="tableCell">
                <html:text property="searchValue" size="20" maxlength="20" onkeypress="return handleEnter(this, event)"/>
            </td>
        </tr>
    </table>
</c:if>
<html:messages id="msg" property="err.search">
    <font color="red"><li><bean:write name="msg" /></li></font>
</html:messages>
