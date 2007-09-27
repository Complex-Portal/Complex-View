<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - This page contains java script code for user display pages.
  --%>

<%@ page language="java"%>

<script language="JavaScript" type="text/javascript">
    // This is a global variable to setup a window.
    var userWindow;

    // Create a new window if it hasnt' created before and bring it to the
    // front if it is focusable.
    function makeUserWindow(link) {
        if (!userWindow || userWindow.closed) {
            userWindow = window.open(link, "user", "scrollbars=yes,height=500,width=600,resizable");
            userWindow.focus();
        }
        else if (userWindow.focus) {
            userWindow.focus();
            userWindow.location.href = link;
        }
    }

    function showUsers() {
        var link = "<%=request.getContextPath()%>" + "/do/show/users";
        makeUserWindow(link);
    }
</script>
