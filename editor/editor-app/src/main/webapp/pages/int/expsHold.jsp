<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - Displays Experiments not yest added for an Interaction.
  --%>

<%@ page language="java"%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>

<h3>Experiments not yet added to the Interaction</h3>

<c:if test="${not empty intForm.expsOnHold}">

    <table width="100%" border="0" cellspacing="1" cellpadding="2">
        <tr class="tableRowHeader">
            <th class="tableCellHeader" width="2%"></th>
            <th class="tableCellHeader" width="10%" colspan="2">
                <bean:message key="label.action"/>
            </th>
            <th class="tableCellHeader" width="10%">
                <bean:message key="label.shortlabel"/>
            </th>
            <th class="tableCellHeader" width="10%">
                <bean:message key="label.pubmedid"/>
            </th>
            <th class="tableCellHeader" width="10%">
                <bean:message key="label.ac"/>
            </th>
            <th class="tableCellHeader" width="58%">
                <bean:message key="label.fullname"/>
            </th>
        </tr>
        <%-- To calculate odd or even row --%>
        <c:set var="row"/>
        <c:forEach var="exp" items="${intForm.expsOnHold}">
            <%-- Different styles for even or odd rows --%>
            <c:choose>
                <c:when test="${row % 2 == 0}">
                    <tr class="tableRowEven">
                </c:when>
                <c:otherwise>
                    <tr class="tableRowOdd">
                </c:otherwise>
            </c:choose>

                <td class="editCell"/>

                <td class="tableCell">
                    <html:submit indexed="true" property="expOnHoldCmd"
                        titleKey="int.exp.button.add.titleKey">
                        <bean:message key="int.exp.button.add"/>
                    </html:submit>
                </td>

                <td class="tableCell">
                    <html:submit indexed="true" property="expOnHoldCmd"
                        titleKey="int.exp.button.hide.titleKey">
                        <bean:message key="int.exp.button.hide"/>
                    </html:submit>
                </td>

                <td class="tableCell">
                    <bean:write name="exp" property="shortLabelLink" filter="false"/>
                </td>
                <td class="tableCell">
                    <bean:write name="exp" property="pubMedLink" filter="false"/>
                </td>
                <td class="tableCell">
                    <bean:write name="exp" property="ac"/>
                </td>
                <td class="tableCell">
                    <bean:write name="exp" property="fullName"/>
                </td>
            </tr>
        </c:forEach>
    </table>
</c:if>