<%@ page import="uk.ac.ebi.intact.application.editor.struts.framework.util.EditorConstants"%>

<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - Displays locks held with the locks manager.
  --%>

<%@ page language="java"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/display" prefix="display" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt"%>

<%-- The id must match with the name this bean is stored in the application ctx --%>
<jsp:useBean id="listener" scope="application"
    beanName="uk.ac.ebi.intact.application.editor.event.EventListener"
    type="uk.ac.ebi.intact.application.editor.event.EventListener"/>

<%-- Store in the page scope for the display library to access it --%>
<bean:define id="locks" name="listener" property="authenticationEvents"
    type="java.util.Set"/>

<jsp:useBean id="now" beanName="java.util.Date" type="java.util.Date"/>
Last Refresh: <i><fmt:formatDate value="${now}" pattern="EE, dd MMM yyyy HH:mm:ss Z"/></i>

<display:table width="100%" name="locks"
        decorator="uk.ac.ebi.intact.application.editor.struts.view.wrappers.UserDisplayWrapper">
    <display:column property="userName" title="User" sort="true"/>
    <display:column property="loginTime" title="Login Date"/>
    <display:column property="lockData" title="Lock Data" />
</display:table>
