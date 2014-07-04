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
import org.primefaces.context.RequestContext;
import org.primefaces.event.TabChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.jami.model.*;
import psidev.psi.mi.jami.model.Annotation;
import psidev.psi.mi.jami.model.Complex;
import psidev.psi.mi.jami.model.Xref;
import psidev.psi.mi.jami.utils.AnnotationUtils;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;
import uk.ac.ebi.intact.core.persister.IntactCore;
import uk.ac.ebi.intact.editor.controller.UserSessionController;
import uk.ac.ebi.intact.editor.controller.curate.AnnotatedObjectController;
import uk.ac.ebi.intact.editor.controller.curate.UnsavedJamiChange;
import uk.ac.ebi.intact.editor.controller.curate.cloner.ComplexJamiCloner;
import uk.ac.ebi.intact.editor.controller.curate.cloner.ParticipantJamiCloner;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.dao.CvTermDao;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.lifecycle.LifeCycleManager;
import uk.ac.ebi.intact.jami.model.IntactPrimaryObject;
import uk.ac.ebi.intact.jami.model.extension.*;
import uk.ac.ebi.intact.jami.model.extension.InteractorAlias;
import uk.ac.ebi.intact.jami.model.extension.InteractorXref;
import uk.ac.ebi.intact.jami.model.lifecycle.LifeCycleEvent;
import uk.ac.ebi.intact.jami.model.lifecycle.LifeCycleEventType;
import uk.ac.ebi.intact.jami.model.lifecycle.LifeCycleStatus;
import uk.ac.ebi.intact.jami.utils.IntactUtils;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Publication;
import uk.ac.ebi.intact.model.util.*;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
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

    private List<SelectItem> aliasTypeSelectItems;
    private List<SelectItem> complexTopicSelectItems;
    private List<SelectItem> databaseSelectItems;
    private List<SelectItem> qualifierSelectItems;
    private List<SelectItem> interactionTypeSelectItems;
    private List<SelectItem> interactorTypeSelectItems;
    private List<SelectItem> evidenceTypeSelectItems;
    private List<SelectItem> confidenceTypeSelectItems;
    private List<SelectItem> parameterTypeSelectItems;
    private List<SelectItem> parameterUnitSelectItems;

    private boolean assignToMe = true;
    private String reasonForReadyForChecking;
    private String reasonForRejection;
    private String reasonForOnHoldFromDialog;

    @Autowired
    @Qualifier("jamiLifeCycleManager")
    private LifeCycleManager lifecycleManager;

    public ComplexController() {

    }

    @PostConstruct
    @Transactional(value = "jamiTransactionManager", readOnly = true)
    public void loadData() {
        aliasTypeSelectItems = new ArrayList<SelectItem>();
        aliasTypeSelectItems.add(new SelectItem( null, "select type", "select type", false, false, true ));
        complexTopicSelectItems = new ArrayList<SelectItem>();
        complexTopicSelectItems.add(new SelectItem(null, "select topic", "select topic", false, false, true));
        databaseSelectItems = new ArrayList<SelectItem>();
        databaseSelectItems.add(new SelectItem( null, "select database", "select database", false, false, true ));
        qualifierSelectItems = new ArrayList<SelectItem>();
        qualifierSelectItems.add(new SelectItem( null, "select qualifier", "select qualifier", false, false, true ));
        interactionTypeSelectItems = new ArrayList<SelectItem>();
        interactionTypeSelectItems.add(new SelectItem(null, "select interaction type", "select interaction type", false, false, true));
        interactorTypeSelectItems = new ArrayList<SelectItem>();
        interactorTypeSelectItems.add(new SelectItem(null, "select complex type", "select complex type", false, false, true));
        evidenceTypeSelectItems = new ArrayList<SelectItem>();
        evidenceTypeSelectItems.add(new SelectItem(null, "select eco code", "select eco code", false, false, true));
        confidenceTypeSelectItems = new ArrayList<SelectItem>();
        confidenceTypeSelectItems.add(new SelectItem(null, "select confidence type", "select confidence type", false, false, true));
        parameterTypeSelectItems = new ArrayList<SelectItem>();
        parameterTypeSelectItems.add(new SelectItem(null, "select parameter type", "select parameter type", false, false, true));
        parameterUnitSelectItems = new ArrayList<SelectItem>();
        parameterUnitSelectItems.add(new SelectItem(null, "select parameter unit", "select parameter unit", false, false, true));

        IntactDao intactDao = ApplicationContextProvider.getBean("intactDao");
        CvTermDao cvDao = intactDao.getCvTermDao();

        IntactCvTerm aliasTypeParent = cvDao.getByMIIdentifier("MI:0300", IntactUtils.ALIAS_TYPE_OBJCLASS);
        loadChildren(aliasTypeParent, aliasTypeSelectItems);

        IntactCvTerm complexTopicParent = cvDao.getByMIIdentifier("MI:0664", IntactUtils.TOPIC_OBJCLASS);
        loadChildren(complexTopicParent, complexTopicSelectItems);

        IntactCvTerm databaseParent = cvDao.getByMIIdentifier("MI:0473", IntactUtils.DATABASE_OBJCLASS);
        loadChildren(databaseParent, databaseSelectItems);

        IntactCvTerm qualifierParent = cvDao.getByMIIdentifier("MI:0353", IntactUtils.QUALIFIER_OBJCLASS);
        loadChildren(qualifierParent, qualifierSelectItems);

        IntactCvTerm interactionTypeParent = cvDao.getByMIIdentifier("MI:0190", IntactUtils.INTERACTION_TYPE_OBJCLASS);
        loadChildren(interactionTypeParent, interactionTypeSelectItems);

        IntactCvTerm interactorTypeParent = cvDao.getByMIIdentifier("MI:0314", IntactUtils.INTERACTOR_TYPE_OBJCLASS);
        loadChildren(interactorTypeParent, interactorTypeSelectItems);

        IntactCvTerm evidenceTypeParent = cvDao.getByMIIdentifier("MI:1331", IntactUtils.DATABASE_OBJCLASS);
        loadChildren(evidenceTypeParent, evidenceTypeSelectItems);

        IntactCvTerm confidenceTypeParent = cvDao.getByMIIdentifier("MI:1064", IntactUtils.CONFIDENCE_TYPE_OBJCLASS);
        loadChildren(confidenceTypeParent, confidenceTypeSelectItems);

        IntactCvTerm parameterTypeParent = cvDao.getByMIIdentifier("MI:0640", IntactUtils.PARAMETER_TYPE_OBJCLASS);
        loadChildren(parameterTypeParent, parameterTypeSelectItems);

        IntactCvTerm parameterUnit = cvDao.getByMIIdentifier("MI:0647", IntactUtils.UNIT_OBJCLASS);
        loadChildren(parameterUnit, parameterTypeSelectItems);
    }

    private void loadChildren(IntactCvTerm parent, List<SelectItem> selectItems){
        for (OntologyTerm child : parent.getChildren()){
            IntactCvTerm cv = (IntactCvTerm)child;
            SelectItem item = createSelectItem(cv);
            if (item != null){
                selectItems.add(item);
            }
            if (!cv.getChildren().isEmpty()){
                loadChildren(cv, selectItems);
            }
        }
    }

    private SelectItem createSelectItem( IntactCvTerm cv ) {
        if (!AnnotationUtils.collectAllAnnotationsHavingTopic(cv.getAnnotations(), null, "hidden").isEmpty()){
            boolean obsolete = AnnotationUtils.collectAllAnnotationsHavingTopic(cv.getAnnotations(), CvTopic.OBSOLETE_MI_REF, CvTopic.OBSOLETE).isEmpty();
            return new SelectItem( cv, cv.getShortName()+((obsolete? " (obsolete)" : "")), cv.getFullName());
        }
        return null;
    }

    public List<SelectItem> getAliasTypeSelectItems() {
        return aliasTypeSelectItems;
    }

    public void setAliasTypeSelectItems(List<SelectItem> aliasTypeSelectItems) {
        this.aliasTypeSelectItems = aliasTypeSelectItems;
    }

    public List<SelectItem> getComplexTopicSelectItems() {
        return complexTopicSelectItems;
    }

    public void setComplexTopicSelectItems(List<SelectItem> complexTopicSelectItems) {
        this.complexTopicSelectItems = complexTopicSelectItems;
    }

    public List<SelectItem> getDatabaseSelectItems() {
        return databaseSelectItems;
    }

    public void setDatabaseSelectItems(List<SelectItem> databaseSelectItems) {
        this.databaseSelectItems = databaseSelectItems;
    }

    public List<SelectItem> getQualifierSelectItems() {
        return qualifierSelectItems;
    }

    public void setQualifierSelectItems(List<SelectItem> qualifierSelectItems) {
        this.qualifierSelectItems = qualifierSelectItems;
    }

    public List<SelectItem> getInteractionTypeSelectItems() {
        return interactionTypeSelectItems;
    }

    public void setInteractionTypeSelectItems(List<SelectItem> interactionTypeSelectItems) {
        this.interactionTypeSelectItems = interactionTypeSelectItems;
    }

    public List<SelectItem> getInteractorTypeSelectItems() {
        return interactorTypeSelectItems;
    }

    public void setInteractorTypeSelectItems(List<SelectItem> interactorTypeSelectItems) {
        this.interactorTypeSelectItems = interactorTypeSelectItems;
    }

    public List<SelectItem> getEvidenceTypeSelectItems() {
        return evidenceTypeSelectItems;
    }

    public void setEvidenceTypeSelectItems(List<SelectItem> evidenceTypeSelectItems) {
        this.evidenceTypeSelectItems = evidenceTypeSelectItems;
    }

    public List<SelectItem> getConfidenceTypeSelectItems() {
        return confidenceTypeSelectItems;
    }

    public void setConfidenceTypeSelectItems(List<SelectItem> confidenceTypeSelectItems) {
        this.confidenceTypeSelectItems = confidenceTypeSelectItems;
    }

    public List<SelectItem> getParameterUnitSelectItems() {
        return parameterUnitSelectItems;
    }

    public void setParameterUnitSelectItems(List<SelectItem> parameterUnitSelectItems) {
        this.parameterUnitSelectItems = parameterUnitSelectItems;
    }

    public List<SelectItem> getParameterTypeSelectItems() {
        return parameterTypeSelectItems;
    }

    public void setParameterTypeSelectItems(List<SelectItem> parameterTypeSelectItems) {
        this.parameterTypeSelectItems = parameterTypeSelectItems;
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
        String name = this.complex.getShortName();
        if (this.complex.getRecommendedName() != null){
            name = this.complex.getRecommendedName();
        }
        else if (this.complex.getSystematicName() != null){
            name = this.complex.getSystematicName();
        }
        else if (!this.complex.getAliases().isEmpty()){
            name = this.complex.getAliases().iterator().next().getName();
        }
        return name;
    }

    public String getName(IntactComplex complex){
        String name = complex.getShortName();
        if (complex.getRecommendedName() != null){
            name = complex.getRecommendedName();
        }
        else if (complex.getSystematicName() != null){
            name = complex.getSystematicName();
        }
        else if (!complex.getAliases().isEmpty()){
            name = complex.getAliases().iterator().next().getName();
        }
        return name;
    }

    public String getOrganism(){
        return this.complex.getOrganism() != null ? this.complex.getOrganism().getCommonName():"organism unknown";
    }

    @Override
    public String clone() {
        String value = clone(getComplex());
        refreshParticipants();

        return value;
    }

    @Override
    protected IntactComplex cloneAnnotatedObject(IntactPrimaryObject ao) {
        // to be overrided
        return (IntactComplex) ComplexJamiCloner.cloneComplex((IntactComplex) ao);
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
        psidev.psi.mi.jami.model.Annotation onHold = AnnotationUtils.collectFirstAnnotationWithTopic(this.complex.getAnnotations(), null, "on-hold");
        return onHold != null ? onHold.getValue() : null;
    }

    @Transactional(value = "jamiTransactionManager")
    public void loadData( ComponentSystemEvent event ) {
        if (!FacesContext.getCurrentInstance().isPostback()) {

            if ( ac != null ) {
                if ( complex == null || !ac.equals( complex.getAc() )) {
                    complex = loadJamiByAc(IntactComplex.class, ac);
                }
            } else {
                ac = complex.getAc();
            }

            if (complex == null) {
                addErrorMessage("No Complex with this AC", ac);
                return;
            }
            setComplex(complex);

            if (complex != null) {
                if (!complex.areParticipantsInitialized()) {
                    Hibernate.initialize(complex.getParticipants());
                }
                refreshParticipants();
            }

            refreshTabsAndFocusXref();
        }

        generalLoadChecks();
    }

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

        if (component.getAc() == null) {
            complex.removeParticipant(component);
            refreshParticipants();
        } else {
            getChangesController().markToDelete(component, (IntactComplex)component.getInteraction());
        }
    }

    @Override
    public boolean doSaveDetails() {
        boolean saved = true;

        refreshParticipants();
        return saved;
    }

    public void refreshParticipants() {
        participantWrappers = new LinkedList<ModelledParticipantWrapper>();

        final Collection<ModelledParticipant> components = complex.getParticipants();

        for ( ModelledParticipant component : components ) {
            participantWrappers.add( new ModelledParticipantWrapper( (IntactModelledParticipant)component, getChangesController(), null ) );
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

        getChangesController().revertComplex(complex, Collections.EMPTY_LIST);
    }

    public String getAc() {
        if ( ac == null && complex != null ) {
            return complex.getAc();
        }
        return ac;
    }

    public int countParticipantsByInteractionAc( String ac ) {
        String sql = "select size(c.participants) from IntactComplex c where c.ac = '"+ac+"'";
        return ((Long)getJamiEntityManager().createQuery(sql).getSingleResult()).intValue();
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

    /**
     * When reverting, we need to refresh the collection of wrappers because they are not part of the IntAct model.
     */
    @Override
    protected void postRevert() {
        refreshParticipants();
    }

    public void cloneParticipant(ModelledParticipantWrapper participantWrapper) {
        IntactModelledParticipant participant = participantWrapper.getParticipant();

        IntactModelledParticipant clone = (IntactModelledParticipant)ParticipantJamiCloner.cloneParticipant(participant);
        addParticipant(clone);
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
        if (fIterator1.hasNext()){
            IntactModelledFeature f1 = fIterator1.next();

            for (IntactModelledFeature f2 : selected){
                if (f1 != f2){
                    f1.getLinkedFeatures().add(f2);
                    f2.getLinkedFeatures().add(f1);
                }
            }
        }


        addInfoMessage("Features linked", "Size of linked features : "+selected.size());
        setUnsavedChanges(true);

    }

    public void unlinkFeature(IntactModelledFeature feature1, IntactModelledFeature feature2) {
        feature1.getLinkedFeatures().remove(feature2);
        feature2.getLinkedFeatures().remove(feature1);

        addInfoMessage("Feature unlinked", feature2.toString());
        setUnsavedChanges(true);
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
        }
    }

    public Collection<ModelledParticipantWrapper> getParticipants() {
        return participantWrappers;
    }

    //////////////////////////////////
    // Participant related methods

    public String getInteractorIdentity(psidev.psi.mi.jami.model.Interactor interactor) {
        if (interactor == null) return null;

        Xref ref = interactor.getPreferredIdentifier();
        if (ref == null){
            return interactor.getShortName();
        }
        return ref.getId();
    }

    public boolean isFeaturesAvailable(){
        boolean featuresAvailable = false;
        Complex interaction = this.complex;
        for(ModelledParticipant component : interaction.getParticipants()){
            featuresAvailable = featuresAvailable || (component.getFeatures().size() > 0);
            if(featuresAvailable){
                continue;
            }
        }
        return featuresAvailable;
    }

    // Confidence
    ///////////////////////////////////////////////

    public void newConfidence() {
        ComplexConfidence confidence = new ComplexConfidence();
        complex.getModelledConfidences().add(confidence);
    }

    public List<ModelledConfidence> getConfidences() {
        if (complex == null) return Collections.EMPTY_LIST;
        return new ArrayList<ModelledConfidence>(complex.getModelledConfidences());
    }

    public List<ModelledParameter> getParameters() {
        if (complex == null) return Collections.EMPTY_LIST;
        return new ArrayList<ModelledParameter>(complex.getModelledParameters());
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
        this.complex.getDbXrefs().add(new InteractorXref());
        setUnsavedChanges(true);
    }

    @Override
    public void newAnnotation(ActionEvent evt) {
        this.complex.getAnnotations().add(new InteractorAnnotation());
        setUnsavedChanges(true);
    }

    @Override
    public void newAlias(ActionEvent evt) {
        this.complex.getAliases().add(new InteractorAlias());
        setUnsavedChanges(true);
    }

    @Override
    public String getCautionMessage() {
        psidev.psi.mi.jami.model.Annotation caution = AnnotationUtils.collectFirstAnnotationWithTopic(this.complex.getAnnotations(), psidev.psi.mi.jami.model.Annotation.CAUTION_MI,
                psidev.psi.mi.jami.model.Annotation.CAUTION);
        return caution != null ? caution.getValue() : null;
    }

    @Override
    public String getCautionMessage(IntactPrimaryObject ao) {
        IntactComplex complex = (IntactComplex)ao;
        psidev.psi.mi.jami.model.Annotation caution = AnnotationUtils.collectFirstAnnotationWithTopic(complex.getAnnotations(), psidev.psi.mi.jami.model.Annotation.CAUTION_MI,
                psidev.psi.mi.jami.model.Annotation.CAUTION);
        return caution != null ? caution.getValue() : null;
    }

    @Override
    public String getInternalRemarkMessage() {
        psidev.psi.mi.jami.model.Annotation caution = AnnotationUtils.collectFirstAnnotationWithTopic(this.complex.getAnnotations(), null,
                "remark-internal");
        return caution != null ? caution.getValue() : null;
    }

    @Override
    public List getAnnotations() {
        return new ArrayList(this.complex.getAnnotations());
    }

    @Override
    public List getAliases() {
        return new ArrayList(this.complex.getAliases());
    }

    public boolean isNewPublication() {
        return complex.getStatus().equals(LifeCycleStatus.NEW);
    }

    public boolean isAssigned() {
        return complex.getStatus().equals(LifeCycleStatus.ASSIGNED);
    }

    public boolean isCurationInProgress() {
        return complex.getStatus().equals(LifeCycleStatus.CURATION_IN_PROGRESS);
    }

    public boolean isReadyForChecking() {
        return complex.getStatus().equals(LifeCycleStatus.READY_FOR_CHECKING);
    }

    public boolean isReadyForRelease() {
        return complex.getStatus().equals(LifeCycleStatus.READY_FOR_RELEASE);
    }

    public boolean isAcceptedOnHold() {
        return complex.getStatus().equals(LifeCycleStatus.ACCEPTED_ON_HOLD);
    }

    public boolean isReleased() {
        return complex.getStatus().equals(LifeCycleStatus.RELEASED);
    }

    public void claimOwnership(ActionEvent evt) {
        lifecycleManager.getGlobalStatus().changeOwnership(complex, getCurrentJamiUser(), null);

        // automatically set as curation in progress if no one was assigned before
        if (isAssigned()) {
            markAsCurationInProgress(evt);
        }

        addInfoMessage("Claimed Complex ownership", "You are now the owner of this complex");
    }

    public void markAsAssignedToMe(ActionEvent evt) {
        lifecycleManager.getNewStatus().assignToCurator(complex, getCurrentJamiUser());

        addInfoMessage("Ownership claimed", "The complex has been assigned to you");

        markAsCurationInProgress(evt);
    }

    public void markAsCurationInProgress(ActionEvent evt) {
        if (!userSessionController.isItMe(complex.getCurrentOwner())) {
            addErrorMessage("Cannot mark as curation in progress", "You are not the owner of this publication");
            return;
        }

        lifecycleManager.getAssignedStatus().startCuration(complex);

        addInfoMessage("Curation started", "Curation is now in progress");
    }

    public void markAsReadyForChecking(ActionEvent evt) {
        if (!userSessionController.isItMe(complex.getCurrentOwner())) {
            addErrorMessage("Cannot mark as Ready for checking", "You are not the owner of this complex");
            return;
        }
        if (isBeenRejectedBefore()) {
            psidev.psi.mi.jami.model.Annotation correctionCommentAnnot = AnnotationUtils.collectFirstAnnotationWithTopic(complex.getAnnotations(), null
                    , CvTopic.CORRECTION_COMMENT);

            if (correctionCommentAnnot != null) {
                reasonForReadyForChecking = correctionCommentAnnot.getValue();
            }
            else{
                reasonForReadyForChecking = null;
            }
        }

        // TODO run a proper sanity check
        boolean sanityCheckPassed = true;

        lifecycleManager.getCurationInProgressStatus().readyForChecking(complex, reasonForReadyForChecking, sanityCheckPassed);

        reasonForReadyForChecking = null;

        addInfoMessage("Complex ready for checking", "Assigned to reviewer: " + complex.getCurrentReviewer().getLogin());
    }

    public void revertReadyForChecking(ActionEvent evt) {
        lifecycleManager.getReadyForCheckingStatus().revert(this.complex);
    }

    public void revertAccepted(ActionEvent evt) {
        lifecycleManager.getReadyForReleaseStatus().revert(this.complex);
    }

    public void putOnHold(ActionEvent evt) {
        setOnHold(reasonForOnHoldFromDialog);

        if (complex.getStatus().equals(LifeCycleStatus.READY_FOR_RELEASE)) {
            lifecycleManager.getReadyForReleaseStatus().putOnHold(complex, reasonForOnHoldFromDialog);
            addInfoMessage("On-hold added to complex", "Complex won't be released until the 'on hold' is removed");
        } else if (complex.getStatus().equals(LifeCycleStatus.RELEASED)) {
            lifecycleManager.getReleasedStatus().putOnHold(complex, reasonForOnHoldFromDialog);
            addInfoMessage("On-hold added to released complex", "Data will be publicly visible until the next release");
        }

        reasonForOnHoldFromDialog = null;
    }

    public void readyForReleaseFromOnHold(ActionEvent evt) {
        this.complex.removeOnHold();

        lifecycleManager.getAcceptedOnHoldStatus().onHoldRemoved(complex, null);
    }

    public void setOnHold(String reason) {
        this.complex.onHold(reason);
    }

    public void onHoldChanged(ValueChangeEvent evt) {
        setUnsavedChanges(true);

        this.complex.onHold((String) evt.getNewValue());
    }

    public boolean isAccepted() {
        if (complex == null || complex.getStatus() == null) {
            return false;
        }

        return complex.getStatus().equals(LifeCycleStatus.ACCEPTED) ||
                complex.getStatus().equals(LifeCycleStatus.ACCEPTED_ON_HOLD)||
                complex.getStatus().equals(LifeCycleStatus.READY_FOR_RELEASE) ||
                complex.getStatus().equals(LifeCycleStatus.RELEASED);
    }

    public boolean isAccepted(IntactComplex pub) {
        return pub.getStatus().equals(LifeCycleStatus.ACCEPTED) ||
                pub.getStatus().equals(LifeCycleStatus.ACCEPTED_ON_HOLD)||
                pub.getStatus().equals(LifeCycleStatus.READY_FOR_RELEASE) ||
                pub.getStatus().equals(LifeCycleStatus.RELEASED);
    }

    public boolean isToBeReviewed(IntactComplex pub) {
        return AnnotationUtils.collectFirstAnnotationWithTopic(pub.getAnnotations(), null, "to-be-reviewed") != null;
    }

    public void acceptComplex(ActionEvent evt) {
        UserSessionController userSessionController = (UserSessionController) getSpringContext().getBean("userSessionController");

        //clear to-be-reviewed
        AnnotationUtils.removeAllAnnotationsWithTopic(this.complex.getAnnotations(), null, "to-be-reviewed");

        lifecycleManager.getReadyForCheckingStatus().accept(complex, "Accepted " + new SimpleDateFormat("yyyy-MMM-dd").format(new Date()).toUpperCase() + " by " + userSessionController.getCurrentUser().getLogin().toUpperCase());

        if (!complex.isOnHold()) {
            lifecycleManager.getAcceptedStatus().readyForRelease(complex, "Accepted and not on-hold");
        }
    }

    public void rejectComplex(ActionEvent evt) {

        rejectComplex(reasonForRejection);

    }

    public void rejectComplex(String reasonForRejection) {
        UserSessionController userSessionController = (UserSessionController) getSpringContext().getBean("userSessionController");
        String date = "Rejected " + new SimpleDateFormat("yyyy-MMM-dd").format(new Date()).toUpperCase() + " by " + userSessionController.getCurrentUser().getLogin().toUpperCase();

        this.complex.onToBeReviewed(date + ". " + reasonForRejection);

        addInfoMessage("Complex rejected", "");

        lifecycleManager.getReadyForCheckingStatus().reject(complex, reasonForRejection);
    }

    public boolean isRejected(IntactComplex complex) {
        return complex.isToBeReviewed();
    }

    public String calculateStatusStyle(IntactComplex complex) {
        if (isAccepted(complex)) {
            return "ia-accepted";
        }

        int timesRejected = 0;
        int timesReadyForChecking = 0;

        for (LifeCycleEvent evt : complex.getLifecycleEvents()) {
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

    public boolean isBeenRejectedBefore() {
        for (LifeCycleEvent evt : complex.getLifecycleEvents()) {
            if (LifeCycleEventType.REJECTED.equals(evt.getEvent())) {
                return true;
            }
        }

        return false;
    }

    public String getToBeReviewed() {
        Annotation annot = AnnotationUtils.collectFirstAnnotationWithTopic(complex.getAnnotations(), null, "to-be-reviewed");
        return annot != null ? annot.getValue() : null;
    }

    public void clearToBeReviewed(ActionEvent evt) {
        AnnotationUtils.removeAllAnnotationsWithTopic(complex.getAnnotations(), null, "to-be-reviewed");
    }

    public boolean isAssignToMe() {
        return assignToMe;
    }

    public void setAssignToMe(boolean assignToMe) {
        this.assignToMe = assignToMe;
    }

    public String getReasonForReadyForChecking() {
        return reasonForReadyForChecking;
    }

    public void setReasonForReadyForChecking(String reasonForReadyForChecking) {
        this.reasonForReadyForChecking = reasonForReadyForChecking;
    }

    public String getReasonForOnHoldFromDialog() {
        return reasonForOnHoldFromDialog;
    }

    public void setReasonForOnHoldFromDialog(String reasonForOnHoldFromDialog) {
        this.reasonForOnHoldFromDialog = reasonForOnHoldFromDialog;
    }

    public String newComplex(Interaction interactionEvidence) {
        if (interactionEvidence == null || interactionEvidence.getAc() == null) {
            addErrorMessage("Cannot create biological complex", "Interaction evidence is empty or not saved");
            return null;
        }

        IntactDao intactDao = ApplicationContextProvider.getBean("intactDao");
        setComplex((IntactComplex)ComplexJamiCloner.cloneInteraction(intactDao.getInteractionDao().getByAc(interactionEvidence.getAc())));

        return "/curate/complex?faces-redirect=true";
    }

    @Transactional(value = "jamiTransactionManager")
    public String newComplex() {

        IntactDao intactDao = ApplicationContextProvider.getBean("intactDao");
        CvTermDao dao = intactDao.getCvTermDao();
        CvTerm type = dao.getByMIIdentifier(Complex.COMPLEX_MI, IntactUtils.INTERACTOR_TYPE_OBJCLASS);

        setComplex(new IntactComplex("name to specify"));
        this.complex.setInteractorType(type);
        lifecycleManager.getStartStatus().create(this.complex, "Created in Editor");

        if (assignToMe) {
            lifecycleManager.getNewStatus().claimOwnership(this.complex);
            lifecycleManager.getAssignedStatus().startCuration(this.complex);
        }

        return "/curate/complex?faces-redirect=true";
    }
}
