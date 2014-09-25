<html>

<%@include file="header.jsp"%>

<div class="grid_12 omega">
    <form id="local-search" name="local-search" action="${complex_search_form}" method="get">
        <fieldset>
            <div class="left">
                <label>
                    <input type="text" name="q" id="local-searchbox" value="">
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
        <li class="active"><a href="${complex_about_url}">About</a></li>
        <li><a href="${complex_documentation_url}">Documentation</a></li>
        <li><a href="${complex_search_url}">Search</a></li>
        <%--<li><a href="${complex_advanced_search_url}">Advanced Search</a></li>--%>
        <%--<li><a href="${complex_downloads_url}">Downloads</a></li>--%>
        <li><a href="${complex_help_url}">Help</a></li>
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
            About
        </p>
    </nav>
    <p>The Complex Portal is a manually curated, encyclopaedic resource of macromolecular complexes from a number of key model organisms, entered into the <a href="${intact_url}">IntAct</a> molecular interaction database. Data includes protein-only complexes as well as protein-small molecule and protein-nucleic acid complexes. All complexes are derived from physical molecular interaction evidences extracted from the literature, and cross-referenced in the entry, or by curator inference from information on orthologues in closely related species. Any complex which currently lacks direct experimental evidence is tagged as such, using controlled vocabulary terms from the <a href="${complex_documentation_url}#evidences">Evidence Code Ontology</a>.</p>
    <p>The number of complexes available is currently limited, but will be added to over time. If you wish to request the curation of one or more complexes, or to amend or contribute to an existing entry, please contact us on <a href="${complex_contact_url}">intact-help</a>.</p>
    <p>To read more on complex curation, go to <a href="${complex_documentation_url}">Documentation</a>.</p>
    <p>To find out more about the search algorithms, go to <a href="${complex_help_url}">Help</a>.</p>
    <p>Complexes can be downloaded in PSI-MI <a href="http://www.psidev.info/mif">XML 2.5.4</a> format from our <a href="${complex_ftp_url}">ftp site</a>. Individual complexes files are grouped by species.</p>
</div>



<%@include file="footer.jsp"%>
</html>