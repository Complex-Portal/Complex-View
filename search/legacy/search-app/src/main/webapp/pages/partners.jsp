<%--
    Page to display a search summary view (used to be the 'binary' view). Current
    specification is as per the mockups June 2004. This page is typically used when a
    Protein is searched for - a summary of the relevant information (eg Interactions
    etc) is displayed.
--%>

<!--
    @author Chris Lewington
    @version $Id$
--%>

<!-- need to provide an error page here to catch unhandled failures -->
<%@ page language="java"  %>
<%@ page buffer="none"    %>
<%@ page autoFlush="true" %>

<!-- Intact classes needed -->
<%@ page import="uk.ac.ebi.intact.searchengine.SearchClass,
                 uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants,
                 uk.ac.ebi.intact.webapp.search.struts.view.beans.PartnersView"%>

<!-- Standard Java classes -->
<%@ page import="uk.ac.ebi.intact.webapp.search.struts.view.beans.PartnersViewBean"%>
<%@ page import="java.util.Collection"%>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>

<!-- may make use of these later to tidy up the JSP a little -->
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>

<%
    SearchWebappContext webappContext = SearchWebappContext.getCurrentInstance();

    //build the URL for hierarchView from the absolute path and the relative beans..
    String hvPath = webappContext.getHierarchViewAbsoluteUrl();
    String minePath = webappContext.getMineAbsoluteUrl();

    //The View object containing the beans to render
    PartnersView partnersView = (PartnersView) request.getAttribute(SearchConstants.VIEW_BEAN);

    //the list of shortlabels for the search matches - need to be highlighted
    //NB the SearchAction ensures this will never be null
    List<String> highlightList = (List<String>) request.getAttribute(SearchConstants.HIGHLIGHT_LABELS_LIST);

    // We set the searchClass in the request, if the search comes directly using the search box on the left.
    // Used for pagination purposes
    if (request.getParameter("searchClass") == null)
    {
        request.setAttribute("searchClass", SearchClass.PROTEIN.getShortName());
    }
%>

<%-- The javascript for the button bars.... --%>
<%@ include file="jscript.html" %>

<span class="smalltext">Search Results for <%= session.getAttribute(SearchConstants.SEARCH_CRITERIA) %> </span>
<br/>
<span class="verysmalltext">(short labels of search criteria matches are
    <span style="color: rgb(255, 0, 0);">highlighted</span>
</span><span class="verysmalltext">)<br></span></p>

<!-- the main form for the page -->
<form name="viewForm">

    <!-- NB the display is basically one table definition, repeated for each Protein
    search match ..... -->

    <!-- interate through the viewbean List and display each one in a new table... -->

    <%
        boolean hasPartners = true;  //decide whether or not to display button bar

                //OK to carry one - otherwise skip the bean
                PartnersViewBean bean = partnersView.getInteractorCandidate();

                //first check for 'orphan' Proteins and display an appropriate message...
                //(NB multiple Protein matches are handled by the 'main' view, but a single
                //match will come through to here so we need the check)
                if(partnersView.getInteractionPartners().isEmpty()) {
                    hasPartners = false;
                    %>
                    <br>
                     <h4>The Protein with Intact name
                        <b><span style="color: rgb(255, 0, 0);"><%= bean.getMainInteractor().getShortLabel() %></span></b>
                        and AC <%= bean.getMainInteractor().getAc()%> has no Interaction partners </h4>
                     <br>
                    <%
                }
                else {
                    //process as normal...
    %>

    <!-- we need the buttons at the top as well as the bottom now -->
    <%@ include file="buttonBar.html" %>

    <%@ include file="tablePagination.jspf"%>

    <!-- the main data table -->
    <table style="width: 100%; background-color: rgb(51, 102, 102);" width="100%"  cellpadding="5">
        <tbody>

            <!-- header row -->
            <tr>
                <!-- padding cell -->
                <td class="headermid"><br>
                </td>

                <td nowrap="nowrap" class="headerlight" rowspan="1" colspan="1">
                    <a href="<%= bean.getHelpLink() + "AnnotatedObject.shortLabel"%>"
                    target="new" class="tdlink" title="Click to see interactors for this molecule">IntAct name<sup>?</sup><br></a>
                </td>

                <td nowrap="nowrap" class="headerlight" colspan="1">
                    <a href="<%= bean.getHelpLink() + "BasicObject.ac"%>" target="new"
                        class="tdlink" title="Click to see molecule details">IntAct Ac<sup>?</sup><br></a>
                </td>

                 <td  class="headerlight">
                   <span title="Click to see interaction details">Number of <br>interactions<sup>?</sup></span><br>
                </td>

                <td nowrap="nowrap" class="headerlight" colspan="1">Identifier<br></td>

                <td nowrap="nowrap" class="headerlight"colspan="1">Gene name<br></td>

                <td class="headerlight">Description<br></td>
            </tr>


            <!-- first row of data (ie the Protein search match) -->
            <tr bgcolor="#eeeeee">

                <!-- checkbox: NB search results to be checked by default -->
                <td class="headermid">
                    <code><input type="checkbox" name="<%= bean.getMainInteractor().getAc()%>" ></code>
                </td>

                <!-- shortlabel with link: seems to be back to this page (!!)... -->
                 <td nowrap="nowrap" style="vertical-align: top; background-color: rgb(255, 255, 255);">
                    <code><a title="Click to see interactors for this molecule" href="<%= bean.getInteractorPartnerURL()%>">
                        <% if(highlightList.contains(bean.getMainInteractor().getShortLabel())) { %>
                            <b><span style="color: rgb(255, 0, 0);"><%= bean.getMainInteractor().getShortLabel()%></span></b>
                        <%
                            }
                            else {
                                //no highlighting
                        %>
                            <%= bean.getMainInteractor().getShortLabel()%>
                        <%
                            }
                        %>
                    </a></code>
                </td>

                <!-- AC, with link to single Protein details page -->
                 <td nowrap="nowrap" style="vertical-align: top; background-color: rgb(255, 255, 255);">
                    <a title="Click to see interactors for this molecule"
                       href="<%= bean.getInteractorSearchURL()%>"><%= bean.getMainInteractor().getAc()%></a><br>
                </td>

                <!-- number of Interactions, link to a'simple' result page for the Interactions
                -->
                <%--
                    ISSUE: The mockup says to go to 'detail-blue', but this is clearly
                    unworkable for even moderate Interaction lists across multiple Experiments,
                    because the 'detail-blue' view will contain far too much information.
                    DECISION: link to the 'front' search page instead, then users can choose
                    what detail they want.
                --%>
                <td nowrap="nowrap"  align="center" style="vertical-align: top; background-color: rgb(255, 255, 255);">
                       <a title="Click to see interaction details" href="<%= bean.getInteractionsSearchURL()%>"><%= bean.getNumberOfInteractions()%></a>
                </td>

                <!-- Uniprot AC-->
                 <td class="data" style="vertical-align: top; background-color: rgb(255, 255, 255);">
                    <% if( null == bean.getUniprotAc() ) { %>
                       -
                    <% } else { %>
                       <a href="<%= bean.getIdentityXrefURL() %>"><%= bean.getUniprotAc() %></a>
                    <% } %>
                </td>

                <!-- gene name, not linked -->
                <td class="data" style="vertical-align: top; background-color: rgb(255, 255, 255);"
                    rowspan="1" colspan="1">
                    <% Collection<String> someGeneNames = bean.getGeneNames();

                       for (Iterator<String> iterator =  someGeneNames.iterator(); iterator.hasNext();) {
                           String aGeneName =  iterator.next();
                           out.write( aGeneName );
                           if( iterator.hasNext() ) {
                               out.write( ", " );
                           }
                       }
                   %>
                </td>
                <!-- description, not linked -->
                <td class="data" style="vertical-align: top; background-color: rgb(255, 255, 255);">
                    <%= bean.getMainInteractor().getFullName()%><br>
                </td>

            </tr>


            <!-- seperating row, containing the 'interacts with' header -->
            <tr bgcolor="#eeeeee">

                <!-- single cell padding -->
                <td class="headerlight"><br>
                </td>

                <!-- heading, spans the table width (6 columns) -->
                <td class="data" rowspan="1" colspan="6" style="background-color: rgb(255, 255, 255);">
                    interacts with
                    <br>
                </td>

            </tr>

            <!-- 1. Protein is done, now look at the partners -->

            <!-- partner rows:
                NB: Each interaction partner needs to be displayed with a summary
                viewbean format itself - we can get a Set of view beans from the
                main view bean for the search result.

            -->
            <%
                Collection<PartnersViewBean> partners = partnersView.getInteractionPartners();

                for( PartnersViewBean partner : partners) {

            %>
            <tr>

                <!-- checkbox -->
                <td class="headermid">
                    <code><input type="checkbox" name="<%= partner.getMainInteractor().getAc()%>"></code>
                </td>

                    <!-- shortlabel, linked back to this view for the partner instead -->
                <td nowrap="nowrap" style="vertical-align: top; background-color: rgb(255, 255, 255);">
                    <code><a href="<%= partner.getInteractorPartnerURL()%>"><nobr><%= partner.getMainInteractor().getShortLabel() %></nobr></a></code>
                </td>

                <!-- AC, linked to single Protein details page -->
                <td nowrap="nowrap" style="vertical-align: top; background-color: rgb(255, 255, 255);">
                    <a href="<%= partner.getInteractorSearchURL()%>"><%= partner.getMainInteractor().getAc()%></a><br>
                </td>

                <!-- number of Interactions, linked new detail page -->

                <td align="center" style="vertical-align: top; background-color: rgb(255, 255, 255);">
                    <a href="<%= partner.getInteractionsSearchURL()%>"><%= partner.getNumberOfInteractions() %></a>
                </td>

                <!-- Uniprot AC, BUT:
                    The mockup says the FIRST partner ONLY is linked to Uniprot (TBD),
                    whilst the others are not. Is this correct? If so then how is
                    the order of partners determined?
                    -->
                <td class="data" style="vertical-align: top; background-color: rgb(255, 255, 255);">
                    <% if( null == partner.getUniprotAc() ) { %>
                       -
                    <% } else { %>
                       <a href="<%= partner.getIdentityXrefURL()%>"><%= partner.getUniprotAc() %></a>
                    <% } %>
                </td>

                <!-- gene name, not linked -->
                <td class="data" style="vertical-align: top; background-color: rgb(255, 255, 255);"
                    rowspan="1" colspan="1">

                    <% Collection somePartnerGeneNames = partner.getGeneNames();

                       for (Iterator iterator =  somePartnerGeneNames.iterator(); iterator.hasNext();) {
                           String aGeneName =  (String) iterator.next();
                           out.write( aGeneName );
                           if( iterator.hasNext() ) {
                               out.write( ", " );
                           }
                       }
                     %>

                </td>

                <!-- description, not linked -->
                <td class="data" style="vertical-align: top; background-color: rgb(255, 255, 255);">
                    <%= partner.getMainInteractor().getFullName() %>
                </td>

            </tr>

            <%
                }   //done the partners
            %>
        </tbody>
    </table>

    <%
                }   //done 'orphan' check
    %>

    <%
            //need button bar underneath too IF this is the last one to be processed...
            if(hasPartners) {
    %>

        <%@ include file="tablePagination.jspf"%>

        <!-- same buttons as at the top of the page -->
        <%@ include file="buttonBar.html" %>
    <%
            } //ends button bar check
    %>

</form>