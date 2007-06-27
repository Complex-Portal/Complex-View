<%@ page language="java" %>

<!--
   - Copyright (c) 2002 The European Bioinformatics Institute, and others.
   - All rights reserved. Please see the file LICENSE
   - in the root directory of this distribution.
   -
   - Allows to submit a query without passing by the home page.
   -
   - @author Andreas Groscurth (groscurt@ebi.ac.uk)
-->

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>

<html>
<head>
     <title>IntAct MiNe: external request management</title>
</head>
<body bgcolor="white">
	<p align="center" style="font-size:18px; font-weight:bold;">
	Please wait, your request is being processed.<br>
    Computing the minimal connection network for<br>
    Interactor AC: <%= request.getParameter ("AC") %>.
    
    <form action="<%=request.getContextPath()%>/do/search" method="post">
          <input type="hidden" name="AC" value="<%= request.getParameter("AC")%>" />
    </form>

    <script language="JavaScript" type="text/javascript">
            document.forms[0].submit();
    </script>
</body>
</html>