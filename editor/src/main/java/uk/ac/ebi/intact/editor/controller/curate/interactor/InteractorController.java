package uk.ac.ebi.intact.editor.controller.curate.interactor;

import org.apache.commons.lang.StringUtils;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.controller.UserSessionController;
import uk.ac.ebi.intact.editor.controller.curate.AnnotatedObjectController;
import uk.ac.ebi.intact.editor.controller.curate.cloner.InteractorIntactCloner;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.validator.ValidatorException;

/**
 * Interactor controller.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "general" )
public class InteractorController extends AnnotatedObjectController {

    private Interactor interactor;

    private String ac;

    private CvInteractorType newInteractorType;

    @Autowired
    private UserSessionController userSessionController;

    public InteractorController() {
    }

    @Override
    public AnnotatedObject getAnnotatedObject() {
        return getInteractor();
    }

    @Override
    public String clone() {
        return clone(interactor, new InteractorIntactCloner());
    }

    @Override
    public void setAnnotatedObject(AnnotatedObject annotatedObject) {
        setInteractor((Interactor) annotatedObject);
    }

    public void loadData( ComponentSystemEvent event ) {
        if (!FacesContext.getCurrentInstance().isPostback()) {

            if ( ac != null ) {
                if ( interactor == null || !ac.equals(interactor.getAc())) {
                    interactor = loadByAc(IntactContext.getCurrentInstance().getDaoFactory().getInteractorDao(), ac);
                }
            } else {
                if ( interactor != null ) ac = interactor.getAc();
            }

            if (interactor == null) {
                super.addErrorMessage("Interactor does not exist", ac);
                return;
            }

            reset();
            refreshTabsAndFocusXref();
        }

        generalLoadChecks();
    }

    private void reset() {
    }

    public void validateAnnotatedObject(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (!CvObjectUtils.isSmallMoleculeType(interactor.getCvInteractorType()) && interactor.getBioSource() == null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Organism is mandatory", "No biosource was defined");
            throw new ValidatorException(message);
        }

        super.validateAnnotatedObject(context, component, value);
    }

    @Override
    public void doPreSave() {
        super.doPreSave();
        cleanSequence(null);
    }

    public String newInteractor() {
        Interactor interactor = newInstance(newInteractorType);
        interactor.setOwner(userSessionController.getUserInstitution());
        interactor.setCvInteractorType(newInteractorType);

        setInteractor(interactor);

        getChangesController().markAsUnsaved(interactor);

        return navigateToObject(interactor);
    }

    // TODO migrate to intact core as this is generic functionality
    public Interactor newInstance(CvInteractorType interactorType) {
        // re-attach xrefs
        interactorType = getDaoFactory().getCvObjectDao(CvInteractorType.class).getByAc(interactorType.getAc());

        if (CvObjectUtils.isProteinType(interactorType) || CvObjectUtils.isPeptideType(interactorType)) {
            return newInstance(ProteinImpl.class.getName());
        } else if (CvObjectUtils.isSmallMoleculeType(interactorType)) {
            return newInstance(SmallMoleculeImpl.class.getName());
        } else if (CvObjectUtils.isNucleicAcidType(interactorType)) {
            return newInstance(NucleicAcidImpl.class.getName());
        } else if (CvObjectUtils.isInteractionType(interactorType)) {
            return newInstance(InteractionImpl.class.getName());
        } else if (CvObjectUtils.isChildOfType(interactorType, CvInteractorType.BIOPOLYMER_MI_REF, true)) {
            return newInstance(PolymerImpl.class.getName());
        } else if (CvObjectUtils.isChildOfType(interactorType, CvInteractorType.POLYSACCHARIDE_MI_REF, true)) {
            return newInstance(PolySaccharideImpl.class.getName());
        } else {
            return newInstance(InteractorImpl.class.getName());
        }
    }

    private Interactor newInstance(String type) {
        Interactor obj = null;

        try {
            Class cvClass = Thread.currentThread().getContextClassLoader().loadClass(type);

            obj = (Interactor) cvClass.newInstance();
        } catch (Exception e) {
            addErrorMessage("Problem creating interactor", "Class "+type);
            e.printStackTrace();
        }

        return obj;
    }

    public String getMoleculeType() {
        if( interactor != null && interactor.getCvInteractorType() != null) {
            if( interactor.getCvInteractorType().getFullName() != null )
                return StringUtils.capitalize( interactor.getCvInteractorType().getFullName() );
            else
                return StringUtils.capitalize( interactor.getCvInteractorType().getShortLabel() );
        }

        return "Interactor";
    }

    public boolean isOrganismApplicable() {
        if (interactor == null || interactor.getCvInteractorType() == null) {
            return true;
        }

        return !(CvInteractorType.SMALL_MOLECULE_MI_REF.equals(interactor.getCvInteractorType().getIdentifier()));
    }

//    public void validateSequence(FacesContext context, UIComponent component, Object value) throws ValidatorException {
//        String seq = getSequence();
//
//        if (seq != null) {
//            if (seq.contains(" ") || seq.contains("\n") || seq.contains("\r")) {
//                addErrorMessage("Invalid sequence", "Illegal characters were found in the sequence (e.g. spaces, return chars...)");
//                FacesContext.getCurrentInstance().renderResponse();
//            }
//        }
//    }

    public void cleanSequence(AjaxBehaviorEvent evt) {
        String seq = getSequence();
        String originalSeq = seq;

        boolean changedSequence = false;

        if (seq != null) {
            // remove all non-alphabetical characters
            seq = seq.replaceAll("\\P{Alpha}", "");
            seq = seq.toUpperCase();

            changedSequence = !(seq.equals(originalSeq));

            if (changedSequence) {
                setSequence(seq);

                addWarningMessage("Sequence updated", "Illegal characters were found in the sequence and were removed automatically");
            }
        }
    }

    public Interactor getInteractor() {
        return interactor;
    }

    public void setInteractor( Interactor interactor ) {
        this.interactor = interactor;

        if (interactor != null){
            this.ac = interactor.getAc();
        }
    }

    public String getSequence() {
        if (interactor instanceof Polymer) {
            Polymer polymer = (Polymer) interactor;
            return polymer.getSequence();
        }

        return null;
    }

    public void setSequence(String sequence) {
        if (interactor instanceof Polymer) {
            Polymer polymer = (Polymer) interactor;
            polymer.setSequence(sequence);
        }
    }

    public boolean isPolymer() {
        return interactor instanceof Polymer;
    }

    public CvInteractorType getNewInteractorType() {
        return newInteractorType;
    }

    public void setNewInteractorType(CvInteractorType newInteractorType) {
        this.newInteractorType = newInteractorType;
    }

    public String getAc() {
        return ac;
    }

    public void setAc(String ac) {
        this.ac = ac;
    }

    public boolean isNoUniprotUpdate() {
        return super.isNoUniprotUpdate((Interactor)getAnnotatedObject());
    }
}
