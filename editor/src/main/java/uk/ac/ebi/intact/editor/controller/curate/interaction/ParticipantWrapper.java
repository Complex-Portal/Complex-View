package uk.ac.ebi.intact.editor.controller.curate.interaction;

import uk.ac.ebi.intact.editor.controller.curate.AnnotatedObjectController;
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

    private AnnotatedObjectController controller;

    public ParticipantWrapper( Component participant, AnnotatedObjectController controller ) {
        this.participant = participant;
        this.controller = controller;
    }

    public Component getParticipant() {
        return participant;
    }

    public void setParticipant( Component participant ) {
        this.participant = participant;
    }

    public String getAuthorGivenName() {
        return controller.findAliasName( CvAliasType.AUTHOR_ASSIGNED_NAME_MI_REF );
    }

    public void setAuthorGivenName( String name ) {
        System.out.println( "ParticipantWrapper.setAuthorGivenName" );
        controller.addOrReplace(CvAliasType.AUTHOR_ASSIGNED_NAME_MI_REF, name  );
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
}
