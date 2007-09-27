<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - Presents the page to add a xreference to an Annotated object.
  --%>

<%@ page language="java"%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/intact.tld" prefix="intact"%>

<jsp:useBean id="user" scope="session"
    beanName="uk.ac.ebi.intact.application.editor.business.EditUser"
    type="uk.ac.ebi.intact.application.editor.business.EditUser"/>

<c:set var="menus" value="${user.view.menus}"/>

<%-- Individual menu lists --%>
<c:set var="dblist" value="${menus['Database_']}"/>
<c:set var="qlist" value="${menus['Qualifier_']}"/>

<%-- Service handler to get the default xref qualifier --%>

<jsp:useBean id="service" scope="application"
    beanName="uk.ac.ebi.intact.application.editor.business.EditorService"
    type="uk.ac.ebi.intact.application.editor.business.EditorService"/>
<bean:define id="defXrefQualifier" name="service" property="defaultXrefQualifier"
    type="java.lang.String"/>

<%-- The anchor name for this page --%>
<a name="xref"/>

<%-- Wrap around the border --%>
<div class="tableBorder">

<%-- Adds a new xreferece. This will invoke addXref action. --%>
<table class="table" width="100%" cellspacing="1" cellpadding="2">
    <tr class="tableLinkRowHeader">
        <th class="tableCellHeader">
            <bean:message key="label.action"/>
        </th>
        <th>
            <a href="javascript:showColumnLink('CvDatabase', document.forms[0].elements['newXref.database'].value)">
                <bean:message key="xref.label.database"/>
            </a>
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
        <th>
            <a href="javascript:showColumnLink('CvXrefQualifier', document.forms[0].elements['newXref.qualifier'].value)">
                <bean:message key="xref.label.reference"/>
            </a>
        </th>
        <th>
            <intact:documentation section="editor.xrefs"/>
        </th>
    </tr>
    <tr class="tableRowOdd">
        <td class="tableCell" align="right" valign="top">
            <html:submit property="dispatch" titleKey="xrefs.button.add.titleKey">
                <bean:message key="xrefs.button.add"/>
            </html:submit>
        </td>

        <td class="tableCell" align="left" valign="top">
            <table>
                <tr>
                    <td>
                        <a href="javascript:makeNewWindow('http://www.ebi.ac.uk/ontology-lookup/browse.do?ontName=MI&termId=MI%3A0444&termName=database%20citation')"><img
                                           alt="IntAct" src="<%=request.getContextPath()%>/images/olsLogo.jpg"
                                           border="0" width="33" align="center" ></a>
                    </td>
                    <td>
                        <html:select property="newXref.database">
                        <html:options name="dblist" />
                        </html:select>
                    </td>
                <tr>
            </table>
        </td>

        <td class="tableCell" align="left" valign="top">
            <html:text property="newXref.primaryId" size="15" onkeypress="return handleEnter(this, event)"/>
        </td>

        <td class="tableCell" align="left" valign="top">
            <html:text property="newXref.secondaryId" size="15" onkeypress="return handleEnter(this, event)"/>
        </td>

        <td class="tableCell" align="left" valign="top">
            <html:text property="newXref.releaseNumber" size="15" onkeypress="return handleEnter(this, event)"/>
        </td>

        <td class="tableCell" align="left" valign="top">
            <table>
                <tr>
                    <td>
                        <a href="javascript:makeNewWindow('http://www.ebi.ac.uk/ontology-lookup/browse.do?ontName=MI&termId=MI%3A0353&termName=xref%20type')"><img
                                           alt="IntAct" src="<%=request.getContextPath()%>/images/olsLogo.jpg"
                                           border="0" width="33" align="center" ></a>
                    </td>
                    <td>
                        <html:select property="newXref.qualifier" value="<%=defXrefQualifier%>">
                        <html:options name="qlist" />
                        </html:select>
                    <td>
                </tr>
            </table>
        </td>
    </tr>
</table>

</div>

<html:messages id="msg" property="xref.db">
    <font color="red"><li><bean:write name="msg" /></li></font>
</html:messages>
<html:messages id="msg" property="xref.pid">
    <font color="red"><li><bean:write name="msg" /></li></font>
</html:messages>
<html:messages id="msg" property="new.xref">
    <font color="red"><li><bean:write name="msg" /></li></font>
</html:messages>
<html:messages id="msg" property="error.xref.go.connection">
    <font color="red"><li><bean:write name="msg" /></li></font>
</html:messages>
<html:messages id="msg" property="error.xref.go.search">
    <font color="red"><li><bean:write name="msg" /></li></font>
</html:messages>
