<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - The part to add a new range to the current feature.
  --%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>

<%-- The current view --%>
<c:set var="view" value="${user.view}"/>

<%-- The boolean menu for link/undetermined --%>
<c:set var="boolean_menu" value="${view.booleanMenu}"/>

<%-- The anchor name for this page --%>
<a name="feature.save.range"/>

<%-- The table id is for testing purposes --%>
<table width="50%" border="0" cellspacing="1" cellpadding="2" id="add.range">
    <tr class="tableRowHeader">
        <th class="tableCellHeader">
            <bean:message key="label.action"/>
        </th>
        <th class="tableCellHeader">From Range</th>
        <th class="tableCellHeader">To Range</th>
        <th class="tableCellHeader">Link</th>
    </tr>

    <tr class="tableRowEven">

        <td class="tableCell" align="right" valign="top">
            <html:submit property="dispatch" titleKey="feature.range.button.add.titleKey">
                <bean:message key="feature.range.button.add"/>
            </html:submit>
        </td>

        <td class="tableCell">
            <html:text property="newRange.fromRange" size="20" maxlength="20" name="featureForm"
                styleClass="inputRequired" onkeypress="return handleEnter(this, event)"/>
            <br/><html:messages id="msg" property="new.fromRange">
                <font color="red"><li><bean:write name="msg" /></li></font>
            </html:messages>
        </td>

        <td class="tableCell">
            <html:text property="newRange.toRange" size="20" maxlength="20" name="featureForm"
                styleClass="inputRequired" onkeypress="return handleEnter(this, event)"/>
            <html:messages id="msg" property="new.toRange">
                <font color="red"><li><bean:write name="msg" /></li></font>
            </html:messages>
        </td>

        <td class="tableCell" align="left" valign="top">
            <html:select property="newRange.link" name="featureForm" styleClass="inputRequired">
                <html:options name="boolean_menu"/>
            </html:select>
        </td>
    </tr>
</table>

<%-- Display errors for invalid range intervals --%>
<html:messages id="msg" property="new.range">
    <font color="red"><li><bean:write name="msg" /></li></font>
</html:messages>
