package uk.ac.ebi.intact.editor.controller.curate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import uk.ac.ebi.intact.dataexchange.enricher.standard.InteractorEnricher;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Component
@Lazy
public class EnricherService {

    @Autowired
    private InteractorEnricher interactorEnricher;

    public InteractorEnricher getInteractorEnricher() {
        return interactorEnricher;
    }
}
