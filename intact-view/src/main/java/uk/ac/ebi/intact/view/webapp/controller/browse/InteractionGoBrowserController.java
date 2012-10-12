package uk.ac.ebi.intact.view.webapp.controller.browse;

import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.FieldNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;
import uk.ac.ebi.intact.view.webapp.util.RootTerm;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>03/09/12</pre>
 */
@Controller("interactionGoBrowser")
@Scope("conversation.access")
@ConversationName("general")
public class InteractionGoBrowserController extends OntologyBrowserController{

    public static final String FIELD_NAME = FieldNames.INTERACTION_XREF;

    @Override
    protected OntologyTerm createRootTerm(OntologySearcher ontologySearcher) {
        final RootTerm rootTerm = new RootTerm( ontologySearcher, "GO Ontology" );
        rootTerm.addChild("GO:0008150", "Biological process");
        rootTerm.addChild("GO:0003674", "Molecular function");
        rootTerm.addChild("GO:0005575", "Cellular component");
        return rootTerm;
    }

    @Override
    public String getFieldName() {
        return FIELD_NAME;
    }
}
