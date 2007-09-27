<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - This page accepts changes to an Annotated object's short label and full name.
  --%>

<%@ page language="java"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>

<%-- Display the mutation only for a new feature --%>
<c:if test="${user.view.newFeature}">
    <html:submit titleKey="feature.mutation.toggle.button.titleKey"
        onclick="skipValidation();" property="dispatch">
        <bean:message key="feature.mutation.toggle.button"/>
    </html:submit>
</c:if>

<h3>Parent protein</h3>

<%-- The table id is for testing purposes --%>
<table width="100%" border="0" cellspacing="1" cellpadding="2" id="parent.protein">
    <tr class="tableRowHeader">
        <th class="tableCellHeader">
            <bean:message key="feature.parent.label.ac"/>
        </th>
        <th class="tableCellHeader">
            <bean:message key="label.shortlabel"/>
        </th>
        <th class="tableCellHeader">
            <bean:message key="label.fullname"/>
        </th>
    </tr>
    <tr class="tableRowEven">
        <td class="tableCell">
            <bean:write property="parentAc" name="featureForm" filter="false"/>
        </td>
        <td class="tableCell">
            <bean:write property="parentShortLabel"name="featureForm"/>
        </td>

        <td class="tableCell">
            <bean:write property="parentFullName" name="featureForm"/>
        </td>
    </tr>
</table>
