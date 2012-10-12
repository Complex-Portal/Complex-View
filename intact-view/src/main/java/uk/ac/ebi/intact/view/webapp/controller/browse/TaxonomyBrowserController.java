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

import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.primefaces.model.TreeNode;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.FieldNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;
import uk.ac.ebi.intact.view.webapp.util.RootTerm;

/**
 * Controller for GoBrowsing
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller("taxonomyBrowser")
@Scope("conversation.access")
@ConversationName("general")
public class TaxonomyBrowserController extends OntologyBrowserController {

    public static final String FIELD_NAME = FieldNames.SPECIES;

    private boolean skipIntermediateTaxons = true;

    @Override
    protected OntologyTerm createRootTerm(OntologySearcher ontologySearcher) {
        final RootTerm rootTerm = new RootTerm(ontologySearcher, "Taxonomy");
        rootTerm.addChild("10239", "Viruses");
        rootTerm.addChild("131567", "cellular organisms");

        return rootTerm;
    }

    @Override
    protected TreeNode createRootTreeNode(OntologyTermWrapper otwRoot) {
        if (skipIntermediateTaxons) {
            return new AutoExpandedTreeNode(otwRoot);
        } else {
            return super.createRootTreeNode(otwRoot);
        }
    }

    @Override
    public String getFieldName() {
        return FIELD_NAME;
    }

    public boolean isSkipIntermediateTaxons() {
        return skipIntermediateTaxons;
    }

    public void setSkipIntermediateTaxons(boolean skipIntermediateTaxons) {
        this.skipIntermediateTaxons = skipIntermediateTaxons;
    }
}