<%--
  Created by IntelliJ IDEA.
  User: maitesin
  Date: 05/12/13
  Time: 10:50
  To change this template use File | Settings | File Templates.
--%>
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
    <section>
        <div class="homeTitles">
            <h2 class="titleHome">IntAct</h2>
            <h5 class="subtitleHome">IntAct provides a freely available, open source database system and analysis tools for molecular interaction data. All interactions are derived from literature curation or direct user submissions and are freely available. To perform a search in the IntAct database use the search box below.</h5>
        </div>
        <br>
        <br>
        <div class="searchDiv">
            <form class="searchForm" method="POST" action="">
                <textarea name="query" rows="10" id="querySearchBox"  type="text"   class="searchBox"    placeholder="Enter search term(s)..."></textarea>
                <br>
                <input name="button" id="button" type="submit" class="searchButton" value="Search" />
            </form>
            <br>
            <br>
            <ul class="helpList">
                <li>Free text search will look by default for interactor identifier, interactor alias, species, interaction identifier, interaction alias and interaction xrefs.</li>
                <li>Search for isoforms of 'P12345' by using 'P12345*'</li>
            </ul>
        </div>
        <div class="exampleDiv">
            <label class="searchExamples">Examples:</label>
            <ul class="exampleList">
                <li>Gene name(s): <form class="exampleForm" id="example1form" action="" method="POST"><input type="hidden" name="query" value="Ndc80"/><a href="#" onclick="document.getElementById('example1form').submit();">Ndc80</a></form></li>
                <li>Protein name(s): <form class="exampleForm" id="example2form" action="" method="POST"><input type="hidden" name="query" value="PCNA"/><a href="#" onclick="document.getElementById('example2form').submit();">PCNA</a></form></li>
                <li>UniProt AC(s): <form class="exampleForm" id="example3form" action="" method="POST"><input type="hidden" name="query" value="Q05471"/><a href="#" onclick="document.getElementById('example3form').submit();">Q05471</a></form></li>
                <%--<li>Pubmed ID: 22540012</li>--%>
            </ul>
            <a href="JavaScript:newPopup('<c:url value="/help/"/>');">Help</a>
        </div>
    </section>
</div>


<%@include file="footer.jsp"%>
</html>