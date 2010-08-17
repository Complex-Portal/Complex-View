package uk.ac.ebi.intact.editor.controller.curate.interactor;

import org.apache.commons.lang.StringUtils;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.controller.curate.AnnotatedObjectController;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;

import javax.faces.event.ComponentSystemEvent;

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

    public InteractorController() {
    }

    public Interactor getInteractor() {
        return interactor;
    }

    public void setInteractor( Interactor interactor ) {
        this.interactor = interactor;
    }

    public void loadData( ComponentSystemEvent event ) {
        if ( ac != null ) {
            if ( interactor == null ) {
                interactor = IntactContext.getCurrentInstance().getDaoFactory().getInteractorDao().getByAc( ac );
            }
        } else {
            if ( interactor != null ) ac = interactor.getAc();
        }
    }

    public String newInteractor() {
        interactor = newInstance(newInteractorType);
        interactor.setOwner(getIntactContext().getInstitution());
        interactor.setCvInteractorType(newInteractorType);

        getUnsavedChangeManager().markAsUnsaved(interactor);

        if (interactor instanceof Interaction) {
            return "/curate/interaction";
        }

        return "/curate/interactor";
    }

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

    @Override
    public AnnotatedObject getAnnotatedObject() {
        return getInteractor();
    }

    @Override
    public void setAnnotatedObject(AnnotatedObject annotatedObject) {
        setInteractor((Interactor)annotatedObject);
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
}
