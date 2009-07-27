/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.view.webapp.controller.browse;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;
import uk.ac.ebi.intact.view.webapp.util.RootTerm;

/**
 * Controller for GoBrowsing
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller("taxonomyBrowser")
@Scope("request")
public class TaxonomyBrowserController extends OntologyBrowserController {

    public static final String FIELD_NAME = "taxid_expanded_id";

    @Override
    protected OntologyTerm createRootTerm(OntologySearcher ontologySearcher) {
        final RootTerm rootTerm = new RootTerm(ontologySearcher, "Taxonomy");
//        rootTerm.addChild("-1", "In vitro");
        rootTerm.addChild("10239", "Viruses");
//        rootTerm.addChild("12884", "Viroids");
//        rootTerm.addChild("12908", "unclassified sequences");
        rootTerm.addChild("131567", "cellular organisms");
//        rootTerm.addChild("28384", "other sequences");

        return rootTerm;
    }

    @Override
    public String getFieldName() {
        return FIELD_NAME;
    }
}