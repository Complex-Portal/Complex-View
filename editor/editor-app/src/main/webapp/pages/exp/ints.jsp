<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - Presents Interaction information for an experiment. The form name is hard
  - coded (expForm).
  --%>

<%@ page language="java"%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/display" prefix="display" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>

<jsp:useBean id="user" scope="session"
    beanName="uk.ac.ebi.intact.application.editor.business.EditUser"
    type="uk.ac.ebi.intact.application.editor.business.EditUser"/>

<jsp:useBean id="service" scope="application"
    beanName="uk.ac.ebi.intact.application.editor.business.EditorService"
    type="uk.ac.ebi.intact.application.editor.business.EditorService"/>

<%-- The current view --%>
<bean:define id="view" name="user" property="view"
    type="uk.ac.ebi.intact.application.editor.struts.view.experiment.ExperimentViewBean"/>

<%-- Number of interactions for the current experiment and the max allowed --%>
<bean:define id="numInts" name="view" property="numberOfInteractions"/>
<bean:define id="maxInts" name="service" property="interactionLimit"/>

<h3>Interactions</h3>

<%-- Check the limit --%>
<c:if test="${numInts gt maxInts}">
    <div class="warning">
        <bean:message key="message.ints.limit.exceed" arg0="<%=numInts.toString()%>"
            arg1="<%=maxInts.toString()%>"/>
    </div>
    <%-- Set a flag to not to display any interactions --%>
    <c:set var="noDisplayInts" value="yes"/>
</c:if>

<%-- Don't display an empty table if there are no interactions to display or too
     many interactions to display.
--%>
<c:if test="${numInts gt 0 and empty noDisplayInts}">
    <%-- Store in the page scope for the display library to access it --%>
    <bean:define id="ints" name="view" property="interactions" type="java.util.List"/>

    <%-- Number of ints allowed to display per page --%>
    <bean:define id="pageSize" name="service" property="interactionPageLimit"
        type="java.lang.String"/>

    <%
        String uri = request.getContextPath() + "/do/next";
    %>

    <display:table width="100%" name="ints" pagesize="<%=pageSize%>"
        requestURI="<%=uri%>">
        <display:column property="action" title="Action" />
        <display:column property="shortLabel" title="Short Label"/>
        <display:column property="ac" title="Ac" />
        <display:column property="fullName" title="Full Name" />
    </display:table>
</c:if>

<html:messages id="msg" property="err.interaction">
    <font color="red"><li><bean:write name="msg" /></li></font>
</html:messages>
