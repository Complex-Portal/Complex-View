<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
    This layout displays the search box to search the CV database and another
    input box to create a new Annotated object.
--%>

<%@ page language="java" %>
<%@ page import="uk.ac.ebi.intact.application.editor.struts.framework.util.EditorConstants"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/intact.tld" prefix="intact"%>

<jsp:include page="misc/user_js.jsp" />

<jsp:useBean id="user" scope="session"
    beanName="uk.ac.ebi.intact.application.editor.business.EditUser"
    type="uk.ac.ebi.intact.application.editor.business.EditUser"/>

<html:form action="/sidebar" focus="searchString">
    <table>
        <tr>
            <td align="left">
                <html:select property="topic" value="<%=user.getSelectedTopic()%>">
                    <html:options name="<%=EditorConstants.EDITOR_TOPICS%>"/>
                </html:select>
            </td>
        </tr>

        <tr>
            <td>
                <table>
                    <tr>
                        <td>
                            <html:submit tabindex="1" property="dispatch">
                                <bean:message key="button.search"/>
                            </html:submit>
                        </td>
                        <td><html:text property="searchString" size="12"/></td>
                        <td>
                            <intact:documentation section="editor.search" />
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <html:submit property="dispatch" onclick="return validate()">
                                <bean:message key="button.create"/>
                            </html:submit>
                        </td>
                        <td>
                            <intact:documentation section="editor.cv.editors" />
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</html:form>
