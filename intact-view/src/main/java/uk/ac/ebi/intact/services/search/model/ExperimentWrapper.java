package uk.ac.ebi.intact.services.search.model;

import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;

import java.util.Collection;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ExperimentWrapper extends AnnotatedObjectWrapper<Experiment> {

    private int interactionsCount;

    public ExperimentWrapper(Experiment data) {
        super(data);

        if (data.getAc() != null) {
            interactionsCount = IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                    .getExperimentDao().countInteractionsForExperimentWithAc(data.getAc());
        }
        else {
            interactionsCount = data.getInteractions().size();
        }
    }

    public Collection<Interaction> getInteractions() {
        return getData().getInteractions();
    }

    public CvIdentification getCvIdentification() {
        return getData().getCvIdentification();
    }

    public CvInteraction getCvInteraction() {
        return getData().getCvInteraction();
    }

    public Publication getPublication() {
        return getData().getPublication();
    }

    public String getCvInteractionAc() {
        return getData().getCvInteractionAc();
    }

    public BioSource getBioSource() {
        return getData().getBioSource();
    }

    public int getInteractionsCount() {
        return interactionsCount;
    }
}
