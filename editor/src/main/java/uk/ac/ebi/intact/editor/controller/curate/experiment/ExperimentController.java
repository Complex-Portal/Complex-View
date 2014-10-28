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
package uk.ac.ebi.intact.editor.controller.curate.experiment;

import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.hibernate.Hibernate;
import org.primefaces.context.RequestContext;
import org.primefaces.model.LazyDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.persister.IntactCore;
import uk.ac.ebi.intact.editor.controller.UserSessionController;
import uk.ac.ebi.intact.editor.controller.curate.AnnotatedObjectController;
import uk.ac.ebi.intact.editor.controller.curate.ChangesController;
import uk.ac.ebi.intact.editor.controller.curate.PersistenceController;
import uk.ac.ebi.intact.editor.controller.curate.cloner.ExperimentIntactCloner;
import uk.ac.ebi.intact.editor.controller.curate.publication.PublicationController;
import uk.ac.ebi.intact.editor.util.CurateUtils;
import uk.ac.ebi.intact.editor.util.LazyDataModelFactory;
import uk.ac.ebi.intact.jami.model.IntactPrimaryObject;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.ExperimentUtils;
import uk.ac.ebi.intact.model.util.PublicationUtils;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "general" )
public class ExperimentController extends AnnotatedObjectController {

    private Experiment experiment;
    private String ac;
    private LazyDataModel<Interaction> interactionDataModel;

    private String reasonForRejection;
    private String correctedComment;

    private String publicationToMoveTo;

    private Pattern EXP_SHORTLABEL_PATTERN = Pattern.compile("[^0-9a-zA-Z]+");

    @Autowired
    private PublicationController publicationController;

    @Autowired
    private UserSessionController userSessionController;

    public ExperimentController() {
    }

    @Override
    public AnnotatedObject getAnnotatedObject() {
        return getExperiment();
    }

    @Override
    public void setAnnotatedObject(AnnotatedObject annotatedObject) {
        setExperiment((Experiment) annotatedObject);
    }

    @Override
    public IntactPrimaryObject getJamiObject() {
        return null;
    }

    @Override
    public void setJamiObject(IntactPrimaryObject annotatedObject) {
        // nothing to do
    }

    @Override
    public String goToParent() {
        return "/curate/publication?faces-redirect=true&includeViewParams=true";
    }

    @SuppressWarnings("unchecked")
    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void loadData( ComponentSystemEvent event ) {
        if (!FacesContext.getCurrentInstance().isPostback()) {

            if ( ac != null ) {
                if ( experiment == null || !ac.equals( experiment.getAc() ) ) {
                    experiment = loadByAc(getDaoFactory().getExperimentDao(), ac);
                    if (experiment != null){
                        // initialise xrefs
                        Hibernate.initialize(experiment.getXrefs());
                        // initialise xrefs
                        Hibernate.initialize(experiment.getAnnotations());
                    }
                    resetToNullIfComplexExperiment();
                }
                if (experiment == null) {
                    addErrorMessage("No Experiment with this AC", ac);
                    return;
                }

                refreshInteractions();
            } else if ( experiment != null ) {
                ac = experiment.getAc();
            }

            if (experiment != null && (!Hibernate.isInitialized(experiment.getXrefs())
                    || !Hibernate.isInitialized(experiment.getAnnotations()))){
                experiment = loadByAc(getDaoFactory().getExperimentDao(), experiment.getAc());
                // initialise xrefs
                Hibernate.initialize(experiment.getXrefs());
                // initialise xrefs
                Hibernate.initialize(experiment.getAnnotations());
            }

            if ( experiment != null && publicationController.getPublication() == null ) {
                publicationController.setPublication( experiment.getPublication() );
            }

            if (reasonForRejection == null) {
                reasonForRejection = getToBeReviewed(experiment);
            }
            if (correctedComment == null) {
                correctedComment = getCorrectionComment();
            }

            refreshTabsAndFocusXref();
        }

        generalLoadChecks();
    }

    /**
     * This method is for backward compatibility only. We exclude all experiments that were created for complex curation and that should all have 'inferred by curator'
     * as interaction detection method
     */
    protected void resetToNullIfComplexExperiment() {
        if (experiment != null){
            if (experiment.getCvInteraction() != null &&
                    CvInteraction.INFERRED_BY_CURATOR_MI_REF.equals(experiment.getCvInteraction().getIdentifier())){
                experiment = null;
            }
        }
    }

    @Override
    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public String clone() {

        String value = clone(experiment, new ExperimentIntactCloner(false));

        return value;
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public String cloneWithInteractions() {

        String value = clone(experiment, new ExperimentIntactCloner(true));

        return value;
    }

    @Override
    public void modifyClone(AnnotatedObject clone) {
        clone.setShortLabel(createExperimentShortLabel());

        setExperiment((Experiment) clone);
        refreshInteractions();
        refreshTabs();

        experiment.setPublication(publicationController.getPublication());

        // don't add the experiment to the oublication so when it reverts, the publication was not changed and the experiment can vanish
        //publicationController.getPublication().addExperiment(experiment);
    }

    @SuppressWarnings({"unchecked"})
    private void refreshInteractions() {
        if (experiment == null) return;

        if (experiment.getAc() == null){
            interactionDataModel = LazyDataModelFactory.createLazyDataModel(experiment.getInteractions());
        }
        else{
            interactionDataModel = LazyDataModelFactory.createLazyDataModel(getCoreEntityManager(),
                    "select i from InteractionImpl i join i.experiments as exp where exp.ac = '" + experiment.getAc() + "'",
                    "i", "ac", true);
        }

//        if (dataModel.getRowCount() > 0 || !IntactCore.isInitialized(experiment.getInteractions())) {
//            interactionDataModel = dataModel;
//        } else {
//            interactionDataModel = LazyDataModelFactory.createLazyDataModel(experiment.getInteractions());
//        }
    }

    @Override
    public void doSave(boolean refreshCurrentView) {
        ChangesController changesController = (ChangesController) getSpringContext().getBean("changesController");
        PersistenceController persistenceController = getPersistenceController();

        doSaveIntact(refreshCurrentView, changesController, persistenceController);
    }

    @Override
    public String doSave() {
        return super.doSave();
    }

    @Override
    public void doSaveIfNecessary(ActionEvent evt) {
        super.doSaveIfNecessary(evt);
    }

    @Override
    public boolean doSaveDetails() {
        if (experiment.getAc() == null) {
            experiment.setShortLabel(createExperimentShortLabel());
            getCorePersister().saveOrUpdate(experiment);
        }

        return true;
    }

    @Override
    public void doPostSave(){
        refreshInteractions();
    }

    public void doPreSave() {
        // new object, add it to the list of experiments of its publication before saving
        if (experiment.getPublication() != null && experiment.getAc() == null) {
            // avoid lazy initialisation when opening experiment and clone if the publication does not have initialised experiments
            if (!Hibernate.isInitialized(publicationController.getPublication().getExperiments())){
                publicationController.setPublication(getDaoFactory().getPublicationDao().getByAc(experiment.getPublication().getAc()));
                experiment.setPublication(publicationController.getPublication());
            }
            publicationController.getPublication().addExperiment(experiment);
        }
    }

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, readOnly = true)
    public String newExperiment(Publication publication) {
        Experiment experiment = new Experiment(userSessionController.getUserInstitution(), createExperimentShortLabel(), null);
        setExperiment(experiment);
        experiment.setPublication(publication);

        experiment.setFullName(publication.getFullName());

        //publication.addExperiment(experiment);

        publicationController.setPublication(publication);

        if (publication.getPublicationId() != null) {
            CvDatabase pubmed = getDaoFactory().getCvObjectDao(CvDatabase.class).getByIdentifier(CvDatabase.PUBMED_MI_REF);
            CvXrefQualifier primaryRef = getDaoFactory().getCvObjectDao(CvXrefQualifier.class).getByIdentifier(CvXrefQualifier.PRIMARY_REFERENCE_MI_REF);

            experiment.addXref(new ExperimentXref(pubmed, publication.getShortLabel(), primaryRef));
        }

        copyPublicationAnnotations(null);

        refreshInteractions();

        // detach publication
        getCoreEntityManager().detach(publication);

        return navigateToObject(experiment);
    }

    private String createExperimentShortLabel() {
        String author=null;

        String authors = getAnnotatedObjectHelper().findAnnotationText(publicationController.getPublication(), CvTopic.AUTHOR_LIST_MI_REF, getDaoFactory());
        if (authors != null) {
            author = authors.split(" ")[0];
        }

        if (author == null) {
            addWarningMessage("The current publication does not have the authors annotation.","Created anonymous short label.");

            author = "anonymous";

        } else {
            // clean reserved characters
            Matcher matcher = EXP_SHORTLABEL_PATTERN.matcher(author.trim().toLowerCase());

            author = matcher.replaceAll("_");
            /*author = author.replaceAll("-", "_");
            author = author.replaceAll(" ", "_");*/
        }

        String year = getAnnotatedObjectHelper().findAnnotationText(publicationController.getPublication(), CvTopic.PUBLICATION_YEAR_MI_REF, getDaoFactory());

        if (year == null) {
            addWarningMessage("The current publication does not have the year annotation.","Correct the label if necessary and add a year it to the publication.");

            year = new SimpleDateFormat("yyyy").format(new Date());
        } else {
            year = String.valueOf(year);
        }

        String shortLabel = author+"-"+year;

        String expLabel = shortLabel.toLowerCase();

        if (experiment != null && experiment.getPublication() == null) {
            experiment.setPublication(publicationController.getPublication());
        }

        String pmid = null;

        if (experiment != null) {
            Publication publication = experiment.getPublication();
            if (publication != null) {
                if (!Hibernate.isInitialized(publication.getXrefs())){
                   publication = getDaoFactory().getPublicationDao().getByAc(publication.getAc());
                }
                PublicationXref xref = PublicationUtils.getPubmedPrimaryReferenceXref(publication);
                if (xref != null){
                    pmid = xref.getPrimaryId();
                }
            }

            if (pmid == null) {
                Experiment tempExp = experiment;
                if (!Hibernate.isInitialized(experiment.getXrefs())){
                   tempExp = getDaoFactory().getExperimentDao().getByAc(experiment.getAc());
                }
                ExperimentXref xref = ExperimentUtils.getPubmedPrimaryReferenceXref(tempExp);

                if (xref != null) {
                    pmid = xref.getPrimaryId();
                }
            }
        } else if (publicationController.getPublication() != null) {
            pmid = publicationController.getPublication().getShortLabel();
        }

        if (pmid != null) {
            return ExperimentUtils.syncShortLabelWithDb(expLabel, pmid);
        } else {
            return expLabel;
        }
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public int countInteractionsByExperimentAc( String ac ) {
        return getDaoFactory().getExperimentDao().countInteractionsForExperimentWithAc(ac);
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public boolean getParticipantsAvailable(Experiment experiment){
        if(countInteractionsByExperimentAc(experiment.getAc()) == 0){
            return false;
        }else{
            int count = 0;
            for(Interaction interaction: experiment.getInteractions()){
                count = interaction.getComponents().size();
                if(count > 0){
                    continue;
                }
            }
            return (count > 0);
        }
    }

    public void acceptExperiment(ActionEvent actionEvent) {

        UserSessionController userSessionController = (UserSessionController) getSpringContext().getBean("userSessionController");

        setAcceptedMessage("Accepted "+new SimpleDateFormat("yyyy-MMM-dd").format(new Date()).toUpperCase()+" by "+userSessionController.getCurrentUser().getLogin().toUpperCase());

        removeAnnotation(CvTopic.TO_BE_REVIEWED);
        removeAnnotation(CvTopic.CORRECTION_COMMENT);

        doSave(actionEvent);

        addInfoMessage("Experiment accepted", experiment.getShortLabel());

        // check if all the experiments have been acted upon, be it to accept them or reject them.
        globalPublicationDecision();
    }

    public void rejectExperiment(ActionEvent actionEvent) {

        UserSessionController userSessionController = (UserSessionController) getSpringContext().getBean("userSessionController");

        if (reasonForRejection.startsWith("Rejected")) {
            reasonForRejection = reasonForRejection.substring(reasonForRejection.indexOf(".")+2);
        }

        String date = "Rejected " +new SimpleDateFormat("yyyy-MMM-dd").format(new Date()).toUpperCase()+" by "+userSessionController.getCurrentUser().getLogin().toUpperCase();

        setToBeReviewed(date+". "+reasonForRejection);

        removeAnnotation(CvTopic.CORRECTION_COMMENT);

        removeAnnotation(CvTopic.ACCEPTED);

        doSave(actionEvent);

        addInfoMessage("Experiment rejected", experiment.getShortLabel()+": "+reasonForRejection);

        globalPublicationDecision();
    }

    private void globalPublicationDecision() {
        int expAccepted = 0;
        int expRejected = 0;

        final Collection<Experiment> experiments = IntactCore.ensureInitializedExperiments(publicationController.getPublication());

        for (Experiment exp : experiments) {
            if (isToBeReviewed(exp)) {
                expRejected++;
            } else if (isAccepted(exp)) {
                expAccepted++;
            }
        }

        boolean allActedUpon = ((expRejected+expAccepted) == experiments.size());

        boolean allAccepted = expAccepted == experiments.size();
        boolean allRejected = expRejected == experiments.size();

        if (allAccepted) {
            UserSessionController userSessionController = (UserSessionController) getSpringContext().getBean("userSessionController");

            getAnnotatedObjectHelper().setAnnotation(publicationController.getPublication(), CvTopic.ACCEPTED,
                    "Accepted " + new SimpleDateFormat("yyyy-MMM-dd").format(new Date()).toUpperCase() + " by " + userSessionController.getCurrentUser().getLogin().toUpperCase());

            addInfoMessage("Publication accepted", "");

            //clear to-be-reviewed
            getAnnotatedObjectHelper().removeAnnotation(publicationController.getPublication(), CvTopic.TO_BE_REVIEWED);


            // refresh experiments with possible changes in publication title, annotations and publication identifier
            publicationController.copyAnnotationsToExperiments(null);
            publicationController.copyPublicationTitleToExperiments(null);
            publicationController.copyPrimaryIdentifierToExperiments();

            publicationController.getLifecycleManager().getReadyForCheckingStatus().accept(publicationController.getPublication(), null);

            if (!PublicationUtils.isOnHold(publicationController.getPublication())) {
                publicationController.getLifecycleManager().getAcceptedStatus().readyForRelease(publicationController.getPublication(), "Accepted and not on-hold");
            }

            publicationController.doSave();

            addInfoMessage("Publication accepted", "All of its experiments have been accepted");

        } else if (allActedUpon) {
            RequestContext requestContext = RequestContext.getCurrentInstance();
            requestContext.execute("publicationActionDlg.show()");
        }
    }

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED)
    public void addCorrectionComment(ActionEvent evt) {
        addInfoMessage("Added correction comment", correctedComment);
        setCorrectionComment(correctedComment);
    }

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED)
    public void setToBeReviewed(String toBeReviewed) {

        if (toBeReviewed == null) {
            removeAnnotation(CvTopic.TO_BE_REVIEWED);
        }

        updateAnnotation(CvTopic.TO_BE_REVIEWED, toBeReviewed);
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public String getToBeReviewed() {
        return findAnnotationText(CvTopic.TO_BE_REVIEWED);
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public String getToBeReviewed(Experiment exp) {
        final Collection<Annotation> annotations = IntactCore.ensureInitializedAnnotations(exp);

        for (Annotation annot : annotations) {
            if (annot != null && annot.getCvTopic() != null) {
                if (CvTopic.TO_BE_REVIEWED.equals(annot.getCvTopic().getShortLabel())) {
                    return annot.getAnnotationText();
                }
            }
        }

        return null;
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void clearToBeReviewed(ActionEvent evt) {
        removeAnnotation(CvTopic.TO_BE_REVIEWED);
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public boolean isAccepted() {
        Experiment exp;

        if (!Hibernate.isInitialized(experiment.getAnnotations())) {
            exp = getDaoFactory().getExperimentDao().getByAc(experiment.getAc());
        } else {
            exp = experiment;
        }
        return ExperimentUtils.isAccepted(exp);
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public boolean isRejected() {
        boolean reviewed = isToBeReviewed(experiment);
        return reviewed;
    }

    /**
     * When reverting, we need to refresh the collection of wrappers because they are not part of the IntAct model.
     */
    @Override
    protected void postRevert() {
        if (experiment.getPublication() != null){
            publicationController.setPublication(experiment.getPublication());
        }
        refreshInteractions();
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public String getAcceptedMessage() {
        return findAnnotationText( CvTopic.ACCEPTED );
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void setAcceptedMessage( String message ) {
        updateAnnotation(CvTopic.ACCEPTED, message);
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public boolean isAccepted(Experiment exp) {
        Experiment e = refreshIfNeeded(exp);
        return ExperimentUtils.isAccepted(e);
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public boolean isToBeReviewed(Experiment exp) {
        Experiment e = refreshIfNeeded(exp);
        return ExperimentUtils.isToBeReviewed(e);
    }

    private Experiment refreshIfNeeded(Experiment exp) {
        Experiment e = exp;

        if (!IntactCore.isInitialized(exp.getAnnotations())) {
            e = getDaoFactory().getExperimentDao().getByAc(exp.getAc());
        }
        return e;
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void copyPublicationAnnotations(ActionEvent evt) {

        CurateUtils.copyPublicationAnnotationsToExperiment(experiment);

        addInfoMessage("Annotations copied from publication", "");

        setUnsavedChanges(true);
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public String getOnHold() {
        return findAnnotationText( CvTopic.ON_HOLD );
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void setOnHold( String reason ) {
        updateAnnotation(CvTopic.ON_HOLD, reason);
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public String moveToPublication() {
        if (publicationToMoveTo != null && !publicationToMoveTo.isEmpty()) {
            Publication publication = findPublicationByAcOrLabel(publicationToMoveTo);

            if (publication == null) {
                addErrorMessage("Cannot move", "No publication found with this AC or PMID: "+publicationToMoveTo);
                return null;
            }

            publicationController.setPublication(publication);
            // don't remove the experiment from the parent publication yet so the revertJami will work properly. It will be added only after saving
            // As an experiment can have only one publication, it will be removed from the previous publication
            //experiment.getPublication().removeExperiment(experiment);

            experiment.setPublication(publication);

            // update the primary reference when moving the experiment
            if (publication.getPublicationId() != null) {
                updateXref(CvDatabase.PUBMED_MI_REF, CvXrefQualifier.PRIMARY_REFERENCE, publication.getShortLabel());
            }

            copyPublicationAnnotations(null);

            // update the shortlabel
            // update the shortlabel
            String newShortLabel = createExperimentShortLabel();
            if (newShortLabel != null){
                experiment.setShortLabel(newShortLabel);
            }

            setExperiment(experiment);

        } else {
            return null;
        }

        loadData(null);

        setUnsavedChanges(true);

        addInfoMessage("Moved experiment", "To publication: "+publicationToMoveTo);

        return null;
    }

    private Publication findPublicationByAcOrLabel(String acOrLabel) {
        Publication publication = getDaoFactory().getPublicationDao().getByAc(acOrLabel.trim());

        if (publication == null) {
            publication = getDaoFactory().getPublicationDao().getByPubmedId(acOrLabel);
        }
        return publication;
    }

    public String getAc() {
        if ( ac == null && experiment != null ) {
            ac = experiment.getAc();
        }
        return ac;
    }

    public void setAc( String ac ) {
        this.ac = ac;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment( Experiment experiment ) {
        this.experiment = experiment;

        if (experiment != null) {
            this.ac = experiment.getAc();

            if (experiment.getPublication() != null) {
                publicationController.setPublication(experiment.getPublication());
            }
        }
    }

    public LazyDataModel<Interaction> getInteractionDataModel() {
        return interactionDataModel;
    }

    public void setInteractionDataModel(LazyDataModel<Interaction> interactionDataModel) {
        this.interactionDataModel = interactionDataModel;
    }

    public String getReasonForRejection() {
        return reasonForRejection;
    }

    public void setReasonForRejection(String reasonForRejection) {
        this.reasonForRejection = reasonForRejection;
    }

    public String getPublicationToMoveTo() {
        return publicationToMoveTo;
    }

    public void setPublicationToMoveTo(String publicationToMoveTo) {
        this.publicationToMoveTo = publicationToMoveTo;
    }

    private PublicationController getPublicationController() {
        return (PublicationController) getSpringContext().getBean("publicationController");
    }

    @Override
    public Collection<String> collectParentAcsOfCurrentAnnotatedObject(){
        Collection<String> parentAcs = new ArrayList<String>();

        addPublicationAcToParentAcs(parentAcs, experiment);

        return parentAcs;
    }

    @Override
    protected void refreshUnsavedChangesBeforeRevert(){
        Collection<String> parentAcs = new ArrayList<String>();

        addPublicationAcToParentAcs(parentAcs, experiment);

        getChangesController().revertExperiment(experiment, parentAcs);
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public String getCorrectionComment() {
        return findAnnotationText(CvTopic.CORRECTION_COMMENT);
    }

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED)
    public void setCorrectionComment(String correctionComment) {
        updateAnnotation(CvTopic.CORRECTION_COMMENT, correctionComment);
    }

    public String getCorrectedComment() {
        return correctedComment;
    }

    public void setCorrectedComment(String correctedComment) {
        this.correctedComment = correctedComment;
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public String getCautionMessage() {
        if (experiment == null){
            return null;
        }
        if (!Hibernate.isInitialized(experiment.getAnnotations())){
            return getAnnotatedObjectHelper().findAnnotationText(getDaoFactory().getExperimentDao().getByAc(experiment.getAc()),
                    CvTopic.CAUTION_MI_REF, getDaoFactory());
        }
        return findAnnotationText(CvTopic.CAUTION_MI_REF);
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public String getInternalRemarkMessage() {
        if (experiment == null){
            return null;
        }
        if (!Hibernate.isInitialized(experiment.getAnnotations())){
            return getAnnotatedObjectHelper().findAnnotationText(getDaoFactory().getExperimentDao().getByAc(experiment.getAc()),
                    CvTopic.INTERNAL_REMARK, getDaoFactory());
        }
        return findAnnotationText(CvTopic.INTERNAL_REMARK);
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public List collectAnnotations() {
        return super.collectAnnotations();
    }

    public List collectAliases() {
        return Collections.EMPTY_LIST;
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public List collectXrefs() {
        return super.collectXrefs();
    }
}
