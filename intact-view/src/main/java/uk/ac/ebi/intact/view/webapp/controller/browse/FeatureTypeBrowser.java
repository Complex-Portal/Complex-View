package uk.ac.ebi.intact.view.webapp.controller.browse;

import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.FieldNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;
import uk.ac.ebi.intact.view.webapp.util.RootTerm;

/**
 * Browser for feature types
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>03/09/12</pre>
 */
@Controller("featureTypeBrowser")
@Scope("conversation.access")
@ConversationName("general")
public class FeatureTypeBrowser extends OntologyBrowserController {

    public static final String FIELD_NAME = FieldNames.INTERACTOR_FEATURE;

    public FeatureTypeBrowser(){
        super();
        this.useName = true;
    }

    @Override
    // give the first root term because we need to add terms which are not considered as children of feature types
    protected OntologyTerm createRootTerm(OntologySearcher ontologySearcher) {
        final RootTerm rootTerm = new RootTerm( ontologySearcher, "Feature type" );
        rootTerm.setIdentifier("MI:0116");

        // mi terms for feature types
        rootTerm.addChild("MI:0505", "experimental feature");
        rootTerm.addChild("MI:0252", "biological feature");

        // mod terms for ptms
        rootTerm.addChild("MOD:00000", "protein modification");

        return rootTerm;
    }

    @Override
    public String getFieldName() {
        return FIELD_NAME;
    }
}
