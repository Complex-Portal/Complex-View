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
package uk.ac.ebi.intact.editor.controller.curate.feature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.primefaces.event.TabChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.jami.model.Complex;
import psidev.psi.mi.jami.model.ModelledParticipant;
import psidev.psi.mi.jami.model.OntologyTerm;
import psidev.psi.mi.jami.utils.AnnotationUtils;
import psidev.psi.mi.jami.utils.RangeUtils;
import uk.ac.ebi.intact.editor.controller.curate.AnnotatedObjectController;
import uk.ac.ebi.intact.editor.controller.curate.cloner.FeatureIntactCloner;
import uk.ac.ebi.intact.editor.controller.curate.cloner.FeatureJamiCloner;
import uk.ac.ebi.intact.editor.controller.curate.interaction.ComplexController;
import uk.ac.ebi.intact.editor.controller.curate.participant.ModelledParticipantController;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.dao.CvTermDao;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.IntactPrimaryObject;
import uk.ac.ebi.intact.jami.model.extension.*;
import uk.ac.ebi.intact.jami.synchronizer.FinderException;
import uk.ac.ebi.intact.jami.synchronizer.PersisterException;
import uk.ac.ebi.intact.jami.synchronizer.SynchronizerException;
import uk.ac.ebi.intact.jami.utils.IntactUtils;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.clone.IntactCloner;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Modelled Feature controller.
 *
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "general" )
public class ModelledFeatureController extends AnnotatedObjectController {

    private static final Log log = LogFactory.getLog( ModelledFeatureController.class );

    private IntactModelledFeature feature;
    private List<ModelledRangeWrapper> rangeWrappers;
    private boolean containsInvalidRanges;

    /**
     * The AC of the feature to be loaded.
     */
    private String ac;

    @Autowired
    private ComplexController complexController;

    @Autowired
    private ModelledParticipantController modelledParticipantController;

    private String newRangeValue;

    private boolean isRangeDisabled;

    private List<SelectItem> typeSelectItems;
    private List<SelectItem> roleSelectItems;
    private List<SelectItem> participantSelectItems;
    private List<SelectItem> aliasTypeSelectItems;
    private List<SelectItem> featureTopicSelectItems;
    private List<SelectItem> databaseSelectItems;
    private List<SelectItem> qualifierSelectItems;
    private List<SelectItem> fuzzyTypeSelectItems;
    private boolean isComplexFeature=false;

    public ModelledFeatureController() {
    }

    @PostConstruct
    @Transactional(value = "jamiTransactionManager")
    public void loadData() {
        typeSelectItems = new ArrayList<SelectItem>();
        typeSelectItems.add(new SelectItem( null, "select type", "select type", false, false, true ));
        roleSelectItems = new ArrayList<SelectItem>();
        roleSelectItems.add(new SelectItem( null, "select role", "select role", false, false, true ));
        participantSelectItems = new ArrayList<SelectItem>();
        participantSelectItems.add(new SelectItem( null, "select participant", "select participant", false, false, true ));
        aliasTypeSelectItems = new ArrayList<SelectItem>();
        aliasTypeSelectItems.add(new SelectItem( null, "select type", "select type", false, false, true ));
        featureTopicSelectItems = new ArrayList<SelectItem>();
        featureTopicSelectItems.add(new SelectItem( null, "select topic", "select topic", false, false, true ));
        databaseSelectItems = new ArrayList<SelectItem>();
        databaseSelectItems.add(new SelectItem( null, "select database", "select database", false, false, true ));
        qualifierSelectItems = new ArrayList<SelectItem>();
        qualifierSelectItems.add(new SelectItem( null, "select qualifier", "select qualifier", false, false, true ));
        fuzzyTypeSelectItems = new ArrayList<SelectItem>();
        fuzzyTypeSelectItems.add(new SelectItem(null, "select status", "select status", false, false, true));

        IntactDao intactDao = ApplicationContextProvider.getBean("intactDao");
        CvTermDao cvDao = intactDao.getCvTermDao();

        IntactCvTerm typeParent = cvDao.getByMIIdentifier("MI:0116", IntactUtils.FEATURE_TYPE_OBJCLASS);
        loadChildren(typeParent, typeSelectItems);

        IntactCvTerm roleParent = cvDao.getByMIIdentifier("MI:0925", IntactUtils.TOPIC_OBJCLASS);
        SelectItem item = createSelectItem(roleParent);
        if (item != null){
            roleSelectItems.add(item);
        }
        loadChildren(roleParent, roleSelectItems);

        IntactCvTerm aliasTypeParent = cvDao.getByMIIdentifier("MI:0300", IntactUtils.ALIAS_TYPE_OBJCLASS);
        loadChildren(aliasTypeParent, aliasTypeSelectItems);

        IntactCvTerm featureTopicParent = cvDao.getByMIIdentifier("MI:0668", IntactUtils.TOPIC_OBJCLASS);
        loadChildren(featureTopicParent, featureTopicSelectItems);

        IntactCvTerm databaseParent = cvDao.getByMIIdentifier("MI:0447", IntactUtils.DATABASE_OBJCLASS);
        loadChildren(databaseParent, databaseSelectItems);

        IntactCvTerm qualifierParent = cvDao.getByMIIdentifier("MI:0353", IntactUtils.QUALIFIER_OBJCLASS);
        loadChildren(qualifierParent, qualifierSelectItems);

        IntactCvTerm statusParent = cvDao.getByMIIdentifier("MI:0333", IntactUtils.RANGE_STATUS_OBJCLASS);
        loadChildren(statusParent, fuzzyTypeSelectItems);
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

    public List<SelectItem> getTypeSelectItems() {
        return typeSelectItems;
    }

    public List<SelectItem> getRoleSelectItems() {
        return roleSelectItems;
    }

    public List<SelectItem> getParticipantSelectItems() {
        return participantSelectItems;
    }

    public List<SelectItem> getAliasTypeSelectItems() {
        return aliasTypeSelectItems;
    }

    public List<SelectItem> getFeatureTopicSelectItems() {
        return featureTopicSelectItems;
    }

    public List<SelectItem> getDatabaseSelectItems() {
        return databaseSelectItems;
    }

    public List<SelectItem> getQualifierSelectItems() {
        return qualifierSelectItems;
    }

    public List<SelectItem> getFuzzyTypeSelectItems() {
        return fuzzyTypeSelectItems;
    }

    @Override
    protected IntactModelledFeature cloneAnnotatedObject(IntactPrimaryObject ao) {
        // to be overrided
        return (IntactModelledFeature) FeatureJamiCloner.cloneFeature((IntactModelledFeature)ao);
    }

    @Override
    public AnnotatedObject getAnnotatedObject() {
        return null;
    }

    @Override
    public void setAnnotatedObject(AnnotatedObject annotatedObject) {
        // does nothing
    }

    @Override
    public IntactPrimaryObject getJamiObject() {
        return this.feature;
    }

    @Override
    public void setJamiObject(IntactPrimaryObject annotatedObject) {
        this.feature = (IntactModelledFeature)annotatedObject;

        refreshParticipantSelectItems();
    }

    private void refreshParticipantSelectItems() {
        this.participantSelectItems.clear();

        if (this.feature.getParticipant() != null){
            ModelledParticipant modelledParticipant = this.feature.getParticipant();
            if (modelledParticipant.getInteractor() instanceof psidev.psi.mi.jami.model.Complex){
                isComplexFeature = true;
                loadParticipants((Complex)modelledParticipant.getInteractor(), this.participantSelectItems);
            }
            else{
                isComplexFeature = false;
            }
        }
    }

    private void loadParticipants(Complex parent, List<SelectItem> selectItems){
        for (ModelledParticipant child : parent.getParticipants()){
            IntactModelledParticipant part = (IntactModelledParticipant)child;
            if (part.getInteractor() instanceof Complex){
                loadParticipants((Complex)part.getInteractor(), selectItems);
            }
            else{
                SelectItem item = new SelectItem( part, part.getInteractor().getShortName()+", "+part.getAc(), part.getInteractor().getFullName());
                selectItems.add(item);
            }
        }
    }

    @Override
    public String goToParent() {
        return "/curate/jparticipant?faces-redirect=true&includeViewParams=true";
    }

    @Transactional(value = "jamiTransactionManager")
    public void loadData( ComponentSystemEvent event ) {
        if (!FacesContext.getCurrentInstance().isPostback()) {

            if ( ac != null ) {
                if ( feature == null || !ac.equals( feature.getAc() ) ) {
                    feature = loadJamiByAc(IntactModelledFeature.class, ac);
                }
            } else {
                if ( feature != null ) ac = feature.getAc();
            }

            if (feature == null) {
                super.addErrorMessage("Feature does not exist", ac);
                return;
            }

            final ModelledParticipant participant = feature.getParticipant();

            if (modelledParticipantController.getParticipant() == null) {
                modelledParticipantController.setParticipant((IntactModelledParticipant)participant);
            }

            if( complexController.getComplex() == null ) {
                final Complex interaction = (Complex)participant.getInteraction();
                complexController.setComplex((IntactComplex) interaction);
            }

            refreshTabsAndFocusXref();
        }

        refreshRangeWrappers();

        if (containsInvalidRanges) {
            addWarningMessage("This feature contains invalid ranges", "Ranges must be fixed before being able to save");
        }

        generalLoadChecks();
    }

    public void refreshRangeWrappers() {
        this.rangeWrappers = new ArrayList<ModelledRangeWrapper>(feature.getRanges().size());

        String sequence = getSequence();

        containsInvalidRanges = false;

        for (psidev.psi.mi.jami.model.Range range : feature.getRanges()) {
            rangeWrappers.add(new ModelledRangeWrapper((ModelledRange)range, sequence));

            if (!containsInvalidRanges && !RangeUtils.validateRange(range, sequence).isEmpty()) {
                containsInvalidRanges = true;
            }
        }
    }

    @Override
    protected IntactCloner newClonerInstance() {
        return new FeatureIntactCloner();
    }

    public String newFeature(ModelledParticipant participant) {
        IntactModelledFeature feature = new IntactModelledFeature();
        feature.setParticipant(participant);
        feature.setShortName(null);

        setFeature(feature);

        refreshRangeWrappers();
        changed();

        return navigateToObject(feature);
    }

    @Transactional(value = "jamiTransactionManager")
    public void newRange(ActionEvent evt) {
        if (newRangeValue == null || newRangeValue.isEmpty()) {
            addErrorMessage("Range value field is empty", "Please provide a range value before clicking on the New Range button");
            return;
        }

        newRangeValue = newRangeValue.trim();

        if (!newRangeValue.contains("-")) {
            addErrorMessage("Illegal range value", "The range must contain a hyphen");
            return;
        }

        String sequence = getSequence();
        psidev.psi.mi.jami.model.Range range = null;
        try {
            range = RangeUtils.createRangeFromString(newRangeValue);
            List<String> messages = RangeUtils.validateRange(range, sequence);
            if (!messages.isEmpty()) {
                for (String msg : messages){
                    addErrorMessage("Range is not valid", msg);
                }
                return;
            }
        } catch (psidev.psi.mi.jami.exception.IllegalRangeException e) {
            addErrorMessage("Range is not valid", e.getMessage());
        }

        range.setLink(false);
        range.setResultingSequence(new ModelledResultingSequence(RangeUtils.extractRangeSequence(range, sequence), null));
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");
        try {
            range = dao.getSynchronizerContext().getModelledRangeSynchronizer().synchronize(range, false);
        } catch (FinderException e) {
            addErrorMessage("Cannot create new Range", e.getMessage());
        } catch (PersisterException e) {
            addErrorMessage("Cannot create new Range", e.getMessage());
        } catch (SynchronizerException e) {
            addErrorMessage("Cannot create new Range", e.getMessage());
        }

        feature.getRanges().add(range);

        refreshRangeWrappers();

        newRangeValue = null;

        setUnsavedChanges(true);
    }

    public void validateFeature(FacesContext context, UIComponent component, Object value) throws ValidatorException {

        if (feature.getRanges().isEmpty()) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Feature without ranges", "One range is mandatory");
            throw new ValidatorException(message);
        }
    }

    private String getSequence() {
        psidev.psi.mi.jami.model.Interactor interactor = feature.getParticipant().getInteractor();

        String sequence = null;

        if (interactor instanceof psidev.psi.mi.jami.model.Polymer) {
            psidev.psi.mi.jami.model.Polymer polymer = (psidev.psi.mi.jami.model.Polymer) interactor;
            sequence = polymer.getSequence();
        }
        return sequence;
    }

    public void markRangeToDelete(ModelledRange range) {
        if (range == null) return;

        feature.getRanges().remove(range);
        refreshRangeWrappers();
    }

    public List<ModelledRangeWrapper> getWrappedRanges() {
        return rangeWrappers;
    }

    public String getAc() {
        if ( ac == null && feature != null ) {
            return feature.getAc();
        }
        return ac;
    }

    public void setAc( String ac ) {
        this.ac = ac;
    }

    public IntactModelledFeature getFeature() {
        return feature;
    }

    @Override
    public void refreshTabsAndFocusXref(){
        refreshTabs();
    }

    @Override
    public void refreshTabs(){
        super.refreshTabs();

        this.isRangeDisabled = false;
    }

    public void setFeature( IntactModelledFeature feature ) {
        this.feature = feature;

        if (feature != null){
            this.ac = feature.getAc();
        }
        refreshParticipantSelectItems();
    }

    @Override
    public void doPreSave() {
        // the feature was just created, add it to the list of features of the participant
        if (feature.getAc() == null){
            modelledParticipantController.getParticipant().addFeature(feature);
        }
    }

    public String getNewRangeValue() {
        return newRangeValue;
    }

    public void setNewRangeValue(String newRangeValue) {
        this.newRangeValue = newRangeValue;
    }

    public boolean isContainsInvalidRanges() {
        return containsInvalidRanges;
    }

    public void setContainsInvalidRanges(boolean containsInvalidRanges) {
        this.containsInvalidRanges = containsInvalidRanges;
    }

    @Override
    public Collection<String> collectParentAcsOfCurrentAnnotatedObject(){
        Collection<String> parentAcs = new ArrayList<String>();

        if (feature.getParticipant() != null){
            IntactModelledParticipant comp = (IntactModelledParticipant)feature.getParticipant();
            if (comp.getAc() != null){
                parentAcs.add(comp.getAc());
            }

            addParentAcsTo(parentAcs, comp);
        }

        return parentAcs;
    }

    @Override
    protected void refreshUnsavedChangesBeforeRevert(){
        Collection<String> parentAcs = new ArrayList<String>();

        if (feature.getParticipant() != null){
            IntactModelledParticipant comp = (IntactModelledParticipant)feature.getParticipant();
            if (comp.getAc() != null){
                parentAcs.add(comp.getAc());
            }

            addParentAcsTo(parentAcs, comp);
        }

        getChangesController().revertFeature(feature, parentAcs);
    }

    /**
     * Get the publication ac of this participant if it exists, the ac of the interaction if it exists and the component ac if it exists and add it to the list or parentAcs
     * @param parentAcs
     * @param comp
     */
    private void addParentAcsTo(Collection<String> parentAcs, IntactModelledParticipant comp) {
        if (comp.getInteraction() != null){
            IntactComplex inter = (IntactComplex)comp.getInteraction();
            addParentAcsTo(parentAcs, inter);
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

    public boolean isRangeDisabled() {
        return isRangeDisabled;
    }

    public void setRangeDisabled(boolean rangeDisabled) {
        isRangeDisabled = rangeDisabled;
    }

    @Override
    public void modifyClone(AnnotatedObject clone) {
        refreshTabs();
    }

    public void onTabChanged(TabChangeEvent e) {

        // the xref tab is active
        super.onTabChanged(e);

        // all the tabs selectOneMenu are disabled, we can process the tabs specific to interaction
        if (isAliasDisabled() && isXrefDisabled() && isAnnotationTopicDisabled()){
            if (e.getTab().getId().equals("rangesTab")){
                isRangeDisabled = false;
            }
            else {
                isRangeDisabled = true;
            }
        }
        else {
            isRangeDisabled = true;
        }
    }

    public boolean isComplexFeature(){
        return this.isComplexFeature;
    }

    @Override
    protected boolean isPublicationParent() {
        return false;
    }

    @Override
    public void newXref(ActionEvent evt) {
        this.feature.getDbXrefs().add(new ModelledFeatureXref());
        setUnsavedChanges(true);
    }

    @Override
    public void newAnnotation(ActionEvent evt) {
        this.feature.getAnnotations().add(new ModelledFeatureAnnotation());
        setUnsavedChanges(true);
    }

    @Override
    public void newAlias(ActionEvent evt) {
        this.feature.getAliases().add(new ModelledFeatureAlias());
        setUnsavedChanges(true);
    }

    @Override
    public String getCautionMessage() {
        psidev.psi.mi.jami.model.Annotation caution = AnnotationUtils.collectFirstAnnotationWithTopic(this.feature.getAnnotations(), psidev.psi.mi.jami.model.Annotation.CAUTION_MI,
                psidev.psi.mi.jami.model.Annotation.CAUTION);
        return caution != null ? caution.getValue() : null;
    }

    @Override
    public String getCautionMessage(IntactPrimaryObject ao) {
        IntactModelledFeature feature = (IntactModelledFeature)ao;
        psidev.psi.mi.jami.model.Annotation caution = AnnotationUtils.collectFirstAnnotationWithTopic(feature.getAnnotations(), psidev.psi.mi.jami.model.Annotation.CAUTION_MI,
                psidev.psi.mi.jami.model.Annotation.CAUTION);
        return caution != null ? caution.getValue() : null;
    }

    @Override
    public String getInternalRemarkMessage() {
        psidev.psi.mi.jami.model.Annotation caution = AnnotationUtils.collectFirstAnnotationWithTopic(this.feature.getAnnotations(), null,
                "remark-internal");
        return caution != null ? caution.getValue() : null;
    }

    @Override
    public List getAnnotations() {
        return super.getAnnotations();
    }

    @Override
    public List getAliases() {
        return super.getAliases();
    }
}