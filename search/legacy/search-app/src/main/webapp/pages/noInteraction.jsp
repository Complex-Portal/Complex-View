<%@ page language="java"  %>
<%@ page buffer="none"    %>
<%@ page autoFlush="true" %>

<%@ page import="uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants,
                 java.util.Collection,
                 java.util.Iterator"%>
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
    Collection info =   (Collection) request.getAttribute(SearchConstants.RESULT_INFO);

%>

<!-- top line info -->
    <span class="middletext">Search Results: No Matches!  <br></span

<h3>Sorry - could not find an interaction


    <%
        if( info.size() == 1 ) {
            out.print( "for" );
        } else {
            out.print( "amongst" );
        }

        out.print( ":<br/> <br/> " );

        for (Iterator iterator = info.iterator(); iterator.hasNext();) {
            String name =  (String) iterator.next();

    %>
           &nbsp;&nbsp; Protein <font color="red"><%= name %></font>
   <%
            if( iterator.hasNext() ) {
                out.println("<br/>");
            }
        } // for
   %>
    <br/>
    <br/>
Please try again!

</h3>
