<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - This page displays the defined features for a feature.
  --%>

<%@ page language="java"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>

<h3>Defined feature</h3>

<c:set var="feature" value="${user.view.definedFeature}"/>

<%-- The table id is for testing purposes --%>
<table width="100%" border="0" cellspacing="1" cellpadding="2" id="defined.features">
    <tr class="tableRowHeader">
        <th class="tableCellHeader" width="20%">
            <bean:message key="label.action"/>
        </th>
        <th class="tableCellHeader" width="20%">
            <bean:message key="label.shortlabel"/>
        </th>
        <th class="tableCellHeader" width="20%">Ranges</th>
        <th class="tableCellHeader" width="40%">
            <bean:message key="label.fullname"/>
        </th>
    </tr>

    <tr class="tableRowEven">
        <td class="tableCell">
            <html:submit titleKey="feature.undetermined.clone.button.titleKey"
                property="dispatch" onclick="skipValidation();">
                <bean:message key="feature.undetermined.clone.button"/>
            </html:submit>
        </td>
        <td class="tableCell">
            <c:out value="${feature.shortLabel}"/>
        </td>
        <td class="tableCell">
            <c:out value="${feature.ranges}"/>
        </td>
        <td class="tableCell">
            <c:out value="${feature.fullName}"/>
        </td>
    </tr>
</table>
