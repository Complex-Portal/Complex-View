<%@ page import="uk.ac.ebi.intact.application.editor.struts.framework.util.EditorMenuFactory"%>

<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - This page contains java script code common to all the pages.
  --%>

<%@ page language="java"%>

<!-- Import the common JS utilis -->
<script type="text/javascript" src="<%=request.getContextPath()%>/js/common.js"></script>

<jsp:useBean id="service" scope="application"
    beanName="uk.ac.ebi.intact.application.editor.business.EditorService"
    type="uk.ac.ebi.intact.application.editor.business.EditorService"/>

<script language="JavaScript" type="text/javascript">
    // Will be invoked when the user selects on a link.
    function show(type, label) {
        var link = "<%=service.getSearchURL(request)%>" + "?searchString=\"" + label
           + "\"&searchClass=" + type;
        //window.alert(link);
        makeNewWindow(link);
    }

    // Links to the search via the column heading.
    // type - the type for search, eg., CvTopic
    // v    - the value to display
    function showColumnLink(type, v) {
        // window.alert(v);
        if (v == "<%= EditorMenuFactory.SELECT_LIST_ITEM%>") {
            alert("Please select an item from the list first!");
            return;
        }
        show(type, v);
    }
</script>
