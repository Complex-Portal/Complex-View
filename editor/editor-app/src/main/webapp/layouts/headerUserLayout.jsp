<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
   - The header layout for the user pop-up windows.
  --%>

<%@ page language="java"%>

<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>

<script language="JavaScript1.2">
    function refresh() {
        window.location.reload(false);
    }
</script>

<span class="header">
    <tiles:getAsString name="header.title"/>
</span>

<div style="text-align: right;">
    <a href="javascript:refresh()">Refresh</a>
    <a href="javascript:window.close()">Close</a>
</div>
