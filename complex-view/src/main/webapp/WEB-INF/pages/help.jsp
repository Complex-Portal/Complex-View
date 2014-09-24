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
        <li><a href="${complex_about_url}">About</a></li>
        <li><a href="${complex_documentation_url}">Documentation</a></li>
        <li><a href="${complex_search_url}">Search</a></li>
        <%--<li><a href="${complex_advanced_search_url}">Advanced Search</a></li>--%>
        <%--<li><a href="${complex_downloads_url}">Downloads</a></li>--%>
        <li class="active"><a href="${complex_help_url}">Help</a></li>
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
    <nav id="breadcrumb">
        <p>
            <a href="${complex_home_url}">${complex_portal_name}</a> &gt;
            Help
        </p>
    </nav>
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

    <h2>Searches in IntAct Complex Portal</h2>
    <p>To do a search you can use the Complex Query Language (CQL), which is based on <a href="http://lucene.apache.org/core/3_6_0/queryparsersyntax.html?conversationContext=3#Terms" target="_blank">Lucene's syntax.</a></p>

    <ul>
        <%--<li>Search will recognize ontologies and synonyms.--%>
            <%--<ul>--%>
                <%--<li><i>Eukaryota</i> will retrieve all children of <i>Eukaryota</i> using the Uniprot taxonomy</li>--%>
                <%--<li><i>affinity techniques</i> will match <i>affinity technology</i> because it is affinity techniques is a synonym of affinity technology in the PSI-MI ontology</li>--%>
            <%--</ul>--%>
        <%--</li>--%>
        <li>Free text search will look by default for:</li>
            <ul>
                <li type="square">Identifiers, names and synonyms of molecules (protein, gene, small molecule).</li>
                <li type="square">Identifiers, names and synonyms of complexes.</li>
                <li type="square">Cross-references of complexes.</li>
                <li type="square">Species</li>
            </ul>
        <li>Search for groups of complexes by using the Gene Ontology. For example, <a href="<c:url value="${complex_search_form}?q=GO:0016491"/>">GO:0016491</a> will search for all complexes annotated with &quot;oxidoreductase activity&quot; and all downstream child terms of this.</li>
        <li>Narrow your initial search result by using the filters on the results page for:</li>
            <ul>
                <li type="square">Species.</li>
                <li type="square">Molecule type.</li>
                <li type="square">Biological role.</li>
            </ul>
        <li>Search based on exact word matches.
            <ul>
                <li><i>Ndc8</i> will not match <i>Ndc80</i></li>
                <%--<li><i>Association</i> will retrieve both <i>physical association</i> and <i>association</i></li>--%>
                <%--<li>To retrieve all isoforms of <i>P12345</i>, use <i>P12345*</i></li>--%>
            </ul>
        </li>
        <li>Default fields are used when no field is specified (simple search) :
            <ul>
                <li type="square">Interactor identifier(s)</li>
                <li type="square">Interactor alias(es)</li>
                <li type="square">Interaction identifier(s)</li>
                <li type="square">Interaction alias(es)</li>
                <li type="square">Interaction Xref(s)</li>
                <li type="square">Interaction species</li>
            </ul>
            For instance, if you put <i>'PCNA'</i> in the simple query box, this will mean the same as <i>complex_id:PCNA OR id:PCNA OR complex_name:PCNA OR alias:PCNA OR species:PCNA OR complex_xref:PCNA</i></li>
        <li>Use OR or space ' ' to search for ANY of the terms in a field</li>
        <li>Use AND if you want to search for those interactions where ALL of your terms are found</li>
        <li>Use quotes (&quot;) if you look for a specific phrase (group of terms that must be searched together) or terms
            containing special characters that may otherwise be interpreted by our query engine (eg. ':' in a GO term)
        </li>
        <li>Use parenthesis for complex queries (e.g. '(XXX OR YYY) AND ZZZ')</li>
        <li>Wildcards (*,?) can be used between letters in a term or at the end of terms to do fuzzy queries, but never at the beginning of a term
        </li>
        <li>Optionally, you can prepend a symbol in from of your term.
            <ul>
                <li type="square">+ (plus): include this term. Equivalent to AND. e.g. +Ndc80</li>
                <li type="square">- (minus): do not include this term. Equivalent to NOT. e.g. -Ndc80</li>
                <li type="square">Nothing in front of the term. Equivalent to OR. e.g. Ndc80</li>
            </ul>
        </li>
    </ul>

    <h2>Complex Query Language (CQL) fields</h2>
<table style="padding-top: 10px"><tbody>
<tr><td><span style="font-weight: bold">Field Name</span></td><td><span style="font-weight: bold">Searches on</span></td><td><span style="font-weight: bold">Example</span></td></tr>
<%--Complex Information--%>
<tr><td>complex_id</td><td>Interaction identifier(s)</td><td><form id="example1form" action="${complex_search_form}" method="GET"><input type="hidden" name="q" value="complex_id:EBI-1224506"/><a href="javascript: document.getElementById('example1form').submit();">complex_id:EBI-1224506</a></form></td></tr>
<tr><td>complex_alias</td><td>Interaction alias(es)</td><td><form id="example2form" action="${complex_search_form}" method="GET"><input type="hidden" name="q" value="complex_alias:&quot;coenzyme Q-cytochrome c reductase&quot;"/><a href="javascript: document.getElementById('example2form').submit();">complex_alias:&quot;coenzyme Q-cytochrome c reductase&quot;</a></form></td></tr>
<tr><td>species</td><td>Interaction Tax ID</td><td><form id="example3form" action="${complex_search_form}" method="GET"><input type="hidden" name="q" value="species:9606"/><a href="javascript: document.getElementById('example3form').submit();">species:9606</a></form></td></tr>
<tr><td>complex_xref</td><td>Interaction xref(s)</td><td><form id="example4form" action="${complex_search_form}" method="GET"><input type="hidden" name="q" value="complex_xref:15210332"/><a href="javascript: document.getElementById('example4form').submit();">complex_xref:15210332</a></form></td></tr>
<%--<tr><td>description</td><td>Interaction description</td><td><a href="" target="_top">taxidB:9606</a></td></tr>--%>
<tr><td>udate</td><td>Last update of the interaction</td><td><form id="example5form" action="${complex_search_form}" method="GET"><input type="hidden" name="q" value="udate:[20110607 TO 20120906]"/><a href="javascript: document.getElementById('example5form').submit();">udate:[20110607 TO 20120906]</a></form></td></tr>
<%--Interactor Information--%>
<tr><td>id</td><td>Interactor identifier(s)</td><td><form id="example6form" action="${complex_search_form}" method="GET"><input type="hidden" name="q" value="id:P38326"/><a href="javascript: document.getElementById('example6form').submit();">id:P38326</a></form></td></tr>
<tr><td>alias</td><td>Interactor alias(es)</td><td><form id="example7form" action="${complex_search_form}" method="GET"><input type="hidden" name="q" value="alias:Cyclin"/><a href="javascript: document.getElementById('example7form').submit();">alias:Cyclin</a></form></td></tr>
<tr><td>ptype</td><td>Interactor type(s)</td><td><form id="example8form" action="${complex_search_form}" method="GET"><input type="hidden" name="q" value="ptype:protein"/><a href="javascript: document.getElementById('example8form').submit();">ptype:protein</a></form></td></tr>
<tr><td>pxref</td><td>Interactor xref(s)</td><td><form id="example9form" action="${complex_search_form}" method="GET"><input type="hidden" name="q" value="pxref:GO:0031577"/><a href="javascript: document.getElementById('example9form').submit();">pxref:GO:0031577</a></form></td></tr>
<tr><td>stc</td><td>Boolean value to know if the Interactor has stoichiometry information</td><td><form id="example10form" action="${complex_search_form}" method="GET"><input type="hidden" name="q" value="stc:true"/><a href="javascript: document.getElementById('example10form').submit();">stc:true</a></form></td></tr>
<%--<tr><td>param</td><td>Boolean value to know if the Interaction has some parameters</td><td>param:true</td></tr>--%>
<%--Other Fields (PSICQUIC, IntAct)--%>
<tr><td>pbiorole</td><td>Biological role(s)</td><td><form id="example11form" action="${complex_search_form}" method="GET"><input type="hidden" name="q" value="pbiorole:enzyme"/><a href="javascript: document.getElementById('example11form').submit();">pbiorole:enzyme</a></form></td></tr>
<tr><td>ftype</td><td>Feature type(s)</td><td><form id="example12form" action="${complex_search_form}" method="GET"><input type="hidden" name="q" value="ftype:&quot;binding site&quot;"/><a href="javascript: document.getElementById('example12form').submit();">ftype:&quot;binding site&quot;</a></form></td></tr>
<tr><td>source</td><td>Source database(s)</td><td><form id="example13form" action="${complex_search_form}" method="GET"><input type="hidden" name="q" value="source:intact"/><a href="javascript: document.getElementById('example13form').submit();">source:intact</a></form></td></tr>
<tr><td>number_participants</td><td>Number of participants</td><td><form id="example14form" action="${complex_search_form}" method="GET"><input type="hidden" name="q" value="number_participants:3"/><a href="javascript: document.getElementById('example14form').submit();">number_participants:3</a></form></td></tr>
</tbody>
</table>

    <%--<h3>IntAct fields</h3>These field names are specific to IntAct and are not in MIQL definition for PSICQUIC.<table style="padding-top: 10px"><tbody>--%>
<%--<tr><td><span style="font-weight: bold">Field Name</span></td><td><span style="font-weight: bold">Searches on</span></td><td><span style="font-weight: bold">Example</span></td></tr>--%>
<%--<tr><td>geneName</td><td>Gene name for Interactor A or B</td><td><a href="http://www.ebi.ac.uk:80/intact/pages/interactions/interactions.xhtml?query=geneName%3Abrca2&amp;conversationContext=3" target="_top">geneName:brca2</a></td></tr>--%>
<%--<tr><td>source</td><td>Source database(s)</td><td><a href="http://www.ebi.ac.uk:80/intact/pages/interactions/interactions.xhtml?query=source%3Ambinfo&amp;conversationContext=3" target="_top">source:mbinfo</a></td></tr>--%>
<%--<tr><td>intact-miscore</td><td>IntAct MI Score (between 0 and 1), based on number of publications, detection methods and interaction types.</td><td><a href="http://www.ebi.ac.uk:80/intact/pages/interactions/interactions.xhtml?query=intact-miscore%3A%5B0.5+TO+1.0%5D&amp;conversationContext=3" target="_top">intact-miscore:[0.5 TO 1.0]</a></td></tr>--%>
<%--</tbody>--%>
<%--</table>--%>

</div>



    <%@include file="footer.jsp"%>
</html>