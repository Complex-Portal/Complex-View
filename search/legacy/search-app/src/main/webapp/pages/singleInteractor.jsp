<%--
    Page to display a single Protein view. Current specification is as per
    the mockups June 2004. This page view will display ONLY single Proteins. O
    bjects such as BioSource and CvObjects are handled elsewhere (currently as before
    via the HtmlBuilder and results.jsp - this will be changed over to JSP-only when I
    have completed the main other views).

    This page will usually be used when a user clicks on a hyperlink of another
    page to view the specific details of a single Protein. Protein searches are handled
    by the overview.jsp page as the interaction partners are required in that case.
--%>

<!--
    @author Chris Lewington
    @version $Id$
-->

<!-- really need to have an error page specified here to catch anything unhandled -->
<%@ page language="java"  %>
<%@ page buffer="none"    %>
<%@ page autoFlush="true" %>

<!-- Mostly used by the javascript for HV -->
<%@ page import="uk.ac.ebi.intact.model.Xref,
                 java.util.Collection,
                 java.util.Iterator"%>
<%@ page import="uk.ac.ebi.intact.webapp.search.SearchWebappContext" %>
<%@ page import="uk.ac.ebi.intact.webapp.search.struts.view.beans.InteractorViewBean" %>
<%@ page import="uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants" %>

<!-- Standard Java imports -->

<!-- taglibs: maybe make use of these later to tidy up the JSP -->
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>

<%
    SearchWebappContext webappContext = SearchWebappContext.getCurrentInstance();

    //build the URL for hierarchView from the absolute path and the relative beans..
    String hvPath = webappContext.getHierarchViewAbsoluteUrl();
    String minePath = webappContext.getMineAbsoluteUrl();

    //The view bean used to provide the data for this JSP. Could probably use
    //the jsp:useBean tag instead, but do it simply for now...
    InteractorViewBean bean = (InteractorViewBean) session.getAttribute(SearchConstants.VIEW_BEAN);
    // InteractorViewBean bean = (InteractorViewBean)request.getAttribute(SearchConstants.VIEW_BEAN);
%>


<%-- The javascript for the button bars.... --%>
<%@ include file="jscript.html" %>

<%--    <span class="middletext">Search Results for <%=session.getAttribute(SearchConstants.SEARCH_CRITERIA)%> <br></span>--%>

<span class="smalltext">Search Results for <%= session.getAttribute(SearchConstants.SEARCH_CRITERIA) %> </span>
<br/>
<span class="verysmalltext">(short labels of search criteria matches are
    <span style="color: rgb(255, 0, 0);">highlighted</span>
</span><span class="verysmalltext">)<br></span></p>
<!--
The (repaired) HTML here is more or less what was specified in the Intact webpage
mockups, June 2004
-->

<form name="viewForm">

<!-- we need the buttons at the top as well as the bottom now -->
<%@ include file="buttonBar.html" %>

    <!-- The main display table -->
    <table style="background-color: rgb(51, 102, 102); width: 100%;" cellpadding="2">
        <tbody>

            <!-- Row 1: top row Protein info NB the checkbox should be selected by default -->
            <tr bgcolor="white">

                <!-- checkbox and main label --->
                <td class="headerdark">
                    <nobr>
                    <input type="checkbox" name="<%= bean.getInteractorAc()%>" >
                    <span class = "whiteheadertext"><%= bean.getInteractorType()%></span>
                    <a href="<%= bean.getHelpLink() + "Interactor"%>"
                    target="new"><sup><b><font color="white">?</font></b></sup></a></nobr>
                </td>

                <!-- shortlabel -->
                <td class="headerdarkmid"
                    rowspan="1" colspan="2">
                    <a href="<%= bean.getHelpLink() + "AnnotatedObject.shortLabel"%>"
                        class="tdlink"
                       target="new">IntAct name:</a>
                    <b><span style="color: rgb(255, 0, 0);"><%= bean.getInteractorIntactName() %></span></b>
                </td>

                <!-- AC:- NB this doesn't appear to need a hyperlink... -->
                <td class="headerdarkmid">
                    <a href="<%= bean.getHelpLink() + "BasicObject.ac"%>"
                        target="new" class="tdlink">Ac:</a> <%= bean.getInteractorAc() %>
                </td>

                <!-- Single cell padding -->
                <td rowspan ="1" class="headerdarkmid"></td>
            </tr>

           <!-- Rows 2 and 3: Biosource info -->
            <tr bgcolor="white">

                <!-- biosource label+ help link -->
                <td rowspan="1" colspan="1" class="headerdarkmid">Source
                    <a
                        href="<%= bean.getHelpLink() + "Interactor.bioSource"%>"
                        target="new"><br>
                    </a>
                </td>

                <td class="lefttop" rowspan="1" colspan="4">
                    <% if (bean.getBioSearchURL() != null) { %>
                        <a href="<%= bean.getBioSearchURL()%>">
                    <% } %>
                        <%= bean. getBioSourceName()%>
                    <% if (bean.getBioSearchURL() != null) { %>
                        </a>
                    <% } %>
                </td>
            </tr>

            <!-- Protein description + Biosource help link (again) -->
            <tr bgcolor="white">
                <td class="headerdarkmid">
                <a href="<%= bean.getHelpLink() + "Interactor.bioSource"%>"
                    target="new" class="tdlink">Description<br> </a>
                </td>

                <!-- The text is the Protein description...-->
                <td class="lefttop" rowspan="1" colspan="4">
                   <%= bean.getInteractorDescription() %>&nbsp;
                </td>
            </tr>

            <!-- Row 4: gene names row -->
            <tr bgcolor="white">
                <td class="headerdarkmid">Gene name(s)<br></td>
               <td class="lefttop" rowspan="1" colspan="4">
                    <%
                        Collection somePartnerGeneNames = bean.getGeneNames();

                        for (Iterator iterator =  somePartnerGeneNames.iterator(); iterator.hasNext();) {
                            String aGeneName =  (String) iterator.next();
                            out.write( aGeneName );
                            if( iterator.hasNext() ) {
                                out.write( ", " );
                            }
                        }
                    %>
               </td>
            </tr>

            <!-- Row 5: gene names row -->
            <tr bgcolor="white">
                <td class="headerdarkmid">Interaction(s)<br></td>
               <td class="lefttop" rowspan="1" colspan="4">
                    <%
                        int count = bean.getInteractionsCount();
                        out.write( "" + count );

                        // Display the link only if there is data to be displayed.
                        if( count > 0 ) {
                            out.write( "&nbsp;(<a href=\""+ bean.getBinaryViewUrl() +"\">see all interaction partners</a>)");
                        }
                    %>
               </td>
            </tr>


            <!-- Xrefs rows (xN)... -->

<%
    //get the first one and process it on its own - it seems we need it to put a search
    //link for it into the first cell of the row, then process the others as per
    //'usual'..(NB assume we have at least one Xref)

//    Xref firstXref = (Xref)bean.getXrefs().iterator().next();
%>

<!--
NOTE: For the first row the 'Xref' link is set up and then the 'loop' body is
executed to do the first (sgd) row, THEN the loop itself will generate the
other rows - so the first 'sgd' row is actually the same row as the 'Xref' row, and the
others are subsequent rows.

-->
        <tr bgcolor="white">

                <!-- label + CvDatabase link for primary ID (assumes first Xref is primary)-->
                <td class="headerdarkmid"
                rowspan="<%= bean.getXrefs().size() %>" colspan="1">
                   <a href="<%= bean.getHelpLink() + "Xref.cvrefType"  %>" class="tdlink">Xref<br></a></td>


        <!-- don't close the row tag here - it will be done in the Xref loop below
        for the first item....
        -->

 <!-- now do all the Xrefs in turn ......... -->
<%
    if(bean.getXrefs().isEmpty()){  %>
    <td class="data" style="vertical-align: top;" colspan="4"> - </td>
            </tr>


  <%  }else{
    Collection xrefs =  bean.getXrefs();
    for(Iterator it = xrefs.iterator(); it.hasNext();) {
        Xref xref = (Xref)it.next();

    //NB HtmlBuilder assumes first Xref is the primary one..
   //I THINK a single Xref has 4 possible cells -
   //search link, primary ID, secondary Id, qualifier (which has search+help links?)

//        if(!xref.equals(firstXref)) {
            //we need to have new rows for each Xref OTHER THAN the first..
%>
            <!--<tr bgcolor="white">-->
<%
//        }
%>
                <!-- cells for each Xref item, starting with the link to
                the Cvdatabase details (ie through search) -->
                <!-- NB the text is assumed to be the shortlabel of the CvDB -->
                <td bgcolor="white" class="data" style="vertical-align: top;">
                    <a href="<%= bean.getCvDbURL(xref)%>"><%= xref.getCvDatabase().getShortLabel()%></a>
                </td>

                <!-- a DB-sourced URL for the Primary Id (the text should be the primary Id)...
                NB if it is null we can't write a link
                -->
                <td bgcolor="white" class="data">
                    <%
                        String idUrl = bean.getPrimaryIdURL(xref);
                        if(idUrl != null) {
                    %>
                    <a href="<%= idUrl %>"><%= xref.getPrimaryId()%></a>
                    <%
                        }
                        else {
                            out.write(xref.getPrimaryId());
                        }
                    %>
                </td>

                <!-- a secondary Id (no links, so simple) -->
                <td bgcolor="white" class="data">
                    <%
                        if(xref.getSecondaryId() != null) out.write(xref.getSecondaryId());
                        else out.write("-");
                    %>
                </td>

                <!-- Xref qualifier (dash if null) -->
                <td bgcolor="white" class="data">
                    <%
                        if(xref.getCvXrefQualifier() != null) {
                    %>
                    <!-- do some links for help and CV info (NB text = qualifier label) -->
                    <a href="<%= bean.getHelpLink() + "Xref.cvXrefType"%>" target="new">Type:</a>
                    <a href="<%= bean.getCvQualifierURL(xref)%>"><%= xref.getCvXrefQualifier().getShortLabel()%></a>

                    <%
                        }
                        else out.write("-");
                    %>
                </td>

        <!-- this tr closes the first (different) row on the first iteration,
            and 'normal' ones subsequently
            -->
        </tr>

<%
    }
    }


      String seq = bean.getSequence();
    if (seq != null) {
%>




        <!-- sequence info (2 rows plus block display) -->
        <tr bgcolor="white">

            <!-- sequence length label -->
            <td class="headerdarkmid">Sequence length<br></td>

            <!-- seq length itself -->
            <td style="vertical-align: top; background-color: rgb(255, 255, 255);"
                class="data" rowspan="1" colspan="4"><%= bean.getSeqLength() %><br>
            </td>
        </tr>

        <!-- checksum -->
        <tr bgcolor="white">

            <!-- label -->
            <td class="headerdarkmid">CRC64 checksum<br></td>

            <!-- value -->
            <td style="vertical-align: top; background-color: rgb(255, 255, 255);"
                class="data" rowspan="1" colspan="4"><%= bean.getCheckSum() %><br>
            </td>

        </tr>

<%
    } // end of display sequence's length and CRC if available.
%>

        <!-- the sequence itself, written as blocks... -->
        <tr bgcolor="white">

            <td class="data" colspan="5" style="background-color: rgb(255, 255, 255);">

<%
    //Write out a formatted sequence, if there is one...

    //The length of one block of amino acids.
    final int SEQBLOCKLENGTH = 10;

    // Sequence itself

    if (seq != null) {
        out.write("<font face=\"Courier New, Courier, monospace\">");
        int blocks = seq.length() / SEQBLOCKLENGTH;
        for (int i = 0; i< blocks; i++){
            out.write(seq.substring( i * SEQBLOCKLENGTH,
                                i * SEQBLOCKLENGTH + SEQBLOCKLENGTH ));
            out.write(" ");
        }
        out.write(seq.substring(blocks*SEQBLOCKLENGTH));
        out.write("</font>");

    }
    else {
        out.write ("<font color=\"#898989\">No sequence available for this interactor.</font>");
    }

%>
            </td>
        </tr>
    </tbody>

</table>
<!-- end of main data table -->

<!-- line spacer -->
<hr size="2">

    <!-- button table, like the one at the top of the page -->
<%@ include file="buttonBar.html" %>

    <!-- the (real) end!! -->
</form>