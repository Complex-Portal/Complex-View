<html>

<%@include file="header.jsp" %>
<jsp:useBean id="details" class="uk.ac.ebi.intact.service.complex.view.ComplexDetails" scope="session"/>
<script type="text/javascript" charset="utf-8" src="<c:url value="/resources/js/d3.v3.min.js"/>"></script>
<script type="text/javascript" charset="utf-8" src="<c:url value="/resources/js/complexviewer-helper.js"/>"></script>

<script>
    $(function () {
        $("#dialog").dialog({
            autoOpen: false,
            position: {my: "right right", at: "left left", of: "#networkContainer"}
        });
        $("#dialog_opener").click(function () {
            $("#dialog").dialog("open");
        });
    });
    
    window.addEventListener("load", function () {
        loadComplexViewer('${sessionScope.details.ac}', '${json_rest}');
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
            Details
        </p>
    </nav>

    <div class="grid_24">
        <div style="width: 80%; background-color: #fff1aa; padding: 10px;margin: auto;">
            <h3><img src="http://www.ebi.ac.uk/web_guidelines/images/icons/EBI-Generic/Generic%20icons/announcement.svg"
                     style="width: 19px; height: 20px;"> Try out our new website</h3>
            <p>Please try our newly designed and implemented Complex Portal: <a href="//www.ebi.ac.uk/complexportal/complex/${sessionScope.details.ac}"
                                                                                target="_blank">www.ebi.ac.uk/complexportal/complex/${sessionScope.details.ac}</a>
            </p>
        </div>
        <div class="grid_12" style="margin-left: 0;">
            <h2><c:choose><c:when test="${not empty sessionScope.details.name}"><c:out
                    value="${sessionScope.details.name}"/></c:when><c:otherwise>&lt;Not available&gt;</c:otherwise></c:choose></h2>

            <h3>Species: <c:choose><c:when test="${not empty sessionScope.details.species}"><c:out
                    value="${sessionScope.details.species}"/></c:when><c:otherwise>&lt;Not available&gt;</c:otherwise></c:choose>
                <br>Accession number: <c:choose><c:when test="${not empty sessionScope.details.ac}"><c:out
                        value="${sessionScope.details.ac}"/></c:when><c:otherwise>&lt;Not available&gt;</c:otherwise></c:choose>
            </h3>
            <c:if test="${not empty sessionScope.details.systematicName || not empty sessionScope.details.synonyms || not empty sessionScope.details.function || not empty sessionScope.details.properties || not empty sessionScope.details.ligand || not empty sessionScope.details.disease || not empty sessionScope.details.complexAssembly}">

            <h4>Summary</h4>
            <br>
            <c:if test="${not empty sessionScope.details.systematicName}">
                <h5>Systematic Name:</h5>

                <p style="text-align: justify; word-break: break-all;"><c:out
                        value="${sessionScope.details.systematicName}"/></p>
            </c:if>
            <c:if test="${not empty sessionScope.details.synonyms}">
                <%--  OLD WAY  --%>
                <%--<h5>Synonyms:</h5>--%>
                <%--<p style="text-align: justify;">--%>
                <%--<c:forEach var="synonym" items="${sessionScope.details.synonyms}">--%>
                <%--<c:out value="${synonym}"/><br>--%>
                <%--</c:forEach>--%>
                <%--</p>--%>

                <%--  NEW WAY  --%>
                <h5>Synonyms:</h5>
                <table id="synosyms" class="synosymsTable">
                    <tbody>
                    <c:forEach var="synonym" items="${sessionScope.details.synonyms}" varStatus="status">
                        <td><c:out value="${synonym}"/></td>
                        <c:if test="${status.count % 2 == 0}">
                            </tr>
                        </c:if>
                    </c:forEach>
                    </tbody>
                </table>
            </c:if>
        </div>
        <div class="grid_12" style="height: 50%; width: 45%;">
            <div class="grid_24">
                <div id="networkControls" class="networkControls" style="margin: 0; padding: 0;">
                    <button id="dialog_opener" class="submit networkButton">Legend</button>
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
                    <a href="${complexviewer_url}${sessionScope.details.ac}"><button>Click to enlarge</button></a>

                    <div id="dialog" title="Legend" style="">
                        <%@include file="legend.jsp" %>
                    </div>
                </div>
                <br>
                <br>
            </div>
            <div class="grid_24">
                <div id="networkContainer" style="border: 1px solid #f1f1f1;"></div>
            </div>
        </div>
        <div class="grid_24">
            <c:if test="${not empty sessionScope.details.functions}">
                <h5>Function:</h5>
                
                <c:forEach var="function" items="${sessionScope.details.functions}">
                    <p style="text-align: justify;"><c:out value="${function}"/></p>
                </c:forEach>
            </c:if>
            <c:if test="${not empty sessionScope.details.properties}">
                <h5>Properties:</h5>
                
                <c:forEach var="property" items="${sessionScope.details.properties}">
                    <p style="text-align: justify;"><c:out value="${property}"/></p>
                </c:forEach>
            </c:if>
            <c:if test="${not empty sessionScope.details.agonists}">
                <h5>Agonist:</h5>

                <c:forEach var="agonist" items="${sessionScope.details.agonists}">
                    <p style="text-align: justify;"><c:out value="${agonist}"/></p>
                </c:forEach>
            </c:if>
            <c:if test="${not empty sessionScope.details.antagonists}">
                <h5>Antagonist:</h5>

                <c:forEach var="antagonist" items="${sessionScope.details.antagonists}">
                    <p style="text-align: justify;"><c:out value="${antagonist}"/></p>
                </c:forEach>
            </c:if>
            <c:if test="${not empty sessionScope.details.ligands}">
                <h5>Ligand:</h5>
                
                <c:forEach var="ligand" items="${sessionScope.details.ligands}">
                    <p style="text-align: justify;"><c:out value="${ligand}"/></p>
                </c:forEach>
            </c:if>
            <c:if test="${not empty sessionScope.details.comments}">
                <h5>Comment:</h5>

                <c:forEach var="comment" items="${sessionScope.details.comments}">
                    <p style="text-align: justify;"><c:out value="${comment}"/></p>
                </c:forEach>
            </c:if>
            <c:if test="${not empty sessionScope.details.diseases}">
                <h5>Disease:</h5>
                
                <c:forEach var="disease" items="${sessionScope.details.diseases}">
                    <p style="text-align: justify;"><c:out value="${disease}"/></p>
                </c:forEach>
            </c:if>
            <c:if test="${not empty sessionScope.details.complexAssemblies}">
                <h5>Complex Assembly:</h5>
                
                <c:forEach var="complexAssembly" items="${sessionScope.details.complexAssemblies}">
                    <p style="text-align: justify;"><c:out value="${complexAssembly}"/></p>
                </c:forEach>
            </c:if>
            </c:if>
        </div>
        <br>

        <div class="grid_24">
            <h4>Participants</h4>
            <table id="participants" class="tablesorter">
                <thead>
                <tr class="trHead">
                    <td>ID</td>
                    <td>Name</td>
                    <td>Description</td>
                    <td>Stoichiometry</td>
                    <td>Biological Role</td>
                    <td>Interactor Type</td>
                    <%--<td>Linked Features</td>--%>
                    <td>Other Features</td>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="part" items="${sessionScope.details.participants}">
                    <tr>
                        <td><c:if test="${not empty part.identifier}"><a target="_blank"
                                                                         href="<c:out value="${part.identifierLink}"/>"><c:out
                                value="${part.identifier}"/></a><br/></c:if>
                        <td><c:if test="${not empty part.name}"><c:out value="${part.name}"/></c:if></td>
                        <td><c:if test="${not empty part.description}"><c:out value="${part.description}"/></c:if></td>
                        <td><c:if test="${not empty part.stochiometry}"><c:out
                                value="${part.stochiometry}"/></c:if></td>
                        <td><c:if test="${not empty part.bioRole}"><c:out value="${part.bioRole}"/></c:if></td>
                        <td><c:if test="${not empty part.interactorType}"><c:out
                                value="${part.interactorType}"/></c:if></td>
                            <%--<td><div class="detailsTable" rel="tooltip" data-placement="left" title="<c:if test="${not empty part.linkedFeatures}"><c:forEach var="linked" items="${part.linkedFeatures}"><c:out value="${linked.featureType}"/><c:out value="${linked.participantId}"/><c:forEach var="range" items="${linked.ranges}">[<c:out value="${range}"/>]</c:forEach></c:forEach></c:if>">--%>
                            <%--<c:if test="${not empty part.linkedFeatures}">--%>
                            <%--<c:forEach var="linked" items="${part.linkedFeatures}">--%>
                            <%--<c:out value="${linked.featureType}"/> <c:out value="${linked.participantId}"/>--%>
                            <%--<c:forEach var="range" items="${linked.ranges}">--%>
                            <%--[<c:out value="${range}"/>]--%>
                            <%--</c:forEach>--%>
                            <%--<br/>--%>
                            <%--</c:forEach>--%>
                            <%--</c:if>--%>
                            <%--</div></td>--%>
                        <td>
                            <div class="detailsTable" rel="tooltip" data-placement="right"
                                 title="<c:if test="${not empty part.otherFeatures}"><c:forEach var="other" items="${part.otherFeatures}"><c:out value="${other.featureType}"/><c:out value="${other.participantId}"/><c:forEach var="range" items="${other.ranges}">[<c:out value="${range}"/>]</c:forEach></c:forEach></c:if>">
                                <c:if test="${not empty part.otherFeatures}">
                                    <c:forEach var="other" items="${part.otherFeatures}">
                                        <c:out value="${other.featureType}"/> <c:out value="${other.participantId}"/>
                                        <c:forEach var="range" items="${other.ranges}">
                                            [<c:out value="${range}"/>]
                                        </c:forEach>
                                        <br/>
                                    </c:forEach>
                                </c:if>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
        <br>

        <div class="grid_24">
            <h4>Cross References</h4>
            <table id="crossReferences" class="tablesorter">
                <thead>
                <tr class="trHead">
                    <td>Type</td>
                    <td>Database</td>
                    <td>Identifier</td>
                    <td>Description</td>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="cross" items="${sessionScope.details.crossReferences}">
                    <tr>
                        <td><c:if test="${not empty cross.qualifier}"><c:out value="${cross.qualifier}"/></c:if></td>
                        <td><c:if test="${not empty cross.database}"><c:out value="${cross.database}"/></c:if></td>
                        <td><c:if test="${not empty cross.identifier}">
                            <c:if test="${not empty cross.searchURL}">
                                <a target="_blank" href="<c:out value="${cross.searchURL}"/>">
                            </c:if>
                            <c:out value="${cross.identifier}"/>
                            <c:if test="${not empty cross.searchURL}">
                                </a>
                            </c:if>
                        </c:if>
                        </td>
                        <td><c:if test="${not empty cross.description}"><c:out
                                value="${cross.description}"/></c:if></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
    <c:if test="${not empty sessionScope.details.institution}">
        <h6>Curated by: <c:out value="${sessionScope.details.institution}"/></h6>
    </c:if>
</div>
<%@include file="footer.jsp" %>
</html>