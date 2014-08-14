package uk.ac.ebi.intact.editor.controller.curate;

import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import psidev.psi.mi.jami.model.Complex;
import psidev.psi.mi.jami.model.ModelledFeature;
import psidev.psi.mi.jami.model.ModelledParticipant;
import uk.ac.ebi.intact.core.persister.IntactCore;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.editor.controller.curate.cvobject.CvObjectController;
import uk.ac.ebi.intact.editor.controller.curate.experiment.ExperimentController;
import uk.ac.ebi.intact.editor.controller.curate.feature.FeatureController;
import uk.ac.ebi.intact.editor.controller.curate.feature.ModelledFeatureController;
import uk.ac.ebi.intact.editor.controller.curate.institution.InstitutionController;
import uk.ac.ebi.intact.editor.controller.curate.interaction.ComplexController;
import uk.ac.ebi.intact.editor.controller.curate.interaction.InteractionController;
import uk.ac.ebi.intact.editor.controller.curate.interactor.InteractorController;
import uk.ac.ebi.intact.editor.controller.curate.organism.BioSourceController;
import uk.ac.ebi.intact.editor.controller.curate.participant.ModelledParticipantController;
import uk.ac.ebi.intact.editor.controller.curate.participant.ParticipantController;
import uk.ac.ebi.intact.editor.controller.curate.publication.PublicationController;
import uk.ac.ebi.intact.jami.model.IntactPrimaryObject;
import uk.ac.ebi.intact.jami.model.extension.IntactComplex;
import uk.ac.ebi.intact.jami.model.extension.IntactModelledFeature;
import uk.ac.ebi.intact.jami.model.extension.IntactModelledParticipant;
import uk.ac.ebi.intact.model.*;

import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Helper controller on conversation scope, that helps to load/save objects within the same transaction as the other AnnotatedObjectControllers.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "general" )
public class CurateController extends JpaAwareController {

    @Autowired
    private ChangesController changesController;

    private String acToOpen;

    private AnnotatedObjectController currentAnnotatedObjectController;

    public String edit(IntactObject intactObject) {
        String suffix = (intactObject.getAc() != null)? "?faces-redirect=true&includeViewParams=true" : "";

        CurateObjectMetadata metadata = getMetadata(intactObject);
        setCurrentAnnotatedObjectController(metadata.getAnnotatedObjectController());

        getCurrentAnnotatedObjectController().refreshTabsAndFocusXref();
        return "/curate/"+metadata.getSlug()+suffix;
    }

    public String editJami(IntactPrimaryObject intactObject) {
        String suffix = (intactObject.getAc() != null)? "?faces-redirect=true&includeViewParams=true" : "";

        CurateJamiMetadata metadata = getJamiMetadata(intactObject);
        setCurrentAnnotatedObjectController(metadata.getObjController());

        metadata.getObjController().setJamiObject(intactObject);

        getCurrentAnnotatedObjectController().refreshTabsAndFocusXref();
        return "/curate/"+metadata.getSlug()+suffix;
    }

    public String editByAc(String ac) {
        if (ac == null) {
            addErrorMessage("Illegal AC", "No AC provided");
            FacesContext.getCurrentInstance().renderResponse();
            return "";
        }

        ac = ac.trim();

        try {
            Class<? extends AnnotatedObject> aoClass = IntactCore.classForAc(getIntactContext(), ac);

            if (aoClass == null) {
                addErrorMessage("Illegal AC", "No annotated object found with this AC: "+ac);
                FacesContext.getCurrentInstance().renderResponse();
            }

            AnnotatedObject ao = getDaoFactory().getAnnotatedObjectDao(aoClass).getByAc(ac);

            if ( ao == null ) {
                addErrorMessage( "AC not found", "There is no IntAct object with ac '" + ac + "'" );
                return "";
            } else {
                return edit(ao);
            }
        } catch (IllegalArgumentException e){
            addErrorMessage( "AC not found", "There is no IntAct object with ac '" + ac + "'" );
            return "";
        }
    }

    public String editJamiByAc(String ac) {
        if (ac == null) {
            addErrorMessage("Illegal AC", "No AC provided");
            FacesContext.getCurrentInstance().renderResponse();
            return "";
        }

        ac = ac.trim();

        IntactPrimaryObject primary = getJamiEntityManager().find(IntactComplex.class, ac);
        if (primary == null){
            primary = getJamiEntityManager().find(IntactModelledParticipant.class, ac);
            if (primary == null){
                primary = getJamiEntityManager().find(IntactModelledFeature.class, ac);

                return editJami(primary);
            }
            else{
                return editJami(primary);
            }
        }
        else{
            return editJami(primary);
        }
    }

    public void save(IntactObject o) {
        save(o, true);
    }

    public void save(IntactObject object, boolean refreshCurrentView) {
        AnnotatedObjectController annotatedObjectController = getMetadata(object).getAnnotatedObjectController();
        annotatedObjectController.doSave(refreshCurrentView);
    }

    public void saveJami(IntactPrimaryObject o) {
        saveJami(o, true);
    }

    public void saveJami(IntactPrimaryObject object, boolean refreshCurrentView) {
        AnnotatedObjectController annotatedObjectController = getJamiMetadata(object).getObjController();
        annotatedObjectController.doSave(refreshCurrentView);
    }

    public void discard(IntactObject object) {

        AnnotatedObjectController annotatedObjectController = getMetadata(object).getAnnotatedObjectController();
        annotatedObjectController.doRevertChanges(null);
    }

    public void discardJami(IntactPrimaryObject object) {

        AnnotatedObjectController annotatedObjectController = getJamiMetadata((object)).getObjController();
        annotatedObjectController.doRevertChanges(null);
    }

    public String cancelEdition(IntactObject object) {

        AnnotatedObjectController annotatedObjectController = getMetadata(object).getAnnotatedObjectController();
        return annotatedObjectController.doCancelEdition();
    }

    public String cancelJamiEdition(IntactPrimaryObject object) {

        AnnotatedObjectController annotatedObjectController = getJamiMetadata((object)).getObjController();
        return annotatedObjectController.doCancelEdition();
    }

    public String newIntactObject(IntactObject object) {
        AnnotatedObjectController annotatedObjectController;
        CurateObjectMetadata meta = getMetadata(object);
        annotatedObjectController = meta.getAnnotatedObjectController();
        setCurrentAnnotatedObjectController(annotatedObjectController);
        getCurrentAnnotatedObjectController().refreshTabsAndFocusXref();

        return "/curate/"+meta.getSlug();
    }

    public String newJamiObject(IntactPrimaryObject object) {
        AnnotatedObjectController annotatedObjectController;
        CurateJamiMetadata meta = getJamiMetadata(((IntactPrimaryObject)object));
        annotatedObjectController = meta.getObjController();
        setCurrentAnnotatedObjectController(annotatedObjectController);
        getCurrentAnnotatedObjectController().refreshTabsAndFocusXref();

        return "/curate/"+meta.getSlug();
    }

    public CurateObjectMetadata getMetadata(IntactObject intactObject) {
        Class<?> iaClass = intactObject.getClass();

        if (Publication.class.isAssignableFrom(iaClass)) {
            PublicationController publicationController = (PublicationController) getSpringContext().getBean("publicationController");
            publicationController.setPublication((Publication)intactObject);
            return new CurateObjectMetadata(publicationController, "publication");
        } else if (Experiment.class.isAssignableFrom(iaClass)) {
            ExperimentController experimentController = (ExperimentController) getSpringContext().getBean("experimentController");
            experimentController.setExperiment((Experiment)intactObject);
            return new CurateObjectMetadata(experimentController, "experiment");
        } else if (Interaction.class.isAssignableFrom(iaClass)) {
            InteractionController interactionController = (InteractionController) getSpringContext().getBean("interactionController");
            interactionController.setInteraction((Interaction)intactObject);
            //interactionController.refreshParticipants();
            //interactionController.refreshExperimentLists();
            return new CurateObjectMetadata(interactionController, "interaction");
        } else if (Interactor.class.isAssignableFrom(iaClass)) {
            InteractorController interactorController = (InteractorController) getSpringContext().getBean("interactorController");
            interactorController.setInteractor((Interactor)intactObject);
            return new CurateObjectMetadata(interactorController, "interactor");
        } else if (Component.class.isAssignableFrom(iaClass)) {
            ParticipantController participantController = (ParticipantController) getSpringContext().getBean("participantController");
            participantController.setParticipant((Component) intactObject);
            return new CurateObjectMetadata(participantController, "participant");
        } else if (Feature.class.isAssignableFrom(iaClass)) {
            FeatureController featureController = (FeatureController) getSpringContext().getBean("featureController");
            featureController.setFeature((Feature) intactObject);
            return new CurateObjectMetadata(featureController, "feature");
        } else if (CvObject.class.isAssignableFrom(iaClass)) {
            CvObjectController cvObjectController = (CvObjectController) getSpringContext().getBean("cvObjectController");
            cvObjectController.setCvObject((CvObject) intactObject);
            return new CurateObjectMetadata(cvObjectController, "cvobject");
        } else if (BioSource.class.isAssignableFrom(iaClass)) {
            BioSourceController bioSourceController = (BioSourceController) getSpringContext().getBean("bioSourceController");
            bioSourceController.setBioSource((BioSource) intactObject);
            return new CurateObjectMetadata(bioSourceController, "organism");
        } else if (Institution.class.isAssignableFrom(iaClass)) {
            InstitutionController institutionController = (InstitutionController) getSpringContext().getBean("institutionController");
            institutionController.setInstitution((Institution) intactObject);
            return new CurateObjectMetadata(institutionController, "institution");
        } else {
            throw new IllegalArgumentException("No view defined for object with type: "+iaClass);
        }
    }

    public CurateJamiMetadata getJamiMetadata(IntactPrimaryObject intactObject) {
        Class<?> iaClass = intactObject.getClass();

        if (Complex.class.isAssignableFrom(iaClass)) {
            return new CurateJamiMetadata("complex",
                    (ComplexController) getSpringContext().getBean("complexController"));
        } else if (ModelledParticipant.class.isAssignableFrom(iaClass)) {
            CurateJamiMetadata meta = new CurateJamiMetadata("cparticipant",
                    (ModelledParticipantController) getSpringContext().getBean("modelledParticipantController"));
            ModelledParticipant part = (ModelledParticipant)intactObject;
            if (part.getInteraction() instanceof IntactComplex){
                IntactComplex parent = (IntactComplex)part.getInteraction();
                if (parent.getAc() != null){
                    meta.getParents().add(parent.getAc());
                }
            }
            return meta;
        } else if (ModelledFeature.class.isAssignableFrom(iaClass)) {
            CurateJamiMetadata meta = new CurateJamiMetadata("cfeature",
                    (ModelledFeatureController) getSpringContext().getBean("modelledFeatureController"));
            ModelledFeature feat = (ModelledFeature)intactObject;
            if (feat.getParticipant() instanceof IntactModelledParticipant){
                IntactModelledParticipant part = (IntactModelledParticipant)feat.getParticipant();
                if (part.getAc() != null){
                    meta.getParents().add(part.getAc());
                }
                if (part.getInteraction() instanceof IntactComplex){
                    IntactComplex parent = (IntactComplex)part.getInteraction();
                    if (parent.getAc() != null){
                        meta.getParents().add(parent.getAc());
                    }
                }
            }
            return meta;
        } else {
            throw new IllegalArgumentException("No view defined for object with type: "+iaClass);
        }
    }

    public String openByAc() {
        if (acToOpen == null) {
            addErrorMessage("Illegal AC", "No AC provided");
            FacesContext.getCurrentInstance().renderResponse();
            return "";
        }

        acToOpen = acToOpen.trim();

        IntactPrimaryObject primary = getJamiEntityManager().find(IntactComplex.class, acToOpen);
        if (primary == null){
            primary = getJamiEntityManager().find(IntactModelledParticipant.class, acToOpen);
            if (primary == null){
                primary = getJamiEntityManager().find(IntactModelledFeature.class, acToOpen);

                if (primary == null){
                    try {
                        Class<? extends AnnotatedObject> aoClass = IntactCore.classForAc(getIntactContext(), acToOpen);

                        if (aoClass == null) {
                            addErrorMessage("Illegal AC", "No annotated object found with this AC: "+acToOpen);
                            FacesContext.getCurrentInstance().renderResponse();
                        }

                        AnnotatedObject ao = getDaoFactory().getAnnotatedObjectDao(aoClass).getByAc(acToOpen);

                        if ( ao == null ) {
                            addErrorMessage( "AC not found", "There is no IntAct object with ac '" + acToOpen + "'" );
                            return "";
                        } else {
                            return edit(ao);
                        }
                    } catch (IllegalArgumentException e){
                        addErrorMessage( "AC not found", "There is no IntAct object with ac '" + acToOpen + "'" );
                        return "";
                    }
                }
                else{
                    return editJami(primary);
                }
            }
            else{
                return editJami(primary);
            }
        }
        else{
            return editJami(primary);
        }
    }

    public boolean isAnnotatedObject(Object obj) {
        return (obj instanceof AnnotatedObject) || (obj instanceof IntactPrimaryObject);
    }

    public class CurateObjectMetadata {
        private String slug;
        private AnnotatedObjectController annotatedObjectController;

        private CurateObjectMetadata(AnnotatedObjectController annotatedObjectController, String slug) {
            this.annotatedObjectController = annotatedObjectController;
            this.slug = slug;
        }

        public String getSlug() {
            return slug;
        }

        public AnnotatedObjectController getAnnotatedObjectController() {
            return annotatedObjectController;
        }
    }

    public class CurateJamiMetadata {
        private String slug;
        private Collection<String> parents = new ArrayList<String>();
        private AnnotatedObjectController objController;

        private CurateJamiMetadata(String slug, AnnotatedObjectController objController) {
            this.slug = slug;
            this.objController = objController;
        }

        public String getSlug() {
            return slug;
        }

        public Collection<String> getParents() {
            return parents;
        }

        public AnnotatedObjectController getObjController() {
            return objController;
        }
    }

    public AnnotatedObjectController getCurrentAnnotatedObjectController() {
        return currentAnnotatedObjectController;
    }

    public void setCurrentAnnotatedObjectController(AnnotatedObjectController currentAnnotatedObjectController) {
        this.currentAnnotatedObjectController = currentAnnotatedObjectController;
    }

    public String getAcToOpen() {
        return acToOpen;
    }

    public void setAcToOpen(String acToOpen) {
        this.acToOpen = acToOpen;
    }
}
