<!--
  - Author: Andreas Groscurth (groscurt@ebi.ac.uk)
  - Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
-->

<%--
  - Displays an ambiguous search result
--%>

<%@ page language="java" %>
<%@ page import="uk.ac.ebi.intact.application.mine.struts.view.AmbiguousBean,
				uk.ac.ebi.intact.application.mine.business.Constants" %>
<%@ page import="java.util.Collection"%>
	
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/intact.tld"      prefix="intact"%>

<script language="JavaScript" type="text/javascript">

	function check() {
		// number stores the number of checked checkboxes
	    var number = 0; 
	    for (var i = 0; i < document.mineForm.elements.length; i++) {
            if (document.mineForm.elements[i].type == "checkbox") {
                if (document.mineForm.elements[i].checked) {
			      number++;
		        }
		    }
		}
		// the number of Ac in the additional search field
		var noAc = document.mineForm.AC.value.length == 0 ? 
					0 : document.mineForm.AC.value.split(",").length;
		noAc += number;
		
		// if there are less than 2 Ac given
		if(noAc < 2) {
		  alert("Please select at least two proteins or " +
		  	"enter at least one search phrase !");
		}
		// if more than the allowed numbers are
		else if(noAc > <%= Constants.MAX_SEARCH_NUMBER %>) {
		  alert("You selected more than <%= Constants.MAX_SEARCH_NUMBER %> proteins! "+
		  	"Please select less proteins !");
		}
		else {
			document.mineForm.submit();
		}
	}
</script>
<span style="font-weight: bold; font-size:16px; color:#336666;">Ambiguous search results</span>
<intact:documentation section="mine.ambiguous.result" />
<span style="font-weight:bold;"></span>
<h4>The parameters you have entered returned ambiguous search results. 
For each of your input parameters, please select the appropriate IntAct object(s) below:</h4>

<hr size="2"><br>
<form name="mineForm" action="<%=request.getContextPath()%>/do/search" method="post">
<%
    Collection ambiguous = (Collection) request.getAttribute("ambiguous");

    java.util.Iterator iter = ambiguous.iterator();
	String path = request.getContextPath();
	path = path.substring(0, path.lastIndexOf("/"));
	// each ambiguous result is shown
	while(iter.hasNext()) {
	  	((AmbiguousBean)iter.next()).printHTML(out, path);
	}
 %>
<br>
<bean:message key="ambiguous.additional.text"/>
<intact:documentation section="mine.ambiguous.additional" /><br>
<input name="AC" size="50"><br><br>
<hr size="2">
<table cellpadding="1" cellspacing="0" border="1" width="100%">
<tr><td align="center">
<html:button titleKey="sidebar.search.button.submit.title" property="" onclick="check()">
  <bean:message key="sidebar.search.button.submit"/>
</html:button></td></tr></table>
</form><br>