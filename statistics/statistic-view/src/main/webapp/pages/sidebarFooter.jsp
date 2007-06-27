<%@ page import="uk.ac.ebi.intact.persistence.dao.DaoFactory" %>
<%@ page import="uk.ac.ebi.intact.context.IntactContext" %>
<%@ page language="java"%>

<!--
  - Author: Bruno Aranda (baranda@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - The common footer for the sidebar.
--%>

User: <%=IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getBaseDao().getDbUserName()%> <br/>
Database: <%=IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getBaseDao().getDbName()%> <br/>

