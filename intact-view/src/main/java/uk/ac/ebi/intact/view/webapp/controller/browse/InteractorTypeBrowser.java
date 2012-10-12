package uk.ac.ebi.intact.view.webapp.controller.browse;

import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.FieldNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.LazyLoadedOntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;

/**
 * Browser for interactor types
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/08/12</pre>
 */
@Controller("interactorTypeBrowser")
@Scope("request")
public class InteractorTypeBrowser extends OntologyBrowserController{
    public static final String FIELD_NAME = FieldNames.INTERACTOR_TYPE;

    @Override
    protected OntologyTerm createRootTerm(OntologySearcher ontologySearcher) {

        try {
            return new LazyLoadedOntologyTerm( ontologySearcher, "MI:0313", "Interactor Type");
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
