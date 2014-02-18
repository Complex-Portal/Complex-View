<html
<%@include file="header.jsp"%>


<nav>
    <ul class="grid_24" id="local-nav">
        <li class="first"><a href="${complex_home_url}">Home</a></li>
        <li><a href="${complex_about_url}">About</a></li>
        <li class="active"><a href="${complex_documentation_url}">Documentation</a></li>
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
        <h2>What can be described as a complex</h2>
        <p>A stable set of (two or more) interacting macromolecules such as proteins which can be co-purified by an acceptable method and have been shown to exist as an isolated, functional unit in vivo. Any interacting non-protein molecular (e.g. small molecules, nucleic acids) will also be included.</p>
        <p>What should not be captured:</p>
        <blockquote>
        <lu>
            <li>Enzyme/substrate, receptor/ligand or any similar transient interactions unless these are a critical part of the complex assembly.</li>
            <li>Proteins associated in a pulldown/coimmunoprecipitation with no functional link or any evidence that this is a defined biological entity rather than a loose affinity complex.</li>
            <li>Proteins with the same function but with either no demonstrable physical link or one that can be inferred by sequence homology.</li>
            <li>Any literature complex where the only evidence is based on genetic interaction data.</li>
        </lu>
        </blockquote>
        <h2>Complex Nomenclature</h2>
        <p><b>Complex recommended name</b> - the most informative, well accepted name in the literature, that is intuitive to the user. Where possible, this will be the same as the equivalent component term in GO. The term should always end in the word 'complex' or homo'n'mer. The recommended name is kept consistent when a complex is conserved across a taxonomic range.</p>
        <p><b>Complex systematic name</b> - Derived using the Reactome rules for naming complexes. In principle this is a string of gene names of the participants of the complex separated by a colon (e.g. &quot;fiba:fib:fibg&quot;). The order is determined by the order of synthesis of the complex. Where several participants join at the same time, or the order is unknown, alphanumeric order is used.</p>
        <p><b>Complex synonym</b> - all other possible names the complex is known by or can be described as.</p>
        <p><b>Short label</b> - currently an obligate part of the data model, it may be possible to remove these once the model is updated. Currently, an appropriate designation for the complex with species indicated using the UniProt five letter code e.g. fibrinogen_human, tfiid_mouse. When the same complex is conserved across a taxonomic range, the root name is maintained across all entries e.g. fibrinogen_human, fibrinogen_mouse, fibrinogen_bovin.</p>

        <h2>Complex Participants</h2>
        <p><b>Proteins</b> - all proteins are derived from, and linked to, UniProtKB, ideally UniProtKB/Swiss-Prot. Isoform and chain designators will be used when appropriate. Should one or more isoforms exist, annotation will be to the canonical protein entry unless either only one isoform is known to exist in the complex or different isoforms give the complex different properties. In the latter case, a separate entry should be made for each variation with detail given in &quot;curated-complex&quot; or &quot;complex-properties&quot; as appropriate (see below for details on complex variants).</p>
        <p><b>Small molecules /polysaccharides</b> - all small molecules are derived from, and linked to, ChEBI. Small molecules are entered if they are integral to the complex or bind to the complex as part of its function, e.g. cofactors, electron donors/acceptors, such as  ATP, H+. Enzyme targets are not added as participants. For example, ATP is entered as a cofactor if the enzyme function is NOT primarily an ATPase (e.g. EBI-9008779 gyrase_ecoli) but NOT entered for ATPases where it is a substrate (e.g. EBI-9007893 mfd-uvra_ecoli, a DNA translocase).</p>
        <p><b>Nucleic acids</b> - nucleic acids are only enteredas participants when they are an obligate part of the complex. In the short term, these will be created as generic molecules linked to ChEBI, this will be re-evaluated when RNACentral is available. For complexes which assemble and then bind to a nucleic acid, this function is indicated in free text and using GO terms such as GO:0003677 DNA binding.</p>
        <h3>Participant Features</h3>
        <p>Any features known to be involved in the reactions is mapped to the underlying sequence, as given in the source database, and cross-referenced to InterPro when possible. If a PTM is required for complex activation, this is curated as a feature and the effects detailed in the annotation field 'Complex-properties'. </p>
        <p>Binding sites or residues within proteins, known to directly interact within the complex are shown as linked features, both in the Table and graphical views.</p>
        <p>Stoichiometry is added, when known.</p>

        <h2>Interaction Type</h2>
        <p>This will always be 'Physical association' - the controlled vocabulary term indicating that these proteins are present in the same complex - unless there are one or two protein species involved in which case it will be 'Direct interaction'. Proteins directly binding to each other within a larger complex will be indicated by linked features (see above).</p>

        <h2>Free text annotation</h2>
        <p><b>Curated-complex</b> - a brief, free-text description of the function of the complex, written in the same style as a UniProtKB/Swiss-Prot entry.  For example &quot;Required for processive DNA replication and may act as a replicative helicase during DNA synthesis. Plays a central role in S-phase genome stability.&quot;.</p>
        <p><b>Complex-properties</b> - details of physical properties of the complex. This may include details about the topology, varying (as opposed to absolute) stoichiometry, molecular weight and Stoke's radius of the complex.</p>
        <p><b>Complex-assembly</b> - experimentally verified structural assembly e.g. homodimer, heterohexamer. Assemblies which have been computationally predicted are not included.</p>
        <p><b>Disease</b> - only added when the disease state has been specifically linked to the protein when in the complex.</p>

        <h2>Structured annotation</h2>
        <p>All structured annotation is entered as database/controlled vocabulary cross-reference with an appropriate qualifier term.</p>
        <p><b>Gene Ontology</b> - used to indicate the function, process and component of the complex as a whole. The Function term, in particular, may not be true for all members of the complex, for example enzyme complexes will be annotated with a catalytic function term even when some subunits play only a regulatory role.</p>
        <p><b>Experimental evidence</b> - when high quality evidence for the existence of this complex is present in an IMEx database, this will be added manually as a cross-reference so that it may be downloaded in the same file as the complex.</p>
        <p><b>3D structure</b> - representative PDB cross-references will be added when the complex has been crystallised in its entirety.</p>
        <p><b>Electron microscopy</b> - representative EMDB cross-references will be added when the complex has been visualised by electron microscopy in its entirety</p>
        <p><a id="evidences"><b>Evidence codes</b></a> - the following ECO codes will be used to indicate the strength of evidence that a complex exists:</p>
        <blockquote>
        <lu>
            <li>ECO:0000021 (physical interaction evidence) indicates that  full experimental evidence for the complexes has been added to the entry. This will consist of either a cross-reference to experimental data in an IMEx database, PDB or EMDB.</li>
            <li>ECO:0000088 (biological system reconstruction) indicates only limited experimental evidence exists for a complex in one species (e.g. mouse) but it is desirable to curate the complex which has been curated in another species (e.g. human) and orthologous gene products exist.</li>
            <li>ECO:0000306 (inference from background scientific knowledge used in manual assertion) if no experimental or only partial evidence is present but the complexes are generally assumed to exist.</li>
            <li>ECO:0000305 (curator inference used in manual assertion) for complexes imported from Reactome that contain CandidateSets after checking of such complexes.</li>
        </lu>
        </blockquote>
        <p><b>Enzymatic activity</b> - the E.C. number linked to IntEnz will be added when an enzyme complex is described.</p>
        <p><b>Additional literature</b> - review articles or experimental data not appropriate for entering into IntAct are added.</p>
        <p><b>Pathway information</b> - for human complexes, crosslinks to Reactome put complexes into a pathway context. Note that the definition of a complex is different in Reactome and in many cases a one-to-many relationship exists.</p>
        <p><b>Disease information</b> - OMIM or EFO cross references may be added if a complex or a chain when within that complex has been linked to a specific disease condition.</p>
        <p><b>Drug target information</b> - cross-links to ChEMBL are used to indicate complexes  which have been used as drug targets.</p>

        <h2>Complex Variants</h2>
        <p>If variant forms of a complex exist i.e. the same functional unit can exist in alternate forms with differing macromolecular composition, these are curated as separate objects. For example, PDGF can exist as a PDGF-A homodimer, PDGF-B homodimer, PDGF-AB heterodimer, PDGF-C homodimer and a PDGF-D homodimer If the variants have well-accepted names, e.g. PDGF-AB, these may be used as the primary name. If not, then the recommended name is qualified by variant 1, variant 2 e.g. TRAMP complex variant 1 (EBI-2352894).</p>

    </div>


<%@include file="footer.jsp"%>
</html>