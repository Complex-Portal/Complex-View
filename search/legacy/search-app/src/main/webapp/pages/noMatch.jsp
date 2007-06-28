<%@ page language="java"  %>
<%@ page buffer="none"    %>
<%@ page autoFlush="true" %>

<%@ page import="uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants"%>
<%@ page import="uk.ac.ebi.intact.webapp.search.SearchWebappContext" %>
<%--
   /**
    * no matches page.
    *
    * @author Chris Lewington
    * @version $Id$
    */
--%>
<!doctype html public "-//w3c//dtd html 4.0 transitional//en">


<!-- top line info -->
    <span class="middletext">Search Results: No Matches!  <br></span

<h3>Sorry - could not find any Result
  by trying to match  <font color="red"> <%= SearchWebappContext.getCurrentInstance().getCurrentSearchQuery() %> </font></h3>

  <!--
  <ul>
      <li>AC,
      <li>short label,
      <li>xref Primary ID or
      <li>a full name.
  </ul>
  -->

  <h3>Please try again!</h3>

</html>