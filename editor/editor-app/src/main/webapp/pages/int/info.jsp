<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - Interaction editor.
  --%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/intact.tld" prefix="intact"%>

<%-- The menus --%>
<c:set var="menus" value="${user.view.menus}"/>

<%-- Individual menu lists --%>
<c:set var="organismmenu" value="${menus['Organism']}"/>
<c:set var="intertypemenu" value="${menus['InteractionType']}"/>

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
            <bean:write property="ac" name="intForm" filter="false"/>
        </td>

        <td class="tableCell">
            <html:text property="shortLabel" size="20" maxlength="20"
                name="intForm" styleClass="inputRequired" onkeypress="return handleEnter(this, event)"/>
        </td>

        <td class="tableCell">
            <html:text property="fullName" size="100" maxlength="250" name="intForm" onkeypress="return handleEnter(this, event)"/>
        </td>
    </tr>
    <tr class="tableRowEven">
        <td class="tableCell" colspan="2">
            <bean:message key="label.creation"/> <bean:write property="created" name="intForm" /> by <bean:write property="creator" name="intForm" />.
        </td>
        <td class="tableCell" colspan="2">
            <bean:message key="label.update"/> <bean:write property="updated" name="intForm" /> by <bean:write property="updator" name="intForm" />.
        </td>
    </tr>
</table>
<html:messages id="msg" property="shortLabel">
    <font color="red"><li><bean:write name="msg" /></li></font>
</html:messages>

<p></p>

<table width="50%" border="0" cellspacing="1" cellpadding="2">
    <tr class="tableLinkRowHeader">
        <th>Kd</th>
        <th>
            <a href="javascript:showColumnLink('CvInteractionType',
                document.forms['intForm'].elements['interactionType'].value)"/>
                <bean:message key="int.label.cvtype"/>
            </a>
        </th>
        <th>
            <a href="javascript:showColumnLink('BioSource',
                document.forms['intForm'].elements['organism'].value)">
                <bean:message key="int.label.biosrc"/>
            </a>
        </th>
    </tr>

    <tr class="tableRowHeader">
        <td class="tableCell">
            <html:text property="kd" name="intForm" size="5" maxlength="16" onkeypress="return handleEnter(this, event)"/>
        </td>

        <td class="tableCell" align="left" valign="top">
            <table>
                <tr>
                    <td>
                        <a href="javascript:makeNewWindow('http://www.ebi.ac.uk/ontology-lookup/browse.do?ontName=MI&termId=MI%3A0190&termName=interaction%20type')"><img
                                           alt="IntAct" src="<%=request.getContextPath()%>/images/olsLogo.jpg"
                                           border="0" width="33" align="center" ></a>
                    </td>
                    <td>
                        <html:select property="interactionType" name="intForm" styleClass="inputRequired">
                        <html:options name="intertypemenu" />
                        </html:select>
                    </td>
                </tr>
            </table>
            <html:messages id="msg" property="interactionType">
                <font color="red"><li><bean:write name="msg" /></li></font>
            </html:messages>
        </td>

        <td class="tableCell" align="left" valign="top">
            <html:select property="organism" name="intForm">
                <html:options name="organismmenu" />
            </html:select>
            <html:messages id="msg" property="organism">
                <font color="red"><li><bean:write name="msg" /></li></font>
            </html:messages>
        </td>
    </tr>
</table>
