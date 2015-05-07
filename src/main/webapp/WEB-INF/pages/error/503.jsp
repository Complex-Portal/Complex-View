<html>

<%@include file="../header.jsp"%>

<div class="grid_12 omega">
    <form id="local-search" name="local-search" action="${complex_search_form}" method="get">
        <fieldset>
            <div class="left">
                <label>
                    <input type="text" name="q" id="local-searchbox" value=""/>
                </label>
                <!-- Include some example searchterms - keep them short and few! -->
                <%--<span class="examples">Examples: <a href="#" title="">thing1</a>, <a href="#" title="">thing2</a>, <a href="#" title="">thing3</a></span>--%>
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
        <li><a href="${complex_download_url}">Download</a></li>
        <li class="last"><a href="${complex_contact_url}">Contact Us</a></li>
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

    <h2>Service Unavaiable</h2>
    <p class="alert">We're sorry but now our server it's overload. Please try in few minutes.</p>

    <%--<aside class="grid_8 omega shortcuts" id="search-extras">--%>
    <%--<div id="ebi_search_results"><h3>More data from EMBL-EBI</h3>--%>
    <%--</div>--%>
    <%--</aside>--%>

</div>
<%@include file="../footer.jsp"%>
</html>