<%--
    Page to display  the statistics of the resultset if a search request recieves to many objects.
    It is called from the tooLarge Action and displays for every searchable IntactType (Experiment,
    Interaction, Protein, Controlled Vocabulary) an count of how many objects are in the result set.

    @author Michael KLeen
    @version $Id$
--%>


<%@ page language="java"  %>
<%@ page buffer="none"    %>
<%@ page autoFlush="true" %>

<%@ page import="uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants,
                 java.util.Iterator"%>
<%@ page import="uk.ac.ebi.intact.webapp.search.struts.view.beans.TooLargeViewBean" %>
<%@ page import="uk.ac.ebi.intact.webapp.search.struts.view.beans.SingleResultViewBean" %>


<%
    TooLargeViewBean bean = (TooLargeViewBean) request.getAttribute(SearchConstants.VIEW_BEAN);

%>

  <span class="smalltext"> </span>

     <span class="middletext">Search Results for <%=session.getAttribute(SearchConstants.SEARCH_CRITERIA)%> <br></span
     <br/>


     <span class="largetext">Sorry the query returns too many hits. No details will be
   displayed, please refine your query.<br></span>
   <span class="smalltext"><br></span>


   <!--
    <h3> Sorry the query for <%//info%> returns to many hits. No details will be
   displayed, please refine your query. </h3>
   -->

<table style="background-color: rgb(241, 245, 248);"
       border="1" cellpadding="5" cellspacing="0" bordercolor="#4b9996" width="100%" >

    <tbody>
        <tr>
             <td  class="headerdark">
                    <span class="whiteheadertext">Intact type</span>

            </td>
            <td class="headerdarkmid">
                   <nobr>  <span class="whiteheadertext">Count</span>  </nobr>
            </td>

             <td class="headerdarkmid">
                   <nobr>  <span class="whiteheadertext">&nbsp</span> </nobr>
            </td>

         </tr>

         <% SingleResultViewBean singleResult;
             for (Iterator iterator = bean.getSingleResults().iterator(); iterator.hasNext();)
             {
                 singleResult = (SingleResultViewBean) iterator.next();
         %>
                     <tr>
                        <td>
                            <!-- Intact Type -->
                            <a href=" <%=singleResult.getHelpURL() %>"
                             target="new" class="tdlink"><%=singleResult.getIntactName()%></a>

                        </td>
                        <!-- Intact Type Count -->
                        <td>
                            <%=singleResult.getCount() %>
                        </td>

                         <td>
                         <a href="<%=singleResult.getSearchLink()%>"
                         class="tdlink"><%=singleResult.getSearchName() %></a>
                         </td>

                    </tr>
         <%  } %>
    </tbody>
</table>