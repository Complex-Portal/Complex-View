package uk.ac.ebi.intact.editor.controller.curate.interaction;

import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persister.IntactCore;
import uk.ac.ebi.intact.editor.controller.curate.AnnotatedObjectHelper;
import uk.ac.ebi.intact.editor.controller.curate.ChangesController;
import uk.ac.ebi.intact.model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    private List<FeatureWrapper> features;

    private CvBiologicalRole biologicalRole;
    private CvExperimentalRole experimentalRole;
    private BioSource expressedIn;
    private InteractionController interactionController;

    private boolean deleted;

    public ParticipantWrapper( Component participant, ChangesController changesController, InteractionController interactionController ) {
        this.participant = participant;
        this.annotatedObjectHelper = newAnnotatedObjectHelper(participant);
        this.changesController = changesController;

        features = new ArrayList<FeatureWrapper>(participant.getFeatures().size());

        for (Feature feature : participant.getFeatures()) {
            features.add(new FeatureWrapper(feature));
        }

        this.interactionController = interactionController;
        this.expressedIn = participant.getExpressedIn();
        this.biologicalRole = participant.getCvBiologicalRole();
        this.experimentalRole = getFirstExperimentalRole();
    }

    public Component getParticipant() {
        return participant;
    }

    public void setParticipant( Component participant ) {
        if (participant != null){
            this.participant = participant;
        }
    }

    public String getAuthorGivenName() {
        return annotatedObjectHelper.findAliasName( CvAliasType.AUTHOR_ASSIGNED_NAME_MI_REF );
    }

    public void setAuthorGivenName( String name ) {
        String author = annotatedObjectHelper.findAliasName(CvAliasType.AUTHOR_ASSIGNED_NAME_MI_REF);
        if ((name == null && author != null) || (name != null && author == null)){
            changesController.markAsUnsaved(participant);
            annotatedObjectHelper.setAlias(CvAliasType.AUTHOR_ASSIGNED_NAME_MI_REF, name  );
        }
        else if (name != null && !name.equalsIgnoreCase(author)){
            changesController.markAsUnsaved(participant);
            annotatedObjectHelper.setAlias(CvAliasType.AUTHOR_ASSIGNED_NAME_MI_REF, name  );
        }
    }

    public CvExperimentalRole getFirstExperimentalRole() {
        if( ! participant.getExperimentalRoles().isEmpty() ) {
            return participant.getExperimentalRoles().iterator().next();
        }
        return null;
    }

    public void setFirstExperimentalRole(CvExperimentalRole role) {
        if( ! participant.getExperimentalRoles().contains(role) && role != null) {
            participant.getExperimentalRoles().clear();
            participant.getExperimentalRoles().add( role );
        }
    }

    public CvExperimentalPreparation getFirstExperimentalPreparation() {
        if( participant.getInteractor() != null ) {
            final Collection<CvExperimentalPreparation> expPreparations = IntactCore.ensureInitializedExperimentalPreparations(participant);
            if( expPreparations != null && ! expPreparations.isEmpty() ) {
                return expPreparations.iterator().next();
            }
        }

        return null;
    }

    public void setFirstExperimentalPreparation( CvExperimentalPreparation prep ) {
        if (participant.getExperimentalPreparations() == null){
            participant.setExperimentalPreparations(new ArrayList<CvExperimentalPreparation>());
        }

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

    public List<FeatureWrapper> getFeatures() {
        return features;
    }

    private AnnotatedObjectHelper newAnnotatedObjectHelper(AnnotatedObject annotatedObject) {
        AnnotatedObjectHelper helper = (AnnotatedObjectHelper) IntactContext.getCurrentInstance().getSpringContext().getBean("annotatedObjectHelper");
        helper.setAnnotatedObject(annotatedObject);

        return helper;
    }

    public void onExperimentalRoleChanged() {

        if (experimentalRole != null){
            participant.getExperimentalRoles().clear();
            participant.addExperimentalRole(experimentalRole);

            interactionController.updateShortLabel();
        }
    }

    public void onBiologicalRoleChanged() {

        if (biologicalRole != null){
            participant.setCvBiologicalRole(biologicalRole);
        }
    }

    public void onExpressedInChanged() {

        participant.setExpressedIn(expressedIn);
    }

    public CvBiologicalRole getBiologicalRole() {

        // hack because of bug primefaces selectOneMenu in tabView which submits null when changing tab.
        if (biologicalRole == null){
            biologicalRole = participant.getCvBiologicalRole();
        }
        return biologicalRole;
    }

    public void setBiologicalRole(CvBiologicalRole biologicalRole) {
        this.biologicalRole = biologicalRole;
    }

    public CvExperimentalRole getExperimentalRole() {
        // hack because of bug primefaces selectOneMenu in tabView which submits null when changing tab.
        if (experimentalRole == null){
            experimentalRole = getFirstExperimentalRole();
        }

        return experimentalRole;
    }

    public void setExperimentalRole(CvExperimentalRole experimentalRole) {
        this.experimentalRole = experimentalRole;
    }

    public BioSource getExpressedIn() {
        // hack because of bug primefaces selectOneMenu in tabView which submits null when changing tab.
        if (expressedIn == null && participant.getExpressedIn() != null){
             expressedIn = participant.getExpressedIn();
        }
        return expressedIn;
    }

    public void setExpressedIn(BioSource expressedIn) {
        this.expressedIn = expressedIn;
    }
}
