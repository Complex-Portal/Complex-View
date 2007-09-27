<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - Presents the page to add an annotation to an Annotated object.
  --%>

<%@ page language="java"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/intact.tld" prefix="intact"%>

<%-- The list of topics --%>
<c:set var="topiclist" value="${user.view.menus['Topic_']}"/>

<%-- The anchor name for this page --%>
<a name="annotation"/>

<%-- Wrap around the border --%>
<div class="tableBorder">

<table class="table" width="100%" border="0" cellspacing="1" cellpadding="2">
    <tr class="tableLinkRowHeader">
        <th class="tableCellHeader">
            <bean:message key="label.action"/>
        </th>
        <th>
            <a href="javascript:showColumnLink('CvTopic', document.forms[0].elements['newAnnotation.topic'].value)">
                Topic
            </a>
        </th>
        <th class="tableCellHeader">Description</th>
        <th>
            <intact:documentation section="editor.annotations"/>
        </th>
    </tr>
    <tr class="tableRowOdd">
        <td class="tableCell" align="left" valign="bottom">
            <html:submit property="dispatch"
                titleKey="annotations.button.add.titleKey">
                <bean:message key="annotations.button.add"/>
            </html:submit>
        </td>


        <td class="tableCell" align="left" valign="top">
            <table>
                <tr>
                    <td>
                        <a href="javascript:makeNewWindow('http://www.ebi.ac.uk/ontology-lookup/browse.do?ontName=MI&termId=MI%3A0590&termName=attribute%20name')"><img
                                           alt="IntAct" src="<%=request.getContextPath()%>/images/olsLogo.jpg"
                                           border="0" width="33" align="center" ></a>
                    </td>
                    <td>
                        <html:select property="newAnnotation.topic" >
                        <html:options name="topiclist"/>
                        </html:select>
                    <td>
                </tr>
            </table>
        </td>
        <td class="tableCell" align="left" valign="top">
            <html:textarea property="newAnnotation.description" rows="3" cols="70"
               onkeypress="return validateComment(this, event);" />
        </td>

        <%-- <td class="tableCell" align="left" valign="top">
            <html:select property="annotationSelect" name="expForm">
                <html:options name="topiclist"/>
            </html:select>
        </td>

        <td class="tableCell" align="left" valign="top">
            <html:textarea property="annotationTextArea" name="expForm" rows="3" cols="70"
               onkeypress="return validateComment(this, event)"
               />
        </td>   --%>


    </tr>
</table>

</div>


<html:messages  id="msg" property="annotation">
    <font color="red"><li><bean:write name="msg" /></li></font>
</html:messages>
<html:messages id="msg" property="new.annotation">
    <font color="red"><li><bean:write name="msg" /></li></font>
</html:messages>
<html:messages id="msg" property="char.not.allowed">
    <font color="red"><li><bean:write name="msg" /></li></font>
</html:messages>
<%--<html:messages id="msg" property="annotation"/>--%>
<%--<html:messages id="msg" property="new.annotation"/>--%>
