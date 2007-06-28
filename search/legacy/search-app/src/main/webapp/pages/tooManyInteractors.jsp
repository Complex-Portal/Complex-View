<%@ page language="java"  %>
<%@ page buffer="none"    %>
<%@ page autoFlush="true" %>

<%@ page import="uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants"%>
 <%--
   /**
    * no matches page.
    *
    * @author Chris Lewington
    * @version $Id$
    */
--%>
<!doctype html public "-//w3c//dtd html 4.0 transitional//en">

<%
    // get the search query
    String criteria =   (String) session.getAttribute(SearchConstants.SEARCH_CRITERIA);

%>

<!-- top line info -->
    <span class="middletext">Search Results: No Matches! <br></span>

 <h3> Sorry - find more than 2 proteins  by trying to match <font color="red"><%= criteria %></font>'
<br/>
<br/>
Please try again!
</h3>