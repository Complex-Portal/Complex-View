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
public class InteractionWrapper extends InteractorWrapper<Interaction> {

    private int interactorCount;

    public InteractionWrapper(Interaction data) {
        super(data);

        if (data.getAc() != null) {
            this.interactorCount = IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                .getInteractionDao().countInteractorsByInteractionAc(data.getAc());
        } else {
            this.interactorCount = data.getComponents().size();
        }
    }

    public Float getKD() {
        return getData().getKD();
    }

    public Collection<Component> getComponents() {
        return getData().getComponents();
    }

    public Collection<Experiment> getExperiments() {
        return getData().getExperiments();
    }

    public CvInteractionType getCvInteractionType() {
        return getData().getCvInteractionType();
    }

    public String getCvInteractionTypeAc() {
        return getData().getCvInteractionTypeAc();
    }

    public String getCrc() {
        return getData().getCrc();
    }

    public Collection<Confidence> getConfidences() {
        return getData().getConfidences();
    }

    public int getInteractorCount() {
        return interactorCount;
    }
}