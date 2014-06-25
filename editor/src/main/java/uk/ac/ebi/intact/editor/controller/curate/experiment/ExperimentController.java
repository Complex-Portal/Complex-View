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
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persister.IntactCore;
import uk.ac.ebi.intact.editor.controller.UserSessionController;
import uk.ac.ebi.intact.editor.controller.curate.AnnotatedObjectController;
import uk.ac.ebi.intact.editor.controller.curate.cloner.ExperimentIntactCloner;
import uk.ac.ebi.intact.editor.controller.curate.publication.PublicationController;
import uk.ac.ebi.intact.editor.util.CurateUtils;
import uk.ac.ebi.intact.editor.util.LazyDataModelFactory;
import uk.ac.ebi.intact.jami.model.IntactPrimaryObject;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.ExperimentUtils;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
    public void loadData( ComponentSystemEvent event ) {
        if (!FacesContext.getCurrentInstance().isPostback()) {

            if ( ac != null ) {
                if ( experiment == null || !ac.equals( experiment.getAc() ) ) {
                    experiment = loadByAc(IntactContext.getCurrentInstance().getDaoFactory().getExperimentDao(), ac);
                }
                if (experiment == null) {
                    addErrorMessage("No Experiment with this AC", ac);
                    return;
                }

                refreshInteractions();
            } else if ( experiment != null ) {
                ac = experiment.getAc();
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

    @SuppressWarnings({"unchecked"})
    private void refreshInteractions() {
        if (experiment == null) return;

        interactionDataModel = LazyDataModelFactory.createLazyDataModel(getCoreEntityManager(),
                "select i from InteractionImpl i join i.experiments as exp where exp.ac = '" + experiment.getAc() + "'",
                "i","ac",true);

//        if (dataModel.getRowCount() > 0 || !IntactCore.isInitialized(experiment.getInteractions())) {
//            interactionDataModel = dataModel;
//        } else {
//            interactionDataModel = LazyDataModelFactory.createLazyDataModel(experiment.getInteractions());
//        }
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

    @Override
    public String clone() {
        return clone(experiment, new ExperimentIntactCloner(false));
    }

    public String cloneWithInteractions() {
        if (!getDaoFactory().getEntityManager().contains(experiment) && !IntactCore.isInitialized(experiment.getInteractions())){
             getDaoFactory().getEntityManager().merge(experiment);
        }

        return clone(experiment, new ExperimentIntactCloner(true));
    }

    public void doPreSave() {
        // new object, add it to the list of experiments of its publication before saving
        if (experiment.getPublication() != null && experiment.getAc() == null) {
            publicationController.getPublication().addExperiment(experiment);
        }
    }

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

        //getUnsavedChangeManager().markAsUnsaved(experiment);

        return navigateToObject(experiment);
    }

    private String createExperimentShortLabel() {
        String author;

        if (publicationController.getFirstAuthor() == null) {
            addWarningMessage("The current publication does not have the authors annotation.","Created anonymous short label.");

            author = "anonymous";

        } else {
            // clean reserved characters
            Matcher matcher = EXP_SHORTLABEL_PATTERN.matcher(publicationController.getFirstAuthor().trim().toLowerCase());

            author = matcher.replaceAll("_");
            /*author = author.replaceAll("-", "_");
            author = author.replaceAll(" ", "_");*/
        }

        String year;

        if (publicationController.getYear() == null) {
            addWarningMessage("The current publication does not have the year annotation.","Correct the label if necessary and add a year it to the publication.");

            year = new SimpleDateFormat("yyyy").format(new Date());
        } else {
            year = String.valueOf(publicationController.getYear());
        }

        String shortLabel = author+"-"+year;

        String expLabel = shortLabel.toLowerCase();

        if (experiment != null && experiment.getPublication() == null) {
            experiment.setPublication(publicationController.getPublication());
        }

        String pmid = null;

        if (experiment != null) {
            pmid = ExperimentUtils.getPubmedId(experiment);
        } else if (publicationController.getPublication() != null) {
            pmid = publicationController.getPublication().getShortLabel();
        }

        if (pmid != null) {
            return ExperimentUtils.syncShortLabelWithDb(expLabel, pmid);
        } else {
            return expLabel;
        }
    }

    public int countInteractionsByExperimentAc( String ac ) {
        return getDaoFactory().getExperimentDao().countInteractionsForExperimentWithAc(ac);
    }

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
            publicationController.acceptPublication(null);
            publicationController.doSave();

            addInfoMessage("Publication accepted", "All of its experiments have been accepted");

        } else if (allActedUpon) {
            RequestContext requestContext = RequestContext.getCurrentInstance();
            requestContext.execute("publicationActionDlg.show()");
        }
    }

    public void addCorrectionComment(ActionEvent evt) {
        addInfoMessage("Added correction comment", correctedComment);
        setCorrectionComment(correctedComment);
    }

    public void setToBeReviewed(String toBeReviewed) {
        if (toBeReviewed == null) {
            removeAnnotation(CvTopic.TO_BE_REVIEWED);
        }

        setAnnotation(CvTopic.TO_BE_REVIEWED, toBeReviewed);
    }

    public String getToBeReviewed() {
        return findAnnotationText(CvTopic.TO_BE_REVIEWED);
    }

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

    public void clearToBeReviewed(ActionEvent evt) {
        removeAnnotation(CvTopic.TO_BE_REVIEWED);
    }

    public boolean isAccepted() {
        Experiment exp;

        if (!Hibernate.isInitialized(experiment.getAnnotations())) {
            exp = getDaoFactory().getExperimentDao().getByAc(experiment.getAc());
        } else {
            exp = experiment;
        }
        return ExperimentUtils.isAccepted(exp);
    }

    public boolean isRejected() {
        return isToBeReviewed(experiment);
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

    public String getAcceptedMessage() {
        return findAnnotationText( CvTopic.ACCEPTED );
    }

    public void setAcceptedMessage( String message ) {
        setAnnotation( CvTopic.ACCEPTED, message );
    }

    public boolean isAccepted(Experiment exp) {
        Experiment e = refreshIfNeeded(exp);
        return ExperimentUtils.isAccepted(e);
    }



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

    @Transactional
    public void copyPublicationAnnotations(ActionEvent evt) {
        CurateUtils.copyPublicationAnnotationsToExperiment(experiment);

        addInfoMessage("Annotations copied from publication", "");

        setUnsavedChanges(true);
    }

    public String getOnHold() {
        return findAnnotationText( CvTopic.ON_HOLD );
    }

    public void setOnHold( String reason ) {
        setAnnotation( CvTopic.ON_HOLD, reason );
    }

    public String moveToPublication() {
        if (publicationToMoveTo != null && !publicationToMoveTo.isEmpty()) {
            Publication publication = findPublicationByAcOrLabel(publicationToMoveTo);

            if (publication == null) {
                addErrorMessage("Cannot move", "No publication found with this AC or PMID: "+publicationToMoveTo);
                return null;
            }

            publicationController.setPublication(publication);
            // don't remove the experiment from the parent publication yet so the revert will work properly. It will be added only after saving
            // As an experiment can have only one publication, it will be removed from the previous publication
            //experiment.getPublication().removeExperiment(experiment);

            experiment.setPublication(publication);

            // update the primary reference when moving the experiment
            if (publication.getPublicationId() != null) {
                setXref(CvDatabase.PUBMED_MI_REF, CvXrefQualifier.PRIMARY_REFERENCE, publication.getShortLabel());
            }

            copyPublicationAnnotations(null);

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

    public String getCorrectionComment() {
         return findAnnotationText(CvTopic.CORRECTION_COMMENT);
    }

    public void setCorrectionComment(String correctionComment) {
        setAnnotation(CvTopic.CORRECTION_COMMENT, correctionComment);
    }

    public String getCorrectedComment() {
        return correctedComment;
    }

    public void setCorrectedComment(String correctedComment) {
        this.correctedComment = correctedComment;
    }
}
