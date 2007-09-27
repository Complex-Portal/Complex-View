 <!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - This page accepts changes to an Annotated object's short label and full name.
  --%>

<%@ page language="java"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/intact.tld" prefix="intact"%>

<table width="100%" border="0" cellspacing="1" cellpadding="2">
    <tr class="tableRowHeader">
        <th class="tableCellHeader">
            <bean:message key="label.ac"/>
        </th>
        <th class="tableCellHeader">
            <bean:message key="label.shortlabel"/>
        </th>
        <th class="tableCellHeader">
            <bean:message key="label.fullname"/>
        </th>
        <th>
            <intact:documentation section="editor.short.labels"/>
        </th>
    </tr>
    <tr class="tableRowEven">
        <td class="tableCell">
            <bean:write property="ac" name="cvForm" filter="false"/>
        </td>
        <td class="tableCell">
            <html:text property="shortLabel" size="20" maxlength="20" name="cvForm"
                styleClass="inputRequired" onkeypress="return handleEnter(this, event)"/>
        </td>

        <td class="tableCell">
            <html:text property="fullName" size="100" maxlength="250" name="cvForm" onkeypress="return handleEnter(this, event)"/>
        </td>
    </tr>
    <tr class="tableRowEven">
        <td class="tableCell" colspan="2">
            <bean:message key="label.creation"/> <bean:write property="created" name="cvForm" /> by <bean:write property="creator" name="cvForm" />.
        </td>
        <td class="tableCell" colspan="2">
            <bean:message key="label.update"/> <bean:write property="updated" name="cvForm" /> by <bean:write property="updator" name="cvForm" />.
        </td>
    </tr>
</table>
<html:messages id="msg" property="shortLabel">
    <font color="red"><li><bean:write name="msg" /></li></font>
</html:messages>
