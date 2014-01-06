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

    <h3>Help: Searches in IntAct</h3>To do a search you can use the Molecular Interaction Query Language (MIQL), which is based on <a href="http://lucene.apache.org/core/3_6_0/queryparsersyntax.html?conversationContext=3#Terms" target="_blank">Lucene's syntax.</a>

    <ul>
        <li>Search based on exact word matches.
            <ul>
                <li><i>BRCA2</i> will not match <i>BRCA2B</i></li>
                <li><i>Association</i> will retrieve both <i>physical association</i> and <i>association</i></li>
                <li>To retrieve all isoforms of <i>P12345</i>, use <i>P12345*</i></li>
            </ul>
        </li>
        <li>Search will recognize ontologies and synonyms.
            <ul>
                <li><i>Eukaryota</i> will retrieve all children of <i>Eukaryota</i> using the Uniprot taxonomy</li>
                <li><i>affinity techniques</i> will match <i>affinity technology</i> because it is affinity techniques is a synonym of affinity technology in the PSI-MI ontology</li>
            </ul>
        </li>
        <li>Default fields are used when no field is specified (simple search) :
            <ul>
                <li>Interactor id, alias</li>
                <li>Interactor species</li>
                <li>Interaction id</li>
                <li>Publication id, first author</li>
                <li>Interaction type</li>
                <li>Interaction detection method</li>
                <li>Interactor xrefs (GO, uniprot secondary xrefs, ...)</li>
                <li>Interaction xrefs (GO, ...)</li>
            </ul>.
            <br />For instance, if you put
            <i>'P12345'</i> in the simple query box,
            this will mean the same as <i>  identifier:P12345 OR pubid:P12345 OR pubauth:P12345 OR species:P12345
                OR type:P12345 OR detmethod:P12345 OR interaction_id:P12345</i></li>
        <li>Use OR or space ' ' to search for ANY of the terms in a field</li>
        <li>Use AND if you want to search for those interactions where ALL of your terms are found</li>
        <li>Use quotes (&quot;) if you look for a specific phrase (group of terms that must be searched together) or terms
            containing special characters that may otherwise be interpreted by our query engine (eg. ':' in a GO term)
        </li>
        <li>Use parenthesis for complex queries (e.g. '(XXX OR YYY) AND ZZZ')</li>
        <li>Wildcards (*,?) can be used between letters in a term or at the end of terms to do fuzzy queries,
            <br />but never at the beginning of a term
        </li>
        <li>Optionally, you can prepend a symbol in from of your term.
            <ul>
                <li>+ (plus): include this term. Equivalent to AND. e.g. +P12345</li>
                <li>- (minus): do not include this term. Equivalent to NOT. e.g. -P12345</li>
                <li>Nothing in front of the term. Equivalent to OR. e.g. P12345</li>
            </ul>
        </li>
    </ul>

    <h3>MIQL fields</h3>You can find more information about the Molecular Interactions Query Language (MIQL) defined for PSICQUIC <a href="http://code.google.com/p/psicquic/wiki/MiqlDefinition?conversationContext=3" target="_blank">Here</a><table style="padding-top: 10px"><tbody>
<tr><td><span style="font-weight: bold">Field Name</span></td><td><span style="font-weight: bold">Searches on</span></td><td><span style="font-weight: bold">Example</span></td></tr>
<%--Complex Information--%>
<tr><td>complex_id</td><td>Complex identifier(s)</td><td><a href="http://www.ebi.ac.uk:80/intact/pages/interactions/interactions.xhtml?query=idA%3AP74565&amp;conversationContext=3" target="_top">idA:P74565</a></td></tr>
<tr><td>complex_name</td><td>Complex name(s)</td><td><a href="http://www.ebi.ac.uk:80/intact/pages/interactions/interactions.xhtml?query=idB%3AP74565&amp;conversationContext=3" target="_top">idB:P74565</a></td></tr>
<tr><td>complex_alias</td><td>Complex alias(es)</td><td><a href="http://www.ebi.ac.uk:80/intact/pages/interactions/interactions.xhtml?query=id%3AP74565&amp;conversationContext=3" target="_top">id:P74565</a></td></tr>
<tr><td>species</td><td>Complex Tax ID</td><td><a href="http://www.ebi.ac.uk:80/intact/pages/interactions/interactions.xhtml?query=alias%3A%28KHDRBS1+OR+HCK%29&amp;conversationContext=3" target="_top">alias:(KHDRBS1 OR HCK)</a></td></tr>
<tr><td>complex_type</td>Complex type(s)<td></td><td><a href="http://www.ebi.ac.uk:80/intact/pages/interactions/interactions.xhtml?query=identifier%3AP74565&amp;conversationContext=3" target="_top">identifier:P74565</a></td></tr>
<tr><td>type</td><td>Type(s)</td><td><a href="http://www.ebi.ac.uk:80/intact/pages/interactions/interactions.xhtml?query=pubauth%3Ascott&amp;conversationContext=3" target="_top">pubauth:scott</a></td></tr>
<tr><td>complex_xref</td><td>Complex xref(s)</td><td><a href="http://www.ebi.ac.uk:80/intact/pages/interactions/interactions.xhtml?query=pubid%3A%2810837477+OR+12029088%29&amp;conversationContext=3" target="_top">pubid:(10837477 OR 12029088)</a></td></tr>
<tr><td>complex_AC</td><td>Complex AC</td><td><a href="http://www.ebi.ac.uk:80/intact/pages/interactions/interactions.xhtml?query=taxidA%3Amouse&amp;conversationContext=3" target="_top">taxidA:mouse</a></td></tr>
<tr><td>description</td><td>Complex description</td><td><a href="http://www.ebi.ac.uk:80/intact/pages/interactions/interactions.xhtml?query=taxidB%3A9606&amp;conversationContext=3" target="_top">taxidB:9606</a></td></tr>
<tr><td>organism_name</td><td></td><td><a href="http://www.ebi.ac.uk:80/intact/pages/interactions/interactions.xhtml?query=species%3Ahuman&amp;conversationContext=3" target="_top">species:human</a></td></tr>
<tr><td>udate</td><td></td><td><a href="http://www.ebi.ac.uk:80/intact/pages/interactions/interactions.xhtml?query=type%3A%22physical+interaction%22&amp;conversationContext=3" target="_top">type:&quot;physical interaction&quot;</a></td></tr>
<tr><td>param</td><td></td><td><a href="http://www.ebi.ac.uk:80/intact/pages/interactions/interactions.xhtml?query=detmethod%3A%22two+hybrid*%22&amp;conversationContext=3" target="_top">detmethod:&quot;two hybrid*&quot;</a></td></tr>
<%--Interactor Information--%>
<tr><td>id</td><td>Interactor identifier(s)</td><td><a href="http://www.ebi.ac.uk:80/intact/pages/interactions/interactions.xhtml?query=interaction_id%3AEBI-761050&amp;conversationContext=3" target="_top">interaction_id:EBI-761050</a></td></tr>
<tr><td>alias</td><td>Interactor alia(es)</td><td><a href="http://www.ebi.ac.uk:80/intact/pages/interactions/interactions.xhtml?query=pbioroleA%3Aenzyme&amp;conversationContext=3" target="_top">pbioroleA:enzyme</a></td></tr>
<tr><td>ptype</td><td>Interactor type(s)</td><td><a href="http://www.ebi.ac.uk:80/intact/pages/interactions/interactions.xhtml?query=pbioroleB%3Aenzyme&amp;conversationContext=3" target="_top">pbioroleB:enzyme</a></td></tr>
<tr><td>pxref</td><td>Interactor xref(s)</td><td><a href="http://www.ebi.ac.uk:80/intact/pages/interactions/interactions.xhtml?query=pbiorole%3Aenzyme&amp;conversationContext=3" target="_top">pbiorole:enzyme</a></td></tr>
<tr><td>stc</td><td>STC</td><td><a href="http://www.ebi.ac.uk:80/intact/pages/interactions/interactions.xhtml?query=ptypeA%3Aprotein&amp;conversationContext=3" target="_top">ptypeA:protein</a></td></tr>
<%--Other Fields (PSICQUIC, IntAct)--%>
<tr><td>pbiorole</td><td>Biological role(s)</td><td><a href="http://www.ebi.ac.uk:80/intact/pages/interactions/interactions.xhtml?query=ptypeB%3Aprotein&amp;conversationContext=3" target="_top">ptypeB:protein</a></td></tr>
<tr><td>ftype</td><td>Feature type(s)</td><td><a href="http://www.ebi.ac.uk:80/intact/pages/interactions/interactions.xhtml?query=ptype%3Aprotein&amp;conversationContext=3" target="_top">ptype:protein</a></td></tr>
<tr><td>source</td><td>Source database(s)</td><td><a href="http://www.ebi.ac.uk:80/intact/pages/interactions/interactions.xhtml?query=pxrefA%3A%22GO%3A0005794%22&amp;conversationContext=3" target="_top">pxrefA:&quot;GO:0005794&quot;</a></td></tr>
<tr><td>number_participants</td><td>Number of participants</td><td><a href="http://www.ebi.ac.uk:80/intact/pages/interactions/interactions.xhtml?query=pxrefB%3A%22GO%3A0005794%22&amp;conversationContext=3" target="_top">pxrefB:&quot;GO:0005794&quot;</a></td></tr>
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