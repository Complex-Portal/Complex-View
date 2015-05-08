<html>

<%@include file="header.jsp"%>
<%--<jsp:useBean id="stats_total"       class="java.lang.Integer"   type="java.lang.Integer"    scope="session"/>--%>
<jsp:useBean id="stats_species"     class="java.util.ArrayList" type="java.util.List"       scope="session"/>
<jsp:useBean id="stats_interactor"  class="java.util.ArrayList" type="java.util.List"       scope="session"/>
<jsp:useBean id="stats_biorole"     class="java.util.ArrayList" type="java.util.List"       scope="session"/>

<div class="grid_12 omega">
    <form id="local-search" name="local-search" action="${complex_search_form}" method="get">
        <fieldset>
            <div class="left">
                <label>
                    <input type="text" name="q" id="local-searchbox" value="<c:out value="${sessionScope.results.originaQuery}"/>">
                </label>
                <!-- Include some example searchterms - keep them short and few! -->
                <span class="examples">Examples: <a href="<c:url value="${complex_search_form}?q=GO:0016491"/>">GO:0016491</a>, <a href="<c:url value="${complex_search_form}?q=Ndc80"/>">Ndc80</a>, <a href="<c:url value="${complex_search_form}?q=Q05471"/>">Q05471</a></span>
            </div>
            <%--<input name="facets" id="facets" type="hidden" value="${facetFields}" />--%>
            <div class="right">
                <input type="submit" value="Search" class="submit">
                <!-- If your search is more complex than just a keyword search, you can link to an Advanced Search,
                     with whatever features you want available -->
            </div>
        </fieldset>
    </form>
</div>


<nav>
    <ul class="grid_24" id="local-nav">
        <li class="first"><a href="${complex_home_url}">Home</a></li>
        <li><a href="${complex_about_url}">About</a></li>
        <li><a href="${complex_documentation_url}">Documentation</a></li>
        <li><a href="${complex_search_url}">Search</a></li>
        <%--<li><a href="${complex_advanced_search_url}">Advanced Search</a></li>--%>
        <%--<li><a href="${complex_downloads_url}">Downloads</a></li>--%>
        <li><a href="${complex_help_url}">Help</a></li>
        <li class="active"><a href="${complex_stats_url}">Statistics</a></li>
        <li class="last"><a href="${complex_download_url}">Download</a></li>
        <%--<li class="last"><a href="${complex_contact_url}">Contact Us</a></li>--%>
        <li class="functional last first"><a class="icon icon-generic" data-icon="\" href="http://www.ebi.ac.uk/support/intact">Feedback</a></li>
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
    <nav id="breadcrumb">
        <p>
            <a href="${complex_home_url}">${complex_portal_name}</a> &gt;
            Statistics
        </p>
    </nav>
    <h2>Statistics:</h2>
    <h3>Species:</h3>
    <ul>
    <c:forEach var="species" items="${sessionScope.stats_species}">
        <li><c:out value="${species.name}"/>: <c:out value="${species.count}"/></li>
    </c:forEach>
    </ul>
    <h3>Interactor type:</h3>
    <ul>
        <c:forEach var="interactor" items="${sessionScope.stats_interactor}">
            <li><c:out value="${interactor.name}"/>: <c:out value="${interactor.count}"/></li>
        </c:forEach>
    </ul>
    <h3>Participant's biological role:</h3>
    <ul>
        <c:forEach var="biorole" items="${sessionScope.stats_biorole}">
            <li><c:out value="${biorole.name}"/>: <c:out value="${biorole.count}"/></li>
        </c:forEach>
    </ul>
</div>



<%@include file="footer.jsp"%>
</html>