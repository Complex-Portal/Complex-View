<html>

<%@include file="header.jsp"%>
<jsp:useBean id="results"  class="uk.ac.ebi.intact.service.complex.view.ComplexRestResult"      scope="session"/>
<jsp:useBean id="htmlOriginalQuery" class="java.lang.String"    scope="session"/>

<div class="grid_12 omega">
    <form id="local-search" name="local-search" action="${complex_search_form}" method="get">
        <fieldset>
            <div class="left">
                <label>
                    <input type="text" name="q" id="local-searchbox" value="<c:out value="${sessionScope.htmlOriginalQuery}"/>">
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
        <li class="active"><a href="${complex_search_url}">Search</a></li>
        <%--<li><a href="${complex_advanced_search_url}">Advanced Search</a></li>--%>
        <%--<li><a href="${complex_downloads_url}">Downloads</a></li>--%>
        <li><a href="${complex_help_url}">Help</a></li>
        <li><a href="${complex_stats_url}">Statistics</a></li>
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

    <h2>No ${complex_portal_name} results found</h2>
    <p class="alert">We're sorry but we couldn't find anything that matched your search for <c:out value="${sessionScope.htmlOriginalQuery}"/></p>

    <section class="grid_24 alpha">
        <%--<h3>Try the advanced search</h3>--%>
        <p>Please consider refining your terms:</p>
        <ul>
            <li>Make sure all words are spelled correctly</li>
            <li>Try different keywords</li>
            <li>Be more precise: use gene or protein IDs, e.g. Ndc80 or Q04571</li>
            <li>Remove quotes around phrases to search for each word individually. <i>bike shed</i> will often show more results than &quot;<i>bike shed</i>&quot;</li>
        </ul>

        <%--<h3>Did you mean...</h3>--%>
        <%--<ul>--%>
            <%--<li>Suggestion 1</li>--%>
            <%--<li>Suggestion 2</li>--%>
            <%--<li>Suggestion 3</li>--%>
        <%--</ul>--%>

        <%--<h4>Still can't find what you're looking for?</h4>--%>
        <%--<p>Please <a href="#" title="">contact our support service</a> for help if you still get no results.</p>--%>

    </section>

    <%--<aside class="grid_8 omega shortcuts" id="search-extras">--%>
        <%--<div id="ebi_search_results"><h3>More data from EMBL-EBI</h3>--%>
        <%--</div>--%>
    <%--</aside>--%>

</div>
<%@include file="footer.jsp"%>
</html>