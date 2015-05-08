<%--
  Created by IntelliJ IDEA.
  User: maitesin
  Date: 05/12/13
  Time: 10:50
  To change this template use File | Settings | File Templates.
--%>
<html>

<%@include file="header.jsp"%>
<jsp:useBean id="stats_total" type="java.lang.Integer" scope="session"/>
<jsp:useBean id="stats_species" type="java.lang.Integer" scope="session"/>
<jsp:useBean id="stats_interactor" type="java.lang.Integer" scope="session"/>
<jsp:useBean id="stats_biorole" type="java.lang.Integer" scope="session"/>

<nav>
    <ul class="grid_24" id="local-nav">
        <li class="first active"><a href="${complex_home_url}">Home</a></li>
        <li><a href="${complex_about_url}">About</a></li>
        <li><a href="${complex_documentation_url}">Documentation</a></li>
        <li><a href="${complex_search_url}">Search</a></li>
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
        <%--<div class="homeTitles">--%>
            <%--&lt;%&ndash;<h5 class="subtitleHome">IntAct provides a freely available, open source database system and analysis tools for molecular interaction data. All interactions are derived from literature curation or direct user submissions and are freely available. To perform a search in the IntAct database use the search box below.</h5>&ndash;%&gt;--%>
            <%--<h5 class="subtitleHome">The Complex Portal is a manually curate, encyclopaedic resource of macromolecular complexes from a number of key model organisms. All data is freely available for search and download. To perform a search for macromolecular complexes use the search box below.</h5>--%>
        <%--</div>--%>
            <p class="intro">The Complex Portal is a manually curated, encyclopaedic resource of macromolecular complexes from a number of key model organisms. All data is freely available for search and download. To perform a search for macromolecular complexes use the search box below.</p>
        <%--<br>--%>
        <%--<br>--%>
            <div class="grid_18 alpha">
                <div class="grid_18">
                    <form class="searchForm" method="GET" action="">
                        <fieldset id="searchFormField">
                            <legend>Search in Complex Portal</legend>
                                <textarea name="q" rows="10" id="querySearchBox"  type="text"   class="searchBox"    placeholder="Enter search term(s)..."></textarea>
                                <br>
                                <input type="submit" class="searchButton submit" value="Search" />
                        </fieldset>
                    </form>
                </div>
                <div class="grid_6 exampleDiv">
                    <h3 class="icon icon-generic searchExamples">Examples</h3>
                    <ul class="exampleList">
                        <li>GO term(s): <form class="exampleForm" id="example4form" action="" method="GET"><input type="hidden" name="q" value="GO:0016491"/> <a href="<c:out value="#"/>" onclick="document.getElementById('example4form').submit();">GO:0016491</a></form></li>
                        <li>Gene name(s): <form class="exampleForm" id="example1form" action="" method="GET"><input type="hidden" name="q" value="Ndc80"/>    <a href="<c:out value="#"/>" onclick="document.getElementById('example1form').submit();">Ndc80</a></form></li>
                        <li>UniProt AC(s): <form class="exampleForm" id="example3form" action="" method="GET"><input type="hidden" name="q" value="Q05471"/>  <a href="<c:out value="#"/>" onclick="document.getElementById('example3form').submit();">Q05471</a></form></li>
                        <li>Protein name(s): <form class="exampleForm" id="example2form" action="" method="GET"><input type="hidden" name="q" value="PCNA"/>  <a href="<c:out value="#"/>" onclick="document.getElementById('example2form').submit();">PCNA</a></form></li>
                        <li>Complex AC: <form class="exampleForm" id="example5form" action="" method="GET"><input type="hidden" name="q" value="EBI-9008420"/><a href="<c:out value="#"/>" onclick="document.getElementById('example5form').submit();">EBI-9008420</a></form></li>
                        <%--<li>Pubmed ID: 22540012</li>--%>
                    </ul>
                    <%--<a href="JavaScript:newPopup('<c:url value="/help/"/>');">Help</a>--%>
                    <p rel="tooltip" title="You can search for one or several of the following types of terms: complex_id, complex_alias, species, complex_xref, udate, id, alias, ptype, stc, pbiorole, ftype, source and number_participants. By default, we search for entries that contain ANY of your search terms. If you would like to restrict your search, please link your terms with 'AND' or use the filters available once you have made an initial search.">Help</p>
                </div>
                <div class="grid_24">
                    <ul class="helpList">
                        <li>To search for a list of terms copy/paste all terms into the search box using either spaces or line breaks to separate them.</li>
                        <li>By default we search for entries containing ANY of the search terms, if you want to be more specific add AND between your search terms.</li>
                        <li>Search for isoforms of '<a href="<c:url value="${complex_search_form}?q=Q07817"/>">Q07817</a>' by using '<a href="<c:url value="${complex_search_form}?q=Q07817*"/>">Q07817*</a>'</li>
                    </ul>
                </div>
            </div>
            <div class="grid_6 stats">
                <section>
                    <h3 class="icon icon-generic" data-icon="g">Data Content</h3>
                    <ul>
                        <li>Complexes: <span style="font-weight: bold;"><c:out value="${stats_total}"/></span></li>
                        <li>Species: <span style="font-weight: bold;"><c:out value="${stats_species}"/></span></li>
                        <li>Interactor types: <span style="font-weight: bold;"><c:out value="${stats_interactor}"/></span></li>
                        <li>Biological role types: <span style="font-weight: bold;"><c:out value="${stats_biorole}"/></span></li>
                    </ul>
                </section>
                <section>
                    <h3>Contributors</h3>
                    <p>Manually curated content is added to IntAct Complex Portal by curators at the EMBL-EBI and the following organisations:</p>
                    <p>
                        <a href="http://www.ceitec.eu/"><img src="<c:url value="/resources/img/Ceitec.png"/>" style="width: 140px; height: 50px;"/></a>
                        <a href="http://mint.bio.uniroma2.it/"><img src="<c:url value="/resources/img/Mint.png"/>" style="width: 140px; height: 50px;"/></a>
                        <a href="http://www.yeastgenome.org/"><img src="<c:url value="/resources/img/SGD.png"/>" style="width: 140px; height: 50px;"/></a>
                    </p>

                </section>
            </div>
    </section>
</div>


<%@include file="footer.jsp"%>
</html>