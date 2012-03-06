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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.primefaces.model.SelectableDataModelWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persister.IntactCore;
import uk.ac.ebi.intact.editor.controller.curate.ParameterizableObjectController;
import uk.ac.ebi.intact.editor.controller.curate.UnsavedChange;
import uk.ac.ebi.intact.editor.controller.curate.cloner.ParticipantIntactCloner;
import uk.ac.ebi.intact.editor.controller.curate.experiment.ExperimentController;
import uk.ac.ebi.intact.editor.controller.curate.interaction.ImportCandidate;
import uk.ac.ebi.intact.editor.controller.curate.interaction.InteractionController;
import uk.ac.ebi.intact.editor.controller.curate.interaction.ParticipantImportController;
import uk.ac.ebi.intact.editor.controller.curate.interaction.ParticipantWrapper;
import uk.ac.ebi.intact.editor.controller.curate.publication.PublicationController;
import uk.ac.ebi.intact.editor.controller.curate.util.IntactObjectComparator;
import uk.ac.ebi.intact.editor.util.SelectableCollectionDataModel;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.clone.IntactCloner;
import uk.ac.ebi.intact.model.util.XrefUtils;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.model.DataModel;
import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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

    private DataModel<Feature> featuresDataModel;
    private Feature[] selectedFeatures;
    private Feature featureToLink1;
    private Feature featureToLink2;
    private Range rangeFeatureLinked1;
    private Range rangeFeatureLinked2;
    private List<SelectItem> featureToLink1RangeSelectItems;
    private List<SelectItem> featureToLink2RangeSelectItems;

    private CvExperimentalRole unspecifiedExperimentalRole;
    private CvBiologicalRole unspecifiedBiologicalRole;

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

    @PostConstruct
    public void initializeDefaultRoles(){
        refresh(null);
    }

    @Transactional(propagation = Propagation.NEVER)
    public synchronized void refresh( ActionEvent evt ) {
        if ( log.isDebugEnabled() ) log.debug( "Loading participant roles" );

        final TransactionStatus transactionStatus = IntactContext.getCurrentInstance().getDataContext().beginTransaction();

        unspecifiedExperimentalRole = getDaoFactory().getCvObjectDao(CvExperimentalRole.class).getByIdentifier(CvExperimentalRole.UNSPECIFIED_PSI_REF);
        unspecifiedBiologicalRole = getDaoFactory().getCvObjectDao(CvBiologicalRole.class).getByIdentifier(CvBiologicalRole.UNSPECIFIED_PSI_REF);

        IntactContext.getCurrentInstance().getDataContext().commitTransaction( transactionStatus );
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
                addErrorMessage("No participant with this AC", ac);
                return;
            }
            
            featuresDataModel = new SelectableDataModelWrapper(new SelectableCollectionDataModel<Feature>(participant.getFeatures()), participant.getFeatures());

            // check if the publication, experiment and interaction are null in their controllers (this happens when the
            // participant page is loaded directly using a URL)

            if (participant.getInteraction() != null){
                Collection<Experiment> experiments = participant.getInteraction().getExperiments();

                if (!IntactCore.isInitialized(experiments)){
                    experiments = getDaoFactory().getExperimentDao().getByInteractionAc(participant.getInteraction().getAc());
                }

                if( experiments.isEmpty()) {
                    addWarningMessage( "The parent interaction of this participant isn't attached to an experiment",
                            "Abort experiment loading." );
                    return;
                }
                else{
                    if ( publicationController.getPublication() == null ) {
                        Publication publication = experiments.iterator().next().getPublication();
                        publicationController.setPublication( publication );
                    }
                    if ( experimentController.getExperiment() == null ) {
                        experimentController.setExperiment( experiments.iterator().next() );
                    }
                }
            }

            if( interactionController.getInteraction() == null ) {
                interactionController.setInteraction( participant.getInteraction() );
            }

            if (participant.getInteractor() != null) {
                interactor = participant.getInteractor().getShortLabel();
            }
        }

        refresh(null);
        generalLoadChecks();
    }

    @Override
    public boolean doSaveDetails(){
       refresh(null);

        return super.doSaveDetails();
    }

    @Override
    public void doPreSave() {
        // create master proteins from the unsaved manager
        final List<UnsavedChange> transcriptCreated = getChangesController().getAllUnsavedProteinTranscripts();

        for (UnsavedChange unsaved : transcriptCreated) {
            IntactObject transcript = unsaved.getUnsavedObject();

            String currentAc = participant != null ? participant.getAc() : null;

            // the object to save is different from the current object. Checks that the scope of this object to save is the ac of the current object being saved
            // if the scope is null or different, the object should not be saved at this stage because we only save the current object and changes associated with it
            // if current ac is null, no unsaved event should be associated with it as this object has not been saved yet
            if (unsaved.getScope() != null && unsaved.getScope().equals(currentAc)){
                getPersistenceController().doSaveMasterProteins(transcript);
                getChangesController().removeFromHiddenChanges(unsaved);
            }
            else if (unsaved.getScope() == null && currentAc == null){
                getPersistenceController().doSaveMasterProteins(transcript);
                getChangesController().removeFromHiddenChanges(unsaved);
            }
        }

        // save the interactor, if it didn't exist and the participant is just being updated
        if (participant.getAc() != null && participant.getInteractor().getAc() == null) {
            getCorePersister().saveOrUpdate(participant.getInteractor());
        }

        // the participant is not persisted, we can add it to the list of components in the interaction
        if (participant.getAc() == null){
            interactionController.getInteraction().addComponent(participant);
        }
    }

    @Override
    public Collection<String> collectParentAcsOfCurrentAnnotatedObject(){
        Collection<String> parentAcs = new ArrayList<String>();

        if (participant.getInteraction() != null){
            addParentAcsTo(parentAcs, participant.getInteraction());
        }

        return parentAcs;
    }

    @Override
    protected void refreshUnsavedChangesBeforeRevert(){
        Collection<String> parentAcs = new ArrayList<String>();

        if (participant.getInteraction() != null){
            addParentAcsTo(parentAcs, participant.getInteraction());
        }

        getChangesController().revertComponent(participant, parentAcs);
    }


    /**
     * Add all the parent acs of this interaction
     * @param parentAcs
     * @param inter
     */
    protected void addParentAcsTo(Collection<String> parentAcs, Interaction inter) {
        if (inter.getAc() != null){
            parentAcs.add(inter.getAc());
        }

        if (IntactCore.isInitialized(inter.getExperiments()) && !inter.getExperiments().isEmpty()){
            for (Experiment exp : inter.getExperiments()){
                addParentAcsTo(parentAcs, exp);
            }
        }
        else if (interactionController.getExperiment() != null){
            Experiment exp = interactionController.getExperiment();
            addParentAcsTo(parentAcs, exp);
        }
        else if (!IntactCore.isInitialized(inter.getExperiments())){
            Collection<Experiment> experiments = IntactCore.ensureInitializedExperiments(inter);

            for (Experiment exp : experiments){
                addParentAcsTo(parentAcs, exp);
            }
        }
    }

    @Override
    public void doPostSave(){
        interactionController.refreshParticipants();
    }

    @Override
    protected IntactCloner newClonerInstance() {
        return new ParticipantIntactCloner();
    }

    public String newParticipant(Interaction interaction) {
        this.interactor = null;

        Component participant = new Component("N/A", interaction, new InteractorImpl(), unspecifiedExperimentalRole, unspecifiedBiologicalRole);
        participant.setInteractor(null);
        participant.setStoichiometry(getEditorConfig().getDefaultStoichiometry());

        // by setting the interaction of a participant, we don't add the participant to the collection of participants for this interaction so if we revert, it will not affect anything.
        // when saving, it will be added to the list of participants for this interaction. we just have to refresh the list of participants
        participant.setInteraction(interaction);

        setParticipant(participant);

        //interaction.addComponent(participant);

        //getUnsavedChangeManager().markAsUnsaved(participant);
        changed();

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
                // chain or isoform, we may have to update it later
                if (importCandidate.isChain() || importCandidate.isIsoform()){
                    Collection<String> parentAcs = new ArrayList<String>();

                    if (participant.getInteraction() != null){
                        addParentAcsTo(parentAcs, participant.getInteraction());
                    }

                    getChangesController().markAsHiddenChange(importCandidate.getInteractor(), participant, parentAcs);
                }
                participant.setInteractor(importCandidate.getInteractor());

                // if the participant is a new participant, we don't need to add a unsaved notice because one already exists for creating a new participant
                if (participant.getAc() != null){
                    setUnsavedChanges(true);
                }
            }
        }
    }

    @Override
    public String doDelete(){
        if (!participant.getFeatures().isEmpty()){
            for (Feature f : participant.getFeatures()){
                if (f.getBoundDomain() != null){
                    Feature bound = f.getBoundDomain();

                    if (bound.getBoundDomain() != null && f.getAc() != null && f.getAc().equalsIgnoreCase(bound.getBoundDomain().getAc())){
                        bound.setBoundDomain(null);
                        getPersistenceController().doSave(bound);
                    }
                    else if (bound.getBoundDomain() != null && f.getAc() == null && f.equals(bound.getBoundDomain())){
                        bound.setBoundDomain(null);
                        getPersistenceController().doSave(bound);
                    }

                    f.setBoundDomain(null);
                }
            }
        }

        return super.doDelete();
    }

    public void markFeatureToDelete(Feature feature) {

        // don't forget to unlink features first
        if (feature.getAc() == null) {
            if (feature.getBoundDomain() != null){
                Feature bound = feature.getBoundDomain();

                if (bound.getBoundDomain() != null && bound.getBoundDomain().equals(feature)){
                    bound.setBoundDomain(null);
                    getChangesController().markAsUnsaved(bound);
                }
                feature.setBoundDomain(null);
            }
            participant.removeFeature(feature);
        } else {
            if (feature.getBoundDomain() != null){
                Feature bound = feature.getBoundDomain();

                if (bound.getBoundDomain() != null && feature.getAc().equalsIgnoreCase(bound.getBoundDomain().getAc())){
                    bound.setBoundDomain(null);
                    getChangesController().markAsUnsaved(bound);
                }
                feature.setBoundDomain(null);
            }

            getChangesController().markToDelete(feature, feature.getComponent());
        }
    }

    public void deleteSelectedFeatures(ActionEvent evt) {
        for (Feature feature : selectedFeatures) {
            markFeatureToDelete(feature);
        }

        addInfoMessage("Features to be deleted", selectedFeatures.length+" have been marked to be deleted");
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
    
    public String participantPrimaryId(Component component) {
        if (component == null) return null;
        if (component.getInteractor() == null) return null;

        final Collection<InteractorXref> xrefs = XrefUtils.getIdentityXrefs(component.getInteractor());

        if (xrefs.isEmpty()) {
            return component.getInteractor().getAc();
        }

        return joinIds(xrefs);
    }

    private String joinIds(Collection<InteractorXref> xrefs) {
        Collection<String> ids = new ArrayList<String>(xrefs.size());

        for (InteractorXref xref : xrefs) {
            ids.add(xref.getPrimaryId());
        }

        return StringUtils.join(ids, ", ");
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

    public Feature[] getSelectedFeatures() {
        return selectedFeatures;
    }

    public void setSelectedFeatures(Feature[] selectedFeatures) {
        this.selectedFeatures = selectedFeatures;
    }

    public Feature getFeatureToLink1() {
        return featureToLink1;
    }

    public void setFeatureToLink1(Feature featureToLink1) {
        this.featureToLink1 = featureToLink1;
    }

    public Feature getFeatureToLink2() {
        return featureToLink2;
    }

    public void setFeatureToLink2(Feature featureToLink2) {
        this.featureToLink2 = featureToLink2;
    }

    public Range getRangeFeatureLinked1() {
        return rangeFeatureLinked1;
    }

    public void setRangeFeatureLinked1(Range rangeFeatureLinked1) {
        this.rangeFeatureLinked1 = rangeFeatureLinked1;
    }

    public Range getRangeFeatureLinked2() {
        return rangeFeatureLinked2;
    }

    public void setRangeFeatureLinked2(Range rangeFeatureLinked2) {
        this.rangeFeatureLinked2 = rangeFeatureLinked2;
    }

    public List<SelectItem> getFeatureToLink1RangeSelectItems() {
        return featureToLink1RangeSelectItems;
    }

    public void setFeatureToLink1RangeSelectItems(List<SelectItem> featureToLink1RangeSelectItems) {
        this.featureToLink1RangeSelectItems = featureToLink1RangeSelectItems;
    }

    public List<SelectItem> getFeatureToLink2RangeSelectItems() {
        return featureToLink2RangeSelectItems;
    }

    public void setFeatureToLink2RangeSelectItems(List<SelectItem> featureToLink2RangeSelectItems) {
        this.featureToLink2RangeSelectItems = featureToLink2RangeSelectItems;
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

    public DataModel<Feature> getFeaturesDataModel() {
        return featuresDataModel;
    }
}