<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - Presents Ranges information for a Feature.
  --%>

<%@ page language="java"%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>

<c:set var="view" value="${user.view}"/>

<%-- The boolean menu for link/undetermined --%>
<c:set var="boolean_menu" value="${view.booleanMenu}"/>

<h3>Ranges</h3>

<c:if test="${not empty featureForm.ranges}">

    <%-- The table id is for testing purposes --%>
    <table width="100%" border="0" cellspacing="1" cellpadding="2" id="ranges">
        <tr class="tableRowHeader">
            <th class="tableCellHeader" width="2%"></th>
            <th class="tableCellHeader" width="10%" colspan="2">
                <bean:message key="label.action"/>
            </th>
            <th class="tableCellHeader" width="20%">From</th>
            <th class="tableCellHeader" width="20%">TO</th>
            <th class="tableCellHeader" width="20%">Link</th>
        </tr>
        <%-- To calculate row or even row --%>
        <c:set var="row"/>
        <c:forEach var="ranges" items="${featureForm.ranges}">
            <%-- Different styles for even or odd rows --%>
            <c:choose>
                <c:when test="${row % 2 == 0}">
                    <tr class="tableRowEven">
                </c:when>
                <c:otherwise>
                    <tr class="tableRowOdd">
                </c:otherwise>
            </c:choose>

            <c:if test="${ranges.editState == 'editing'}" var="edit"/>
            <c:if test="${ranges.editState != 'editing'}" var="notEdit"/>
            <c:if test="${ranges.editState == 'saving'}" var="save"/>
            <c:if test="${ranges.editState == 'error'}" var="error"/>

            <%-- Fill with appropriate color; simply sets the color for the
                 cell; no information is displayed yet.
            --%>
            <c:if test="${edit}">
                <td class="tableCell"/>
            </c:if>

            <c:if test="${save}">
                <td class="editCell"/>
            </c:if>

            <c:if test="${error}">
                <td class="errorCell"/>
            </c:if>

                <%-- Buttons; Edit or Save depending on the bean state;
                     Delete is visible regardless of the state.
                 --%>
                <td class="tableCell">
                    <c:if test="${edit}">
                        <html:submit indexed="true" property="rangeCmd"
                            titleKey="feature.range.button.edit.titleKey">
                            <bean:message key="feature.range.button.edit"/>
                        </html:submit>
                    </c:if>

                    <c:if test="${notEdit}">
                        <html:submit indexed="true" property="rangeCmd"
                            titleKey="feature.range.button.save.titleKey">
                            <bean:message key="feature.range.button.save"/>
                        </html:submit>
                    </c:if>
                </td>

               <%-- Delete button: common to all --%>
                <td class="tableCell">
                    <html:submit indexed="true" property="rangeCmd"
                        titleKey="feature.range.button.delete.titleKey">
                        <bean:message key="feature.range.button.delete"/>
                    </html:submit>
                </td>

                <%-- View mode --%>
                <c:if test="${edit}">
                    <td class="tableCell">
                        <bean:write name="ranges" property="fromRange" filter="false"/>
                    </td>
                    <td class="tableCell">
                        <bean:write name="ranges" property="toRange" filter="false"/>
                    </td>
                    <td class="tableCell">
                        <bean:write name="ranges" property="link"/>
                    </td>
                </c:if>

                <%-- Save or Error mode --%>
                <c:if test="${save or error}">
                    <td class="tableCell">
                        <html:text name="ranges" size="10" property="fromRange" indexed="true"/>
                        <br/><html:messages id="msg" property="edit.fromRange">
                                <font color="red"><li><bean:write name="msg" /></li></font>
                            </html:messages>
                    </td>
                    <td class="tableCell">
                        <html:text name="ranges" size="10" property="toRange" indexed="true"/>
                        <br/><html:messages id="msg" property="edit.toRange">
                             <font color="red"><li><bean:write name="msg" /></li></font>
                        </html:messages>
                    </td>
                    <td class="tableCell">
                        <html:select name="ranges" property="link" indexed="true">
                            <html:options name="boolean_menu" />
                        </html:select>
                    </td>
                </c:if>
            </tr>

            <%-- Increment row by 1 --%>
            <c:set var="row" value="${row + 1}"/>
        </c:forEach>
    </table>
</c:if>

<%-- Invalid values for from and to --%>
<html:messages id="msg" property="edit.range">
    <font color="red"><li><bean:write name="msg" /></li></font>
</html:messages>
<html:messages id="msg" property="feature.range.unsaved">
    <font color="red"><li><bean:write name="msg" /></li></font>
</html:messages>
<html:messages id="msg" property="feature.range.exists">
    <font color="red"><li><bean:write name="msg" /></li></font>
</html:messages>
<html:messages id="msg" property="feature.range.empty">
    <font color="red"><li><bean:write name="msg" /></li></font>
</html:messages>