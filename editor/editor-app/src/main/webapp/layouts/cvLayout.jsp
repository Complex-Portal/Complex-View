<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - The look & feel layout for editing a CV object.
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  --%>

<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>

<%-- CSS for editor --%>
<style type="text/css">
    <%@ include file="/layouts/styles/editor.css" %>
</style>

<%-- The error page at the top --%>
<tiles:insert attribute="error"/>

<tiles:insert attribute="cv"/>
