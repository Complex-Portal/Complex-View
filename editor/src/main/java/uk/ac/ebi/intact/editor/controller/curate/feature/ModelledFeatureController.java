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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.jami.model.*;
import psidev.psi.mi.jami.utils.AnnotationUtils;
import psidev.psi.mi.jami.utils.RangeUtils;
import uk.ac.ebi.intact.editor.controller.curate.AnnotatedObjectController;
import uk.ac.ebi.intact.editor.controller.curate.cloner.FeatureIntactCloner;
import uk.ac.ebi.intact.editor.controller.curate.cloner.FeatureJamiCloner;
import uk.ac.ebi.intact.editor.controller.curate.interaction.ComplexController;
import uk.ac.ebi.intact.editor.controller.curate.participant.ModelledParticipantController;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.context.UserContext;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.IntactPrimaryObject;
import uk.ac.ebi.intact.jami.model.extension.*;
import uk.ac.ebi.intact.jami.synchronizer.FinderException;
import uk.ac.ebi.intact.jami.synchronizer.IntactDbSynchronizer;
import uk.ac.ebi.intact.jami.synchronizer.PersisterException;
import uk.ac.ebi.intact.jami.synchronizer.SynchronizerException;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.clone.IntactCloner;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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

    private List<SelectItem> participantSelectItems;
    private boolean isComplexFeature=false;

    private String cautionMessage = null;
    private String internalRemark = null;

    public ModelledFeatureController() {
    }

    @Override
    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public String clone() {
        if (!getJamiEntityManager().contains(getFeature())){
            IntactModelledFeature reloadedFeature = getJamiEntityManager().merge(this.feature);
            setFeature(reloadedFeature);
        }

        String value = clone(getFeature());

        getJamiEntityManager().detach(getFeature());

        return value;
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
        setFeature((IntactModelledFeature)annotatedObject);
    }

    private void refreshParticipantSelectItems() {
        participantSelectItems = new ArrayList<SelectItem>();
        participantSelectItems.add(new SelectItem(null, "select participant", "select participant", false, false, true));

        if (this.feature.getParticipant() != null){
            ModelledEntity modelledParticipant = this.feature.getParticipant();
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

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void loadData( ComponentSystemEvent event ) {
        if (!FacesContext.getCurrentInstance().isPostback()) {
            if ( (feature == null && ac != null) || (ac != null && feature != null && !ac.equals( feature.getAc() ))) {
                setFeature(loadByJamiAc(IntactModelledFeature.class, ac));
            }

            if (feature == null) {
                super.addErrorMessage("Feature does not exist", ac);
                return;
            }

            final ModelledEntity participant = feature.getParticipant();

            if (modelledParticipantController.getParticipant() == null) {
                modelledParticipantController.setParticipant((IntactModelledParticipant)participant);
            }

            if( complexController.getComplex() == null ) {
                final Complex interaction = (Complex)((IntactModelledParticipant)participant).getInteraction();
                complexController.setComplex((IntactComplex) interaction);
            }

            refreshTabsAndFocusXref();
            if (containsInvalidRanges) {
                addWarningMessage("This feature contains invalid ranges", "Ranges must be fixed before being able to save");
            }

            generalJamiLoadChecks();
        }
    }

    public void refreshRangeWrappers() {
        this.rangeWrappers = new ArrayList<ModelledRangeWrapper>(feature.getRanges().size());

        String sequence = getSequence();

        containsInvalidRanges = false;

        for (psidev.psi.mi.jami.model.Range range : feature.getRanges()) {
            // override sequence if range has a participant
            if (range.getParticipant() != null){
                psidev.psi.mi.jami.model.Interactor interactor = range.getParticipant().getInteractor();

                if (interactor instanceof psidev.psi.mi.jami.model.Polymer) {
                    psidev.psi.mi.jami.model.Polymer polymer = (psidev.psi.mi.jami.model.Polymer) interactor;
                    sequence = polymer.getSequence();
                }
            }
            rangeWrappers.add(new ModelledRangeWrapper((ModelledRange)range, sequence, this));

            if (!containsInvalidRanges && !RangeUtils.validateRange(range, sequence).isEmpty()) {
                containsInvalidRanges = true;
            }
        }
    }

    @Override
    protected IntactCloner newClonerInstance() {
        return new FeatureIntactCloner();
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void refreshRangeProperties(ModelledRange range){
        try {
            getIntactDao().getSynchronizerContext().getModelledRangeSynchronizer().synchronizeProperties(range);
        } catch (FinderException e) {
            addErrorMessage("Cannot synchronize range" + range, e.getMessage());
        } catch (PersisterException e) {
            addErrorMessage("Cannot synchronize range" + range, e.getMessage());
        } catch (SynchronizerException e) {
            addErrorMessage("Cannot synchronize range" + range, e.getMessage());
        }
    }

    public String newFeature(ModelledParticipant participant) {
        IntactModelledFeature feature = new IntactModelledFeature();
        feature.setParticipant(participant);
        feature.setShortName(null);
        feature.setCreated(new Date());
        UserContext jamiUserContext = ApplicationContextProvider.getBean("jamiUserContext");
        feature.setCreator(jamiUserContext.getUserId());
        feature.setUpdator(jamiUserContext.getUserId());
        feature.setUpdated(feature.getCreated());

        setFeature(feature);

        refreshRangeWrappers();
        changed();

        return navigateToJamiObject(feature);
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
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
        IntactDao dao = getIntactDao();
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
            refreshRangeWrappers();
            refreshInfoMessages();
            refreshParticipantSelectItems();
        }
        else{
            this.ac = null;
        }
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
        return this.cautionMessage;
    }

    @Override
    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public String getJamiCautionMessage(IntactPrimaryObject ao) {
        Collection<Annotation> annots = getIntactDao().getModelledFeatureDao().getAnnotationsForFeature(ao.getAc());
        psidev.psi.mi.jami.model.Annotation caution = AnnotationUtils.collectFirstAnnotationWithTopic(annots, psidev.psi.mi.jami.model.Annotation.CAUTION_MI,
                psidev.psi.mi.jami.model.Annotation.CAUTION);
        return caution != null ? caution.getValue() : null;
    }

    @Override
    public String getInternalRemarkMessage() {
        return this.internalRemark;
    }

    @Override
    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public List collectXrefs() {
        if (!this.feature.areXrefsInitialized()){
            IntactModelledFeature reloaded = getJamiEntityManager().merge(this.feature);
            setFeature(reloaded);
        }

        List<Xref> xrefs = new ArrayList<Xref>(this.feature.getDbXrefs());

        getJamiEntityManager().detach(this.feature);
        return xrefs;
    }

    @Override
    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public List collectAliases() {
        if (!this.feature.areAliasesInitialized()){
            IntactModelledFeature reloaded = getJamiEntityManager().merge(this.feature);
            setFeature(reloaded);
        }

        List<Alias> aliases = new ArrayList<Alias>(this.feature.getAliases());

        getJamiEntityManager().detach(this.feature);
        return aliases;
    }

    @Override
    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public List collectAnnotations() {
        if (!this.feature.areAnnotationsInitialized()){
            IntactModelledFeature reloaded = getJamiEntityManager().merge(this.feature);
            setFeature(reloaded);
        }

        List<Annotation> xrefs = new ArrayList<Annotation>(this.feature.getAnnotations());

        getJamiEntityManager().detach(this.feature);
        return xrefs;
    }

    @Override
    public IntactDbSynchronizer getDbSynchronizer() {
        return getIntactDao().getSynchronizerContext().getModelledFeatureSynchronizer();
    }

    @Override
    public String getJamiObjectName() {
        return this.feature.getShortName();
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public int getAliasesSize() {
        if (this.feature.areAliasesInitialized()){
            return this.feature.getAliases().size();
        }
        else{
            return getIntactDao().getModelledFeatureDao().countAliasesForFeature(this.ac);
        }
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public int getAnnotationsSize() {
        if (this.feature.areAnnotationsInitialized()){
            return this.feature.getAnnotations().size();
        }
        else{
            return getIntactDao().getModelledFeatureDao().countAnnotationsForFeature(this.ac);
        }
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public int getXrefsSize() {
        if (this.feature.areXrefsInitialized()){
            return this.feature.getDbXrefs().size();
        }
        else{
            return getIntactDao().getModelledFeatureDao().countXrefsForFeature(this.ac);
        }
    }

    public void removeJamiAlias(Alias alias){
        this.feature.getAliases().remove(alias);
    }

    public void removeJamiXref(Xref xref){
        this.feature.getDbXrefs().remove(xref);
    }

    public void removeJamiAnnotation(Annotation annot){
        this.feature.getAnnotations().remove(annot);
    }

    public boolean isAliasNotEditable(Alias alias){
        return false;
    }

    public boolean isAnnotationNotEditable(Annotation annot){
        return false;
    }

    @Override
    public void doPostSave(){
        modelledParticipantController.refreshFeatures();
    }

    private void refreshInfoMessages() {
        Annotation remark = AnnotationUtils.collectFirstAnnotationWithTopic(this.feature.getAnnotations(), null,
                "remark-internal");
        this.internalRemark = remark != null ? remark.getValue() : null;
        Annotation caution = AnnotationUtils.collectFirstAnnotationWithTopic(this.feature.getAnnotations(), Annotation.CAUTION_MI,
                Annotation.CAUTION);
        this.cautionMessage = caution != null ? caution.getValue() : null;
    }

    @Override
    protected boolean areXrefsInitialised() {
        return this.feature.areXrefsInitialized();
    }
}