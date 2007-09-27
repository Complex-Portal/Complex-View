<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - EBI specific layout. Uses the
  --%>

<%@ page language="java"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>

<%--
    Some strange reasons, the scope has to be in page not application with
    Tomcat 4.0. The problem doesn't exist on Tomcat 4.1.x
    --%>
<jsp:useBean id="helper" class="uk.ac.ebi.html.Helper" scope="page">
    <jsp:setProperty name="helper" property="out" value="<%= out %>" />
    <jsp:setProperty name="helper" property="topMenuItem"
        value="internal/seqdb/projects" />
    <jsp:setProperty name="helper" property="loadMethods"
        value="//checkBrowser(); genjob(); setem()" />
    <jsp:setProperty name="helper" property="unLoadMethods" value="//closeNotify()" />
    <jsp:setProperty name="helper" property="headerScript"
        value="<script language = \'javascript\'></script>" />
</jsp:useBean>

<jsp:getProperty name="helper" property="header"/>

<title><tiles:getAsString name="title"/></title>
<html:base/>
<link rel="stylesheet" type="text/css" href="styles/intact.css"/>
<link rel="stylesheet" type="text/css"
    href="http://www3.ebi.ac.uk/internal/seqdb/include/stylesheet.css"/>

<jsp:getProperty name="helper" property="headerBody"/>
