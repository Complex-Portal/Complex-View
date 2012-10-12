package uk.ac.ebi.intact.view.webapp.controller.browse;

import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.FieldNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;
import uk.ac.ebi.intact.view.webapp.util.RootTerm;

/**
 * Browser for annotation topic
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>03/09/12</pre>
 */
@Controller("annotationTopicBrowser")
@Scope("conversation.access")
@ConversationName("general")
public class AnnotationTopicBrowser extends OntologyBrowserController {

    public static final String FIELD_NAME = FieldNames.INTERACTION_ANNOTATIONS;

    public AnnotationTopicBrowser(){
        super();
        this.useName = true;
    }

    @Override
    // give the first root term because we need to add terms which are not considered as children of interaction attributes
    protected OntologyTerm createRootTerm(OntologySearcher ontologySearcher) {
        final RootTerm rootTerm = new RootTerm( ontologySearcher, "Interaction annotation topics" );

        rootTerm.addChild("MI:0664", "interaction attribute name");
        // not real interaction attributes in the ontology but are used as interaction annotation topics
        rootTerm.addChild("MI:1045", "curation content");
        rootTerm.addChild("MI:0954", "curation quality");

        // annotations normally at the level of publication but are copied to interaction
        rootTerm.addChild("MI:1093", "bibliographic attribute name");

        return rootTerm;
    }

    @Override
    public String getFieldName() {
        return FIELD_NAME;
    }
}
