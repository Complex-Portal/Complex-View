<%@ page language="java"%>

<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id: sidebarFooter.jsp 8862 2007-06-27 13:09:58Z baranda $
  - Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - The common footer for the sidebar.
--%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>

<c:if test="${not empty sessionScope.user}">

    <jsp:useBean id="user"
                 scope="session"
                 beanName="uk.ac.ebi.intact.searchengine.business.IntactUserI"
                 type="uk.ac.ebi.intact.searchengine.business.IntactUserI"/>

    <c:if test="${not empty user.userName}">
        User: <c:out value="${user.userName}"/>
        <br/>
    </c:if>

    <c:if test="${not empty user.databaseName}">
        Database: <c:out value="${user.databaseName}"/>
    </c:if>

</c:if>
