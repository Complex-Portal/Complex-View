package uk.ac.ebi.intact.editor.controller.curate.util;

import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.model.*;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@org.springframework.stereotype.Component
public class CoreDeleter extends JpaAwareController {

    public void delete(IntactObject io) {
        if (io instanceof Experiment) {
            deleteExperiment((Experiment) io);
        } else if (io instanceof Interaction) {
            deleteInteraction((Interaction) io);
        } else if (io instanceof Component) {
            deleteParticipant((Component) io);
        } else if (io instanceof Feature) {
            deleteFeature((Feature) io);
        }

        IntactObject managedEntity = getDaoFactory().getEntityManager().merge(io);
        getDaoFactory().getEntityManager().remove(managedEntity);
    }

    private void deleteExperiment(Experiment experiment) {
        experiment.getPublication().removeExperiment(experiment);
    }

    private void deleteInteraction(Interaction interaction) {
        for (Experiment exp : interaction.getExperiments()) {
            exp.removeInteraction(interaction);
        }
    }

    private void deleteParticipant(Component participant) {
        participant.getInteraction().removeComponent(participant);
    }

    private void deleteFeature(Feature feature) {
        if (feature != null && feature.getComponent() != null) {
            feature.getComponent().removeBindingDomain(feature);
        }
    }
}
