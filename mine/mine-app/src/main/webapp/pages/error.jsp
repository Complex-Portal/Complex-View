<!--
  - Author: Andreas Groscurth (groscurt@ebi.ac.uk)
  - Version: $Id: error.jsp,v 1.5 2003/03/27 17:34:08 skerrien Exp $
  - Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - Displays an error message stored in the struts framework.
--%>

<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld"  prefix="bean" %>

<jsp:useBean id="error" scope="request" class="uk.ac.ebi.intact.application.mine.struts.view.ErrorBean" />

<h1><font color="red">Validation Error</font></h1>
You must correct the following error(s) before proceeding:
<p><b><%= error.getError() %></b></p>
</hr>