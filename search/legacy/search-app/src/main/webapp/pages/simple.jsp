<!-- simple.jsp -->
<%--
    Page to display a 'simple' initial table view of search results. Current
    specification is as per the mockups June 2004. This will be the first page a user sees
    after performing a search - the links provided here will take them to the more
    specific views as defined in the other search mockup pages.

    NOTE:
    Design decision to be made - this view will allow multiple type matches to be displayed,
    so for example if the search string matches Proteins, Experiments AND interactions then
    a simple table can be displayed for each result type. Is this required, or does it have
    to be as now ie only a single type search match?

    DECISION: provide multiple type results.


IMPORTANT: This will now require a change to the search app logic, as currently
it assumes that no Protein type specified means 'show a binary view'. With this view now as the
first one, any subsequeent views will have the type specified anyway, so we must distinguish
between a single Protein detail request and a request for a partners view. This has an impact on
other types too. Maybe this JSP has to set an additional request/form parameter, 'front',
to identify the source page of the request to the Action classes.
--%>

<!--
    @author Chris Lewington, Samuel Kerrien (skerrien@ebi.ac.uk)
    @version $Id$
-->

<%-- need to provide an error page here to catch unhandled failures --%>
<%@ page language="java"  %>
<%@ page buffer="none"    %>
<%@ page autoFlush="true" %>

<%-- Intact classes needed --%>
<%@ page import="uk.ac.ebi.intact.model.Experiment,
                 uk.ac.ebi.intact.model.Interaction,
                 uk.ac.ebi.intact.model.Interactor,
                 uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants,
                 uk.ac.ebi.intact.webapp.search.struts.view.beans.SimpleViewBean"%>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.StringTokenizer" %>

<%-- Standard Java classes --%>

<%-- may make use of these later to tidy up the JSP a little --%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>

<%

    SearchWebappContext webappContext = SearchWebappContext.getCurrentInstance();

    //build the URL for hierarchView from the absolute path and the relative beans..
    // is used by the script included in the file jscript.html
    String hvPath = webappContext.getHierarchViewAbsoluteUrl();
    String minePath = webappContext.getMineAbsoluteUrl();

    //The List of view beans used to provide the data for this JSP. This is in fact
    //a List of sublists, partitioned by result type.
    List<List<SimpleViewBean>> partitionList = (List<List<SimpleViewBean>>) request.getAttribute(SearchConstants.VIEW_BEAN_LIST);
%>
<%-- The javascript for the button bars.... --%>
<%@ include file="jscript.html" %>
<!-- top line info -->
<%--    <span class="middletext">Search Results for <%=session.getAttribute(SearchConstants.SEARCH_CRITERIA)%> <br></span>--%>
<span class="smalltext">Search Results for

    <%
        Object objParams = session.getAttribute(SearchConstants.SEARCH_CRITERIA);

        if (objParams != null)
        {
            String params = objParams.toString();
            if( params.length() > 20 ) {

                // split the params and display 10 per lines.
                StringTokenizer st = new StringTokenizer( params, "," );
                int count = 0;
                while( st.hasMoreTokens() ) {
                    out.write( st.nextToken() );
                    out.write( ',' );
                    count++;
                    if( (count % 10) == 0 ) {
                        out.write( "<br>" );
                    }
                }

            } else {
                out.write( params );
            }
        }

    %>

</span>
     <br/>

<span class="verysmalltext">(short labels of search criteria matches are
    <span style="color: rgb(255, 0, 0);">highlighted</span>
</span><span class="verysmalltext">)<br></span></p>

<%-- Firstly need to check that we have at least one set of results that we can display,
because if not then we should NOT display the message below..
--%>

    <span class="verysmalltext">Please click on any name to view more detail.</span>
  <span class="smalltext"> <br> </span>

</p>
<%

%>


<!-- the main form for the page -->
<form name="viewForm">

<%-- process each remaining partitioned List in turn and display its results in the appropriate
table.....
--%>

<%-- The header should be displayed for each table - need to iterate over each
partitioned list to do this --%>

<%
    List<SimpleViewBean> displayList = null;     //use this as each iteration holder
    for (Iterator<List<SimpleViewBean>> it1 = partitionList.iterator(); it1.hasNext();)
    {
        displayList = it1.next();

        //need a handle on this to write out some header info and typecheck...
        SimpleViewBean firstItem = displayList.iterator().next();

        SearchClass firstItemSearchClass = SearchClass.valueOfMappedClass(firstItem.getObject().getClass());
%>

<%-- If we get to here we know we have something to show, so we need a button bar...

(top) button bar: NB will include an 'XML' button when that functionality is integrated
NB DON'T want buttons for CvObjects...(so put this one inside the loop...)

--%>
 <%@ include file="buttonBar.html" %>

<%-- pagination --%>
 <%@ include file="tablePagination.jspf"%>

<!-- result table -->
<table style="background-color: rgb(241, 245, 248);"
       border="1" cellpadding="5" cellspacing="0" bordercolor="#4b9996" width="100%" >

    <tbody>

        <!-- header row: cells contain only text and help links, self-explanatory -->
        <tr>
            <%
                //just get hold of the intact type and display, UNLESS it is CvObject
                //in which case we display 'controlled vocabularies'
                if(firstItemSearchClass.isCvObjectSubclass()) {
            %>
      <!--      <td style="vertical-align: top;">Controlled Vocabulary Terms<br>
            </td> -->

             <td  class="headerdark">
                   <nobr>  <span class="whiteheadertext">Controlled Vocabulary Terms</span>

                <a href="<%=firstItem.getHelpUrl()%>" target="new"
                   class="whitelink"><sup>?</sup>
                </a></nobr>
            </td>

            <%
                }
                else {
                    //need plurals - appending 's' works in most cases..
            %>

            <td  class="headerdark">
                   <nobr> <span class="whiteheadertext"> <%=firstItem.getIntactType() + "s"%></span>
                <a href="<%=firstItem.getHelpUrl()%>" target="new"
                   class="whitelink"><sup>?</sup></a></nobr>

            </td>


            <%
                }
            %>

            <td nowrap="nowrap" class="headerdarkmid" > <!-- rowspan="1" colspan="1" -->
                <a href="<%= firstItem.getHelpLink() + "AnnotatedObject.shortLabel"%>" target="new"
                   class="tdlink">Name</a>
            </td>

            <td nowrap="nowrap" class="headerdarkmid">
                <a href="<%= firstItem.getHelpLink() + "BasicObject.ac"%>" target="new"
                   class="tdlink">Ac</a>
            </td>

              <%
                if(firstItemSearchClass == SearchClass.PROTEIN ||
                   firstItemSearchClass == SearchClass.NUCLEIC_ACID ||
                   firstItemSearchClass == SearchClass.SMALL_MOLECULE) {
            %>
            <td class="headerdarkmid">Gene-Name</td>
            <% } %>

            <td nowrap="nowrap" class="headerdarkmid"> <!-- rowspan="1" colspan="3" -->
                <a href="<%= firstItem.getHelpLink() + "AnnotatedObject.fullName"%>" target="new"
                   class="tdlink">Description
                </a>
            </td>

            <%-- now for Interactions and Experiments we need an extra header.. --%>
            <%
                if(firstItemSearchClass == SearchClass.EXPERIMENT) {
            %>
            <td class="headerdarkmid">Interactions
            </td>
            <%
                }
                else if(Interaction.class.isAssignableFrom(firstItem.getObject().getClass())) {
            %>


            <%-- colspan="2" --%>
            <td nowrap="nowrap" class="headerdarkmid">Experiment</td>
            <td nowrap="nowrap" class="headerdarkmid">Proteins</td>
            <%
                } else if (firstItemSearchClass == SearchClass.PROTEIN ||
                           firstItemSearchClass == SearchClass.NUCLEIC_ACID ||
                           firstItemSearchClass == SearchClass.SMALL_MOLECULE) {
            %>
             <td nowrap="nowrap" class="headerdarkmid">Interactions
            </td>
            <td nowrap="nowrap" class="headerdarkmid">Interactors
            </td>
            <% } %>
        </tr>


<%-- now iterate through the list to display the table rows... --%>
<%
        //Each table is roughly the same format - but for eg Experiments and Interactions
        //we display some extra info. Simplest way is to iterate through a List, swapping
        //the List over after each partitioned List has been processed.
        for(Iterator<SimpleViewBean> it = displayList.iterator(); it.hasNext();) {

            //know it is the correct type by the time we get here
            SimpleViewBean bean = it.next();

            //set up the searchURL - this is different depending upon the display type,
            //so for Experiments and Interactions this should link to 'detail-blue',
            //but for Proteins it should go to the partners view and for CvObjects
            //it should do a 'single CVObject' view (ie what is in results.jsp now)
            String searchURL = bean.getObjSearchURL();


%>


        <!-- data row: -->
        <tr>

            <!-- checkbox - presumably checked by default? NOT NEEDED for CvObjects and Experiments -->
            <%
                if( firstItemSearchClass.isCvObjectSubclass()
                     ||
                    firstItemSearchClass == SearchClass.EXPERIMENT ) {
            %>
            <!-- Single cell padding -->
            <td>
             &nbsp;
            </td>
            <%
                } else {
            %>

            <!-- checkbox -->
            <td align="right" style="vertical-align: top;" >
                <input name="<%= bean.getObjAc()%>" type="checkbox" class="td.righttop" >
            </td>
            <%
                }
            %>

          <%
          if(firstItemSearchClass == SearchClass.PROTEIN ||
             firstItemSearchClass == SearchClass.NUCLEIC_ACID ||
             firstItemSearchClass == SearchClass.SMALL_MOLECULE)  { %>

               <%-- name (ie Intact shortlabel), and linked to a suitable view -
                need to set a value in the request to identify this JSP, so that the struts
                Action classes know what to do with eg Protein search requests..
            --%>

            <td nowrap="nowrap" style="vertical-align: top;">
               <a href="<%= searchURL + "&" + SearchConstants.PAGE_SOURCE + "=partner" + "&filter=ac&page=1"%>">
                   <b><span style="color: rgb(255, 0, 0);"><%=bean.getObjIntactName()%></span></b></a><br>
            </td>

            <!-- Ac linked to single view -->

              <td nowrap="nowrap" style="vertical-align: top;">
               <a href="<%= searchURL + "&" + SearchConstants.PAGE_SOURCE + "=single"+ "&filter=ac" %>">
                  <%= bean.getObjAc() %></a><br>
            </td>




               <!-- Gene Name (not linked)  -->
           <td class="lefttop" rowspan="1" colspan="1">
                <%
                    Collection<String> somePartnerGeneNames = bean.getGeneNames((Interactor)bean.getObject());

                    for (Iterator<String> iteratorGene =  somePartnerGeneNames.iterator(); iteratorGene.hasNext();) {
                        String aGeneName =  iteratorGene.next();
                        out.write( aGeneName );
                        if( iteratorGene.hasNext() ) {
                            out.write( ", " );
                        }
                    }
                %>
            </td>

          <% } else { %>

                <%-- name (ie Intact shortlabel), and linked to a suitable view -
                need to set a value in the request to identify this JSP, so that the struts
                Action classes know what to do with eg Protein search requests..
            --%>

            <td nowrap="nowrap" style="vertical-align: top;">
               <a href="<%= searchURL + "&filter=ac&page=1" %>">
                   <b><span style="color: rgb(255, 0, 0);"><%=bean.getObjIntactName()%></span></b></a><br>
            </td>

            <!-- Ac linked to single view -->

              <td nowrap="nowrap" style="vertical-align: top;">
               <a href="<%= searchURL + "&filter=ac&page=1" %>"><%= bean.getObjAc() %></a><br>
            </td>






          <% } %>
            <!-- Description (ie full name - or a dash if there isn't one), not linked -->
            <td class="lefttop"> <!-- rowspan="1" colspan="3" -->
                <%= bean.getObjDescription()%><br>
            </td>

            <!-- 'number of related items', ie interactions for Experiments,
                    Proteins for Interactions. This is not linked to anything either -->
            <%
                if ( firstItemSearchClass == SearchClass.INTERACTION ) {
            %>
            <td class="data">
                <%
                    Interaction interaction = (Interaction) bean.getObject();
                    Collection experiments = interaction.getExperiments();

                    for ( Iterator iterator = experiments.iterator(); iterator.hasNext(); ) {
                        Experiment experiment = (Experiment) iterator.next();
                 %>
                        <a href="<%= bean.getObjSearchURL( Experiment.class, experiment.getAc() ) + "&filter=ac" %>"><%= experiment.getShortLabel() %></a>
                 <%
                        if( iterator.hasNext() ) {
                            out.write( "<br>" );
                        }
                    }
                %>
            </td>
            <%
                }
            %>

            <!-- 'number of related items', ie interactions for Experiments,
                    Proteins for Interactions. This is not linked to anything either -->
            <%
                if(firstItemSearchClass == SearchClass.EXPERIMENT ||
                    firstItemSearchClass == SearchClass.INTERACTION) {
            %>
            <td class="data">
                <nobr><%= bean.getRelatedItemsSize() %><br></nobr>
            </td>
            <%
                }
            %>

             <%
                 if (firstItemSearchClass == SearchClass.PROTEIN ||
                     firstItemSearchClass == SearchClass.NUCLEIC_ACID ||
                     firstItemSearchClass == SearchClass.SMALL_MOLECULE)
                 {

             %>
            <td class="data">
                <nobr><%= bean.getNumberOfInteractions(((Interactor)bean.getObject())) %><br></nobr>
            </td>
            <td class="data">
                <nobr><%= bean.getNumberOfInteractors(((Interactor)bean.getObject())) %><br></nobr>
            </td>
            <%
                }
            %>


        </tr>

        <%
        }   //closes the row loop
        %>

    </tbody>

</table>

<%

    }   //ends the partition loop - now do the next group...

%>

<%-- pagination --%>
<%@ include file="tablePagination.jspf"%>

<%@ include file="buttonBar.html" %>

<br>

</form> <%-- the end of the page details --%>