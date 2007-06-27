<%@ page import="uk.ac.ebi.intact.application.predict.struts.framework.PredictConstants,
                 uk.ac.ebi.intact.application.predict.business.PredictService"%>

<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - Displays the results data from the Pay-As-You-Go Prediction Algorithm.
  --%>

<%@ page language="java" %>

<%@ taglib uri="http://jakarta.apache.org/taglibs/display" prefix="display" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>

<%
    // To Allow access to Editor Service.
    PredictService service = (PredictService)
            application.getAttribute(PredictConstants.SERVICE);
%>

<script language="JavaScript" type="text/javascript">
    // This is a global variable to setup a window.
    var newWindow;

    // Create a new window if it hasnt' created before and bring it to the
    // front if it is focusable.
    function makeNewWindow(link) {
        if (!newWindow || newWindow.closed) {
            newWindow = window.open(link, "display", "scrollbars=yes,height=500,width=600");
            newWindow.focus();
        }
        else if (newWindow.focus) {
            newWindow.focus();
            newWindow.location.href = link;
        }
    }

    // Displays the link from the protein id.
    function showProtein(link) {
        makeNewWindow(link);
    }

    // Will be invoked when the user selects on a link.
    function show(topic, label) {
        var link = "<%=service.getSearchURL(request)%>"
            + "?searchString=" + label + "&searchClass=" + topic;
        //    window.alert(link);
        makeNewWindow(link);
    }

</script>

<h1>
    Top 50 proteins predicted for
    <c:out value="${user.specieLink}" escapeXml="false"/>
    as the best candidates for the next round of pull-down experiments via
    the Pay-As-You-Go Strategy
</h1>

<hr/>

<%
    String uri = request.getContextPath() + "/predict.do";
%>

<display:table width="100%" name="<%=PredictConstants.PREDICTION%>"
    scope="request" pagesize="20" requestURI="<%=uri%>">
    <display:column property="rank" title="Rank" />
    <display:column property="shortLabelLink" title="Short Label" />
    <display:column property="fullName" title="Full Name" />
</display:table>
