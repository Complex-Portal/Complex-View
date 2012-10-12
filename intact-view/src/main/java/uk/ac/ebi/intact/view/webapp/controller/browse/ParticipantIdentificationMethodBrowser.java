package uk.ac.ebi.intact.view.webapp.controller.browse;

import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.FieldNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.LazyLoadedOntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;

/**
 * Browser for participant identification methods
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/08/12</pre>
 */
@Controller("participantIdentificationMethodBrowser")
@Scope("conversation.access")
@ConversationName("general")
public class ParticipantIdentificationMethodBrowser extends OntologyBrowserController {

    public static final String FIELD_NAME = FieldNames.INTERACTOR_DET_METHOD;

    @Override
    protected OntologyTerm createRootTerm(OntologySearcher ontologySearcher) {

        try {
            return new LazyLoadedOntologyTerm( ontologySearcher, "MI:0002", "Participant Identification method");
        } catch (SolrServerException e) {
            e.printStackTrace();
            addErrorMessage("Could not load the tree", "");
        }
        return null;
    }

    @Override
    public String getFieldName() {
        return FIELD_NAME;
    }
}
