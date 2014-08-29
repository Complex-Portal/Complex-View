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

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.hibernate.Hibernate;
import org.primefaces.event.TabChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.jami.model.*;
import psidev.psi.mi.jami.utils.AliasUtils;
import psidev.psi.mi.jami.utils.AnnotationUtils;
import uk.ac.ebi.intact.editor.controller.UserSessionController;
import uk.ac.ebi.intact.editor.controller.curate.AnnotatedObjectController;
import uk.ac.ebi.intact.editor.controller.curate.UnsavedJamiChange;
import uk.ac.ebi.intact.editor.controller.curate.cloner.ComplexJamiCloner;
import uk.ac.ebi.intact.editor.controller.curate.cloner.ParticipantJamiCloner;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.context.UserContext;
import uk.ac.ebi.intact.jami.dao.CvTermDao;
import uk.ac.ebi.intact.jami.lifecycle.ComplexBCLifecycleEventListener;
import uk.ac.ebi.intact.jami.lifecycle.LifeCycleManager;
import uk.ac.ebi.intact.jami.model.IntactPrimaryObject;
import uk.ac.ebi.intact.jami.model.extension.*;
import uk.ac.ebi.intact.jami.model.lifecycle.LifeCycleEvent;
import uk.ac.ebi.intact.jami.model.lifecycle.LifeCycleEventType;
import uk.ac.ebi.intact.jami.model.lifecycle.LifeCycleStatus;
import uk.ac.ebi.intact.jami.model.lifecycle.Releasable;
import uk.ac.ebi.intact.jami.synchronizer.FinderException;
import uk.ac.ebi.intact.jami.synchronizer.IntactDbSynchronizer;
import uk.ac.ebi.intact.jami.synchronizer.PersisterException;
import uk.ac.ebi.intact.jami.synchronizer.SynchronizerException;
import uk.ac.ebi.intact.jami.utils.IntactUtils;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.Interaction;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ValueChangeEvent;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "general" )
public class ComplexController extends AnnotatedObjectController {

    private static final Log log = LogFactory.getLog( ComplexController.class );

    private IntactComplex complex;
    private String ac;

    private LinkedList<ModelledParticipantWrapper> participantWrappers;

    @Autowired
    private UserSessionController userSessionController;

    private boolean isParticipantDisabled;
    private boolean isParameterDisabled;
    private boolean isConfidenceDisabled;
    private boolean isLifeCycleDisabled;

    private boolean assignToMe = true;

    @Autowired
    @Qualifier("jamiLifeCycleManager")
    private LifeCycleManager lifecycleManager;

    private String name = null;
    private String toBeReviewed = null;
    private String onHold = null;
    private String correctionComment = null;
    private String cautionMessage = null;
    private String internalRemark = null;
    private String recommendedName = null;
    private String systematicName = null;
    private String description = null;
    private String complexProperties = null;

    public ComplexController() {
    }

    @Override
    public AnnotatedObject getAnnotatedObject() {
        return null;
    }

    @Override
    public void setAnnotatedObject(AnnotatedObject annotatedObject) {
        // nothing to do
    }

    @Override
    public IntactPrimaryObject getJamiObject() {
        return this.complex;
    }

    @Override
    public void setJamiObject(IntactPrimaryObject annotatedObject) {
        setComplex((IntactComplex) annotatedObject);
    }

    public String getName(){
        return this.name;
    }

    @Override
    protected boolean areXrefsInitialised() {
        return this.complex.areXrefsInitialized();
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public String extractName(IntactComplex complex){
        String name = complex.getShortName();
        Collection<Alias> aliases = getIntactDao().getComplexDao().getAliasesForInteractor(complex.getAc());
        Alias recName = AliasUtils.collectFirstAliasWithType(aliases, Alias.COMPLEX_RECOMMENDED_NAME_MI, Alias.COMPLEX_RECOMMENDED_NAME);
        if (recName != null){
            name = recName.getName();
        }
        else{
            recName = AliasUtils.collectFirstAliasWithType(aliases, Alias.COMPLEX_SYSTEMATIC_NAME_MI, Alias.COMPLEX_SYSTEMATIC_NAME);
            if (recName != null){
                name = recName.getName();
            }
            else if (!aliases.isEmpty()){
                name = aliases.iterator().next().getName();
            }
        }
        return name;
    }

    public String getOrganism(){
        return this.complex.getOrganism() != null ? this.complex.getOrganism().getCommonName():"organism unknown";
    }

    @Override
    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public String clone() {

        if (!getJamiEntityManager().contains(getComplex())){
            IntactComplex reloadedComplex = getJamiEntityManager().merge(this.complex);
            setComplex(reloadedComplex);
        }
        String value = clone(getComplex());
        refreshParticipants();

        getJamiEntityManager().detach(getComplex());

        return value;
    }

    @Override
    protected IntactComplex cloneAnnotatedObject(IntactPrimaryObject ao) {
        // to be overrided
        IntactComplex complex = (IntactComplex)ComplexJamiCloner.cloneComplex((IntactComplex) ao);
        getLifecycleManager().getStartStatus().create(complex, "Created in Editor", getIntactDao().getUserContext().getUser());

        if (assignToMe) {
            lifecycleManager.getNewStatus().claimOwnership(complex, getIntactDao().getUserContext().getUser());
            lifecycleManager.getAssignedStatus().startCuration(complex, getIntactDao().getUserContext().getUser());
        }
        return complex;
    }

    @Override
    public void refreshTabsAndFocusXref(){
        refreshTabs();
    }

    @Override
    public void refreshTabs(){
        super.refreshTabs();
        isParticipantDisabled = false;
        isParameterDisabled = true;
        isConfidenceDisabled = true;
        isLifeCycleDisabled = true;
    }

    public String getOnHold(){
        return onHold != null ? onHold : "";
    }

    public String getCorrectionComment(){return correctionComment != null ? correctionComment : null;}

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void loadData( ComponentSystemEvent event ) {
        if (!FacesContext.getCurrentInstance().isPostback()) {

            if ( (complex == null && ac != null) || (ac != null && complex != null && !ac.equals( complex.getAc() ))) {
                setComplex(loadByJamiAc(IntactComplex.class, ac));
            }

            if (complex == null) {
                addErrorMessage("No Complex with this AC", ac);
                return;
            }

            refreshTabsAndFocusXref();
            generalJamiLoadChecks();
        }
    }

    @Override
    public void forceRefreshCurrentViewObject(){
        super.forceRefreshCurrentViewObject();

        if (complex != null) {
            refreshParticipants();
        }
    }

    @Override
    public void doPreSave() {
        // create master proteins from the unsaved manager
        final List<UnsavedJamiChange> transcriptCreated = super.getChangesController().getAllUnsavedJamiProteinTranscripts();
        String currentAc = complex != null ? complex.getAc() : null;

        for (UnsavedJamiChange unsaved : transcriptCreated) {
            IntactPrimaryObject transcript = unsaved.getUnsavedObject();

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
    }

    public void markParticipantToDelete(IntactModelledParticipant component) {
        if (component == null) return;
        complex.removeParticipant(component);
        refreshParticipants();
        changed();
    }

    @Override
    public boolean doSaveDetails() {
        boolean saved = true;

        refreshParticipants();
        return saved;
    }

    public void refreshParticipants() {
        final Collection<ModelledParticipant> components = complex.getParticipants();

        participantWrappers = new LinkedList<ModelledParticipantWrapper>();

        for ( ModelledParticipant component : components ) {
            participantWrappers.add( new ModelledParticipantWrapper( (IntactModelledParticipant)component, getChangesController(), this ) );
        }
    }

    public void addParticipant(IntactModelledParticipant component) {
        complex.addParticipant(component);

//        participantWrappers.addFirst(new ParticipantWrapper(component, getChangesController(), this));
        participantWrappers.add(new ModelledParticipantWrapper(component, getChangesController(), null));

        setUnsavedChanges(true);
    }

    @Override
    protected void refreshUnsavedChangesBeforeRevert(){
        if (complex != null){
            getChangesController().revertComplex(complex, Collections.EMPTY_LIST);
            if (complex.getAc() == null){
                setComplex(null);
            }
        }
    }

    public String getAc() {
        if ( ac == null && complex != null ) {
            return complex.getAc();
        }
        return ac;
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public int countParticipantsByInteractionAc( String ac ) {
        return getIntactDao().getComplexDao().countParticipantsForComplex(ac);
    }

    public int countParticipantsByInteraction( IntactComplex interaction) {
        if (interaction.getAc() != null) return countParticipantsByInteractionAc(interaction.getAc());

        return interaction.getParticipants().size();
    }

    public void deleteParticipant(ModelledParticipantWrapper participantWrapper) {
        participantWrapper.setDeleted(true);

        IntactModelledParticipant participant = participantWrapper.getParticipant();
        setUnsavedChanges(true);

        StringBuilder participantInfo = new StringBuilder();

        if (participant.getInteractor() != null) {
            participantInfo.append(participant.getInteractor().getShortName());
            participantInfo.append(" ");
        }

        if (participant.getAc() != null) {
            participantInfo.append("(").append(participant.getAc()+")");
        }

        addInfoMessage("Participant marked to be removed.", participantInfo.toString());
    }

    public void revertParticipant(ModelledParticipantWrapper participantWrapper) {
        participantWrapper.setDeleted(false);

        IntactModelledParticipant participant = participantWrapper.getParticipant();
        setUnsavedChanges(false);

        StringBuilder participantInfo = new StringBuilder();

        if (participant.getInteractor() != null) {
            participantInfo.append(participant.getInteractor().getShortName());
            participantInfo.append(" ");
        }

        if (participant.getAc() != null) {
            participantInfo.append("(").append(participant.getAc()).append(")");
        }
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public String getInteractorIdentity(String interactorAc){
        if (interactorAc == null){
            return null;
        }
        Interactor interactor = getIntactDao().getInteractorDao(IntactInteractor.class).getByAc(interactorAc);
        if (interactor == null){
            return null;
        }
        Xref ref = interactor.getPreferredIdentifier();
        if (ref == null){
            return interactor.getShortName();
        }
        else{
            return ref.getId();
        }
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void cloneParticipant(ModelledParticipantWrapper participantWrapper) {
        IntactModelledParticipant participant = getJamiEntityManager().merge(participantWrapper.getParticipant());

        IntactModelledParticipant clone = (IntactModelledParticipant)ParticipantJamiCloner.cloneParticipant(participant);
        addParticipant(clone);
        getJamiEntityManager().detach(participant);
    }

    public void linkSelectedFeatures(ActionEvent evt) {
        List<IntactModelledFeature> selected = new ArrayList<IntactModelledFeature>();

        for (ModelledParticipantWrapper pw : participantWrappers) {
            for (ModelledFeatureWrapper fw : pw.getFeatures()) {
                if (fw.isSelected()) {
                    selected.add(fw.getFeature());
                }
            }
        }

        Iterator<IntactModelledFeature> fIterator1 = selected.iterator();
        while (fIterator1.hasNext()){
            IntactModelledFeature f1 = fIterator1.next();

            for (IntactModelledFeature f2 : selected){
                if (f1 != f2){
                    f1.getLinkedFeatures().add(f2);
                    f2.getLinkedFeatures().add(f1);
                }
            }
            fIterator1.remove();
        }


        addInfoMessage("Features linked", "Size of linked features : "+selected.size());
        setUnsavedChanges(true);
        refreshParticipants();
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void unlinkFeature(ModelledFeatureWrapper wrapper) {
        Feature feature1 = wrapper.getFeature();
        IntactModelledFeature feature2 = wrapper.getSelectedLinkedFeature();
        if (feature2 != null){
            feature1.getLinkedFeatures().remove(feature2);
            feature2.getLinkedFeatures().remove(feature1);

            for (ModelledParticipantWrapper pw : participantWrappers) {
                ModelledFeatureWrapper wrapperToRemove=null;
                for (ModelledFeatureWrapper fw : pw.getFeatures()) {
                    if (fw.getFeature() == wrapper.getSelectedLinkedFeature()) {
                        wrapperToRemove = fw;
                        break;
                    }
                }
                if (wrapperToRemove != null){
                    wrapperToRemove.getLinkedFeatures().clear();
                    wrapperToRemove.getLinkedFeatures().addAll(feature2.getLinkedFeatures());
                    break;
                }
            }

            addInfoMessage("Feature unlinked", feature2.toString());
            setUnsavedChanges(true);
            wrapper.getLinkedFeatures().clear();
            wrapper.getLinkedFeatures().addAll(feature1.getLinkedFeatures());
        }
    }

    public void selectLinkedFeature(ModelledFeatureWrapper wrapper, IntactModelledFeature linked){
         wrapper.setSelectedLinkedFeature(linked);
    }

    public void setAc( String ac ) {
        this.ac = ac;
    }

    public IntactComplex getComplex() {
        return complex;
    }

    public void setComplex(IntactComplex complex) {
        this.complex = complex;
        if (complex != null) {
            this.ac = complex.getAc();

            refreshName();
            refreshInfoMessages();
            refreshParticipants();
        }
        else{
            this.ac = null;
        }
    }

    private void refreshName() {
        this.name = this.complex.getShortName();
        if (this.complex.getRecommendedName() != null){
            this.name = this.complex.getRecommendedName();
        }
        else if (this.complex.getSystematicName() != null){
            this.name = this.complex.getSystematicName();
        }
        else if (!this.complex.getAliases().isEmpty()){
            this.name = this.complex.getAliases().iterator().next().getName();
        }

        this.systematicName = this.complex.getSystematicName();
        this.recommendedName = this.complex.getRecommendedName();
    }

    private void refreshInfoMessages() {
        this.toBeReviewed = this.complex.getToBeReviewedComment();
        this.onHold = this.complex.getOnHoldComment();
        this.correctionComment = this.complex.getCorrectionComment();
        Annotation remark = AnnotationUtils.collectFirstAnnotationWithTopic(this.complex.getAnnotations(), null,
                "remark-internal");
        this.internalRemark = remark != null ? remark.getValue() : null;
        Annotation caution = AnnotationUtils.collectFirstAnnotationWithTopic(this.complex.getAnnotations(), Annotation.CAUTION_MI,
                Annotation.CAUTION);
        this.cautionMessage = caution != null ? caution.getValue() : null;
        Annotation desc = AnnotationUtils.collectFirstAnnotationWithTopic(this.complex.getAnnotations(), null,
                "curated-complex");
        this.description = desc != null ? desc.getValue() : null;
        this.complexProperties = this.complex.getPhysicalProperties();
    }

    public String getComplexProperties() {
        return complexProperties;
    }

    public void setComplexProperties(String complexProperties) {
        this.complex.setPhysicalProperties(complexProperties);
        this.complexProperties = complexProperties;
    }

    public Collection<ModelledParticipantWrapper> getParticipants() {
        return participantWrappers;
    }


    // Confidence
    ///////////////////////////////////////////////

    public void newConfidence() {
        ComplexConfidence confidence = new ComplexConfidence(IntactUtils.createMIConfidenceType("unspecified", null), "to set");
        complex.getModelledConfidences().add(confidence);
    }

    public void newParameter() {
        ComplexParameter param = new ComplexParameter(IntactUtils.createMIParameterType("unspecified", null), new ParameterValue(new BigDecimal(0)));
        complex.getModelledParameters().add(param);
    }

    public boolean isParticipantDisabled() {
        return isParticipantDisabled;
    }

    public void setParticipantDisabled(boolean participantDisabled) {
        isParticipantDisabled = participantDisabled;
    }

    public boolean isParameterDisabled() {
        return isParameterDisabled;
    }

    public void setParameterDisabled(boolean parameterDisabled) {
        isParameterDisabled = parameterDisabled;
    }

    public boolean isConfidenceDisabled() {
        return isConfidenceDisabled;
    }

    public void setConfidenceDisabled(boolean confidenceDisabled) {
        isConfidenceDisabled = confidenceDisabled;
    }

    public boolean isLifeCycleDisabled() {
        return isLifeCycleDisabled;
    }

    public void setLifeCycleDisabled(boolean advancedDisabled) {
        isLifeCycleDisabled = advancedDisabled;
    }

    public void onTabChanged(TabChangeEvent e) {

        // the xref tab is active
        super.onTabChanged(e);

        // all the tabs selectOneMenu are disabled, we can process the tabs specific to interaction
        if (isAliasDisabled() && isXrefDisabled() && isAnnotationTopicDisabled()){
            if (e.getTab().getId().equals("participantsTab")){
                isParticipantDisabled = false;
                isParameterDisabled = true;
                isConfidenceDisabled = true;
                isLifeCycleDisabled = true;
            }
            else if (e.getTab().getId().equals("parametersTab")){
                isParticipantDisabled = true;
                isParameterDisabled = false;
                isConfidenceDisabled = true;
                isLifeCycleDisabled = true;
            }
            else if (e.getTab().getId().equals("confidencesTab")){
                isParticipantDisabled = true;
                isParameterDisabled = true;
                isConfidenceDisabled = false;
                isLifeCycleDisabled = true;
            }
            else {
                isParticipantDisabled = true;
                isParameterDisabled = true;
                isConfidenceDisabled = true;
                isLifeCycleDisabled = false;
            }
        }
        else {
            isParticipantDisabled = true;
            isParameterDisabled = true;
            isConfidenceDisabled = true;
            isLifeCycleDisabled = true;
        }
    }

    @Override
    public void newXref(ActionEvent evt) {
        this.complex.getDbXrefs().add(new InteractorXref(IntactUtils.createMIDatabase("unspecified", null), "to set"));
        setUnsavedChanges(true);
    }

    @Override
    public void newAnnotation(ActionEvent evt) {
        this.complex.getAnnotations().add(new InteractorAnnotation(IntactUtils.createMITopic("unspecified", null)));
        setUnsavedChanges(true);
    }

    @Override
    public void newAlias(ActionEvent evt) {
        this.complex.getAliases().add(new InteractorAlias("to set"));
        setUnsavedChanges(true);
    }

    @Override
    public String getCautionMessage() {
        return cautionMessage;
    }

    @Override
    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public String getJamiCautionMessage(IntactPrimaryObject ao) {
        Collection<Annotation> annots = getIntactDao().getComplexDao().getAnnotationsForInteractor(ao.getAc());
        psidev.psi.mi.jami.model.Annotation caution = AnnotationUtils.collectFirstAnnotationWithTopic(annots, psidev.psi.mi.jami.model.Annotation.CAUTION_MI,
                psidev.psi.mi.jami.model.Annotation.CAUTION);
        return caution != null ? caution.getValue() : null;
    }

    @Override
    public String getInternalRemarkMessage() {
        return this.internalRemark;
    }

    @Override
    public List collectAnnotations() {
        return new ArrayList(this.complex.getAnnotations());
    }

    @Override
    public List collectAliases() {
        return new ArrayList(this.complex.getAliases());
    }

    public boolean isAliasNotEditable(Alias alias){
        if (AliasUtils.doesAliasHaveType(alias, Alias.COMPLEX_RECOMMENDED_NAME_MI, Alias.COMPLEX_RECOMMENDED_NAME)){
            return true;
        }
        else if (AliasUtils.doesAliasHaveType(alias, Alias.COMPLEX_SYSTEMATIC_NAME_MI, Alias.COMPLEX_SYSTEMATIC_NAME)){
            return true;
        }
        else {
            return false;
        }
    }

    public boolean isAnnotationNotEditable(Annotation annot){
        if (AnnotationUtils.doesAnnotationHaveTopic(annot, null, Releasable.ON_HOLD)){
            return true;
        }
        else if (AnnotationUtils.doesAnnotationHaveTopic(annot, Annotation.COMPLEX_PROPERTIES_MI, Annotation.COMPLEX_PROPERTIES)){
            return true;
        }
        else if (AnnotationUtils.doesAnnotationHaveTopic(annot, null, Releasable.TO_BE_REVIEWED)){
            return true;
        }
        else if (AnnotationUtils.doesAnnotationHaveTopic(annot, null, Releasable.CORRECTION_COMMENT)){
            return true;
        }
        else if (AnnotationUtils.doesAnnotationHaveTopic(annot, null, "curated-complex")){
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public List collectXrefs() {
        if (!this.complex.areXrefsInitialized()){
            IntactComplex reloadedComplex = getJamiEntityManager().merge(this.complex);
            setComplex(reloadedComplex);
        }

        List<Xref> xrefs = new ArrayList(this.complex.getDbXrefs());

        getJamiEntityManager().detach(this.complex);
        return xrefs;
    }

    public boolean isNewPublication() {
        return complex.getStatus() == LifeCycleStatus.NEW;
    }

    public boolean isAssigned() {
        return complex.getStatus() == LifeCycleStatus.ASSIGNED;
    }

    public boolean isCurationInProgress() {
        return complex.getStatus() == LifeCycleStatus.CURATION_IN_PROGRESS;
    }

    public boolean isReadyForChecking() {
        return complex.getStatus() == LifeCycleStatus.READY_FOR_CHECKING;
    }

    public boolean isReadyForRelease() {
        return complex.getStatus() == LifeCycleStatus.READY_FOR_RELEASE;
    }

    public boolean isAcceptedOnHold() {
        return complex.getStatus() == LifeCycleStatus.ACCEPTED_ON_HOLD;
    }

    public boolean isReleased() {
        return complex.getStatus() == LifeCycleStatus.RELEASED;
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void claimOwnership(ActionEvent evt) {
        getIntactTransactionSynchronization().registerDaoForSynchronization(getIntactDao());

        IntactComplex reloadedComplex = null;
        try {
            reloadedComplex = (IntactComplex)getDbSynchronizer().synchronize(this.complex, false);
            setComplex(reloadedComplex);
            getLifecycleManager().getGlobalStatus().changeOwnership(complex, getCurrentJamiUser(), null);

            // automatically set as curation in progress if no one was assigned before
            if (isAssigned()) {
                markAsCurationInProgress(evt);
            }

            addInfoMessage("Claimed Complex ownership", "You are now the owner of this complex");
        } catch (FinderException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot synchronize complex: " + e.getMessage(), e.getMessage());
        } catch (PersisterException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot synchronize complex: " + e.getMessage(), e.getMessage());
        } catch (SynchronizerException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot synchronize complex: " + e.getMessage(), e.getMessage());
        }
        getJamiEntityManager().detach(this.complex);
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void markAsAssignedToMe(ActionEvent evt) {
        getIntactTransactionSynchronization().registerDaoForSynchronization(getIntactDao());

        IntactComplex reloadedComplex = null;
        try {
            reloadedComplex = (IntactComplex)getDbSynchronizer().synchronize(this.complex, false);
            setComplex(reloadedComplex);
            getLifecycleManager().getNewStatus().assignToCurator(complex, getCurrentJamiUser(), getCurrentJamiUser());

            addInfoMessage("Ownership claimed", "The complex has been assigned to you");

            markAsCurationInProgress(evt);
        } catch (FinderException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot synchronize complex: " + e.getMessage(), e.getMessage());
        } catch (PersisterException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot synchronize complex: " + e.getMessage(), e.getMessage());
        } catch (SynchronizerException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot synchronize complex: " + e.getMessage(), e.getMessage());
        }
        getJamiEntityManager().detach(this.complex);
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void markAsCurationInProgress(ActionEvent evt) {

        if (!userSessionController.isJamiUserMe(complex.getCurrentOwner())) {
            addErrorMessage("Cannot mark as curation in progress", "You are not the owner of this publication");
            return;
        }

        getIntactTransactionSynchronization().registerDaoForSynchronization(getIntactDao());

        IntactComplex reloadedComplex = null;
        try {
            reloadedComplex = (IntactComplex)getDbSynchronizer().synchronize(this.complex, false);
            setComplex(reloadedComplex);
            getLifecycleManager().getAssignedStatus().startCuration(complex, getIntactDao().getUserContext().getUser());

            addInfoMessage("Curation started", "Curation is now in progress");
        } catch (FinderException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot synchronize complex: " + e.getMessage(), e.getMessage());
        } catch (PersisterException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot synchronize complex: " + e.getMessage(), e.getMessage());
        } catch (SynchronizerException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot synchronize complex: " + e.getMessage(), e.getMessage());
        }
        getJamiEntityManager().detach(this.complex);
    }



    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void markAsReadyForChecking(ActionEvent evt) {
        if (!userSessionController.isJamiUserMe(complex.getCurrentOwner())) {
            addErrorMessage("Cannot mark as Ready for checking", "You are not the owner of this complex");
            return;
        }
        if (isBeenRejectedBefore()) {
            correctionComment = this.complex.getCorrectionComment();
        }

        getIntactTransactionSynchronization().registerDaoForSynchronization(getIntactDao());

        IntactComplex reloadedComplex = null;
        try {
            reloadedComplex = (IntactComplex)getDbSynchronizer().synchronize(this.complex, false);
            setComplex(reloadedComplex);
            // TODO run a proper sanity check
            boolean sanityCheckPassed = true;

            getLifecycleManager().getCurationInProgressStatus().readyForChecking(complex, correctionComment, sanityCheckPassed, getIntactDao().getUserContext().getUser());

            correctionComment = null;

            addInfoMessage("Complex ready for checking", "Assigned to reviewer: " + complex.getCurrentReviewer().getLogin());
        } catch (FinderException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot synchronize complex: " + e.getMessage(), e.getMessage());
        } catch (PersisterException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot synchronize complex: " + e.getMessage(), e.getMessage());
        } catch (SynchronizerException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot synchronize complex: " + e.getMessage(), e.getMessage());
        }
        getJamiEntityManager().detach(this.complex);
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void revertReadyForChecking(ActionEvent evt) {
        if (!userSessionController.isJamiUserMe(complex.getCurrentOwner())) {
            addErrorMessage("Cannot mark as Ready for checking", "You are not the owner of this complex");
            return;
        }
        getIntactTransactionSynchronization().registerDaoForSynchronization(getIntactDao());
        IntactComplex reloadedComplex = null;
        try {
            reloadedComplex = (IntactComplex)getDbSynchronizer().synchronize(this.complex, false);
            setComplex(reloadedComplex);
            getLifecycleManager().getReadyForCheckingStatus().revert(this.complex, getIntactDao().getUserContext().getUser());

            correctionComment = null;

            addInfoMessage("Complex ready for checking", "Assigned to reviewer: " + complex.getCurrentReviewer().getLogin());
        } catch (FinderException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot synchronize complex: " + e.getMessage(), e.getMessage());
        } catch (PersisterException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot synchronize complex: " + e.getMessage(), e.getMessage());
        } catch (SynchronizerException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot synchronize complex: " + e.getMessage(), e.getMessage());
        }
        getJamiEntityManager().detach(this.complex);
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void revertAccepted(ActionEvent evt) {
        if (!userSessionController.isJamiUserMe(complex.getCurrentOwner())) {
            addErrorMessage("Cannot mark as Ready for checking", "You are not the owner of this complex");
            return;
        }

        getIntactTransactionSynchronization().registerDaoForSynchronization(getIntactDao());
        IntactComplex reloadedComplex = null;
        try {
            reloadedComplex = (IntactComplex)getDbSynchronizer().synchronize(this.complex, false);
            setComplex(reloadedComplex);
            getLifecycleManager().getReadyForReleaseStatus().revert(this.complex, getIntactDao().getUserContext().getUser());

        } catch (FinderException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot synchronize complex: " + e.getMessage(), e.getMessage());
        } catch (PersisterException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot synchronize complex: " + e.getMessage(), e.getMessage());
        } catch (SynchronizerException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot synchronize complex: " + e.getMessage(), e.getMessage());
        }
        getJamiEntityManager().detach(this.complex);
    }

    public void removeOnHold(ActionEvent evt) {

        this.complex.removeOnHold();
    }

    public void removeToBeReviewed(ActionEvent evt) {

        this.complex.removeToBeReviewed();
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void putOnHold(ActionEvent evt) {
        getIntactTransactionSynchronization().registerDaoForSynchronization(getIntactDao());
        IntactComplex reloadedComplex = null;
        try {
            reloadedComplex = (IntactComplex)getDbSynchronizer().synchronize(this.complex, false);
            setComplex(reloadedComplex);
            if (complex.getStatus().equals(LifeCycleStatus.READY_FOR_RELEASE)) {
                getLifecycleManager().getReadyForReleaseStatus().putOnHold(complex, onHold, getIntactDao().getUserContext().getUser());
                addInfoMessage("On-hold added to complex", "Complex won't be released until the 'on hold' is removed");
            } else if (complex.getStatus().equals(LifeCycleStatus.RELEASED)) {
                getLifecycleManager().getReleasedStatus().putOnHold(complex, onHold, getIntactDao().getUserContext().getUser());
                addInfoMessage("On-hold added to released complex", "Data will be publicly visible until the next release");
            }
            else{
                setOnHold(onHold);
            }
            this.onHold = this.complex.getOnHoldComment();

        } catch (FinderException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot synchronize complex: " + e.getMessage(), e.getMessage());
        } catch (PersisterException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot synchronize complex: " + e.getMessage(), e.getMessage());
        } catch (SynchronizerException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot synchronize complex: " + e.getMessage(), e.getMessage());
        }
        getJamiEntityManager().detach(this.complex);
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void readyForReleaseFromOnHold(ActionEvent evt) {
        getIntactTransactionSynchronization().registerDaoForSynchronization(getIntactDao());
        IntactComplex reloadedComplex = null;
        try {
            reloadedComplex = (IntactComplex)getDbSynchronizer().synchronize(this.complex, false);
            setComplex(reloadedComplex);
            getLifecycleManager().getAcceptedOnHoldStatus().onHoldRemoved(complex, null, getIntactDao().getUserContext().getUser());
            this.onHold = null;

        } catch (FinderException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot synchronize complex: " + e.getMessage(), e.getMessage());
        } catch (PersisterException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot synchronize complex: " + e.getMessage(), e.getMessage());
        } catch (SynchronizerException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot synchronize complex: " + e.getMessage(), e.getMessage());
        }
        getJamiEntityManager().detach(this.complex);
    }

    public void setOnHold(String reason) {
        this.onHold = reason;
    }

    public void setCorrectionComment(String reason) {
        this.correctionComment = reason;
    }

    public void setToBeReviewed(String reason) {
        this.toBeReviewed = reason;
    }

    public void onHoldChanged(ValueChangeEvent evt) {
        setUnsavedChanges(true);
        String newValue = (String) evt.getNewValue();
        if (newValue != null && newValue.length() > 0){
            this.complex.onHold(newValue);
            this.onHold = newValue;
        }
    }

    public String getRecommendedName() {
        return recommendedName;
    }

    public void setRecommendedName(String recommendedName) {
        this.recommendedName = recommendedName;
    }

    public String getSystematicName() {
        return systematicName;
    }

    public void setSystematicName(String systematicName) {
        this.systematicName = systematicName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void onRecommendedNameChanged(ValueChangeEvent evt) {
        setUnsavedChanges(true);
        String newValue = (String) evt.getNewValue();
        if (newValue == null || newValue.length() == 0){
            this.complex.setRecommendedName(null);
            this.recommendedName = null;
        }
        else{
            this.complex.setRecommendedName(newValue);
            this.recommendedName = newValue;
        }
    }

    public void onSystematicNameChanged(ValueChangeEvent evt) {
        setUnsavedChanges(true);

        String newValue = (String) evt.getNewValue();
        if (newValue == null || newValue.length() == 0){
            this.complex.setSystematicName(null);
            this.systematicName = null;
        }
        else{
            this.complex.setSystematicName(newValue);
            this.systematicName = newValue;
        }
    }

    public void onDescriptionChanged(ValueChangeEvent evt) {
        setUnsavedChanges(true);

        this.description = (String) evt.getNewValue();

        Annotation curatedComplex = AnnotationUtils.collectFirstAnnotationWithTopic(this.complex.getAnnotations(), null, "curated-complex");
        if (curatedComplex != null){
            curatedComplex.setValue(this.description);
        }
        else{
            this.complex.getAnnotations().add(new InteractorAnnotation(IntactUtils.createMITopic("curated-complex", null), this.description));
        }
    }

    public void onComplexPropertiesChanged(ValueChangeEvent evt) {
        setUnsavedChanges(true);

        String newValue = (String) evt.getNewValue();
        if (newValue == null || newValue.length() == 0){
            this.complex.setPhysicalProperties(null);
            this.complexProperties = null;
        }
        else{
            this.complex.setPhysicalProperties(newValue);
            this.complexProperties = newValue;
        }
    }

    public boolean isAccepted() {
        if (complex == null || complex.getStatus() == null) {
            return false;
        }

        return complex.getStatus() == LifeCycleStatus.ACCEPTED ||
                complex.getStatus() == LifeCycleStatus.ACCEPTED_ON_HOLD ||
                complex.getStatus() == LifeCycleStatus.READY_FOR_RELEASE ||
                complex.getStatus() == LifeCycleStatus.RELEASED;
    }

    public boolean isAccepted(IntactComplex pub) {
        return pub.getStatus() == LifeCycleStatus.ACCEPTED ||
                pub.getStatus() == LifeCycleStatus.ACCEPTED_ON_HOLD ||
                pub.getStatus() == LifeCycleStatus.READY_FOR_RELEASE ||
                pub.getStatus() == LifeCycleStatus.RELEASED;
    }

    public boolean isToBeReviewed(IntactComplex pub) {
        return pub.isToBeReviewed();
    }

    public boolean isOnHold(IntactComplex pub) {
        return pub.isOnHold();
    }

    public boolean isCorrectionComment(IntactComplex pub) {
        return pub.hasCorrectionComment();
    }

    public boolean isComplexToBeReviewed() {
        return this.complex.isToBeReviewed();
    }

    public boolean isComplexOnHold() {
        return this.complex.isOnHold();
    }

    public boolean isComplexWithCorrectionComment() {
        return this.complex.hasCorrectionComment();
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void acceptComplex(ActionEvent evt) {

        UserSessionController userSessionController = (UserSessionController) getSpringContext().getBean("userSessionController");
        getIntactTransactionSynchronization().registerDaoForSynchronization(getIntactDao());
        IntactComplex reloadedComplex = null;
        try {
            reloadedComplex = (IntactComplex)getDbSynchronizer().synchronize(this.complex, false);
            setComplex(reloadedComplex);
            getLifecycleManager().getReadyForCheckingStatus().accept(complex, "Accepted " + new SimpleDateFormat("yyyy-MMM-dd").format(new Date()).toUpperCase() + " by " + userSessionController.getCurrentUser().getLogin().toUpperCase(), getIntactDao().getUserContext().getUser());

            if (!complex.isOnHold()) {
                lifecycleManager.getAcceptedStatus().readyForRelease(complex, "Accepted and not on-hold", getIntactDao().getUserContext().getUser());
            }

        } catch (FinderException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot synchronize complex: " + e.getMessage(), e.getMessage());
        } catch (PersisterException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot synchronize complex: " + e.getMessage(), e.getMessage());
        } catch (SynchronizerException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot synchronize complex: " + e.getMessage(), e.getMessage());
        }
        getJamiEntityManager().detach(this.complex);
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void rejectComplex(ActionEvent evt) {

        rejectComplex(toBeReviewed);

    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void rejectComplex(String reasonForRejection) {
        getIntactTransactionSynchronization().registerDaoForSynchronization(getIntactDao());
        IntactComplex reloadedComplex = null;
        try {
            reloadedComplex = (IntactComplex)getDbSynchronizer().synchronize(this.complex, false);
            setComplex(reloadedComplex);
            UserSessionController userSessionController = (UserSessionController) getSpringContext().getBean("userSessionController");
            String date = "Rejected " + new SimpleDateFormat("yyyy-MMM-dd").format(new Date()).toUpperCase() + " by " + userSessionController.getCurrentUser().getLogin().toUpperCase();

            addInfoMessage("Complex rejected", "");

            getLifecycleManager().getReadyForCheckingStatus().reject(this.complex, date + ". " + reasonForRejection, getIntactDao().getUserContext().getUser());

            this.toBeReviewed = this.complex.getToBeReviewedComment();

        } catch (FinderException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot synchronize complex: " + e.getMessage(), e.getMessage());
        } catch (PersisterException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot synchronize complex: " + e.getMessage(), e.getMessage());
        } catch (SynchronizerException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot synchronize complex: " + e.getMessage(), e.getMessage());
        }

        getJamiEntityManager().detach(this.complex);
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public String calculateStatusStyle(IntactComplex complex) {
        if (isAccepted(complex)) {
            return "ia-accepted";
        }

        int timesRejected = 0;
        int timesReadyForChecking = 0;

        Collection<LifeCycleEvent> events = complex.areLifeCycleEventsInitialized() ? complex.getLifecycleEvents() :
                getIntactDao().getComplexDao().getLifeCycleEventsForComplex(complex.getAc());

        for (LifeCycleEvent evt : events) {
            if (LifeCycleEventType.REJECTED.equals(evt.getEvent())) {
                timesRejected++;
            } else if (LifeCycleEventType.READY_FOR_CHECKING.equals(evt.getEvent())) {
                timesReadyForChecking++;
            }
        }

        if (LifeCycleStatus.CURATION_IN_PROGRESS.equals(complex.getStatus()) && timesRejected > 0) {
            return "ia-rejected";
        } else if (LifeCycleStatus.READY_FOR_CHECKING.equals(complex.getStatus()) && timesReadyForChecking > 1) {
            return "ia-corrected";
        }

        return "";
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public boolean isBeenRejectedBefore() {
        for (LifeCycleEvent evt : collectLifecycleEvents()) {
            if (LifeCycleEventType.REJECTED.equals(evt.getEvent())) {
                return true;
            }
        }

        return false;
    }

    public String getToBeReviewed() {
        return toBeReviewed != null ? toBeReviewed : "";
    }

    public void clearToBeReviewed(ActionEvent evt) {
        this.complex.removeToBeReviewed();
        toBeReviewed = null;
    }

    public boolean isAssignToMe() {
        return assignToMe;
    }

    public void setAssignToMe(boolean assignToMe) {
        this.assignToMe = assignToMe;
    }

    public boolean isNewComplex() {
        return complex.getStatus().equals(LifeCycleStatus.NEW);
    }


    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public String newComplex(Interaction interactionEvidence) {
        if (interactionEvidence == null || interactionEvidence.getAc() == null) {
            addErrorMessage("Cannot create biological complex", "Interaction evidence is empty or not saved");
            return null;
        }
        CvTermDao dao = getIntactDao().getCvTermDao();
        CvTerm type = dao.getByMIIdentifier(Complex.COMPLEX_MI, IntactUtils.INTERACTOR_TYPE_OBJCLASS);

        InteractionEvidence ev = getIntactDao().getInteractionDao().getByAc(interactionEvidence.getAc());
        // the interaction evidence is loaded with jami
        if (ev != null){
            try {
                IntactComplex complex = (IntactComplex)ComplexJamiCloner.cloneInteraction(ev);
                setComplex(complex);
            } catch (SynchronizerException e) {
                // clear cache
                getIntactDao().getSynchronizerContext().clearCache();
                addErrorMessage("Cannot clone the interaction evidence as a complex: "+e.getMessage(), ExceptionUtils.getFullStackTrace(e));
            } catch (FinderException e) {
                // clear cache
                getIntactDao().getSynchronizerContext().clearCache();
                addErrorMessage("Cannot clone the interaction evidence as a complex: "+e.getMessage(), ExceptionUtils.getFullStackTrace(e));
            } catch (PersisterException e) {
                // clear cache
                getIntactDao().getSynchronizerContext().clearCache();
                addErrorMessage("Cannot clone the interaction evidence as a complex: "+e.getMessage(), ExceptionUtils.getFullStackTrace(e));
            }
        }
        // the interaction evidence does not exist as it must be a complex
        else {
            setComplex((IntactComplex)ComplexJamiCloner.cloneComplex(getIntactDao().getComplexDao().getByAc(interactionEvidence.getAc())));
        }

        this.complex.setInteractorType(type);

        getLifecycleManager().getStartStatus().create(this.complex, "Created in Editor", getIntactDao().getUserContext().getUser());

        if (assignToMe) {
            lifecycleManager.getNewStatus().claimOwnership(this.complex, getIntactDao().getUserContext().getUser());
            lifecycleManager.getAssignedStatus().startCuration(this.complex, getIntactDao().getUserContext().getUser());
        }

        return "/curate/complex?faces-redirect=true";
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public String newComplex() {

        CvTermDao dao = getIntactDao().getCvTermDao();
        CvTerm type = dao.getByMIIdentifier(Complex.COMPLEX_MI, IntactUtils.INTERACTOR_TYPE_OBJCLASS);

        setComplex(new IntactComplex("name to specify"));
        UserSessionController userSessionController = ApplicationContextProvider.getBean("userSessionController");
        this.complex.setSource(userSessionController.getUserSource());
        this.complex.setCreatedDate(new Date());
        this.complex.setUpdatedDate(this.complex.getCreatedDate());
        UserContext jamiUserContext = getIntactDao().getUserContext();
        this.complex.setCreator(jamiUserContext.getUserId());
        this.complex.setUpdator(jamiUserContext.getUserId());
        this.complex.setInteractorType(type);
        getLifecycleManager().getStartStatus().create(this.complex, "Created in Editor", jamiUserContext.getUser());

        if (assignToMe) {
            lifecycleManager.getNewStatus().claimOwnership(this.complex, jamiUserContext.getUser());
            lifecycleManager.getAssignedStatus().startCuration(this.complex, jamiUserContext.getUser());
        }

        return "/curate/complex?faces-redirect=true";
    }

    @Override
    public IntactDbSynchronizer getDbSynchronizer() {
        return getIntactDao().getSynchronizerContext().getComplexSynchronizer();
    }

    @Override
    public String getJamiObjectName() {
        return getName();
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public int getParametersSize() {
        if (this.complex.areParametersInitialized()){
            return this.complex.getModelledParameters().size();
        }
        else{
            // reload complex without flushing changes
            return getIntactDao().getComplexDao().countParametersForComplex(this.ac);
        }
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public int getConfidencesSize() {
        if (this.complex.areConfidencesInitialized()){
            return this.complex.getModelledConfidences().size();
        }
        else{
            // reload complex without flushing changes
            return getIntactDao().getComplexDao().countConfidencesForComplex(this.ac);
        }
    }

    /**
     * No transactional as it should always be initialised when loaded recommended name and systematic name when loading the page
     * @return
     */
    public int getAliasesSize() {
        return this.complex.getAliases().size();
    }

    /**
     * No transactional as it should always be initialised when loaded recommended name and systematic name when loading the page
     * @return
     */
    public int getAnnotationsSize() {
        return this.complex.getAnnotations().size();
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public int getXrefsSize() {
        if (this.complex.areXrefsInitialized()){
            return this.complex.getDbXrefs().size();
        }
        else{
            return getIntactDao().getComplexDao().countXrefsForInteractor(this.ac);
        }
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public Collection<ModelledConfidence> collectConfidences() {
        if (this.complex.areConfidencesInitialized()){
            return this.complex.getModelledConfidences();
        }
        else{
            // reload complex without flushing changes
            IntactComplex reloadedComplex = getJamiEntityManager().merge(this.complex);
            Collection<ModelledConfidence> confs = reloadedComplex.getModelledConfidences();
            setComplex(reloadedComplex);
            Hibernate.initialize(confs);
            getJamiEntityManager().detach(reloadedComplex);
            return confs;
        }
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public Collection<ModelledParameter> collectParameters() {
        if (this.complex.areParametersInitialized()){
            return this.complex.getModelledParameters();
        }
        else{
            // reload complex without flushing changes
            IntactComplex reloadedComplex = getJamiEntityManager().merge(this.complex);
            Collection<ModelledParameter> params = reloadedComplex.getModelledParameters();
            setComplex(reloadedComplex);
            Hibernate.initialize(params);
            getJamiEntityManager().detach(reloadedComplex);
            return params;
        }
    }

    public void removeConfidence(ModelledConfidence conf){
        this.complex.getModelledConfidences().remove(conf);
    }

    public void removeParameter(ModelledParameter param){
        this.complex.getModelledParameters().remove(param);
    }

    public void removeJamiAlias(Alias alias){
        this.complex.getAliases().remove(alias);
    }

    public void removeJamiXref(Xref xref){
        this.complex.getDbXrefs().remove(xref);
    }

    public void removeJamiAnnotation(Annotation annot){
        this.complex.getAnnotations().remove(annot);
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public Collection<LifeCycleEvent> collectLifecycleEvents() {
        if (this.complex.areLifeCycleEventsInitialized()){
            return this.complex.getLifecycleEvents();
        }
        else{
            // reload complex without flushing changes
            IntactComplex reloadedComplex = getJamiEntityManager().merge(this.complex);
            Collection<LifeCycleEvent> events = reloadedComplex.getLifecycleEvents();
            Hibernate.initialize(events);
            setComplex(reloadedComplex);
            getJamiEntityManager().detach(reloadedComplex);
            return events;
        }
    }

    @Override
    protected boolean isPublicationParent() {
        return false;
    }

    public LifeCycleManager getLifecycleManager() {
        this.lifecycleManager.registerListener(new ComplexBCLifecycleEventListener());
        return lifecycleManager;
    }

    public void addCorrectionComment(ActionEvent evt) {
        addInfoMessage("Added correction comment", correctionComment);
        this.complex.onCorrectionComment(correctionComment);
    }

    public void removeCorrectionComment(ActionEvent evt) {
        addInfoMessage("Removed correction comment", correctionComment);
        this.complex.removeCorrectionComment();
    }
}
