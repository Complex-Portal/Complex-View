<%--
  Created by IntelliJ IDEA.
  User: mkoch
  Date: 12/01/16
  Time: 10:36
  To change this template use File | Settings | File Templates.
--%>
<html>
<%@include file="header.jsp" %>
<script type="text/javascript" charset="utf-8" src="<c:url value="/resources/js/d3.v3.min.js"/>"></script>
<script type="text/javascript" charset="utf-8" src="<c:url value="/resources/js/complexviewer-helper.js"/>"></script>
<script>
    window.addEventListener("load", function () {
        loadComplexViewer('${current_ac}', '${json_rest}');
    });
</script>
<div class="grid_12 omega">
    <form id="local-search" name="local-search" action="${complex_search_form}" method="get">
        <fieldset>
            <div class="left">
                <label>
                    <input type="text" name="q" id="local-searchbox"
                           value="<c:out value="${sessionScope.results.originaQuery}"/>">
                </label>
                <!-- Include some example searchterms - keep them short and few! -->
                <span class="examples">Examples: <a href="<c:url value="${complex_search_form}?q=GO:0016491"/>">GO:0016491</a>, <a
                        href="<c:url value="${complex_search_form}?q=Ndc80"/>">Ndc80</a>, <a
                        href="<c:url value="${complex_search_form}?q=Q05471"/>">Q05471</a></span>
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
        <li><a href="${complex_stats_url}">Statistics</a></li>
        <li class="last"><a href="${complex_download_url}">Download</a></li>
        <%--<li class="last"><a href="${complex_contact_url}">Contact Us</a></li>--%>
        <li class="functional last first"><a class="icon icon-generic" data-icon="\"
                                             href="http://www.ebi.ac.uk/support/intact">Feedback</a></li>
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
    <nav id="breadcrumb">
        <p>
            <a href="${complex_home_url}">${complex_portal_name}</a> &gt;
            <a href="${complex_home_url}/details/${current_ac}">Details</a> &gt;
            Complex Viewer
        </p>
    </nav>
    <div class="grid_17">
        <div id="networkContainer" style="border: 1px solid #f1f1f1;"></div>
    </div>
    <div class="grid_7">
        <div id="networkControls" class="networkControls" style="margin: 0; padding: 0;">
            <select id="annotationsSelect" onChange="changeAnnotations();">
                <option value="MI features" selected='selected'>MI features</option>
                <option value="UniprotKB">UniprotKB</option>
                <option value="SuperFamily">SuperFamily</option>
                <option value="Interactor">Interactor</option>
                <option value="None">None</option>
            </select>
            <button onclick="xlv.expandAll();">Expand All</button>
            <button id="Reset" class="submit networkButton" onclick="xlv.reset();">Reset</button>
            <button id="ExportSVG" class="submit networkButton" onclick="exportSVG()">Export SVG</button>

            <div id="dialog" title="Legend" style="">
                <%@include file="legend.jsp" %>
            </div>
        </div>
    </div>
</div>
<%@include file="footer.jsp" %>
</html>
