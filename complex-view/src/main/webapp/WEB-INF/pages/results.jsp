<%@ page import="uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexSearchResults" %>
<html>
<body>
    <jsp:useBean id="table" class="uk.ac.ebi.intact.service.complex.view.ComplexRestResult" scope="session"/>
	<ol>
    <%
        for( ComplexSearchResults res : table.getElements() ) {
        %>
               <li><p class="complex_name"><b><%= res.getComplexName() %> complex (<%= res.getOrganismName() %>)</b></p>
                   <p class="complex_description"><%= res.getDescription() %></p>
                   <p class="complex_ac">Intact AC: <%= res.getComplexAC() %></p>
                </li>
        <%}%>
    </ol>
</body>
</html>