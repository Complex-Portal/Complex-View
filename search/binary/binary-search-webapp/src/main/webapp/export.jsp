<%@ page import="org.apache.lucene.search.Sort"%><%@ page import="psidev.psi.mi.search.SearchResult"%><%@ page import="psidev.psi.mi.search.Searcher"%><%@ page import="psidev.psi.mi.tab.PsimiTabWriter"%><%@ page import="java.util.List"%><%@ page contentType="text/plain" language="java" %>

<%

String searchQuery = request.getParameter("q");
String indexDir = request.getParameter("d");
String sortColumn = request.getParameter("sort");
String asc = request.getParameter("asc");

List interactions;
Integer firstResult = new Integer(0);
Integer maxResults = new Integer(50);

boolean headerEnabled = true;

do {

    Sort sort = null;

    if (sortColumn != null && sortColumn.length() > 0)
    {
        sort = new Sort(sortColumn, !Boolean.parseBoolean(asc));
    }

    SearchResult result = Searcher.search(searchQuery, indexDir, firstResult, maxResults, sort);
    interactions = result.getInteractions();

    PsimiTabWriter writer = new PsimiTabWriter();
    writer.setHeaderEnabled(headerEnabled);
    writer.write(interactions, out);
    out.flush();

    headerEnabled = false;

    firstResult = new Integer(firstResult.intValue()+ maxResults.intValue());

} while (!interactions.isEmpty());

%>