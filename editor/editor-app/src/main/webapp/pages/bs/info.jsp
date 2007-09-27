<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - The information page for BioSource editor. This page displays (accepts changes)
  - shortlabel, fullname, tax id, cell type and tissue.
  --%>

<%@ page language="java"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/intact.tld" prefix="intact"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>

<%-- The menus --%>
<c:set var="menus" value="${user.view.menus}"/>

<%-- Individual menu lists --%>
<c:set var="tissuemenu" value="${menus['Tissue']}"/>
<c:set var="cellmenu" value="${menus['Cell']}"/>

<%-- The anchor name for this page --%>
<a name="info"/>

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
            <bean:write property="ac" name="bsForm" filter="false"/>
        </td>

        <td class="tableCell">
            <html:text property="shortLabel" size="20" maxlength="20"
                name="bsForm" styleClass="inputRequired" onkeypress="return handleEnter(this, event)"/>
        </td>

        <td class="tableCell">
            <html:text property="fullName" size="100" maxlength="250" name="bsForm" onkeypress="return handleEnter(this, event)"/>
        </td>
    </tr>
    <tr class="tableRowEven">
        <td class="tableCell" colspan="2">
            <bean:message key="label.creation"/> <bean:write property="created" name="bsForm" /> by <bean:write property="creator" name="bsForm" />.
        </td>
        <td class="tableCell" colspan="2">
            <bean:message key="label.update"/> <bean:write property="updated" name="bsForm" /> by <bean:write property="updator" name="bsForm" />.
        </td>
    </tr>
</table>
<html:messages id="msg" property="shortLabel">
    <font color="red"><li><bean:write name="msg" /></li></font>
</html:messages>

<p></p>

<table width="50%" border="0" cellspacing="1" cellpadding="2">
    <tr class="tableLinkRowHeader">
        <th class="tableCellHeader" width="30%">
            <bean:message key="label.action"/>
        </th>
        <th>
            <bean:message key="biosource.label.tax"/>
        </th>
        <th>
            <a href="javascript:showColumnLink('CvTissue',
                document.forms['bsForm'].elements['tissue'].value)">
                <bean:message key="biosource.label.tissue"/>
            </a>
        </th>
        <th>
            <a href="javascript:showColumnLink('CvCellType',
                document.forms['bsForm'].elements['cellType'].value)">
                <bean:message key="biosource.label.cell"/>
            </a>
        </th>
    </tr>
    <tr class="tableRowEven">
        <td class="tableCell">
            <html:submit property="dispatch" onclick="skipValidation();"
                titleKey="biosource.button.taxid.titleKey">
                <bean:message key="biosource.button.taxid"/>
            </html:submit>
        </td>

        <td class="tableCell">
            <html:text property="taxId" name="bsForm" size="10" maxlength="16"
                styleClass="inputRequired" onkeypress="return handleEnter(this, event)"/>
            <html:messages id="msg" property="taxId">
                <font color="red"><li><bean:write name="msg" /></li></font>
            </html:messages>
        </td>

        <td class="tableCell" align="left" valign="top">
            <html:select property="tissue" name="bsForm">
                <html:options name="tissuemenu"/>
            </html:select>
        </td>

        <td class="tableCell" align="left" valign="top">
            <html:select property="cellType" name="bsForm">
                <html:options name="cellmenu"/>
            </html:select>
        </td>
    </tr>
</table>
<html:messages id="message" message="true">
	<span class="warning">
		<bean:write name="message" filter="false"/>
	</span>	
</html:messages>

<html:messages id="msg" property="bs.taxid">
    <font color="red"><li><bean:write name="msg" /></li></font>
</html:messages>
<html:messages id="msg" property="bs.sanity.taxid.dup">
    <font color="red"><li><bean:write name="msg" /></li></font>
</html:messages>
        