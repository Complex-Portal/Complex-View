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
package uk.ac.ebi.intact.editor.controller.curate.interaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.hibernate.Hibernate;
import org.primefaces.model.DualListModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persister.IntactCore;
import uk.ac.ebi.intact.core.util.DebugUtil;
import uk.ac.ebi.intact.editor.controller.UserSessionController;
import uk.ac.ebi.intact.editor.controller.curate.ParameterizableObjectController;
import uk.ac.ebi.intact.editor.controller.curate.UnsavedChange;
import uk.ac.ebi.intact.editor.controller.curate.cloner.EditorIntactCloner;
import uk.ac.ebi.intact.editor.controller.curate.cloner.InteractionIntactCloner;
import uk.ac.ebi.intact.editor.controller.curate.experiment.ExperimentController;
import uk.ac.ebi.intact.editor.controller.curate.publication.PublicationController;
import uk.ac.ebi.intact.editor.controller.curate.util.IntactObjectComparator;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.clone.IntactCloner;
import uk.ac.ebi.intact.model.clone.IntactClonerException;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.model.util.IllegalLabelFormatException;
import uk.ac.ebi.intact.model.util.InteractionShortLabelGenerator;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.model.SelectItem;
import java.util.*;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "general" )
public class InteractionController extends ParameterizableObjectController {

    private static final Log log = LogFactory.getLog( InteractionController.class );

    private static final String FIG_LEGEND = "MI:0599";

    private Interaction interaction;
    private String ac;

    private DualListModel<String> experimentLists;
    private List<SelectItem> experimentSelectItems;

    private LinkedList<ParticipantWrapper> participantWrappers;

    private Experiment experiment;
    private List<Experiment> experimentsToUpdate;

    private String experimentToCopyTo;
    private String experimentToMoveTo;

    @Autowired
    private PublicationController publicationController;

    @Autowired
    private ExperimentController experimentController;

    @Autowired
    private UserSessionController userSessionController;

    public InteractionController() {
        experimentsToUpdate = new ArrayList<Experiment>();
    }

    @Override
    public AnnotatedObject getAnnotatedObject() {
        return getInteraction();
    }

    @Override
    public void setAnnotatedObject(AnnotatedObject annotatedObject) {
        setInteraction((Interaction)annotatedObject);
    }

    @Override
    public String goToParent() {
        return "/curate/experiment?faces-redirect=true&includeViewParams=true";
    }

    public DualListModel<String> getExperimentLists() {
        return experimentLists;
    }

    public void setExperimentLists( DualListModel<String> experimentLists ) {
        this.experimentLists = experimentLists;
    }

    @Override
    public String clone() {
        String value = clone(getAnnotatedObject(), newClonerInstance());

        refreshParticipants();
        refreshExperimentLists();
        refreshParentControllers();

        return value;
    }

    @Transactional(readOnly = true)
    public void loadData( ComponentSystemEvent event ) {
        if (!FacesContext.getCurrentInstance().isPostback()) {

            if ( ac != null ) {
                if ( interaction == null || !ac.equals( interaction.getAc() ) || !Hibernate.isInitialized(interaction.getExperiments())) {
                    interaction = loadByAc(IntactContext.getCurrentInstance().getDaoFactory().getInteractionDao(), ac);
                }
            } else {
                ac = interaction.getAc();
            }

            if (interaction == null) {
                addErrorMessage("No interaction with this AC", ac);
                return;
            }
            setInteraction(interaction);

            if ( interaction.getExperiments().isEmpty() ) {
                addErrorMessage( "This interaction isn't attached to an experiment", "Please add one or delete it" );
            } else {

                // check if the publication or experiment are null in their controllers (this happens when the interaction
                // page is loaded directly using a URL)
                refreshParentControllers();
            }

            refreshExperimentLists();

            if (interaction != null) {
                if (!Hibernate.isInitialized(interaction.getComponents())) {
                    interaction = IntactContext.getCurrentInstance().getDaoFactory().getInteractionDao().getByAc( ac );
                }
                refreshParticipants();
            }
        }

        generalLoadChecks();
    }

    private void refreshParentControllers() {
        Publication currentPublication = null;
        Experiment currentExperiment = null;

        // the interaction does not have any experiments
        if (!interaction.getExperiments().isEmpty()){
            currentExperiment = interaction.getExperiments().iterator().next();

            currentPublication = currentExperiment.getPublication();
        }

        // need to refresh experiment controller and publication controller for all these cases :
        // - first time we load an interaction, publication controller and experiment controller is null and need to be set
        // - the interaction does not have any publication and/or experiment and we set experiment and publication to null
        // - the interaction loaded is from a different experiment and/or publication than the previous interaction loaded. In this case, we refresh the parents
        if ( publicationController.getPublication() == null) {
            publicationController.setPublication( currentPublication );
        }
        else if (currentPublication == null){
            publicationController.setPublication( null );
        }
        else if (!publicationController.getPublication().getAc().equalsIgnoreCase(currentPublication.getAc())){
            publicationController.setPublication( currentPublication );
        }

        if ( experimentController.getExperiment() == null ) {
            experimentController.setExperiment( currentExperiment );
        }
        else if (currentExperiment == null){
            experimentController.setExperiment( null );
        }
        else if (!experimentController.getExperiment().getAc().equalsIgnoreCase(currentExperiment.getAc())){
            experimentController.setExperiment( currentExperiment );
        }
    }

    public void refreshExperimentLists() {
        this.experimentSelectItems = new ArrayList<SelectItem>();

        SelectItem selectItem = new SelectItem(null, "Select experiment");
        selectItem.setNoSelectionOption(true);

        experimentSelectItems.add(selectItem);

        if ( interaction.getExperiments().size() > 1 ) {
            addWarningMessage( "There are more than one experiment attached to this interaction",
                    DebugUtil.acList(interaction.getExperiments()).toString());
        }

        if (!interaction.getExperiments().isEmpty()) {
            experiment = interaction.getExperiments().iterator().next();

            // reset the publication in case the experiment was in a different publication
            if (experiment != null && experiment.getPublication() != null){
                publicationController.setPublication(experiment.getPublication());
            }
        }

        if (publicationController.getPublication() != null) {
            Publication pub = publicationController.getPublication();

            if (!IntactCore.isInitialized(pub.getExperiments())) {
                pub = getDaoFactory().getPublicationDao().getByAc(pub.getAc());
                publicationController.setPublication(pub);
            }

            // publication does have experiments so we can propose them
            if (!pub.getExperiments().isEmpty()){
                for ( Experiment e : pub.getExperiments() ) {
                    String description = completeExperimentLabel(e);
                    experimentSelectItems.add(new SelectItem(e, description, e.getFullName()));
                }
            }
        }
    }

    public String completeExperimentLabel(Experiment e) {
        return e.getShortLabel()+" | "+
                                (e.getCvInteraction() != null? e.getCvInteraction().getShortLabel()+", " : "")+
                                (e.getCvIdentification() != null? e.getCvIdentification().getShortLabel()+", " : "")+
                                (e.getBioSource() != null? e.getBioSource().getShortLabel() : "");
    }

    public void forceRefreshCurrentViewObject(){
        super.forceRefreshCurrentViewObject();

        if (interaction != null) {
            if (!Hibernate.isInitialized(interaction.getComponents())) {
                interaction = IntactContext.getCurrentInstance().getDaoFactory().getInteractionDao().getByAc( ac );
            }
            refreshExperimentLists();
            refreshParticipants();
        }
    }

    @Override
    public void doPreSave() {
        // create master proteins from the unsaved manager
        final List<UnsavedChange> transcriptCreated = super.getChangesController().getAllUnsavedProteinTranscripts();
        String currentAc = interaction != null ? interaction.getAc() : null;

        for (UnsavedChange unsaved : transcriptCreated) {
            IntactObject transcript = unsaved.getUnsavedObject();

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

        // Reload experiments
        Collection<Experiment> experiments = new ArrayList<Experiment>( interaction.getExperiments() );

        if (interaction.getExperiments().isEmpty() && experiment != null){
            experiments.add(experiment);
        }

        interaction.getExperiments().clear();

        for ( Experiment exp : experiments ) {
            Experiment reloaded = exp;

            // if experiment already exists, reload it
            if (exp.getAc() != null){
                reloaded = getDaoFactory().getExperimentDao().getByAc( exp.getAc() );
            }

            if (reloaded != null){
                interaction.getExperiments().add( reloaded );
            }
        }
    }

    public void markParticipantToDelete(Component component) {
        if (component == null) return;

        if (component.getAc() == null) {
            interaction.removeComponent(component);
            refreshParticipants();
        } else {
            getChangesController().markToDelete(component, component.getInteraction());
        }
    }

    @Override
    public boolean doSaveDetails() {
        boolean saved = true;

        // ensure that all the components are persisted (hack to fix Issue 652).
        // There seems to be components with null AC when the master protein of that
        // component needs to be saved
        for (ParticipantWrapper pw : participantWrappers) {
            Component component = pw.getParticipant();

            if (component.getAc() == null) {
                getCorePersister().saveOrUpdate(component);
            }
        }

        for (Experiment experimentToUpdate : experimentsToUpdate) {
            getCorePersister().saveOrUpdate(experimentToUpdate);
        }

        if (experiment != null) {
            experiment = reload(experiment);

            interaction.addExperiment(experiment);

            getDaoFactory().getExperimentDao().update(experiment);

            experimentController.setExperiment(experiment);
        }

        refreshParticipants();

        if (interaction.getAc() == null) {
            updateShortLabel();
        }

        return saved;
    }

    public void experimentChanged(AjaxBehaviorEvent evt) {

        Experiment newExp = reload(experiment);

        experimentController.setExperiment(newExp);

        Collection<Experiment> experiments = new ArrayList<Experiment>(interaction.getExperiments());
        for (Experiment exp : experiments){
            exp = reload(exp);
            interaction.removeExperiment(exp);
        }

        interaction.addExperiment(newExp);

        experimentsToUpdate.add(newExp);

        changed();
    }

    private Experiment reload(Experiment oldExp) {
        if (oldExp != null && oldExp.getAc() != null) {
            oldExp = getDaoFactory().getExperimentDao().getByAc(oldExp.getAc());
        }
        return oldExp;
    }

    @Transactional(readOnly = true)
    public String newInteraction(Publication publication, Experiment exp) {
        Interaction interaction = new InteractionImpl();
        interaction.setOwner(userSessionController.getUserInstitution());

        CvInteractorType type = getDaoFactory().getCvObjectDao(CvInteractorType.class).getByPsiMiRef(CvInteractorType.INTERACTION_MI_REF);
        interaction.setCvInteractorType(type);

        setInteraction(interaction);

        if (publication != null) {
            publicationController.setPublication(publication);
        }

        if (exp != null) {

            Experiment reloadedExp = exp;

            if (!IntactCore.isInitialized(exp.getInteractions())) {
                reloadedExp = getDaoFactory().getExperimentDao().getByAc(exp.getAc());
            }

            experimentController.setExperiment(reloadedExp);
            interaction.addExperiment(reloadedExp);
        }

        refreshExperimentLists();
        refreshParticipants();

        changed();

        return navigateToObject(interaction);
    }

    @Override
    public void modifyClone(AnnotatedObject clone) {
        Interaction clonedInteraction = (Interaction) clone;

        try {
            updateShortLabel(clonedInteraction);
        } catch (IllegalLabelFormatException e) {
            addErrorMessage("Couldn't auto-create label", e.getMessage());
        }
    }

    @Override
    protected IntactCloner newClonerInstance() {
        return new InteractionIntactCloner(reload(experiment));
    }

    public void refreshParticipants() {
        participantWrappers = new LinkedList<ParticipantWrapper>();

        final Collection<Component> components = interaction.getComponents();

        for ( Component component : components ) {
            participantWrappers.add( new ParticipantWrapper( component, getChangesController() ) );
        }
    }

    public void addParticipant(Component component) {
        interaction.addComponent(component);
        participantWrappers.addFirst(new ParticipantWrapper( component, getChangesController() ));

        if (participantWrappers.size() > 1) {
            try {
                updateShortLabel();
            } catch (Exception e) {
                addErrorMessage("Problem updating shortLabel", e.getMessage());
            }

        }

        setUnsavedChanges(true);
    }

    @Override
    public Collection<String> collectParentAcsOfCurrentAnnotatedObject(){
        Collection<String> parentAcs = new ArrayList<String>();

        if (IntactCore.isInitialized(interaction.getExperiments()) && !interaction.getExperiments().isEmpty()){
            for (Experiment exp : interaction.getExperiments()){
                addParentAcsTo(parentAcs, exp);
            }
        }
        else if (experiment != null){
            addParentAcsTo(parentAcs, experiment);
        }
        else if (!IntactCore.isInitialized(interaction.getExperiments())){
            Collection<Experiment> experiments = IntactCore.ensureInitializedExperiments(interaction);

            for (Experiment exp : experiments){
                addParentAcsTo(parentAcs, exp);
            }
        }

        return parentAcs;
    }

    @Override
    protected void refreshUnsavedChangesBeforeRevert(){
        Collection<String> parentAcs = new ArrayList<String>();

        if (IntactCore.isInitialized(interaction.getExperiments()) && !interaction.getExperiments().isEmpty()){
            for (Experiment exp : interaction.getExperiments()){
                addParentAcsTo(parentAcs, exp);
            }
        }
        else if (experiment != null){
            addParentAcsTo(parentAcs, experiment);
        }
        else if (!IntactCore.isInitialized(interaction.getExperiments())){
            Collection<Experiment> experiments = IntactCore.ensureInitializedExperiments(interaction);

            for (Experiment exp : experiments){
                addParentAcsTo(parentAcs, exp);
            }
        }

        getChangesController().revertInteraction(interaction, parentAcs);
    }

    public String copyToExperiment() {
        Interaction newInteraction = null;

        if (experimentToCopyTo != null && !experimentToCopyTo.isEmpty()) {
            Experiment experiment = findExperimentByAcOrLabel(experimentToCopyTo);

            if (experiment == null) {
                addErrorMessage("Cannot copy", "No experiment found with this AC or short label: "+experimentToMoveTo);
                return null;
            }

            newInteraction = cloneAnnotatedObject(interaction, newClonerInstance());

            // remove all experiments attached to the new interaction
            Collection<Experiment> experiments = new ArrayList(newInteraction.getExperiments());
            for (Experiment exp : experiments){
                newInteraction.removeExperiment(exp);
            }

            // add the new experiment
            newInteraction.addExperiment(experiment);

        } else {
            return null;
        }

        setInteraction(newInteraction);
        setUnsavedChanges(true);

        addInfoMessage("Copied interaction", "To experiment: "+experimentToCopyTo);

        return getCurateController().edit(newInteraction);
    }

    public String moveToExperiment() {
        if (experimentToMoveTo != null && !experimentToMoveTo.isEmpty()) {
            Experiment experiment = findExperimentByAcOrLabel(experimentToMoveTo);

            if (experiment == null) {
                addErrorMessage("Cannot move", "No experiment found with this AC or short label: "+experimentToMoveTo);
                return null;
            }

            // remove all experiments
            Collection<Experiment> experiments = new ArrayList(interaction.getExperiments());
            for (Experiment exp : experiments){
                interaction.removeExperiment(exp);
            }

            // add new experiment
            interaction.addExperiment(experiment);

        } else {
            return null;
        }

        setInteraction(interaction);

        refreshExperimentLists();

        setUnsavedChanges(true);

        addInfoMessage("Moved interaction", "To experiment: "+experimentToMoveTo);

        return null;
    }

    private Experiment findExperimentByAcOrLabel(String acOrLabel) {
        Experiment experiment = getDaoFactory().getExperimentDao().getByAc(acOrLabel.trim());

        if (experiment == null) {
            experiment = getDaoFactory().getExperimentDao().getByShortLabel(acOrLabel);
        }
        return experiment;
    }

    public String getAc() {
        if ( ac == null && interaction != null ) {
            return interaction.getAc();
        }
        return ac;
    }

    public int countParticipantsByInteractionAc( String ac ) {
        return getDaoFactory().getInteractionDao().countInteractorsByInteractionAc( ac );
    }

    public int countParticipantsByInteraction( Interaction interaction) {
        if (interaction.getAc() != null) return countParticipantsByInteractionAc(interaction.getAc());

        return interaction.getComponents().size();
    }

    public Experiment getFirstExperiment( Interaction interaction ) {
        if (interaction.getExperiments().isEmpty()){
            return null;
        }
        return interaction.getExperiments().iterator().next();
    }

    @Override
    public void doRevertChanges(ActionEvent evt) {
        super.doRevertChanges(evt);

        /*for (ParticipantWrapper wrapper : participantWrappers) {
            revertParticipant(wrapper);
        }*/
    }

    /*public void deleteParticipant(ParticipantWrapper participantWrapper) {
        participantWrapper.setDeleted(true);

        Component participant = participantWrapper.getParticipant();
        //interaction.removeComponent(participant);
        //refreshParticipants();
        setUnsavedChanges(true);

        StringBuilder participantInfo = new StringBuilder();

        if (participant.getInteractor() != null) {
            participantInfo.append(participant.getInteractor().getShortLabel());
            participantInfo.append(" ");
        }

        if (participant.getAc() != null) {
            participantInfo.append("(").append(participant.getAc()+")");
        }

        updateShortLabel();

        addInfoMessage("Participant marked to be removed.", participantInfo.toString());
    }*/

    public void updateShortLabel() {
        try {
            updateShortLabel(getInteraction());
        } catch (IllegalLabelFormatException e) {
            addErrorMessage("Couldn't auto-create label", e.getMessage());
        }
    }

    private void updateShortLabel(Interaction interaction) throws IllegalLabelFormatException {
        if (participantWrappers.isEmpty()) {
            return;
        }

        //Issue 208: Interaction short-labels will be handled manually when in complexes
        if (belongsToCuratedComplex()) {
            return;
        }

        String shortLabel = InteractionShortLabelGenerator.createCandidateShortLabel(interaction);
        shortLabel = InteractionShortLabelGenerator.nextAvailableShortlabel(shortLabel);

        interaction.setShortLabel(shortLabel);
    }

    private boolean belongsToCuratedComplex() {
        for (Experiment exp : interaction.getExperiments()) {

            // to avoid lazy initialization of annotations in the experiments, checks if it is initialized, if not reload them
            Collection<Annotation> annotations = IntactCore.ensureInitializedAnnotations(exp);

            for (Annotation annot : annotations) {
                if (CvTopic.CURATED_COMPLEX.equals(annot.getCvTopic().getShortLabel())) {
                    return true;
                }
            }
        }

        return false;
    }

    /*public void revertParticipant(ParticipantWrapper participantWrapper) {
        participantWrapper.setDeleted(false);

        Component participant = participantWrapper.getParticipant();
        setUnsavedChanges(false);

        StringBuilder participantInfo = new StringBuilder();

        if (participant.getInteractor() != null) {
            participantInfo.append(participant.getInteractor().getShortLabel());
            participantInfo.append(" ");
        }

        if (participant.getAc() != null) {
            participantInfo.append("(").append(participant.getAc()).append(")");
        }
    }*/

    /**
     * When reverting, we need to refresh the collection of wrappers because they are not part of the IntAct model.
     */
    @Override
    protected void postRevert() {
        refreshExperimentLists();
        refreshParticipants();
        refreshParentControllers();
    }

    public void cloneParticipant(ParticipantWrapper participantWrapper) {
        Component participant = participantWrapper.getParticipant();

        IntactCloner cloner = new EditorIntactCloner();

        try {
            Component clone = cloner.clone(participant);
            addParticipant(clone);
        } catch (IntactClonerException e) {
            addErrorMessage("Problem cloning participant", e.getMessage());
            handleException(e);
        }
    }

    public void linkSelectedFeatures(ActionEvent evt) {
        List<Feature> selected = new ArrayList<Feature>();

        for (ParticipantWrapper pw : participantWrappers) {
            for (FeatureWrapper fw : pw.getFeatures()) {
                if (fw.isSelected()) {
                    selected.add(fw.getFeature());
                }
            }
        }

        System.out.println("Selected "+selected.size()+": "+selected);

        if (selected.size() != 2) {
            addErrorMessage("Incorrect feature selection", "Two features need to be selected if you want to link them. Currently selected: "+selected.size());
            return;
        }

        Feature featureToLink1 = selected.get(0);
        Feature featureToLink2 = selected.get(1);

        // clean any existing linked association, just in case the curator links an already linked feature with another
        // binding domain
        clearBoundDomain(featureToLink1);
        clearBoundDomain(featureToLink2);

        // link the features
        featureToLink1.setBoundDomain(featureToLink2);
        featureToLink2.setBoundDomain(featureToLink1);


        addInfoMessage("Features linked", DebugUtil.intactObjectToString(featureToLink1, false)+" and "+DebugUtil.intactObjectToString(featureToLink2, false));
        setUnsavedChanges(true);

    }

    public void unlinkFeature(Feature feature) {
       clearBoundDomain(feature);

        addInfoMessage("Feature unlinked", DebugUtil.intactObjectToString(feature, false));
        setUnsavedChanges(true);
    }

    private void clearBoundDomain(Feature feature) {
        if (feature.getBoundDomain() != null) {
            feature.getBoundDomain().setBoundDomain(null);
        }

        feature.setBoundDomain(null);
    }

    public String getImexId() {
        return findXrefPrimaryId(CvDatabase.IMEX_MI_REF, CvXrefQualifier.IMEX_PRIMARY_MI_REF);
    }

    public void setImexId(String imexId) {
        setXref(CvDatabase.IMEX_MI_REF, CvXrefQualifier.IMEX_PRIMARY_MI_REF, imexId);
    }

    public void setAc( String ac ) {
        this.ac = ac;
    }

    public List<SelectItem> getExperimentSelectItems() {
        return experimentSelectItems;
    }

    public Interaction getInteraction() {
        return interaction;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experiment) {
        if (this.experiment == null){
            this.experiment = experiment;
            experimentController.setExperiment(experiment);
        }
        else if (!this.experiment.equals(experiment)) {
            this.experiment = experiment;

            experimentController.setExperiment(experiment);
        }
    }

    public void setInteraction( Interaction interaction ) {
        this.interaction = interaction;

        if (interaction != null) {
            this.ac = interaction.getAc();

            if ( Hibernate.isInitialized(interaction.getExperiments()) && !interaction.getExperiments().isEmpty()) {
                experimentController.setExperiment(interaction.getExperiments().iterator().next());
            }
        }
    }

    public Collection<ParticipantWrapper> getParticipants() {
        return participantWrappers;
    }

    public String getExperimentToMoveTo() {
        return experimentToMoveTo;
    }

    public void setExperimentToMoveTo(String experimentToMoveTo) {
        this.experimentToMoveTo = experimentToMoveTo;
    }

    public String getExperimentToCopyTo() {
        return experimentToCopyTo;
    }

    public void setExperimentToCopyTo(String experimentToCopyTo) {
        this.experimentToCopyTo = experimentToCopyTo;
    }

     public String getFigureLegend() {
        return findAnnotationText(FIG_LEGEND);
    }

    public void setFigureLegend(String figureLegend) {
        setAnnotation(FIG_LEGEND, figureLegend);
    }

    //////////////////////////////////
    // Participant related methods

    public String getInteractorIdentity(Interactor interactor) {
        if (interactor == null) return null;

        final Collection<InteractorXref> identities =
                AnnotatedObjectUtils.searchXrefsByQualifier( interactor, CvXrefQualifier.IDENTITY_MI_REF );
        StringBuilder sb = new StringBuilder(64);
        for ( Iterator<InteractorXref> iterator = identities.iterator(); iterator.hasNext(); ) {
            InteractorXref xref = iterator.next();
            sb.append( xref.getPrimaryId() );
            if( iterator.hasNext() ) {
                sb.append( "|" );
            }
        }
        return sb.toString();
    }

    public boolean isFeaturesAvailable(){
        boolean featuresAvailable = false;
        Interaction interaction = getInteraction();
        for(Component component : interaction.getComponents()){
            featuresAvailable = featuresAvailable || (component.getBindingDomains().size() > 0);
            if(featuresAvailable){
                continue;
            }
        }
        return featuresAvailable;
    }

    // Confidence
    ///////////////////////////////////////////////

    public void newConfidence() {
        Confidence confidence = new Confidence();
        interaction.addConfidence(confidence);
    }

    public List<Confidence> getConfidences() {
        if (interaction == null) return Collections.EMPTY_LIST;

        final List<Confidence> confidences = new ArrayList<Confidence>( interaction.getConfidences() );
        Collections.sort( confidences, new IntactObjectComparator() );
        return confidences;
    }
}