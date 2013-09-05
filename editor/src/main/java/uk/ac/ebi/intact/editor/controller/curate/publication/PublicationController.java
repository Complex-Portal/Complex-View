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
package uk.ac.ebi.intact.editor.controller.curate.publication;

import edu.ucla.mbi.imex.central.ws.v20.IcentralFault;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.hibernate.Hibernate;
import org.joda.time.DateTime;
import org.primefaces.context.RequestContext;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.LazyDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.cdb.webservice.Authors;
import uk.ac.ebi.cdb.webservice.Result;
import uk.ac.ebi.intact.bridges.citexplore.CitexploreClient;
import uk.ac.ebi.intact.bridges.imexcentral.DefaultImexCentralClient;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;
import uk.ac.ebi.intact.core.config.SequenceCreationException;
import uk.ac.ebi.intact.core.config.SequenceManager;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.lifecycle.LifecycleManager;
import uk.ac.ebi.intact.core.persister.IntactCore;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.ImexCentralManager;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.PublicationImexUpdaterException;
import uk.ac.ebi.intact.editor.controller.UserSessionController;
import uk.ac.ebi.intact.editor.controller.curate.AnnotatedObjectController;
import uk.ac.ebi.intact.editor.controller.curate.PersistenceController;
import uk.ac.ebi.intact.editor.controller.curate.experiment.ExperimentController;
import uk.ac.ebi.intact.editor.util.CurateUtils;
import uk.ac.ebi.intact.editor.util.LazyDataModelFactory;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.user.Preference;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.model.util.ExperimentUtils;
import uk.ac.ebi.intact.model.util.PublicationUtils;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.persistence.Query;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope("conversation.access")
@ConversationName("general")
public class PublicationController extends AnnotatedObjectController {

    private static final Log log = LogFactory.getLog(PublicationController.class);

    public static final String SUBMITTED = "MI:0878";
    public static final String CURATION_REQUEST = "MI:0873";
    private static final String CURATION_DEPTH = "MI:0955";

    private Publication publication;
    private String ac;

    private String identifier;
    private String identifierToOpen;
    private String identifierToImport;

    private boolean assignToMe = true;

    private String datasetToAdd;
    private String[] datasetsToRemove;
    private List<SelectItem> datasetsSelectItems;

    private String reasonForReadyForChecking;
    private String reasonForRejection;
    private String reasonForOnHoldFromDialog;

    private boolean isCitexploreActive;

    private boolean isLifeCycleDisabled;

    private LazyDataModel<Interaction> interactionDataModel;

    @Autowired
    private UserSessionController userSessionController;

    @Autowired
    private LifecycleManager lifecycleManager;

    @Autowired
    private ImexCentralManager imexCentralManager;

    private String curationDepth;

    private String newDatasetDescriptionToCreate;
    private String newDatasetNameToCreate;

    public PublicationController() {
    }

    @Override
    public AnnotatedObject getAnnotatedObject() {
        return getPublication();
    }

    @Override
    public void setAnnotatedObject(AnnotatedObject annotatedObject) {
        setPublication((Publication) annotatedObject);
    }

    public void loadData(ComponentSystemEvent event) {
        if (!FacesContext.getCurrentInstance().isPostback()) {

            datasetsSelectItems = new ArrayList<SelectItem>();

            loadByAc();

            refreshTabsAndFocusXref();
        }

        generalLoadChecks();
    }

    private void loadByAc() {

        if (ac != null) {
            if (publication == null || !ac.equals(publication.getAc())) {
                publication = loadByAc(IntactContext.getCurrentInstance().getDaoFactory().getPublicationDao(), ac);

                if (publication == null) {
                    publication = getDaoFactory().getPublicationDao().getByPubmedId(ac);
                    if (publication != null) {
                        ac = publication.getAc();
                    } else {
                        super.addErrorMessage("No publication with this AC", ac);
                    }
                }
            }

            refreshDataModels();
            if (publication != null) {
                loadFormFields();
            }

            //getCuratorContextController().removeFromUnsavedByAc(ac);

        } else if (publication != null) {
            ac = publication.getAc();
            loadFormFields();
        }
    }

    private void refreshDataModels() {
        interactionDataModel = LazyDataModelFactory.createLazyDataModel(getCoreEntityManager(),
                "select i from InteractionImpl i join fetch i.experiments as exp " +
                        "where exp.publication.ac = '" + ac + "' order by exp.shortLabel asc",
                "select count(i) from InteractionImpl i join i.experiments as exp " +
                        "where exp.publication.ac = '" + ac + "'");
    }

    private void loadFormFields() {
        // reset previous dataset actions in the form
        this.datasetsToRemove = null;
        this.datasetToAdd = null;
        datasetsSelectItems = new ArrayList<SelectItem>();

        DatasetPopulator populator = getDatasetPopulator();

        for (Annotation annotation : publication.getAnnotations()) {
            if (annotation.getCvTopic() != null && CvTopic.DATASET_MI_REF.equals(annotation.getCvTopic().getIdentifier())) {
                String datasetText = annotation.getAnnotationText();

                SelectItem datasetSelectItem = populator.createSelectItem(datasetText);
                datasetsSelectItems.add(datasetSelectItem);
            }
        }

        // load curationDepth
        this.curationDepth = getCurationDepthAnnotation();
    }

    public boolean isCitexploreOnline() {
        if (isCitexploreActive) {
            return true;
        }
        if (log.isDebugEnabled()) log.debug("Checking Europe Pubmed Central status");
        try {
            URL url = new URL("http://www.ebi.ac.uk/webservices/citexplore/v3.0.1/service?wsdl");
            final URLConnection urlConnection = url.openConnection();
            urlConnection.setConnectTimeout(1000);
            urlConnection.setReadTimeout(1000);
            urlConnection.connect();
        } catch (Exception e) {
            log.debug("\tEurope Pubmed Central is not reachable");

            isCitexploreActive = false;
            return false;
        }

        isCitexploreActive = true;
        return true;
    }

    public String newAutocomplete() {
        identifier = identifierToImport;

        if (identifier == null) {
            addErrorMessage("Cannot auto-complete", "ID is empty");
            RequestContext requestContext = RequestContext.getCurrentInstance();
            requestContext.execute("newPublicationDlg.hide()");
            return null;
        }

        // check if already exists
        Publication existingPublication = getDaoFactory().getPublicationDao().getByPubmedId(identifier);

        if (existingPublication != null) {
            setPublication(existingPublication);
            addWarningMessage("Publication already exists", "Loaded from the database");
            return "/curate/publication?faces-redirect=true&includeViewParams=true";
        } else {

            // check if it already exists in IMEx central
            try {

                if (imexCentralManager.isPublicationAlreadyRegisteredInImexCentral(identifier)) {
                    RequestContext requestContext = RequestContext.getCurrentInstance();
                    requestContext.execute("newPublicationDlg.hide()");
                    requestContext.execute("imexCentralActionDlg.show()");
                    return null;
                } else {
                    createNewPublication(null);

                    return "/curate/publication?faces-redirect=true";
                }
            }
            // cannot check IMEx central, add warning and create publication
            catch (ImexCentralException e) {
                log.error(e.getMessage(), e);
                addWarningMessage("Impossible to check with IMExcentral if " + identifier + " is already curated", e.getMessage());
                createNewPublication(null);

                return "/curate/publication?faces-redirect=true";
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                addWarningMessage("Impossible to check with IMExcentral if " + identifier + " is already curated", e.getMessage());
                createNewPublication(null);

                return "/curate/publication?faces-redirect=true";
            }
        }
    }

    public void createNewPublication(ActionEvent evt) {

        if (identifier == null) {
            addErrorMessage("Cannot create publication", "ID is empty");
            return;
        }

        newEmpty();
        autocomplete(publication, identifier);

        identifier = null;
        identifierToImport = null;
    }

    public void createNewEmptyPublication(ActionEvent evt) {

        if (identifier == null) {
            addErrorMessage("Cannot create publication", "ID is empty");
            return;
        }

        newEmpty();

        identifier = null;
        identifierToImport = null;
    }

    public void doFormAutocomplete(ActionEvent evt) {
        if (publication.getShortLabel() != null) {
            autocomplete(publication, publication.getShortLabel());
        }
    }

    public void autocomplete(Publication publication, String id) {
        CitexploreClient citexploreClient = null;

        try {
            citexploreClient = new CitexploreClient();
        } catch (Exception e) {
            addErrorMessage("Cannot auto-complete", "Citexplore service is down at the moment");
            return;
        }

        try {
            final Result citation = citexploreClient.getCitationById(id);

            // new publication. No autocompletion available and this publication can be created under unassigned
            if (citation == null && publication.getAc() == null) {
                addErrorMessage("No citation was found, the auto completion has been aborted", "PMID: " + id);
                setPublication(null);
                return;
            } else if (citation == null && publication.getAc() != null) {
                addErrorMessage("This pubmed id does not exist and the autocompletion has been aborted", "PMID: " + id);
                return;
            }

            setPrimaryReference(id);

            publication.setFullName(citation.getTitle());

            StringBuilder journalBuf = new StringBuilder(128);
            final String abbreviation = citation.getJournalInfo().getJournal().getISOAbbreviation();
            final String issn = citation.getJournalInfo().getJournal().getISSN();
            journalBuf.append(abbreviation);
            if (issn != null) {
                journalBuf.append(" (").append(issn).append(")");
            }
            setJournal(journalBuf.toString());

            setYear(citation.getJournalInfo().getYearOfPublication());

            StringBuilder sbAuthors = new StringBuilder(64);

            if (citation.getAuthorList() != null) {
                Iterator<Authors> authorIterator = citation.getAuthorList().getAuthor().iterator();
                while (authorIterator.hasNext()) {
                    Authors author = authorIterator.next();
                    sbAuthors.append(author.getLastName()).append(" ");

                    if (author.getInitials() != null) {
                        for (int i = 0; i < author.getInitials().length(); i++) {
                            char initial = author.getInitials().charAt(i);
                            sbAuthors.append(initial).append(".");
                        }
                    }

                    if (authorIterator.hasNext()) sbAuthors.append(", ");
                }

                setAuthors(sbAuthors.toString());
            }

            getChangesController().markAsUnsaved(publication);

            addInfoMessage("Auto-complete successful", "Fetched details for: " + id);

        } catch (Throwable e) {
            addErrorMessage("Problem auto-completing publication", e.getMessage());
            e.printStackTrace();
        }
    }

    @Transactional(value = "transactionManager")
    public String newEmptyUnassigned() {
        SequenceManager sequenceManager = (SequenceManager) getSpringContext().getBean("sequenceManager");
        try {
            sequenceManager.createSequenceIfNotExists("unassigned_seq");
        } catch (SequenceCreationException e) {
            handleException(e);
        }

        identifier = PublicationUtils.nextUnassignedId(getIntactContext());

        // check if already exists, so we skip this unassigned
        Publication existingPublication = getDaoFactory().getPublicationDao().getByPubmedId(identifier);

        if (existingPublication != null) {
            setPublication(existingPublication);
            addWarningMessage("Publication already exists", "Loaded from the database");
            return "/curate/publication?faces-redirect=true&includeViewParams=true";
        } else {
            // check if it already exists in IMEx central
            try {

                if (imexCentralManager.isPublicationAlreadyRegisteredInImexCentral(identifier)) {
                    RequestContext requestContext = RequestContext.getCurrentInstance();
                    requestContext.execute("newPublicationDlg.hide()");
                    requestContext.execute("imexCentralUnassignedActionDlg.show()");
                    return null;
                } else {
                    newEmpty();

                    identifier = null;
                    identifierToImport = null;

                    return "/curate/publication?faces-redirect=true";
                }
            }
            // cannot check IMEx central, add warning and create publication
            catch (ImexCentralException e) {
                addWarningMessage("Impossible to check with IMExcentral if " + identifier + " is already curated", e.getMessage());
                newEmpty();

                identifier = null;
                identifierToImport = null;

                return "/curate/publication?faces-redirect=true";
            } catch (Exception e) {
                addWarningMessage("Impossible to check with IMExcentral if " + identifier + " is already curated", e.getMessage());
                newEmpty();

                identifier = null;
                identifierToImport = null;

                return "/curate/publication?faces-redirect=true";
            }
        }
    }

    @Transactional(value = "transactionManager")
    public void newEmpty() {

        Publication publication = new Publication(userSessionController.getUserInstitution(), identifier);
        setPublication(publication);

        // add the primary reference xref
        setPrimaryReference(identifier);

        interactionDataModel = LazyDataModelFactory.createEmptyDataModel();

        String defaultCurationDepth;
        final Preference curDepthPref = getCurrentUser().getPreference("curation.depth");

        if (curDepthPref != null) {
            defaultCurationDepth = curDepthPref.getValue();
        } else {
            defaultCurationDepth = getEditorConfig().getDefaultCurationDepth();
        }

        setCurationDepth(defaultCurationDepth);

        lifecycleManager.getStartStatus().create(publication, "Created in Editor");

        if (assignToMe) {
            lifecycleManager.getNewStatus().claimOwnership(publication);
            lifecycleManager.getAssignedStatus().startCuration(publication);
        }

        getChangesController().markAsUnsaved(publication);
    }

    public void openByPmid(ActionEvent evt) {
        identifier = identifierToOpen;

        if (identifier == null || identifier.trim().length() == 0) {
            addErrorMessage("PMID is empty", "No PMID was supplied");
        } else {
            Publication publicationToOpen = getDaoFactory().getPublicationDao().getByPubmedId(identifier);

            if (publicationToOpen == null) {
                addErrorMessage("PMID not found", "There is no publication with PMID '" + identifier + "'");
            } else {
                publication = publicationToOpen;
                ac = publication.getAc();
            }

            identifierToOpen = null;
        }

    }

    public void doClose(ActionEvent evt) {
        publication = null;
        ac = null;
    }

    public boolean isNewPublication() {
        return publication.getStatus().getIdentifier().equals(CvPublicationStatusType.NEW.identifier());
    }

    public boolean isAssigned() {
        return publication.getStatus().getIdentifier().equals(CvPublicationStatusType.ASSIGNED.identifier());
    }

    public boolean isCurationInProgress() {
        return publication.getStatus().getIdentifier().equals(CvPublicationStatusType.CURATION_IN_PROGRESS.identifier());
    }

    public boolean isReadyForChecking() {
        return publication.getStatus().getIdentifier().equals(CvPublicationStatusType.READY_FOR_CHECKING.identifier());
    }

    public boolean isReadyForRelease() {
        return publication.getStatus().getIdentifier().equals(CvPublicationStatusType.READY_FOR_RELEASE.identifier());
    }

    public boolean isAcceptedOnHold() {
        return publication.getStatus().getIdentifier().equals(CvPublicationStatusType.ACCEPTED_ON_HOLD.identifier());
    }

    public boolean isReleased() {
        return publication.getStatus().getIdentifier().equals(CvPublicationStatusType.RELEASED.identifier());
    }

    public void claimOwnership(ActionEvent evt) {
        lifecycleManager.getGlobalStatus().changeOwnership(publication, getCurrentUser(), null);

        // automatically set as curation in progress if no one was assigned before
        if (isAssigned()) {
            markAsCurationInProgress(evt);
        }

        addInfoMessage("Claimed publication ownership", "You are now the owner of this publication");
    }

    public void markAsAssignedToMe(ActionEvent evt) {
        lifecycleManager.getNewStatus().assignToCurator(publication, getCurrentUser());

        addInfoMessage("Ownership claimed", "The publication has been assigned to you");

        markAsCurationInProgress(evt);
    }

    public void markAsCurationInProgress(ActionEvent evt) {
        if (!userSessionController.isItMe(publication.getCurrentOwner())) {
            addErrorMessage("Cannot mark as curation in progress", "You are not the owner of this publication");
            return;
        }

        lifecycleManager.getAssignedStatus().startCuration(publication);

        addInfoMessage("Curation started", "Curation is now in progress");

        // try to register/update record in IMEx central if they don't have IMEx. IMEx records are updated automatically with a cronjob
        if (publication.getAc() != null && getImexId()==null){
            try {
                imexCentralManager.registerAndUpdatePublication(publication.getAc());
            } catch (ImexCentralException e) {
                addWarningMessage("Impossible to register/update status of " + identifier + " in IMEx central", e.getMessage());
            }
        }
    }

    public void markAsReadyForChecking(ActionEvent evt) {
        if (!userSessionController.isItMe(publication.getCurrentOwner())) {
            addErrorMessage("Cannot mark as Ready for checking", "You are not the owner of this publication");
            return;
        }


        if (isBeenRejectedBefore()) {
            List<String> correctionComments = new ArrayList<String>();

            for (Experiment exp : IntactCore.ensureInitializedExperiments(publication)) {
                Annotation correctionCommentAnnot = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel(exp, CvTopic.CORRECTION_COMMENT);

                if (correctionCommentAnnot != null) {
                    correctionComments.add(correctionCommentAnnot.getAnnotationText());
                }

                reasonForReadyForChecking = StringUtils.join(correctionComments, "; ");
            }

        }

        // TODO run a proper sanity check
        boolean sanityCheckPassed = true;

        lifecycleManager.getCurationInProgressStatus().readyForChecking(publication, reasonForReadyForChecking, sanityCheckPassed);

        reasonForReadyForChecking = null;

        addInfoMessage("Publication ready for checking", "Assigned to reviewer: " + publication.getCurrentReviewer().getLogin());
        // try to register/update record in IMEx central. IMEx records are updated automatically with a cron job so if it has an IMEx id we do nothing
        if (publication.getAc() != null && getImexId() == null){
            try {
                imexCentralManager.registerAndUpdatePublication(publication.getAc());
            } catch (ImexCentralException e) {
                addWarningMessage("Impossible to register/update status of " + identifier + " in IMEx central", e.getMessage());
            }
        }
    }

    @Transactional
    public void revertReadyForChecking(ActionEvent evt) {
        LifecycleEvent event = PublicationUtils.getLastEventOfType(publication, CvLifecycleEventType.READY_FOR_CHECKING.identifier());
        publication.removeLifecycleEvent(event);

        publication.setStatus(getDaoFactory().getCvObjectDao(CvPublicationStatus.class).getByIdentifier(CvPublicationStatusType.CURATION_IN_PROGRESS.identifier()));
        // try to register/update record in IMEx central. IMEx records are updated automatically with a cron job so if it has an IMEx id we do nothing
        if (publication.getAc() != null && getImexId() == null){
            try {
                imexCentralManager.registerAndUpdatePublication(publication.getAc());
            } catch (ImexCentralException e) {
                addWarningMessage("Impossible to register/update status of " + identifier + " in IMEx central", e.getMessage());
            }
        }
    }

    @Transactional
    public void revertAccepted(ActionEvent evt) {
        LifecycleEvent readyForReleaseEvt = PublicationUtils.getLastEventOfType(publication, CvLifecycleEventType.READY_FOR_RELEASE.identifier());
        LifecycleEvent acceptedEvt = PublicationUtils.getLastEventOfType(publication, CvLifecycleEventType.ACCEPTED.identifier());

        if (readyForReleaseEvt != null) {
            publication.removeLifecycleEvent(readyForReleaseEvt);
        }

        if (acceptedEvt != null) {
            publication.removeLifecycleEvent(acceptedEvt);
        }

        publication.setStatus(getDaoFactory().getCvObjectDao(CvPublicationStatus.class).getByIdentifier(CvPublicationStatusType.READY_FOR_CHECKING.identifier()));
        // try to register/update record in IMEx central. IMEx records are updated automatically with a cron job so if it has an IMEx id we do nothing
        if (publication.getAc() != null && getImexId() == null){
            try {
                imexCentralManager.registerAndUpdatePublication(publication.getAc());
            } catch (ImexCentralException e) {
                addWarningMessage("Impossible to register/update status of " + identifier + " in IMEx central", e.getMessage());
            }
        }
    }

    @Transactional
    public void putOnHold(ActionEvent evt) {
        setOnHold(reasonForOnHoldFromDialog);

        if (CvPublicationStatusType.READY_FOR_RELEASE.identifier().equals(publication.getStatus().getIdentifier())) {
            lifecycleManager.getReadyForReleaseStatus().putOnHold(publication, reasonForOnHoldFromDialog);
            addInfoMessage("On-hold added to publication", "Publication won't be released until the 'on hold' is removed");
        } else if (CvPublicationStatusType.RELEASED.identifier().equals(publication.getStatus().getIdentifier())) {
            lifecycleManager.getReleasedStatus().putOnHold(publication, reasonForOnHoldFromDialog);
            addInfoMessage("On-hold added to released publication", "Data will be publicly visible until the next release");
        }

        reasonForOnHoldFromDialog = null;
        // try to register/update record in IMEx central. IMEx records are updated automatically with a cron job so if it has an IMEx id we do nothing
        if (publication.getAc() != null && getImexId() == null){
            try {
                imexCentralManager.registerAndUpdatePublication(publication.getAc());
            } catch (ImexCentralException e) {
                addWarningMessage("Impossible to register/update status of " + identifier + " in IMEx central", e.getMessage());
            }
        }
    }

    @Transactional
    public void readyForReleaseFromOnHold(ActionEvent evt) {
        removeAnnotation(CvTopic.ON_HOLD);

        lifecycleManager.getAcceptedOnHoldStatus().onHoldRemoved(publication, null);
        // try to register/update record in IMEx central. IMEx records are updated automatically with a cron job so if it has an IMEx id we do nothing
        if (publication.getAc() != null && getImexId() == null){
            try {
                imexCentralManager.registerAndUpdatePublication(publication.getAc());
            } catch (ImexCentralException e) {
                addWarningMessage("Impossible to register/update status of " + identifier + " in IMEx central", e.getMessage());
            }
        }
    }

    public boolean isAllExperimentsAccepted() {
        return ExperimentUtils.areAllAccepted(publication.getExperiments());
    }

    public boolean isBackToCurationButtonRendered() {
        return isButtonRendered(CvLifecycleEventType.READY_FOR_CHECKING);
    }

    public boolean isBackToCheckingButtonRendered() {
        boolean render = isButtonRendered(CvLifecycleEventType.READY_FOR_RELEASE);

        if (!render) {
            render = isButtonRendered(CvLifecycleEventType.ACCEPTED);
        }

        return render;
    }

    private boolean isButtonRendered(CvLifecycleEventType eventType) {
        LifecycleEvent event = PublicationUtils.getLastEventOfType(publication, eventType.identifier());

        if (event == null) {
            return false;
        }

        DateTime eventTime = new DateTime(event.getWhen());

        return new DateTime().isBefore(eventTime.plusMinutes(getEditorConfig().getRevertDecisionTime()));
    }

    public void doSaveAndClose(ActionEvent evt) {
        doSave(evt);
        doClose(evt);
    }

    @Override
    public void doSave(boolean refreshCurrentView) {
        boolean registerPublication = (publication != null && publication.getAc() == null);
        super.doSave(refreshCurrentView);
        // try to register/update record in IMEx central. IMEx records are updated automatically with a cron job so if it has an IMEx id we do nothing
        if (registerPublication && getImexId() == null){
            try{
                imexCentralManager.registerAndUpdatePublication(publication.getAc());
            }
            // cannot check IMEx central, add warning and create publication
            catch (ImexCentralException e) {
                addWarningMessage("Impossible to register " + identifier + " in IMEx central", e.getMessage());
            }
        }
    }

    @Override
    public String doDelete() {
        if (publication.getAc() != null){
            try{
                imexCentralManager.discardPublication(publication.getAc());
            }
            // cannot check IMEx central, add warning and create publication
            catch (ImexCentralException e) {
                addWarningMessage("Impossible to discard " + identifier + " in IMEx central. Please do it manually.", e.getMessage());
            }
        }

        return super.doDelete();
    }

    public void addDataset(ActionEvent evt) {
        if (datasetToAdd != null) {
            datasetsSelectItems.add(getDatasetPopulator().createSelectItem(datasetToAdd));

            addAnnotation(CvTopic.DATASET_MI_REF, datasetToAdd);

            Collection<Experiment> experiments = publication.getExperiments();

            if (!IntactCore.isInitialized(publication.getExperiments())) {
                experiments = getDaoFactory().getExperimentDao().getByPubId(publication.getShortLabel());
            }

            if (!experiments.isEmpty()) {
                Collection<String> parentAcs = new ArrayList<String>();
                if (publication.getAc() != null) {
                    parentAcs.add(publication.getAc());
                }
                for (Experiment experiment : experiments) {
                    newAnnotatedObjectHelper(experiment).addAnnotation(CvTopic.DATASET_MI_REF, datasetToAdd);

                    getChangesController().markAsUnsaved(experiment, parentAcs);
                }
            }

            // reset the dataset to add as it has already been added
            datasetToAdd = null;
            setUnsavedChanges(true);
        }
    }

    public void createAndAddNewDataset(ActionEvent evt) {
        if (newDatasetDescriptionToCreate == null) {
            addErrorMessage("A short sentence describing the dataset is required", "dataset description required");
        }
        if (newDatasetNameToCreate == null) {
            addErrorMessage("A short dataset name is required", "dataset name required");
        }
        if (newDatasetDescriptionToCreate != null && newDatasetNameToCreate != null) {

            String newDataset = newDatasetNameToCreate + " - " + newDatasetDescriptionToCreate;
            DatasetPopulator populator = getDatasetPopulator();

            String sql = "select distinct a.annotationText from Annotation a where a.cvTopic.identifier = :dataset and lower(a.annotationText) like :name";
            Query query = getDaoFactory().getEntityManager().createQuery(sql);
            query.setParameter("dataset", CvTopic.DATASET_MI_REF);
            query.setParameter("name", newDatasetNameToCreate.toLowerCase() + " -%");

            if (!query.getResultList().isEmpty()) {
                addErrorMessage("A dataset with this name already exists. Cannot create two datasets with same name", "dataset name already exists");
            } else {
                // add the new dataset to the publication and list of datasets possible to remove from this publication
                datasetsSelectItems.add(populator.createSelectItem(newDataset));

                addAnnotation(CvTopic.DATASET_MI_REF, newDataset);

                Collection<Experiment> experiments = publication.getExperiments();

                if (!IntactCore.isInitialized(publication.getExperiments())) {
                    experiments = getDaoFactory().getExperimentDao().getByPubId(publication.getShortLabel());
                }

                if (!experiments.isEmpty()) {
                    Collection<String> parentAcs = new ArrayList<String>();
                    if (publication.getAc() != null) {
                        parentAcs.add(publication.getAc());
                    }
                    for (Experiment experiment : experiments) {
                        newAnnotatedObjectHelper(experiment).addAnnotation(CvTopic.DATASET_MI_REF, newDataset);

                        getChangesController().markAsUnsaved(experiment, parentAcs);
                    }
                }

                // reset the dataset to add as it has already been added
                newDatasetDescriptionToCreate = null;
                newDatasetNameToCreate = null;

                // save publication and refresh datasetPopulator
                getCorePersister().saveOrUpdate(publication);

                populator.refresh(null);
            }
        }
    }

    public void removeDatasets(ActionEvent evt) {
        if (datasetsToRemove != null) {
            for (String datasetToRemove : datasetsToRemove) {
                Iterator<SelectItem> iterator = datasetsSelectItems.iterator();

                while (iterator.hasNext()) {
                    SelectItem selectItem = iterator.next();
                    if (datasetToRemove.equals(selectItem.getValue())) {
                        iterator.remove();
                    }
                }

                removeAnnotation(CvTopic.DATASET_MI_REF, datasetToRemove);

                Collection<Experiment> experiments = publication.getExperiments();

                if (!IntactCore.isInitialized(publication.getExperiments())) {
                    experiments = getDaoFactory().getExperimentDao().getByPubId(publication.getShortLabel());
                }

                if (!experiments.isEmpty()) {
                    Collection<String> parentAcs = new ArrayList<String>();
                    if (publication.getAc() != null) {
                        parentAcs.add(publication.getAc());
                    }
                    for (Experiment experiment : experiments) {
                        newAnnotatedObjectHelper(experiment).removeAnnotation(CvTopic.DATASET_MI_REF, datasetToRemove);

                        getChangesController().markAsUnsaved(experiment, parentAcs);
                    }
                }
            }
            setUnsavedChanges(true);
        }
    }

    public boolean isUnassigned() {
        return publication.getShortLabel() != null && publication.getShortLabel().startsWith("unassigned");
    }

    private String createExperimentShortLabel() {
        return getFirstAuthor() + "-" + getYear();
    }

    public int countExperiments(Publication pub) {
        if (Hibernate.isInitialized(pub.getExperiments())) {
            return pub.getExperiments().size();
        } else if (pub.getAc() != null) {
            return getDaoFactory().getPublicationDao().countExperimentsForPublicationAc(pub.getAc());
        }

        return -1;
    }

    public int countInteractions(Publication pub) {
        if (pub.getAc() != null) {
            return getDaoFactory().getPublicationDao().countInteractionsForPublicationAc(pub.getAc());
        }

        return -1;
    }

    public boolean getParticipantsAvailable(Publication publication) {
        if (countInteractions(publication) == 0) {
            return false;
        } else {
            int count = 0;
            for (Experiment experiment : publication.getExperiments()) {
                for (Interaction interaction : experiment.getInteractions()) {
                    count = interaction.getComponents().size();
                    if (count > 0) {
                        continue;
                    }
                }
            }
            return (count > 0);
        }
    }


    public String getAc() {
        if (ac == null && publication != null) {
            return publication.getAc();
        }
        return ac;
    }

    public void setAc(String ac) {
        this.ac = ac;
    }

    public Publication getPublication() {
        return publication;
    }

    @Override
    public void refreshTabsAndFocusXref() {
        refreshTabs();
    }

    @Override
    public void refreshTabs() {
        super.refreshTabs();
        this.isLifeCycleDisabled = true;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;

        if (publication != null) {
            this.ac = publication.getAc();
        }
    }


    public String getJournal() {
        final String annot = findAnnotationText(CvTopic.JOURNAL_MI_REF);
        return annot;
    }


    public void setJournal(String journal) {
        setAnnotation(CvTopic.JOURNAL_MI_REF, journal);
    }

    public String getContactEmail() {
        return findAnnotationText(CvTopic.CONTACT_EMAIL_MI_REF);
    }

    public void setContactEmail(String contactEmail) {
        setAnnotation(CvTopic.CONTACT_EMAIL_MI_REF, contactEmail);
    }

    public String getSubmitted() {
        return findAnnotationText(SUBMITTED);
    }

    public void setSubmitted(String submitted) {
        setAnnotation(SUBMITTED, submitted);
    }

    public String getCurationRequest() {
        return findAnnotationText(CURATION_REQUEST);
    }

    public void setCurationRequest(String requestedCuration) {
        setAnnotation(CURATION_REQUEST, requestedCuration);
    }

    public Short getYear() {
        String strYear = findAnnotationText(CvTopic.PUBLICATION_YEAR_MI_REF);

        if (strYear != null) {
            return Short.valueOf(strYear);
        }

        return null;
    }

    public void setYear(Short year) {
        setAnnotation(CvTopic.PUBLICATION_YEAR_MI_REF, year);
    }

    public String getIdentifier() {
        if (publication != null) {
            String id = getPrimaryReference();

            if (id != null) {
                identifier = id;
            }
        }

        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;

        if (identifier != null && getAnnotatedObject() != null) {
            setPrimaryReference(identifier);

            Collection<Experiment> experiments = publication.getExperiments();

            if (!IntactCore.isInitialized(publication.getExperiments())) {
                experiments = getDaoFactory().getExperimentDao().getByPubId(publication.getShortLabel());
            }
            if (!experiments.isEmpty()) {
                Collection<String> parentAcs = new ArrayList<String>();
                if (publication.getAc() != null) {
                    parentAcs.add(publication.getAc());
                }
                for (Experiment experiment : experiments) {
                    newAnnotatedObjectHelper(experiment).setXref(CvDatabase.PUBMED_MI_REF, CvXrefQualifier.PRIMARY_REFERENCE_MI_REF, identifier, null);

                    getChangesController().markAsUnsaved(experiment, parentAcs);
                }
            }
        }
    }

    public String getPrimaryReference() {
        return findXrefPrimaryId(CvDatabase.PUBMED_MI_REF, CvXrefQualifier.PRIMARY_REFERENCE_MI_REF);
    }

    public void setPrimaryReference(String id) {
        setXref(CvDatabase.PUBMED_MI_REF, CvXrefQualifier.PRIMARY_REFERENCE_MI_REF, id);
    }

    public String getAuthors() {
        return findAnnotationText(CvTopic.AUTHOR_LIST_MI_REF);
    }

    public void setAuthors(String authors) {
        setAnnotation(CvTopic.AUTHOR_LIST_MI_REF, authors);
    }

    public String getOnHold() {
        return findAnnotationText(CvTopic.ON_HOLD);
    }

    public void setOnHold(String reason) {
        setAnnotation(CvTopic.ON_HOLD, reason);
    }

    public void setExperimentAnnotation(String topic, String text) {

        Collection<Experiment> experiments = publication.getExperiments();

        if (!IntactCore.isInitialized(publication.getExperiments())) {
            experiments = getDaoFactory().getExperimentDao().getByPubId(publication.getShortLabel());
        }

        if (!experiments.isEmpty()) {
            Collection<String> parentAcs = new ArrayList<String>();
            if (publication.getAc() != null) {
                parentAcs.add(publication.getAc());
            }
            for (Experiment experiment : experiments) {
                newAnnotatedObjectHelper(experiment).setAnnotation(topic, text);

                getChangesController().markAsUnsaved(experiment, parentAcs);
            }
        }
    }

    public void onHoldChanged(ValueChangeEvent evt) {
        setUnsavedChanges(true);

        setExperimentAnnotation(CvTopic.ON_HOLD, (String) evt.getNewValue());
    }

    public void curationDepthChanged() {
        setUnsavedChanges(true);

        setCurationDepthAnnot(curationDepth);
        setExperimentAnnotation(CURATION_DEPTH, curationDepth);
    }

    public void contactEmailChanged(ValueChangeEvent evt) {
        setUnsavedChanges(true);

        setExperimentAnnotation(CvTopic.CONTACT_EMAIL_MI_REF, (String) evt.getNewValue());
    }

    public void publicationYearChanged(ValueChangeEvent evt) {
        setUnsavedChanges(true);

        setExperimentAnnotation(CvTopic.PUBLICATION_YEAR_MI_REF, Short.toString((Short) evt.getNewValue()));
    }

    public void publicationTitleChanged(ValueChangeEvent evt) {
        setUnsavedChanges(true);

        copyPublicationTitleToExperiments(null);
    }

    public void publicationIdentifierChanged(ValueChangeEvent evt) {
        setUnsavedChanges(true);

        setIdentifier((String) evt.getNewValue());
    }

    public String getAcceptedMessage() {
        return findAnnotationText(CvTopic.ACCEPTED);
    }

    public String getCurationDepth() {
        return this.curationDepth;
    }

    public String getCurationDepthAnnotation() {
        return findAnnotationText(CURATION_DEPTH);
    }

    public String getShowCurationDepth() {
        String depth = getCurationDepth();
        if (depth == null) {
            depth = "curation depth undefined";
        }
        return depth;
    }

    public void setCurationDepth(String curationDepth) {
        this.curationDepth = curationDepth;
    }

    public void setCurationDepthAnnot(String curationDepth) {
        setAnnotation(CURATION_DEPTH, curationDepth);
    }

    @Transactional(readOnly = true)
    public boolean isAccepted() {
        if (isAcceptedOrBeyond(publication)) return true;

        Collection<Experiment> experiments = publication.getExperiments();

        if (!IntactCore.isInitialized(publication.getExperiments())) {
            experiments = getDaoFactory().getExperimentDao().getByPubId(publication.getShortLabel());
        }

        return ExperimentUtils.areAllAccepted(experiments);
    }

    public boolean isAcceptedOrBeyond(Publication pub) {
        if (pub == null || pub.getStatus() == null) {
            return false;
        }

        return pub.getStatus().getIdentifier().equals(CvPublicationStatusType.ACCEPTED.identifier()) ||
                pub.getStatus().getIdentifier().equals(CvPublicationStatusType.ACCEPTED_ON_HOLD.identifier()) ||
                pub.getStatus().getIdentifier().equals(CvPublicationStatusType.READY_FOR_RELEASE.identifier()) ||
                pub.getStatus().getIdentifier().equals(CvPublicationStatusType.RELEASED.identifier());
    }

    @Transactional(readOnly = true)
    public boolean isAccepted(Publication pub) {
        if (isAcceptedOrBeyond(pub)) return true;

        if (!Hibernate.isInitialized(pub.getExperiments())) {
            pub = getDaoFactory().getPublicationDao().getByAc(pub.getAc());
        }

        if (pub.getExperiments().isEmpty()) {
            return false;
        }

        return PublicationUtils.isAccepted(pub);
    }

    public void setAcceptedMessage(String message) {
        setAnnotation(CvTopic.ACCEPTED, message);
    }

    public boolean isToBeReviewed(Publication pub) {
        if (!IntactCore.isInitialized(pub.getExperiments())) {
            pub = getDaoFactory().getPublicationDao().getByAc(pub.getAc());
        }

        if (pub.getExperiments().isEmpty()) {
            return false;
        }

        return PublicationUtils.isToBeReviewed(pub);
    }

    public String getImexId() {
        return findXrefPrimaryId(CvDatabase.IMEX_MI_REF, CvXrefQualifier.IMEX_PRIMARY_MI_REF);
    }

    public void setImexId(String imexId) {
        setXref(CvDatabase.IMEX_MI_REF, CvXrefQualifier.IMEX_PRIMARY_MI_REF, imexId);
    }

    public String getPublicationTitle() {
        return publication.getFullName();
    }

    public void setPublicationTitle(String publicationTitle) {
        publication.setFullName(publicationTitle);
    }


    public String getFirstAuthor() {
        final String authors = getAuthors();

        if (authors != null) {
            return authors.split(" ")[0];
        }

        return null;
    }

    public void acceptPublication(ActionEvent evt) {
        UserSessionController userSessionController = (UserSessionController) getSpringContext().getBean("userSessionController");

        setAcceptedMessage("Accepted " + new SimpleDateFormat("yyyy-MMM-dd").format(new Date()).toUpperCase() + " by " + userSessionController.getCurrentUser().getLogin().toUpperCase());

        addInfoMessage("Publication accepted", "");

        //clear to-be-reviewed
        removeAnnotation(CvTopic.TO_BE_REVIEWED);

        // refresh experiments with possible changes in publication title, annotations and publication identifier
        copyAnnotationsToExperiments(null);
        copyPublicationTitleToExperiments(null);
        copyPrimaryIdentifierToExperiments();

        lifecycleManager.getReadyForCheckingStatus().accept(publication, null);

        if (!PublicationUtils.isOnHold(publication)) {
            lifecycleManager.getAcceptedStatus().readyForRelease(publication, "Accepted and not on-hold");
        }
    }

    public void assignNewImex(ActionEvent evt) {
        // save publication changes first
        getCorePersister().saveOrUpdate(publication);

        registerEditorListenerIfNotDoneYet();

        try {

            if (Pattern.matches(ImexCentralManager.PUBMED_REGEXP.toString(), publication.getPublicationId())) {
                imexCentralManager.assignImexAndUpdatePublication(publication.getAc());

                addInfoMessage("Successfully assigned new IMEx identifier to the publication " + publication.getShortLabel(), "");
            } else {
                addErrorMessage("Impossible to assign new IMEx id to unassigned publication. Must be assigned manually in IMEx central.", "");
            }

        } catch (PublicationImexUpdaterException e) {
            addErrorMessage("Impossible to assign new IMEx id", e.getMessage());
        } catch (ImexCentralException e) {
            IcentralFault f = (IcentralFault) e.getCause();

            processImexCentralException(publication.getShortLabel(), e, f);
        } catch (Exception e) {
            addErrorMessage("Impossible to assign new IMEx id", e.getMessage());
        }

        loadByAc();

        getChangesController().removeFromUnsaved(publication, collectParentAcsOfCurrentAnnotatedObject());
    }

    private void processImexCentralException(String publication, ImexCentralException e, IcentralFault f) {
        if (f.getFaultInfo().getFaultCode() == DefaultImexCentralClient.USER_NOT_AUTHORIZED) {
            addErrorMessage("User not authorized", e.getMessage());
        } else if (f.getFaultInfo().getFaultCode() == DefaultImexCentralClient.OPERATION_NOT_VALID) {
            addErrorMessage("Operation not valid", e.getMessage());
        } else if (f.getFaultInfo().getFaultCode() == DefaultImexCentralClient.IDENTIFIER_MISSING) {
            addErrorMessage("Publication identifier is missing", e.getMessage());
        } else if (f.getFaultInfo().getFaultCode() == DefaultImexCentralClient.IDENTIFIER_UNKNOWN) {
            addErrorMessage("Publication identifier is unknown (must be valid pubmed)", e.getMessage());
        } else if (f.getFaultInfo().getFaultCode() == DefaultImexCentralClient.NO_RECORD) {
            addErrorMessage("No IMEx record could be found for " + publication, e.getMessage());
        } else if (f.getFaultInfo().getFaultCode() == DefaultImexCentralClient.NO_RECORD_CREATED) {
            addErrorMessage("The publication could not be registered in IMEx central. Must be a valid pubmed Id", e.getMessage());
        } else if (f.getFaultInfo().getFaultCode() == DefaultImexCentralClient.STATUS_UNKNOWN) {
            addErrorMessage("The status of the publication is unknown is IMEx central", e.getMessage());
        } else if (f.getFaultInfo().getFaultCode() == DefaultImexCentralClient.NO_IMEX_ID) {
            addErrorMessage("No IMEx identifier could be found for this publication", e.getMessage());
        } else if (f.getFaultInfo().getFaultCode() == DefaultImexCentralClient.UNKNOWN_USER) {
            addErrorMessage("Unknown user in IMEx central", e.getMessage());
        } else if (f.getFaultInfo().getFaultCode() == DefaultImexCentralClient.UNKNOWN_GROUP) {
            addErrorMessage("Unknown group in IMEx central", e.getMessage());
        } else if (f.getFaultInfo().getFaultCode() == DefaultImexCentralClient.OPERATION_NOT_SUPPORTED) {
            addErrorMessage("Operation not supported in IMEx central", e.getMessage());
        } else if (f.getFaultInfo().getFaultCode() == DefaultImexCentralClient.INTERNAL_SERVER_ERROR) {
            addErrorMessage("Internal server error (IMEx central not responding)", e.getMessage());
        } else {
            addErrorMessage("Fatal error (IMEx central not responding)", e.getMessage());
        }
    }

    private void registerEditorListenerIfNotDoneYet() {
        if (imexCentralManager.getListenerList().getListenerCount() == 0) {
            imexCentralManager.addListener(new EditorImexCentralListener());
        }
    }

    public void rejectPublication(ActionEvent evt) {

        List<String> rejectionComments = new ArrayList<String>();

        for (Experiment exp : IntactCore.ensureInitializedExperiments(publication)) {
            if (ExperimentUtils.isToBeReviewed(exp)) {
                ExperimentController experimentController = (ExperimentController) getSpringContext().getBean("experimentController");
                rejectionComments.add("[" + exp.getShortLabel() + ": " + experimentController.getToBeReviewed(exp) + "]");
            }
        }

        rejectPublication(reasonForRejection + (rejectionComments.isEmpty() ? "" : " - " + StringUtils.join(rejectionComments, ", ")));

    }

    public void rejectPublication(String reasonForRejection) {
        UserSessionController userSessionController = (UserSessionController) getSpringContext().getBean("userSessionController");
        String date = "Rejected " + new SimpleDateFormat("yyyy-MMM-dd").format(new Date()).toUpperCase() + " by " + userSessionController.getCurrentUser().getLogin().toUpperCase();

        setToBeReviewed(date + ". " + reasonForRejection);

        addInfoMessage("Publication rejected", "");

        // refresh experiments with possible changes in publication title, annotations and publication identifier
        //copyAnnotationsToExperiments(null);
        //copyPublicationTitleToExperiments(null);
        copyPrimaryIdentifierToExperiments();

        lifecycleManager.getReadyForCheckingStatus().reject(publication, reasonForRejection);
    }

    public boolean isRejected(Publication publication) {
        return PublicationUtils.isRejected(publication);
    }

    public String getReasonForRejection(Publication publication) {
        LifecycleEvent event = PublicationUtils.getLastEventOfType(publication, CvLifecycleEventType.REJECTED.identifier());

        if (event != null) {
            return event.getNote();
        }

        return null;
    }

    public String calculateStatusStyle(Publication publication) {
        if (isAccepted(publication)) {
            return "ia-accepted";
        }

        int timesRejected = 0;
        int timesReadyForChecking = 0;

        for (LifecycleEvent evt : IntactCore.ensureInitializedLifecycleEvents(publication)) {
            if (CvLifecycleEventType.REJECTED.identifier().equals(evt.getEvent().getIdentifier())) {
                timesRejected++;
            } else if (CvLifecycleEventType.READY_FOR_CHECKING.identifier().equals(evt.getEvent().getIdentifier())) {
                timesReadyForChecking++;
            }
        }

        if (publication.getStatus().getIdentifier().equals(CvPublicationStatusType.CURATION_IN_PROGRESS.identifier()) && timesRejected > 0) {
            return "ia-rejected";
        } else if (publication.getStatus().getIdentifier().equals(CvPublicationStatusType.READY_FOR_CHECKING.identifier()) && timesReadyForChecking > 1) {
            return "ia-corrected";
        }

        return "";
    }

    public boolean isBeenRejectedBefore() {
        for (LifecycleEvent evt : IntactCore.ensureInitializedLifecycleEvents(publication)) {
            if (CvLifecycleEventType.REJECTED.identifier().equals(evt.getEvent().getIdentifier())) {
                return true;
            }
        }

        return false;
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

    public void clearToBeReviewed(ActionEvent evt) {
        removeAnnotation(CvTopic.TO_BE_REVIEWED);

        Collection<Experiment> experiments = publication.getExperiments();

        if (!IntactCore.isInitialized(publication.getExperiments())) {
            experiments = getDaoFactory().getExperimentDao().getByPubId(publication.getShortLabel());
        }
        if (!experiments.isEmpty()) {
            Collection<String> parentAcs = new ArrayList<String>();
            if (publication.getAc() != null) {
                parentAcs.add(publication.getAc());
            }
            for (Experiment experiment : experiments) {
                newAnnotatedObjectHelper(experiment).removeAnnotation(CvTopic.TO_BE_REVIEWED);

                getChangesController().markAsUnsaved(experiment, parentAcs);
            }
        }
    }

    public void copyAnnotationsToExperiments(ActionEvent evt) {
        for (Experiment exp : publication.getExperiments()) {
            CurateUtils.copyPublicationAnnotationsToExperiment(exp);
            Collection<String> parent = new ArrayList<String>();
            if (publication.getAc() != null) {
                parent.add(publication.getAc());
            }

            getChangesController().markAsUnsaved(exp, parent);
        }

        addInfoMessage("Annotations copied", publication.getExperiments().size() + " experiments were modified");
    }

    public void copyPublicationTitleToExperiments(ActionEvent evt) {
        for (Experiment exp : publication.getExperiments()) {
            exp.setFullName(publication.getFullName());
            Collection<String> parent = new ArrayList<String>();
            if (publication.getAc() != null) {
                parent.add(publication.getAc());
            }

            getChangesController().markAsUnsaved(exp, parent);
        }

        addInfoMessage("Publication title copied", publication.getExperiments().size() + " experiments were modified");
    }

    public void copyPrimaryIdentifierToExperiments() {
        Collection<Experiment> experiments = publication.getExperiments();

        if (publication.getShortLabel() != null) {
            if (!IntactCore.isInitialized(publication.getExperiments())) {
                experiments = getDaoFactory().getExperimentDao().getByPubId(publication.getShortLabel());
            }
            if (!experiments.isEmpty()) {
                Collection<String> parentAcs = new ArrayList<String>();
                if (publication.getAc() != null) {
                    parentAcs.add(publication.getAc());
                }
                for (Experiment experiment : experiments) {
                    newAnnotatedObjectHelper(experiment).setXref(CvDatabase.PUBMED_MI_REF, CvXrefQualifier.PRIMARY_REFERENCE_MI_REF, this.publication.getShortLabel(), null);

                    getChangesController().markAsUnsaved(experiment, parentAcs);
                }
            }
        }
    }

    public List<SelectItem> getDatasetsSelectItems() {
        return datasetsSelectItems;
    }

    public String getDatasetToAdd() {
        return datasetToAdd;
    }

    public void setDatasetToAdd(String datasetToAdd) {
        this.datasetToAdd = datasetToAdd;
    }

    public String[] getDatasetsToRemove() {
        return datasetsToRemove;
    }

    public void setDatasetsToRemove(String[] datasetsToRemove) {
        this.datasetsToRemove = datasetsToRemove;
    }

    public DatasetPopulator getDatasetPopulator() {
        return (DatasetPopulator) IntactContext.getCurrentInstance().getSpringContext().getBean("datasetPopulator");
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

    public String getIdentifierToOpen() {
        return identifierToOpen;
    }

    public void setIdentifierToOpen(String identifierToOpen) {
        this.identifierToOpen = identifierToOpen;
    }

    public String getIdentifierToImport() {
        return identifierToImport;
    }

    public void setIdentifierToImport(String identifierToImport) {
        this.identifierToImport = identifierToImport;
    }

    @Override
    protected void refreshUnsavedChangesBeforeRevert() {

        getChangesController().revertPublication(publication);
    }

    @Override
    public String goToParent() {
        return "/curate/curate?faces-redirect=true";
    }

    @Override
    protected void postRevert() {
        loadFormFields();
    }

    @Override
    public void doPostSave() {
        loadFormFields();
        refreshDataModels();
        getDatasetPopulator().refresh(null);
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

    public boolean isLifeCycleDisabled() {
        return isLifeCycleDisabled;
    }

    public void setLifeCycleDisabled(boolean lifeCycleDisabled) {
        isLifeCycleDisabled = lifeCycleDisabled;
    }

    public boolean isAssignedIMEx() {
        return getImexId() != null;
    }

    public boolean isAssignableIMEx() {
        return getImexId() == null && curationDepth != null
                && "imex curation".equalsIgnoreCase(curationDepth) && Pattern.matches(ImexCentralManager.PUBMED_REGEXP.toString(), publication.getPublicationId());
    }

    @Override
    public void modifyClone(AnnotatedObject clone) {
        refreshTabs();
    }

    @Override
    public void onTabChanged(TabChangeEvent e) {

        super.onTabChanged(e);

        if (isAnnotationTopicDisabled() && isAliasDisabled() && isXrefDisabled()) {
            if (e.getTab().getId().equals("lifeCycleTab")) {
                isLifeCycleDisabled = false;
            } else {
                isLifeCycleDisabled = true;
            }
        } else {
            isLifeCycleDisabled = true;
        }
    }

    public String getNewDatasetDescriptionToCreate() {
        return newDatasetDescriptionToCreate;
    }

    public void setNewDatasetDescriptionToCreate(String newDatasetDescriptionToCreate) {
        this.newDatasetDescriptionToCreate = newDatasetDescriptionToCreate;
    }

    public String getNewDatasetNameToCreate() {
        return newDatasetNameToCreate;
    }

    public void setNewDatasetNameToCreate(String newDatasetNameToCreate) {
        this.newDatasetNameToCreate = newDatasetNameToCreate;
    }
}
