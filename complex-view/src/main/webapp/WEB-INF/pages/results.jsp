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

    <jsp:useBean id="results"  class="uk.ac.ebi.intact.service.complex.view.ComplexRestResult" scope="session"/>
    <jsp:useBean id="pageInfo" class="uk.ac.ebi.intact.service.complex.view.Page"              scope="session"/>
    <c:choose>
        <c:when test="${not empty sessionScope.results.originaQuery}">
            <c:choose>
                <c:when test="${sessionScope.pageInfo.totalNumberOfElements != 0}">
                    <h2 class="titleResults">Search result for '<c:out value="${sessionScope.results.originaQuery}"/>'</h2>
                    <h3 class="subtitleResults"><c:out value="${sessionScope.pageInfo.totalNumberOfElements}"/> curated complexes were found.</h3>
                    <div class="results">
                        <div class="pages">
                            <c:if test="${sessionScope.pageInfo.prevPage != -1 || sessionScope.pageInfo.nextPage != -1}">
                                <c:if test="${sessionScope.pageInfo.prevPage != -1}">
                                    <span class="pagePrevSpan">
                                        <form class="pagePrevForm" method="POST" action="">
                                            <input name="query"  id="queryPrevTop"  hidden="true" value="<c:out value="${sessionScope.results.originaQuery}"/>"/>
                                            <input name="page"   id="pagePrevTop"   hidden="true" value="<c:out value="${sessionScope.pageInfo.prevPage}"/>"/>
                                            <input name="button" class="prevPage" id="buttonPrevTop" value="PrevPage" type="submit"/>
                                        </form>
                                    </span>
                                </c:if>
                                <span class="pageCurrentSpan">
                                    <form name="Top" class="pageCurrentForm" method="POST" action="" onsubmit="return checkPageNumberTop()">
                                        Page <input name="pageFake" type="text" class="pageCurrent" value="<c:out value="${sessionScope.pageInfo.pageForShow}"/>"/> of <c:out value="${sessionScope.pageInfo.lastPageForShow}"/>
                                        <input name="page" type="text" hidden="true"/>
                                        <input name="query"  id="queryCurrent"  hidden="true" value="<c:out value="${sessionScope.results.originaQuery}"/>"/>
                                        <input name="button" id="buttonCurrent" hidden="true" type="submit">
                                    </form>
                                </span>
                                <c:if  test="${sessionScope.pageInfo.nextPage != -1}">
                                    <span class="pageNextSpan">
                                        <form class="pageNextForm" method="POST" action="">
                                            <input name="query"  id="queryNext"  hidden="true" value="<c:out value="${sessionScope.results.originaQuery}"/>"/>
                                            <input name="page"   id="pageNext"   hidden="true" value="<c:out value="${sessionScope.pageInfo.nextPage}"/>"/>
                                            <input name="button" class="nextPage" id="buttonNext" value="NextPage" type="submit"/>
                                        </form>
                                    </span>
                                </c:if>
                            </c:if>
                        </div>
                        <br><br>
                        <ol start="<c:out value="${sessionScope.pageInfo.startListCount}"/>">
                             <c:forEach var="res" items="${sessionScope.results.elements}">
                                 <li><a class="complex_name" href="<c:url value="/details/${res.complexAC}"/>"><c:out value="${res.complexName}" /> (<c:out value="${res.organismName}"/>)</a> <label class="complex_ac"><c:out value="${res.complexAC}" /></label>
                                    <br><label class="complex_description"><c:out value="${res.description}" /></label>
                                    <br>
                                </li>
                             </c:forEach>
                        </ol>
                        <div class="pages">
                            <c:if test="${sessionScope.pageInfo.prevPage != -1 || sessionScope.pageInfo.nextPage != -1}">
                                <c:if test="${sessionScope.pageInfo.prevPage != -1}">
                                    <span class="pagePrevSpan">
                                        <form class="pagePrevForm" method="POST" action="">
                                            <input name="query"  id="queryPrev"  hidden="true" value="<c:out value="${sessionScope.results.originaQuery}"/>"/>
                                            <input name="page"   id="pagePrev"   hidden="true" value="<c:out value="${sessionScope.pageInfo.prevPage}"/>"/>
                                            <input name="button" class="prevPage" id="buttonPrev" value="PrevPage" type="submit"/>
                                        </form>
                                    </span>
                                </c:if>
                                <span class="pageCurrentSpan">
                                    <form name="Bottom" class="pageCurrentForm" method="POST" action="" onsubmit="return checkPageNumberBottom()">
                                        Page <input name="pageFake" type="text" class="pageCurrent" value="<c:out value="${sessionScope.pageInfo.pageForShow}"/>"/> of <c:out value="${sessionScope.pageInfo.lastPageForShow}"/>
                                        <input name="page" type="text" hidden="true"/>
                                        <input name="query"  id="queryCurrent"  hidden="true" value="<c:out value="${sessionScope.results.originaQuery}"/>"/>
                                        <input name="button" id="buttonCurrent" hidden="true" type="submit">
                                    </form>
                                </span>
                                <c:if  test="${sessionScope.pageInfo.nextPage != -1}">
                                    <span class="pageNextSpan">
                                        <form class="pageNextForm" method="POST" action="">
                                            <input name="query"  id="queryNextBottom"  hidden="true" value="<c:out value="${sessionScope.results.originaQuery}"/>"/>
                                            <input name="page"   id="pageNextBottom"   hidden="true" value="<c:out value="${sessionScope.pageInfo.nextPage}"/>"/>
                                            <input name="button" class="nextPage" id="buttonNextBottom" value="NextPage" type="submit"/>
                                        </form>
                                    </span>
                                </c:if>
                            </c:if>
                        </div>
                    </div>
            </c:when>
            <c:otherwise> <!--Case 0 results-->
                 <h2 class="titleResults">Search result for '<c:out value="${sessionScope.results.originaQuery}"/>'</h2>
                 <h3 class="subtitleResults"><c:out value="${sessionScope.pageInfo.totalNumberOfElements}"/> curated complexes were found.</h3>
                 <div class="searchDivZeroResults">
                     <form class="searchForm" method="POST" action="">
                         <textarea name="query" rows="10" id="querySearchBox"  type="text"   class="searchBox"    placeholder="Enter search term(s)..."><c:out value="${sessionScope.results.originaQuery}"/></textarea>
                         <br>
                         <input name="button" id="button" type="submit" class="searchButton" value="Search" />
                     </form>
                 </div>
            </c:otherwise>
        </c:choose>
    </c:when>
        <c:otherwise>
            <h2 class="titleNoData">No data available</h2>
            <h3 class="subtitleNoData">Please contact with the webadmin</h3>
        </c:otherwise>
    </c:choose>
</div>


<%@include file="footer.jsp"%>
</html>