<html>

<%@include file="header.jsp"%>
<jsp:useBean id="details" class="uk.ac.ebi.intact.service.complex.view.ComplexDetails" scope="session"/>


<div class="grid_12 omega">
    <form id="local-search" name="local-search" action="${complex_search_form}" method="post">
        <fieldset>
            <div class="left">
                <label>
                    <input type="text" name="query" id="local-searchbox" value="">
                </label>
                <!-- Include some example searchterms - keep them short and few! -->
                <%--<span class="examples">Examples: <a href="#" title="">thing1</a>, <a href="#" title="">thing2</a>, <a href="#" title="">thing3</a></span>--%>
            </div>
            <input name="facets" id="facets" type="hidden" value="${facetFields}" />
            <div class="right">
                <input type="submit" name="submit" value="Search" class="submit">
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

    <h2><c:choose><c:when test="${not empty sessionScope.details.name}"><c:out value="${sessionScope.details.name}"/></c:when><c:otherwise>&lt;Not available&gt;</c:otherwise></c:choose></h2>
    <h3>Species: <c:choose><c:when test="${not empty sessionScope.details.specie}"><c:out value="${sessionScope.details.specie}"/></c:when><c:otherwise>&lt;Not available&gt;</c:otherwise></c:choose>
    <br>Accession number: <c:choose><c:when test="${not empty sessionScope.details.ac}"><c:out value="${sessionScope.details.ac}"/></c:when><c:otherwise>&lt;Not available&gt;</c:otherwise></c:choose></h3>
    <div class="grid_24">
        <c:if test="${not empty sessionScope.details.systematicName || not empty sessionScope.details.synonyms || not empty sessionScope.details.function || not empty sessionScope.details.properties || not empty sessionScope.details.ligand || not empty sessionScope.details.disease || not empty sessionScope.details.complexAssembly}">
            <div class="grid_24">
                <h4>Summary</h4>
                <br>
                <c:if test="${not empty sessionScope.details.systematicName}">
                    <h5>Systematic Name:</h5>
                    <p style="text-align: justify;"><c:out value="${sessionScope.details.systematicName}"/></p>
                </c:if>
                <c:if test="${not empty sessionScope.details.synonyms}">
                    <h5>Synonyms:</h5>
                    <p style="text-align: justify;">
                    <c:forEach var="synonym" items="${sessionScope.details.synonyms}">
                        <c:out value="${synonym}"/><br>
                    </c:forEach>
                    </p>
                </c:if>
                <c:if test="${not empty sessionScope.details.function}">
                    <h5>Function:</h5>
                    <p style="text-align: justify;"><c:out value="${sessionScope.details.function}"/></p>
                </c:if>
                <c:if test="${not empty sessionScope.details.properties}">
                    <h5>Properties:</h5>
                    <p style="text-align: justify;"><c:out value="${sessionScope.details.properties}"/></p>
                </c:if>
                <c:if test="${not empty sessionScope.details.ligand}">
                    <h5>Ligand:</h5>
                    <p style="text-align: justify;"><c:out value="${sessionScope.details.ligand}"/></p>
                </c:if>
                <c:if test="${not empty sessionScope.details.disease}">
                    <h5>Disease:</h5>
                    <p style="text-align: justify;"><c:out value="${sessionScope.details.disease}"/></p>
                </c:if>
                <c:if test="${not empty sessionScope.details.complexAssembly}">
                    <h5>Complex Assembly:</h5>
                    <p style="text-align: justify;"><c:out value="${sessionScope.details.complexAssembly}"/></p>
                </c:if>
            </div>
            <br>
        </c:if>
        <div class="grid_24">
            <h4>Participants</h4>
            <table id="participants" class="table">
                <thead>
                    <tr class="trHead">
                        <td>ID</td>
                        <td>Name</td>
                        <td>Description</td>
                        <td>Stochiometry</td>
                        <td>Biological Role</td>
                        <td>Interactor Type</td>
                        <td>Linked Features</td>
                        <td>Other Features</td>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="part" items="${sessionScope.details.participants}">
                        <tr>
                            <td><c:if test="${not empty part.identifier}"><a target="_blank" href="<c:out value="${part.identifierLink}"/>"><c:out value="${part.identifier}"/></a><br/></c:if>
                                <c:if test="${not empty part.interactorAC}"><a target="_blank" href="http://www.ebi.ac.uk/intact/molecule/<c:out value="${part.interactorAC}"/>"><c:out value="${part.interactorAC}"/></a></c:if></td>
                            <td><c:if test="${not empty part.name}"><c:out value="${part.name}"/></c:if></td>
                            <td><c:if test="${not empty part.description}"><c:out value="${part.description}"/></c:if></td>
                            <td><c:if test="${not empty part.stochiometry}"><c:out value="${part.stochiometry}"/></c:if></td>
                            <td><c:if test="${not empty part.bioRole}"><c:out value="${part.bioRole}"/></c:if></td>
                            <td><c:if test="${not empty part.interactorType}"><c:out value="${part.interactorType}"/></c:if></td>
                            <td><div class="detailsTable" rel="tooltip" data-placement="left" title="<c:if test="${not empty part.linkedFeatures}"><c:forEach var="linked" items="${part.linkedFeatures}"><c:out value="${linked.featureType}"/><c:out value="${linked.participantId}"/><c:forEach var="range" items="${linked.ranges}">[<c:out value="${range}"/>]</c:forEach>
</c:forEach></c:if>">
                                <c:if test="${not empty part.linkedFeatures}">
                                <c:forEach var="linked" items="${part.linkedFeatures}">
                                    <c:out value="${linked.featureType}"/> <c:out value="${linked.participantId}"/>
                                        <c:forEach var="range" items="${linked.ranges}">
                                            [<c:out value="${range}"/>]
                                        </c:forEach>
                                        <br/>
                                    </c:forEach>
                                </c:if>
                            </div></td>
                            <td><div class="detailsTable" rel="tooltip" data-placement="right" title="<c:if test="${not empty part.otherFeatures}"><c:forEach var="other" items="${part.otherFeatures}"><c:out value="${other.featureType}"/><c:out value="${other.participantId}"/><c:forEach var="range" items="${other.ranges}">[<c:out value="${range}"/>]</c:forEach>
</c:forEach></c:if>">
                                <c:if test="${not empty part.otherFeatures}">
                                    <c:forEach var="other" items="${part.otherFeatures}">
                                        <c:out value="${other.featureType}"/> <c:out value="${other.participantId}"/>
                                        <c:forEach var="range" items="${other.ranges}">
                                            [<c:out value="${range}"/>]
                                        </c:forEach>
                                        <br/>
                                    </c:forEach>
                                </c:if>
                            </div></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
        <br>
        <div class="grid_24">
            <h4>Cross References</h4>
            <table id="crossReferences" class="table">
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
                            <td><c:if test="${not empty cross.description}"><c:out value="${cross.description}"/></c:if></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>


<%@include file="footer.jsp"%>
</html>