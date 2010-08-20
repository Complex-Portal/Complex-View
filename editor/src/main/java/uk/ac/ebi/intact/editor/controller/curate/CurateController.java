package uk.ac.ebi.intact.editor.controller.curate;

import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.editor.controller.curate.cvobject.CvObjectController;
import uk.ac.ebi.intact.editor.controller.curate.experiment.ExperimentController;
import uk.ac.ebi.intact.editor.controller.curate.feature.FeatureController;
import uk.ac.ebi.intact.editor.controller.curate.interaction.InteractionController;
import uk.ac.ebi.intact.editor.controller.curate.interactor.InteractorController;
import uk.ac.ebi.intact.editor.controller.curate.organism.BioSourceController;
import uk.ac.ebi.intact.editor.controller.curate.participant.ParticipantController;
import uk.ac.ebi.intact.editor.controller.curate.publication.PublicationController;
import uk.ac.ebi.intact.model.*;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "general" )
public class CurateController extends JpaAwareController {

    public String edit(IntactObject intactObject) {
        Class<?> iaClass = intactObject.getClass();

        String suffix = (intactObject.getAc() != null)? "?faces-redirect=true&includeViewParams=true" : "";

        if (Publication.class.isAssignableFrom(iaClass)) {
            PublicationController publicationController = (PublicationController) getSpringContext().getBean("publicationController");
            publicationController.setPublication((Publication)intactObject);
            return "/curate/publication"+suffix;
        } else if (Experiment.class.isAssignableFrom(iaClass)) {
            ExperimentController experimentController = (ExperimentController) getSpringContext().getBean("experimentController");
            experimentController.setExperiment((Experiment)intactObject);
            return "/curate/experiment"+suffix;
        } else if (Interaction.class.isAssignableFrom(iaClass)) {
            InteractionController interactionController = (InteractionController) getSpringContext().getBean("interactionController");
            interactionController.setInteraction((Interaction)intactObject);
            return "/curate/interaction"+suffix;
        } else if (Interactor.class.isAssignableFrom(iaClass)) {
            InteractorController interactorController = (InteractorController) getSpringContext().getBean("interactorController");
            interactorController.setInteractor((Interactor)intactObject);
            return "/curate/interactor"+suffix;
        } else if (Component.class.isAssignableFrom(iaClass)) {
            ParticipantController participantController = (ParticipantController) getSpringContext().getBean("participantController");
            participantController.setParticipant((Component) intactObject);
            return "/curate/participant"+suffix;
        } else if (Feature.class.isAssignableFrom(iaClass)) {
            FeatureController featureController = (FeatureController) getSpringContext().getBean("featureController");
            featureController.setFeature((Feature) intactObject);
            return "/curate/feature"+suffix;
        } else if (CvObject.class.isAssignableFrom(iaClass)) {
            CvObjectController cvObjectController = (CvObjectController) getSpringContext().getBean("cvObjectController");
            cvObjectController.setCvObject((CvObject) intactObject);
            return "/curate/cvobject"+suffix;
        } else if (BioSource.class.isAssignableFrom(iaClass)) {
            BioSourceController bioSourceController = (BioSourceController) getSpringContext().getBean("bioSourceController");
            bioSourceController.setBioSource((BioSource) intactObject);
            return "/curate/organism"+suffix;
        } else {
            throw new IllegalArgumentException("No view defined for object with type: "+iaClass);
        }
    }

}
