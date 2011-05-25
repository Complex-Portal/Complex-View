/**
 * Copyright 2010 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.editor.controller.curate.participant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.controller.curate.ParameterizableObjectController;
import uk.ac.ebi.intact.editor.controller.curate.cloner.ParticipantIntactCloner;
import uk.ac.ebi.intact.editor.controller.curate.experiment.ExperimentController;
import uk.ac.ebi.intact.editor.controller.curate.interaction.ImportCandidate;
import uk.ac.ebi.intact.editor.controller.curate.interaction.InteractionController;
import uk.ac.ebi.intact.editor.controller.curate.interaction.ParticipantImportController;
import uk.ac.ebi.intact.editor.controller.curate.interaction.ParticipantWrapper;
import uk.ac.ebi.intact.editor.controller.curate.publication.PublicationController;
import uk.ac.ebi.intact.editor.controller.curate.util.IntactObjectComparator;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.clone.IntactCloner;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;
import java.util.*;

/**
 * Participant controller.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "general" )
public class ParticipantController extends ParameterizableObjectController {

    private static final Log log = LogFactory.getLog( ParticipantController.class );

    private Component participant;

    private String interactor;
    private List<ImportCandidate> interactorCandidates;

    /**
     * The AC of the participant to be loaded.
     */
    private String ac;

    @Autowired
    private PublicationController publicationController;

    @Autowired
    private ExperimentController experimentController;

    @Autowired
    private InteractionController interactionController;

    public ParticipantController() {
    }

    @Override
    public AnnotatedObject getAnnotatedObject() {
        return getParticipant();
    }

    @Override
    public void setAnnotatedObject(AnnotatedObject annotatedObject) {
        setParticipant((Component)annotatedObject);
    }

    @Override
    public String goToParent() {
        return "/curate/interaction?faces-redirect=true&includeViewParams=true";
    }

    public void loadData( ComponentSystemEvent event ) {
        if (!FacesContext.getCurrentInstance().isPostback()) {
            if ( ac != null ) {
                if ( participant == null || !ac.equals( participant.getAc() ) ) {
                    participant = loadByAc(IntactContext.getCurrentInstance().getDaoFactory().getComponentDao(), ac);
                }
            } else {
                if ( participant != null ) ac = participant.getAc();
            }

            if (participant == null) {
                addErrorMessage("No participant loaded", "That's annoying!");
                return;
            }

            // check if the publication, experiment and interaction are null in their controllers (this happens when the
            // participant page is loaded directly using a URL)

            if( participant.getInteraction().getExperiments().isEmpty()) {
                addWarningMessage( "The parent interaction of this participant isn't attached to an experiment",
                        "Abort experiment loading." );
                return;
            }

            if ( publicationController.getPublication() == null ) {
                Publication publication = participant.getInteraction().getExperiments().iterator().next().getPublication();
                publicationController.setPublication( publication );
            }

            if ( experimentController.getExperiment() == null ) {
                experimentController.setExperiment( participant.getInteraction().getExperiments().iterator().next() );
            }

            if( interactionController.getInteraction() == null ) {
                interactionController.setInteraction( participant.getInteraction() );
            }

            if (participant.getInteractor() != null) {
                interactor = participant.getInteractor().getShortLabel();
                System.out.println("Interactor: "+interactor);
            }
        }

        generalLoadChecks();
    }

    @Override
    public void doPreSave() {
        // save the interactor, if it didn't exist and the participant is just being updated
        if (participant.getAc() != null && participant.getInteractor().getAc() == null) {
            getCorePersister().saveOrUpdate(participant.getInteractor());
        }

        interactionController.getInteraction().addComponent(participant);
        interactionController.refreshParticipants();
    }

    @Override
    protected IntactCloner newClonerInstance() {
        return new ParticipantIntactCloner();
    }

    public String newParticipant(Interaction interaction) {
        Component participant = new Component("N/A", interaction, new InteractorImpl(), new CvExperimentalRole(), new CvBiologicalRole());
        participant.setInteractor(null);
        participant.getExperimentalRoles().clear();
        participant.setCvBiologicalRole(null);
        participant.setStoichiometry(getEditorConfig().getDefaultStoichiometry());

        setParticipant(participant);

        //interaction.addComponent(participant);

        //getUnsavedChangeManager().markAsUnsaved(participant);

        return navigateToObject(participant);
    }

    public void importInteractor(ActionEvent evt) {
        ParticipantImportController participantImportController = (ParticipantImportController) getSpringContext().getBean("participantImportController");
        interactorCandidates = new ArrayList<ImportCandidate>(participantImportController.importParticipant(interactor));

        if (interactorCandidates.size() == 1) {
            interactorCandidates.get(0).setSelected(true);
        }
    }

    public void addInteractorToParticipant(ActionEvent evt) {
        for (ImportCandidate importCandidate : interactorCandidates) {
            if (importCandidate.isSelected()) {
                if (importCandidate.isChain() || importCandidate.isIsoform()){
                    getChangesController().markToCreatedTranscriptWithoutMaster(importCandidate.getInteractor());
                }
                participant.setInteractor(importCandidate.getInteractor());
                setUnsavedChanges(true);
            }
        }
    }

    public void markFeatureToDelete(Feature feature) {
        if (feature.getAc() == null) {
            participant.removeFeature(feature);
        } else {
            getChangesController().markToDelete(feature, feature.getComponent());
        }
    }

    public String getAc() {
        if ( ac == null && participant != null ) {
            return participant.getAc();
        }
        return ac;
    }

    public void setAc( String ac ) {
        this.ac = ac;
    }

    public Component getParticipant() {
        return participant;
    }

    public ParticipantWrapper getParticipantWrapper() {
        return new ParticipantWrapper( participant, getChangesController() );
    }

    public void setParticipant( Component participant ) {
        this.participant = participant;

        if (participant != null){
            this.ac = participant.getAc();
        }
    }

    public String getAuthorGivenName() {
        return findAliasName( CvAliasType.AUTHOR_ASSIGNED_NAME_MI_REF );
    }

    public void setAuthorGivenName( String name ) {
        addOrReplace(CvAliasType.AUTHOR_ASSIGNED_NAME_MI_REF, name  );
    }

    public CvExperimentalPreparation getFirstExperimentalPreparation( Component participant ) {
        if( participant.getInteractor() != null ) {
            if( ! participant.getExperimentalPreparations().isEmpty() ) {
                return participant.getExperimentalPreparations().iterator().next();
            }
        }

        return null;
    }

    public String getInteractor() {
        return interactor;
    }

    public void setInteractor(String interactor) {
        this.interactor = interactor;
    }

    public List<ImportCandidate> getInteractorCandidates() {
        return interactorCandidates;
    }

    public void setInteractorCandidates(List<ImportCandidate> interactorCandidates) {
        this.interactorCandidates = interactorCandidates;
    }

    // Confidence
    ///////////////////////////////////////////////

    public void newConfidence() {
        ComponentConfidence confidence = new ComponentConfidence();
        participant.addConfidence(confidence);
    }

    public List<ComponentConfidence> getConfidences() {
        if (participant == null) return Collections.EMPTY_LIST;

        final List<ComponentConfidence> confidences = new ArrayList<ComponentConfidence>( participant.getConfidences() );
        Collections.sort( confidences, new IntactObjectComparator() );
        return confidences;
    }
}