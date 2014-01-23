<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ page import="java.util.List" %>
<%@ page import="uk.ac.ebi.intact.service.complex.view.ComplexDetailsParticipants" %>
<%@ page import="uk.ac.ebi.intact.service.complex.view.ComplexDetailsCrossReferences" %>
<%@ page import="java.util.Collections" %>
<%@ page import="uk.ac.ebi.intact.service.complex.view.ComplexDetailsFeatures" %>
<html>

<%@include file="header.jsp"%>

<div id="local-masthead" class="masthead grid_24 nomenu">

    <!-- local-title -->

    <div class="grid_24 alpha" id="local-title">
        <h1><a href="${complex_home_url}" title="Back to ${complex_portal_name} homepage"><img src="http://www.ebi.ac.uk/intact/images/IntAct_logo.png"> Complex Portal</a></h1>
    </div>

    <!-- /local-title -->

    <!-- /local-nav -->

    <nav>
        <ul class="grid_24" id="local-nav">
            <li class="first active"><a href="${complex_home_url}">Home</a></li>
            <li><a href="${complex_documentation_url}">Documentation</a></li>
            <li><a href="${complex_about_url}">About ${complex_portal_name}</a></li>
            <li class="last"><a href="${complex_help_url}">Help</a></li>
            <!-- If you need to include functional (as opposed to purely navigational) links in your local menu,
                 add them here, and give them a class of "functional". Remember: you'll need a class of "last" for
                 whichever one will show up last...
                 For example: -->
        </ul>

    </nav>

    <!-- /local-nav -->

</div>

</header>

<div id="content" role="main" class="grid_24 clearfix">
    <!-- Suggested layout containers -->
    <!--
    <section>
        <h2>Fluid Width</h2>

        <div class="grid_12 alpha">
            <h3>Code</h3>
            <ul>
                <li><a href="ebi-level2-boilerplate-blank-fluid.txt">ebi-level2-boilerplate-blank-fluid.txt</a></li>
            </ul>
        </div>

        <div class="grid_12 omega">
            <h3>Examples</h3>
            <ul>
                <li><a href="ebi-level2-boilerplate-blank-fluid.html">Blank</a> - big, empty space between header and footer</li>
                <li><a href="ebi-level2-boilerplate-right-sidebar-fluid.html">Right sidebar</a> - for content-driven pages deeper in your site's hierarchy, the sidebar could contain secondary navigation</li>
                <li><a href="ebi-level2-boilerplate-4cols-fluid.html">Four columns</a> - an example of dividing the page into four broad content areas</li>
                <li><a href="ebi-level2-boilerplate-search-results-fluid.html">Search results</a> - an example page with filters or facets for your results in a left-hand sidebar, and a right-hand sidebar for global results</li>
            </ul>
            <h3>Search Results Examples</h3>
            <ul>
                <li><a href="ebi-search-results-complete.html">Search Results Complete</a> - template for a basic search results page </li>
                <li><a href="ebi-no-search-results.html">No Search Results</a> - template for a page of zero results </li>
            </ul>
        </div>

    </section>
    -->
    <!-- End suggested layout containers -->

    <jsp:useBean id="details" class="uk.ac.ebi.intact.service.complex.view.ComplexDetails" scope="session"/>
    <h2 class="titleDetails"><%if(details.getName() != null){%><%= details.getName() %><%}else{%>&lt;Not available&gt;<%}%> <%if (details.getSpecie() != null){%>(<%= details.getSpecie() %>)<%}%></h2>
    <h3 class="subtitleDetails">IntAct AC: <%if(details.getAc() != null){%><%= details.getAc() %><%}else{%>&lt;Not available&gt;<%}%></h3>
    <div class="details">
        <%if(details.getSystematicName() != null || details.getSynonyms().size() != 0 || details.getFunction() != null || details.getProperties() != null){%>
        <div class="section">
            <h4 class="sectionTitle">Summary</h4>
            <%if(details.getSystematicName() != null){%>
                <label class="sectionEntry">Systematic Name:</label>
                <br>
                <label class="sectionValue"><%=details.getSystematicName()%></label>
            <br><br>
            <%}if(details.getSynonyms().size() != 0){%>
                <label class="sectionEntry">Synonyms:</label>
                <br>
                <%for( String synonym : details.getSynonyms() ) {%>
                    <label class="sectionValue"><%=synonym%><br></label>
                <%}%>
            <br><br>
            <%}if(details.getFunction() != null){%>
                <label class="sectionEntry">Function:</label>
                <br>
                <label class="sectionValue"><%=details.getFunction()%></label>
            <br><br>
            <%}if(details.getProperties() != null){%>
                <label class="sectionEntry">Properties:</label>
                <br>
                <label class="sectionValue"><%=details.getProperties()%></label>
            <%}%>
        </div>
        <br>
        <%}%>
        <div class="section">
            <h4 class="sectionTitle">Participants</h4>
            <table id="participants" class="participants">
                <thead>
                    <tr>
                        <td>ID</td>
                        <td>Name</td>
                        <td>Description</td>
                        <td>Stochiometry</td>
                        <td>Biological Role</td>
                        <td>Interactor Type</td>
                        <td>Linked Features</td>
                        <td>Other Features</td>
                    </tr>
                </thead>
                <tbody>
                    <% for(ComplexDetailsParticipants part : details.getParticipants()) { %>
                        <tr>
                            <td><%if(part.getIdentifier() != null){%><a target="_blank" href="<%=part.getIdentifierLink()%>"><%=part.getIdentifier()%></a><br/><%}%>
                                <%if(part.getInteractorAC() != null){%><a target="_blank" href="http://www.ebi.ac.uk/intact/molecule/<%=part.getInteractorAC()%>"><%=part.getInteractorAC()%></a><%}%></td>
                            <td><%if(part.getName() != null){%><%=part.getName()%><%}%></td>
                            <td><%if(part.getDescription() != null){%><%=part.getDescription()%><%}%></td>
                            <td><%if(part.getStochiometry() != null){%><%=new Double(part.getStochiometry()).intValue()%><%}%></td>
                            <td><%if(part.getBioRole() != null){%><%=part.getBioRole()%><%}%></td>
                            <td><%if(part.getInteractorType() != null){%><%=part.getInteractorType()%><%}%></td>
                            <td><%if(part.getLinkedFeatures().size() > 0){%><%for (ComplexDetailsFeatures linked : part.getLinkedFeatures()) {%><%=linked.getFeatureType()%> <%=linked.getParticipantId()%>
                                <%for(String range : linked.getRanges()){%>[<%=range%>] <%}%><br/><%}}%></td>
                            <td><%if(part.getOtherFeatures().size() > 0){%><%for (ComplexDetailsFeatures other : part.getOtherFeatures()) {%><%=other.getFeatureType()%> <%=other.getParticipantId()%>
                                <%for(String range : other.getRanges()){%>[<%=range%>] <%}%><br/><%}}%></td>
                        </tr>
                    <%}%>
                </tbody>
            </table>
        </div>
        <br>
        <div class="section">
            <h4 class="sectionTitle">Cross References</h4>
            <table id="crossReferences" class="crossReferences">
                <thead>
                <tr>
                    <td>Type</td>
                    <td>Database</td>
                    <td>Identifier</td>
                    <td>Description</td>
                </tr>
                </thead>
                <tbody>
                <% for(ComplexDetailsCrossReferences cross : details.getCrossReferences()) { %>
                <tr>
                    <td><%if(cross.getQualifier() != null){%><%=cross.getQualifier()%><%}%></td>
                    <td><%if(cross.getDatabase() != null){%><%=cross.getDatabase()%><%}%></td>
                    <td><%if(cross.getIdentifier() != null){%><%if(cross.getSearchURL() != null){%><a target="_blank" href="<%=cross.getSearchURL()%>"><%}%><%=cross.getIdentifier()%><%if(cross.getSearchURL() != null){%></a><%}}%></td>
                    <td><%if(cross.getDescription() != null){%><%=cross.getDescription()%><%}%></td>
                </tr>
                <%}%>
                </tbody>
            </table>
        </div>
    </div>
</div>


<%@include file="footer.jsp"%>
</html>