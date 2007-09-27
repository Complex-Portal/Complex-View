<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - The page to search for Experiments (from an Interaction editor).
  --%>

<%@ page language="java"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/intact.tld" prefix="intact"%>

<%-- The anchor name for this page --%>
<a name="int.exp.search"/>

<table width="50%" border="0" cellspacing="1" cellpadding="2">
    <tr class="tableRowHeader">
        <th class="tableCellHeader" width="10%" colspan="2">
            <bean:message key="label.action"/>
        </th>
        <th class="tableCellHeader" width="30%"/>
        <th>
            <intact:documentation section="editor.int.experiments"/>
        </th>
    </tr>

    <tr class="tableRowEven">
        <td class="tableCell">
            <html:submit titleKey="int.exp.button.recent.titleKey"
                property="dispatch">
                <bean:message key="int.exp.button.recent"/>
            </html:submit>
        </td>
        <td class="tableCell">
            <html:submit titleKey="int.exps.button.search.titleKey"
                property="dispatch">
                <bean:message key="int.exp.button.search"/>
            </html:submit>
        </td>
        <td class="tableCell">
            <html:text property="expSearchValue" size="20" maxlength="20" onkeypress="return handleEnter(this, event)"/>
        </td>
    </tr>
</table>
<html:messages id="msg" property="int.sanity.exp">
    <font color="red"><li><bean:write name="msg" /></li></font>
</html:messages>
<html:messages id="msg" property="err.search">
    <font color="red"><li><bean:write name="msg" /></li></font>
</html:messages>
<html:messages id="msg" property="int.exp.search">
    <font color="red"><li><bean:write name="msg" /></li></font>
</html:messages>
