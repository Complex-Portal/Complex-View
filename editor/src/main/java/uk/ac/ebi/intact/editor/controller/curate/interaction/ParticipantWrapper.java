package uk.ac.ebi.intact.editor.controller.curate.interaction;

import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.controller.curate.AnnotatedObjectHelper;
import uk.ac.ebi.intact.editor.controller.curate.ChangesController;
import uk.ac.ebi.intact.model.*;

/**
 * Wrapped participant to allow handling of special fields (eg. author given name) from the interaction view. 
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0
 */
public class ParticipantWrapper {

    private Component participant;
    private AnnotatedObjectHelper annotatedObjectHelper;
    private ChangesController changesController;

    private boolean deleted;
    
    public ParticipantWrapper( Component participant, ChangesController changesController ) {
        this.participant = participant;
        this.annotatedObjectHelper = newAnnotatedObjectHelper(participant);
        this.changesController = changesController;
    }

    public Component getParticipant() {
        return participant;
    }

    public void setParticipant( Component participant ) {
        this.participant = participant;
    }

    public String getAuthorGivenName() {
        return annotatedObjectHelper.findAliasName( CvAliasType.AUTHOR_ASSIGNED_NAME_MI_REF );
    }

    public void setAuthorGivenName( String name ) {
        annotatedObjectHelper.addOrReplace(CvAliasType.AUTHOR_ASSIGNED_NAME_MI_REF, name  );
    }

    public CvExperimentalRole getFirstExperimentalRole() {
        if( ! participant.getExperimentalRoles().isEmpty() ) {
            return participant.getExperimentalRoles().iterator().next();
        }
        return null;
    }

    public void setFirstExperimentalRole(CvExperimentalRole role) {
        if( ! participant.getExperimentalRoles().contains( role ) ) {
            participant.getExperimentalRoles().clear();
            participant.getExperimentalRoles().add( role );
        }
    }

    public CvExperimentalPreparation getFirstExperimentalPreparation() {
        if( participant.getInteractor() != null ) {
            if( ! participant.getExperimentalPreparations().isEmpty() ) {
                return participant.getExperimentalPreparations().iterator().next();
            }
        }

        return null;
    }

    public void setFirstExperimentalPreparation( CvExperimentalPreparation prep ) {
            if( ! participant.getExperimentalPreparations().contains( prep ) ) {
                participant.getExperimentalPreparations().clear();
                participant.getExperimentalPreparations().add( prep );
            }
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;

        if (participant.getAc() != null) {
            if (deleted) {
                changesController.markToDelete(participant, participant.getInteraction());
            } else {
                changesController.removeFromDeleted(participant, participant.getInteraction());
            }
        }
    }

    private AnnotatedObjectHelper newAnnotatedObjectHelper(AnnotatedObject annotatedObject) {
        AnnotatedObjectHelper helper = (AnnotatedObjectHelper) IntactContext.getCurrentInstance().getSpringContext().getBean("annotatedObjectHelper");
        helper.setAnnotatedObject(annotatedObject);

        return helper;
    }
}
