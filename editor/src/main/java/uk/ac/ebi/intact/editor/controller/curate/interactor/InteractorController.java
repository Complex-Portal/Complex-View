package uk.ac.ebi.intact.editor.controller.curate.interactor;

import org.apache.commons.lang.StringUtils;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.controller.curate.AnnotatedObjectController;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.Interactor;

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
            if ( interactor == null || !ac.equals( interactor.getAc() ) ) {
                interactor = IntactContext.getCurrentInstance().getDaoFactory().getInteractorDao().getByAc( ac );
            }
        } else {
            if ( interactor != null ) ac = interactor.getAc();
        }
    }

    public String getMoleculeType() {
        if( interactor != null ) {
            if( interactor.getCvInteractorType().getFullName() != null )
                return StringUtils.capitalize( interactor.getCvInteractorType().getFullName() );
            else
                return StringUtils.capitalize( interactor.getCvInteractorType().getShortLabel() );
        }

        return null;
    }

    @Override
    public AnnotatedObject getAnnotatedObject() {
        return getInteractor();
    }

    @Override
    public void setAnnotatedObject(AnnotatedObject annotatedObject) {
        setInteractor((Interactor)annotatedObject);
    }
}
