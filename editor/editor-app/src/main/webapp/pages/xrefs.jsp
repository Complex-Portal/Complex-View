<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - For editing/saving a Xref for an Controlled Vocab object.
  --%>

<%@ page language="java"%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>

<!-- User stored in a session -->
<jsp:useBean id="user" scope="session"
    beanName="uk.ac.ebi.intact.application.editor.business.EditUser"
    type="uk.ac.ebi.intact.application.editor.business.EditUser"/>

<c:set var="view" value="${user.view}"/>
<c:set var="dblist" value="${view.menus['Database']}"/>
<c:set var="qlist" value="${view.menus['Qualifier']}"/>

<%-- The anchor name for this page --%>
<a name="xref.edit"/>

<h3>Crossreferences</h3>

<c:if test="${not empty view.xrefs}">

    <table width="100%" border="0" cellspacing="1" cellpadding="2">
        <tr class="tableRowHeader">
            <th class="tableCellHeader" width="2%"></th>
            <th class="tableCellHeader" colspan="2">
                <bean:message key="label.action"/>
            </th>
            <th class="tableCellHeader">
                <bean:message key="xref.label.database"/>
            </th>
            <th class="tableCellHeader">
                <bean:message key="xref.label.primary"/>
            </th>
            <th class="tableCellHeader">
                <bean:message key="xref.label.secondary"/>
            </th>
            <th class="tableCellHeader">
                <bean:message key="xref.label.release"/>
            </th>
            <th class="tableCellHeader">
                <bean:message key="xref.label.reference"/>
            </th>
        </tr>
        <%-- To calculate row or even row --%>
        <c:set var="row"/>
        <c:forEach var="xrefs" items="${view.xrefs}">
            <!-- Different styles for even or odd rows -->
            <c:choose>
                <c:when test="${row % 2 == 0}">
                    <tr class="tableRowEven">
                </c:when>
                <c:otherwise>
                    <tr class="tableRowOdd">
                </c:otherwise>
            </c:choose>
            <c:set var="row" value="${row + 1}"/>

            <c:if test="${xrefs.editState == 'editing'}" var="edit"/>
            <c:if test="${xrefs.editState == 'saving'}" var="save"/>
            <c:if test="${xrefs.editState == 'error'}" var="error"/>

            <c:if test="${edit}">
                <td class="tableCell"/>
            </c:if>

            <c:if test="${save}">
                <td class="editCell"/>
            </c:if>

            <c:if test="${error}">
                <td class="errorCell"/>
            </c:if>

            <%-- The following loop is under <tr> tag --%>

                <%-- Buttons; Edit or Save depending on the bean state;
                     Delete is visible regardless of the state.
                 --%>
                <td class="tableCell">
                    <c:if test="${edit}">
                        <html:submit indexed="true" property="xrefCmd"
                            titleKey="xrefs.button.edit.titleKey">
                            <bean:message key="xrefs.button.edit"/>
                        </html:submit>
                    </c:if>

                    <c:if test="${save or error}">
                        <html:submit indexed="true" property="xrefCmd"
                            titleKey="xrefs.button.save.titleKey">
                            <bean:message key="xrefs.button.save"/>
                        </html:submit>
                    </c:if>
                </td>

                <td class="tableCell">
                    <html:submit indexed="true" property="xrefCmd"
                            titleKey="xrefs.button.delete.titleKey">
                        <bean:message key="xrefs.button.delete"/>
                    </html:submit>
                </td>

                <%-- In view mode --%>
                <c:if test="${edit}">
                    <td class="tableCell">
                        <bean:write name="xrefs" property="databaseLink" filter="false"/>
                    </td>
                    <td class="tableCell">
                        <bean:write name="xrefs" property="primaryIdLink" filter="false"/>
                    </td>
                    <td class="tableCell">
                        <bean:write name="xrefs" property="secondaryId"/>
                    </td>
                    <td class="tableCell">
                        <bean:write name="xrefs" property="releaseNumber"/>
                    </td>
                    <td class="tableCell">
                        <bean:write name="xrefs" property="qualifierLink" filter="false"/>
                    </td>
                </c:if>

                <%-- In save or error mode --%>
                <c:if test="${save or error}">
                    <td class="tableCell">
                        <html:select name="xrefs" property="database" indexed="true">
                            <html:options name="dblist" />
                        </html:select>
                    </td>
                    <td class="tableCell">
                        <html:text name="xrefs" size="15" property="primaryId" indexed="true" onkeypress="return handleEnter(this, event)"/>
                    </td>
                    <td class="tableCell">
                        <html:text name="xrefs" size="15" property="secondaryId" indexed="true" onkeypress="return handleEnter(this, event)"/>
                    </td>
                    <td class="tableCell">
                        <html:text name="xrefs" size="15" property="releaseNumber" indexed="true" onkeypress="return handleEnter(this, event)"/>
                    </td>
                    <td class="tableCell">
                        <html:select name="xrefs" property="qualifier" indexed="true">
                            <html:options name="qlist" />
                        </html:select>
                    </td>
                </c:if>
            </tr>
        </c:forEach>
    </table>

    <%-- Errors are displayed here --%>
    <html:messages id="msg" property="xref.exists"/>
    <html:messages id="msg" property="xref.unsaved"/>
</c:if>
