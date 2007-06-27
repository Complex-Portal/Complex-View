<!--
   - Copyright (c) 2002 The European Bioinformatics Institute, and others.
   - All rights reserved. Please see the file LICENSE
   - in the root directory of this distribution.
   -
   - Forwards the result of MiNe to HierarchView
   -
   - @author Andreas Groscurth (groscurt@ebi.ac.uk)
-->
<%@ page language="java" %>
<%@ page import="uk.ac.ebi.intact.application.mine.business.IntactUserI,
				uk.ac.ebi.intact.application.mine.business.Constants" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>

<%	IntactUserI user = (IntactUserI)session.getAttribute(Constants.USER);

	String link = request.getContextPath();
	link = link.substring(0, link.lastIndexOf("/") + 1); 
	link = user.getHVLink(link);
%>
<meta http-equiv="refresh" content="3; URL=<%= link %>">
<strong>The results are forwarded to HierarchView !<br>
If the forward is not working please click <a href="<%= link %>">here</a>
</strong>


