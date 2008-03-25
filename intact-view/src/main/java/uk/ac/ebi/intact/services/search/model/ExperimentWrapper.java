package uk.ac.ebi.intact.services.search.model;

import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ExperimentWrapper extends AnnotatedObjectWrapper<Experiment> {

    private int interactionsCount;
    private Object interactions;

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

    public Object getInteractions() {
        if (interactions == null) {

            if (getData().getAc() != null) {
                interactions = new CriteriaDataModel(DetachedCriteria.forClass(Interaction.class)
                        .createAlias("experiments", "exp")
                        .add(Restrictions.eq("exp.ac", getData().getAc())));
            } else {
                interactions = new ArrayList<Interaction>(getData().getInteractions());
            }
        }
        return interactions;
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
