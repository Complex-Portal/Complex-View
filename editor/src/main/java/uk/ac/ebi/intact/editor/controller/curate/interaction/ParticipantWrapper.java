package uk.ac.ebi.intact.editor.controller.curate.interaction;

import uk.ac.ebi.intact.editor.controller.curate.AnnotatedObjectHelper;
import uk.ac.ebi.intact.editor.controller.curate.UnsavedChangeManager;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.CvAliasType;
import uk.ac.ebi.intact.model.CvExperimentalPreparation;
import uk.ac.ebi.intact.model.CvExperimentalRole;

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
    private UnsavedChangeManager unsavedChangeManager;

    private boolean deleted;
    
    public ParticipantWrapper( Component participant, UnsavedChangeManager unsavedChangeManager ) {
        this.participant = participant;
        this.annotatedObjectHelper = new AnnotatedObjectHelper(participant);
        this.unsavedChangeManager = unsavedChangeManager;
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
                unsavedChangeManager.markToDelete(participant);
            } else {
                unsavedChangeManager.removeFromDeleted(participant);
            }
        }
    }
}
