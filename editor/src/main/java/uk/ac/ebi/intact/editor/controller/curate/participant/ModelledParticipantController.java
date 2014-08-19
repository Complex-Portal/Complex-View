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
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.SelectableDataModelWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.jami.model.*;
import psidev.psi.mi.jami.utils.AnnotationUtils;
import uk.ac.ebi.intact.editor.controller.curate.AnnotatedObjectController;
import uk.ac.ebi.intact.editor.controller.curate.UnsavedJamiChange;
import uk.ac.ebi.intact.editor.controller.curate.cloner.ParticipantJamiCloner;
import uk.ac.ebi.intact.editor.controller.curate.interaction.*;
import uk.ac.ebi.intact.editor.util.SelectableCollectionDataModel;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.context.UserContext;
import uk.ac.ebi.intact.jami.dao.CvTermDao;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.IntactPrimaryObject;
import uk.ac.ebi.intact.jami.model.extension.*;
import uk.ac.ebi.intact.jami.synchronizer.FinderException;
import uk.ac.ebi.intact.jami.synchronizer.IntactDbSynchronizer;
import uk.ac.ebi.intact.jami.synchronizer.PersisterException;
import uk.ac.ebi.intact.jami.synchronizer.SynchronizerException;
import uk.ac.ebi.intact.jami.utils.IntactUtils;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.CvTopic;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.model.DataModel;
import java.util.*;

/**
 * Modelled Participant controller.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "general" )
public class ModelledParticipantController extends AnnotatedObjectController {

    private static final Log log = LogFactory.getLog( ModelledParticipantController.class );

    private IntactModelledParticipant participant;

    private String interactor;
    private List<ImportJamiCandidate> interactorCandidates;

    private DataModel<ModelledFeatureWrapper> featuresDataModel;
    private ModelledFeatureWrapper[] selectedFeatures;

    /**
     * The AC of the participant to be loaded.
     */
    private String ac;

    @Autowired
    private ComplexController interactionController;

    private String cautionMessage = null;
    private String internalRemark = null;
    private String participantPrimaryId=null;

    private boolean isFeatureDisabled;

    public ModelledParticipantController() {
    }

    @Override
    public AnnotatedObject getAnnotatedObject() {
        return null;
    }

    @Override
    public IntactPrimaryObject getJamiObject() {
        return this.participant;
    }

    @Override
    public void setJamiObject(IntactPrimaryObject annotatedObject) {
        setParticipant((IntactModelledParticipant)annotatedObject);
    }

    @Override
    public void setAnnotatedObject(AnnotatedObject annotatedObject) {
        // do nothing
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void loadData( ComponentSystemEvent event ) {
        if (!FacesContext.getCurrentInstance().isPostback()) {

            if ( (participant == null && ac != null) || (ac != null && participant != null && !ac.equals( participant.getAc() ))) {
                setParticipant(loadByJamiAc(IntactModelledParticipant.class, ac));
            }

            if (participant == null) {
                addErrorMessage("No participant with this AC", ac);
                return;
            }

            if (participant.getInteraction() != null){
                if (interactionController.getComplex() == null || !interactionController.getComplex().getAc().equalsIgnoreCase(((IntactComplex) participant.getInteraction()).getAc())){
                    if( interactionController.getComplex() == null ) {
                        interactionController.setComplex((IntactComplex) participant.getInteraction());
                    }
                }
            }

            if (participant.getInteractor() != null) {
                interactor = participant.getInteractor().getShortName();
            }

            refreshTabsAndFocusXref();
            generalJamiLoadChecks();
        }
    }

    @Override
    public void doPreSave() {
        // create master proteins from the unsaved manager
        final List<UnsavedJamiChange> transcriptCreated = getChangesController().getAllUnsavedJamiProteinTranscripts();

        for (UnsavedJamiChange unsaved : transcriptCreated) {
            IntactPrimaryObject transcript = unsaved.getUnsavedObject();

            String currentAc = participant != null ? participant.getAc() : null;

            // the object to save is different from the current object. Checks that the scope of this object to save is the ac of the current object being saved
            // if the scope is null or different, the object should not be saved at this stage because we only save the current object and changes associated with it
            // if current ac is null, no unsaved event should be associated with it as this object has not been saved yet
            if (unsaved.getScope() != null && unsaved.getScope().equals(currentAc)){
                getPersistenceController().doSaveJamiMasterProteins(transcript);
                getChangesController().removeFromHiddenChanges(unsaved);
            }
            else if (unsaved.getScope() == null && currentAc == null){
                getPersistenceController().doSaveJamiMasterProteins(transcript);
                getChangesController().removeFromHiddenChanges(unsaved);
            }
        }

        // the participant is not persisted, we can add it to the list of components in the interaction
        if (participant.getAc() == null){
            interactionController.getComplex().getParticipants().add(participant);
        }
    }

    @Override
    public Collection<String> collectParentAcsOfCurrentAnnotatedObject(){
        Collection<String> parentAcs = new ArrayList<String>();

        if (participant.getInteraction() != null){
            addParentAcsTo(parentAcs, (IntactComplex)participant.getInteraction());
        }

        return parentAcs;
    }

    @Override
    protected void refreshUnsavedChangesBeforeRevert(){
        if (participant != null){
            Collection<String> parentAcs = new ArrayList<String>();

            if (participant.getInteraction() != null){
                addParentAcsTo(parentAcs, (IntactComplex)participant.getInteraction());
            }

            getChangesController().revertModelledParticipant(participant, parentAcs);
        }
    }


    /**
     * Add all the parent acs of this interaction
     * @param parentAcs
     * @param inter
     */
    protected void addParentAcsTo(Collection<String> parentAcs, IntactComplex inter) {
        if (inter.getAc() != null){
            parentAcs.add(inter.getAc());
        }
    }

    @Override
    public void doPostSave(){
        interactionController.refreshParticipants();
    }

    @Override
    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public String clone() {
        if (!getJamiEntityManager().contains(getParticipant())){
            IntactModelledParticipant reloadedParticipant = getJamiEntityManager().merge(this.participant);
            setParticipant(reloadedParticipant);
        }

        String value = clone(getParticipant());

        getJamiEntityManager().detach(getParticipant());

        return value;
    }

    @Override
    protected IntactModelledParticipant cloneAnnotatedObject(IntactPrimaryObject ao) {
        // to be overrided
        return (IntactModelledParticipant) ParticipantJamiCloner.cloneParticipant((IntactModelledParticipant) ao);
    }

    public String newParticipant(IntactComplex interaction) {
        this.interactor = null;

        IntactDao intactDao = getIntactDao();
        CvTermDao cvObjectService = intactDao.getCvTermDao();

        CvTerm defaultBiologicalRole = cvObjectService.getByMIIdentifier(Participant.UNSPECIFIED_ROLE_MI, IntactUtils.BIOLOGICAL_ROLE_OBJCLASS);

        IntactModelledParticipant participant = new IntactModelledParticipant(new IntactInteractor("unspecified"));
        participant.setCreated(new Date());
        participant.setUpdated(participant.getCreated());
        UserContext jamiUserContext = ApplicationContextProvider.getBean("jamiUserContext");
        participant.setCreator(jamiUserContext.getUserId());
        participant.setUpdator(jamiUserContext.getUserId());
        participant.setBiologicalRole(defaultBiologicalRole);
        participant.setStoichiometry(new IntactStoichiometry((int)getEditorConfig().getDefaultStoichiometry()));

        // by setting the interaction of a participant, we don't add the participant to the collection of participants for this interaction so if we revertJami, it will not affect anything.
        // when saving, it will be added to the list of participants for this interaction. we just have to refresh the list of participants
        participant.setInteraction(interaction);

        setParticipant(participant);

        changed();

        return navigateToJamiObject(participant);
    }

    public void importInteractor(ActionEvent evt) {
        ModelledParticipantImportController participantImportController = (ModelledParticipantImportController) getSpringContext().getBean("modelledParticipantImportController");
        interactorCandidates = new ArrayList<ImportJamiCandidate>(participantImportController.importParticipant(interactor));

        if (interactorCandidates.size() == 1) {
            interactorCandidates.get(0).setSelected(true);
        }
    }

    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED)
    public void addInteractorToParticipant(ActionEvent evt) {
        for (ImportJamiCandidate importCandidate : interactorCandidates) {
            if (importCandidate.isSelected()) {
                // chain or isoform, we may have to update it later
                if (importCandidate.isChain() || importCandidate.isIsoform()){
                    Collection<String> parentAcs = new ArrayList<String>();

                    if (participant.getInteraction() != null){
                        addParentAcsTo(parentAcs, (IntactComplex)participant.getInteraction());
                    }
                }
                try {
                    participant.setInteractor(getIntactDao().
                            getSynchronizerContext().
                            getInteractorSynchronizer().
                            synchronize(importCandidate.getInteractor(), true));
                } catch (FinderException e) {
                    // clear cache
                    getIntactDao().getSynchronizerContext().clearCache();
                    addErrorMessage("Cannot import interactor: " + e.getMessage(), ExceptionUtils.getFullStackTrace(e));
                } catch (PersisterException e) {
                    // clear cache
                    getIntactDao().getSynchronizerContext().clearCache();
                    addErrorMessage("Cannot import interactor: " + e.getMessage(), ExceptionUtils.getFullStackTrace(e));
                } catch (SynchronizerException e) {
                    // clear cache
                    getIntactDao().getSynchronizerContext().clearCache();
                    addErrorMessage("Cannot import interactor: " + e.getMessage(), ExceptionUtils.getFullStackTrace(e));
                }

                // if the participant is a new participant, we don't need to add a unsaved notice because one already exists for creating a new participant
                if (participant.getAc() != null){
                    setUnsavedChanges(true);
                }
            }
        }
    }

    public void markFeatureToDelete(ModelledFeature feature) {
        participant.removeFeature(feature);
        refreshFeatures();
        changed();
    }

    public void deleteSelectedFeatures(ActionEvent evt) {
        for (ModelledFeatureWrapper feature : selectedFeatures) {
            markFeatureToDelete(feature.getFeature());
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

    public int getMinStoichiometry(){
        return this.participant.getStoichiometry() != null ? this.participant.getStoichiometry().getMinValue() : 0;
    }

    public int getMaxStoichiometry(){
        return this.participant.getStoichiometry() != null ? this.participant.getStoichiometry().getMaxValue() : 0;
    }

    public void setMinStoichiometry(int stc){
        if (this.participant.getStoichiometry() == null){
            this.participant.setStoichiometry(new IntactStoichiometry(stc));
        }
        else {
            Stoichiometry stoichiometry = participant.getStoichiometry();
            this.participant.setStoichiometry(new IntactStoichiometry(stc, Math.max(stc, stoichiometry.getMaxValue())));
        }
    }

    public void setMaxStoichiometry(int stc){
        if (this.participant.getStoichiometry() == null){
            this.participant.setStoichiometry(new IntactStoichiometry(stc));
        }
        else {
            Stoichiometry stoichiometry = participant.getStoichiometry();
            this.participant.setStoichiometry(new IntactStoichiometry(Math.min(stc, stoichiometry.getMinValue()), stc));
        }
    }

    public IntactModelledParticipant getParticipant() {
        return participant;
    }

    public ModelledParticipantWrapper getModelledParticipantWrapper() {
        return new ModelledParticipantWrapper( participant, getChangesController(), interactionController );
    }

    @Override
    public void refreshTabsAndFocusXref(){
        refreshTabs();

        this.isFeatureDisabled = false;
    }

    public void setParticipant( IntactModelledParticipant participant ) {
        this.participant = participant;

        if (participant != null){
            this.ac = participant.getAc();
            refreshInfoMessages();
            refreshFeatures();
            refreshParticipantPrimaryId();
        }
        else{
            this.ac = null;
        }
    }

    public String getParticipantPrimaryId() {
        return this.participantPrimaryId;
    }

    @Override
    protected void doPostDelete(){
        // remove this one from the complex
        if (participant.getInteraction() != null){
            interactionController.setAc(((IntactComplex)participant.getInteraction()).getAc());
        }
        interactionController.setComplex(null);
    }

    private void refreshParticipantPrimaryId(){
        if (participant == null) this.participantPrimaryId = null;

        final Xref xrefs = participant.getInteractor().getPreferredIdentifier();

        if (xrefs == null && participant.getInteractor() instanceof IntactInteractor) {
            String ac = ((IntactInteractor)participant.getInteractor()).getAc();
            this.participantPrimaryId = ac != null ? ac : participant.getInteractor().getShortName();
        }
        else if (xrefs == null){
            this.participantPrimaryId = participant.getInteractor().getShortName();
        }
        else{
            this.participantPrimaryId = xrefs.getId();
        }
    }

    private String joinIds(Collection<Xref> xrefs) {
        Collection<String> ids = new ArrayList<String>(xrefs.size());

        for (Xref xref : xrefs) {
            ids.add(xref.getId());
        }

        return StringUtils.join(ids, ", ");
    }

    public String getInteractor() {
        return interactor;
    }

    public void setInteractor(String interactor) {
        this.interactor = interactor;
    }

    public List<ImportJamiCandidate> getInteractorCandidates() {
        return interactorCandidates;
    }

    public void setInteractorCandidates(List<ImportJamiCandidate> interactorCandidates) {
        this.interactorCandidates = interactorCandidates;
    }

    public ModelledFeatureWrapper[] getSelectedFeatures() {
        return selectedFeatures;
    }

    public void setSelectedFeatures(ModelledFeatureWrapper[] selectedFeatures) {
        this.selectedFeatures = selectedFeatures;
    }

    public DataModel<ModelledFeatureWrapper> getFeaturesDataModel() {
        return featuresDataModel;
    }

    @Override
    public void modifyClone(AnnotatedObject clone) {
        refreshTabs();
    }

    @Override
    public void newXref(ActionEvent evt) {
        this.participant.getXrefs().add(new ModelledParticipantXref(IntactUtils.createMIDatabase("unspecified", null), "to set"));
        setUnsavedChanges(true);
    }

    @Override
    public void newAnnotation(ActionEvent evt) {
        this.participant.getAnnotations().add(new ModelledParticipantAnnotation(IntactUtils.createMITopic("unspecified", null)));
        setUnsavedChanges(true);
    }

    @Override
    public void newAlias(ActionEvent evt) {
        this.participant.getAliases().add(new ModelledParticipantAlias("to set"));
        setUnsavedChanges(true);
    }

    @Override
    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public List collectXrefs() {
        if (!this.participant.areXrefsInitialized()){
            IntactModelledParticipant reloaded = getJamiEntityManager().merge(this.participant);
            setParticipant(reloaded);
        }

        List<Xref> xrefs = new ArrayList<Xref>(this.participant.getXrefs());

        getJamiEntityManager().detach(this.participant);
        return xrefs;
    }

    @Override
    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public List collectAliases() {
        if (!this.participant.areAliasesInitialized()){
            IntactModelledParticipant reloaded = getJamiEntityManager().merge(this.participant);
            setParticipant(reloaded);
        }

        List<Alias> aliases = new ArrayList<Alias>(this.participant.getAliases());

        getJamiEntityManager().detach(this.participant);
        return aliases;
    }

    @Override
    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public List collectAnnotations() {
        if (!this.participant.areAnnotationsInitialized()){
            IntactModelledParticipant reloaded = getJamiEntityManager().merge(this.participant);
            setParticipant(reloaded);
        }

        List<Annotation> xrefs = new ArrayList<Annotation>(this.participant.getAnnotations());

        getJamiEntityManager().detach(this.participant);
        return xrefs;
    }

    @Override
    public String getCautionMessage() {
        return this.cautionMessage;
    }

    @Override
    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public String getJamiCautionMessage(IntactPrimaryObject ao) {
        Collection<Annotation> annots = getIntactDao().getModelledParticipantDao().getAnnotationsForParticipant(ao.getAc());
        psidev.psi.mi.jami.model.Annotation caution = AnnotationUtils.collectFirstAnnotationWithTopic(annots, psidev.psi.mi.jami.model.Annotation.CAUTION_MI,
                psidev.psi.mi.jami.model.Annotation.CAUTION);
        return caution != null ? caution.getValue() : null;
    }

    @Override
    public String getInternalRemarkMessage() {
        return this.internalRemark;
    }

    @Override
    public IntactDbSynchronizer getDbSynchronizer() {
        return getIntactDao().getSynchronizerContext().getModelledParticipantSynchronizer();
    }

    @Override
    public String getJamiObjectName() {
        return getInteractor();
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public int getAliasesSize() {
        if (this.participant.areAliasesInitialized()){
            return this.participant.getAliases().size();
        }
        else{
            return getIntactDao().getModelledParticipantDao().countAliasesForParticipant(this.ac);
        }
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public int getAnnotationsSize() {
        if (this.participant.areAnnotationsInitialized()){
            return this.participant.getAnnotations().size();
        }
        else{
            return getIntactDao().getModelledParticipantDao().countAnnotationsForParticipant(this.ac);
        }
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public int getXrefsSize() {
        if (this.participant.areXrefsInitialized()){
            return this.participant.getXrefs().size();
        }
        else{
            return getIntactDao().getModelledParticipantDao().countXrefsForParticipant(this.ac);
        }
    }

    public void removeJamiAlias(Alias alias){
        this.participant.getAliases().remove(alias);
    }

    public void removeJamiXref(Xref xref){
        this.participant.getXrefs().remove(xref);
    }

    public void removeJamiAnnotation(Annotation annot){
        this.participant.getAnnotations().remove(annot);
    }

    public boolean isAliasNotEditable(Alias alias){
        return false;
    }

    public boolean isAnnotationNotEditable(Annotation annot){
        return false;
    }

    private void refreshInfoMessages() {
        Annotation remark = AnnotationUtils.collectFirstAnnotationWithTopic(this.participant.getAnnotations(), null,
                "remark-internal");
        this.internalRemark = remark != null ? remark.getValue() : null;
        Annotation caution = AnnotationUtils.collectFirstAnnotationWithTopic(this.participant.getAnnotations(), Annotation.CAUTION_MI,
                Annotation.CAUTION);
        this.cautionMessage = caution != null ? caution.getValue() : null;
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public boolean isJamiNoUniprotUpdate() {

        if (participant.getInteractor() instanceof IntactInteractor && !((IntactInteractor)participant.getInteractor()).areAnnotationsInitialized()){
            return AnnotationUtils.collectFirstAnnotationWithTopic(getIntactDao().getInteractorBaseDao().
                    getAnnotationsForInteractor(((IntactInteractor)participant.getInteractor()).getAc()), null, CvTopic.NON_UNIPROT) != null;
        }
        else{
            return AnnotationUtils.collectFirstAnnotationWithTopic(participant.getInteractor().getAnnotations(), null, CvTopic.NON_UNIPROT) != null;
        }
    }

    public void refreshFeatures() {
        final Collection<ModelledFeature> components = participant.getFeatures();

        List<ModelledFeatureWrapper> wrappers = new LinkedList<ModelledFeatureWrapper>();

        for ( ModelledFeature component : components ) {
            wrappers.add( new ModelledFeatureWrapper( (IntactModelledFeature)component ) );
        }

        featuresDataModel = new SelectableDataModelWrapper(new SelectableCollectionDataModel<ModelledFeatureWrapper>(wrappers), wrappers);
    }

    @Override
    public void forceRefreshCurrentViewObject(){
        super.forceRefreshCurrentViewObject();

        if (participant != null) {
            refreshFeatures();
        }
    }

    @Override
    public boolean doSaveDetails() {
        boolean saved = true;

        refreshFeatures();
        return saved;
    }

    @Override
    protected void postRevert() {
        refreshFeatures();
    }

    @Override
    protected boolean areXrefsInitialised() {
        return this.participant.areXrefsInitialized();
    }

    @Override
    protected boolean isPublicationParent() {
        return false;
    }

    public void onTabChanged(TabChangeEvent e) {

        // the xref tab is active
        super.onTabChanged(e);

        // all the tabs selectOneMenu are disabled, we can process the tabs specific to interaction
        if (isAliasDisabled() && isXrefDisabled() && isAnnotationTopicDisabled()){
            if (e.getTab().getId().equals("featuresTab")){
                isFeatureDisabled = false;
            }
            else {
                isFeatureDisabled = true;
            }
        }
        else {
            isFeatureDisabled = true;
        }
    }

}