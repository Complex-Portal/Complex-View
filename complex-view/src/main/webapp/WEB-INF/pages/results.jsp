<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ page import="uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexSearchResults" %>
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
            <li class="last"><a href="${complex_about_url}">About ${complex_portal_name}</a></li>
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

        <jsp:useBean id="results" class="uk.ac.ebi.intact.service.complex.view.ComplexRestResult" scope="session"/>
    <h2>Search result for '<%= results.getOriginaQuery() %>'</h2>
    <h3><%= results.getTotalNumberOfElements() %> curated complexes were found.</h3>
    <div class="results">
        <ol start="<%= results.getStartListCount() %>">
            <%
                for( ComplexSearchResults res : results.getElements() ) {
            %>
            <li><p class="complex_name"><%= res.getComplexName() %> complex (<%= res.getOrganismName() %>)</p>
                <p class="complex_description"><%= res.getDescription() %></p>
                <p class="complex_ac">Intact AC: <%= res.getComplexAC() %></p>
            </li>
            <%}%>
        </ol>
        <div class="pages">
            <% if ( (results.getPrevPage() != -1) || (results.getNextPage() != -1) ) { %>
                <% if (results.getPrevPage() != -1) { %>
                    <form class="pagePrevForm" method="POST" action="">
                        <input name="query"  id="queryPrev"  hidden="true" value="<%= results.getOriginaQuery() %>"/>
                        <input name="page"   id="pagePrev"   hidden="true" value="<%= results.getPrevPage() %>"/>
                        <input name="button" id="buttonPrev" value="PrevPage" type="submit"/>
                    </form>
                <%}%>
                <form class="pageCurrentForm" method="POST" action="">
                    Page <input name="page" type="text" class="pageCurrent" value="<%= results.getPage() %>"/> of <%= results.getLastPage() %>
                    <input name="query"  id="queryCurrent"  hidden="true" value="<%= results.getOriginaQuery() %>"/>
                    <input name="button" id="buttonCurrent" hidden="true" type="submit">
                </form>
                <% if (results.getNextPage() != -1) { %>
                    <form class="pageNextForm" method="POST" action="">
                        <input name="query"  id="queryNext"  hidden="true" value="<%= results.getOriginaQuery() %>"/>
                        <input name="page"   id="pageNext"   hidden="true" value="<%= results.getNextPage() %>"/>
                        <input name="button" id="buttonNext" value="NextPage" type="submit"/>
                    </form>
                <%}%>
            <%}%>
        </div>
    </div>
</div>


<%@include file="footer.jsp"%>
</html>