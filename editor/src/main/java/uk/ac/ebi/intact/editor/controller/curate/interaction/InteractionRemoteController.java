/**
 * Copyright 2011 The European Bioinformatics Institute, and others.
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

import org.apache.commons.lang.StringUtils;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.editor.controller.UserSessionController;
import uk.ac.ebi.intact.editor.controller.curate.CurateController;
import uk.ac.ebi.intact.editor.controller.curate.experiment.ExperimentController;
import uk.ac.ebi.intact.editor.controller.curate.publication.PublicationController;
import uk.ac.ebi.intact.model.*;

import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * TODO comment this class header.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "general" )
public class InteractionRemoteController extends JpaAwareController {

    private String[] proteins;
    private String pubRef;

    private Collection<Interaction> interactions;
    private Publication publication;
    private Experiment experiment;
    private List<SelectItem> experimentSelectItems;

    private BioSource hostOrganism;
    private CvInteraction cvInteraction;
    private CvIdentification cvIdentification;

    public InteractionRemoteController() {
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void loadData( ComponentSystemEvent event ) {
        interactions = getDaoFactory().getInteractionDao().getByInteractorsPrimaryId(true, proteins);

        Iterator<Interaction> iterator = interactions.iterator();

        if (pubRef != null) {
            while (iterator.hasNext()) {
                Interaction interaction = iterator.next();

                for (Experiment exp : interaction.getExperiments()) {
                    if (!exp.getPublication().getPublicationId().equals(pubRef)) {
                        iterator.remove();
                    }
                }
            }
        }

        // redirect if one found
        if (interactions.size() == 1) {
            try {
                HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
                HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
                response.sendRedirect(request.getContextPath()+"/interaction/"+interactions.iterator().next().getAc());

                FacesContext.getCurrentInstance().responseComplete();
            } catch (IOException e) {
                handleException(e);
            }
        } else if (interactions.size() == 0) {
            publication = getDaoFactory().getPublicationDao().getByPubmedId(pubRef);

            if (publication != null) {
                InteractionController interactionController = (InteractionController) getSpringContext().getBean("interactionController");

                experimentSelectItems = new ArrayList<SelectItem>();
                experimentSelectItems.add(new SelectItem(null, "-- Select experiment --", null, false, true, true));

                for (Experiment exp : publication.getExperiments()) {
                    experimentSelectItems.add(new SelectItem(exp, interactionController.completeExperimentLabel(exp)));
                }
            }
        }
    }

    public String createNewInteraction() {
        if (experiment == null && (cvIdentification == null || cvInteraction == null || hostOrganism == null)) {
            addErrorMessage("Fields missing", "Select a value from all the drop down lists for the experiments");
            return null;
        }

        CurateController curateController = (CurateController) getSpringContext().getBean("curateController");

        PublicationController publicationController = (PublicationController) getSpringContext().getBean("publicationController");

        if (publication == null) {
            // create one
            publicationController.setIdentifier(pubRef);
            publicationController.newEmpty();
            publicationController.setUnsavedChanges(true);

            publication = publicationController.getPublication();
            publicationController.autocomplete(publication, pubRef);

            publicationController.doSave(false);
        } else {
            publicationController.setPublication(publication);
        }

        ExperimentController experimentController = (ExperimentController) getSpringContext().getBean("experimentController");

        if (experiment == null) {


            experimentController.newExperiment(publication);
            experimentController.setUnsavedChanges(true);

            experiment = experimentController.getExperiment();
            experiment.setCvIdentification(cvIdentification);
            experiment.setBioSource(hostOrganism);
            experiment.setCvInteraction(cvInteraction);

            experimentController.doSave(false);

        } else {
            experimentController.setExperiment(experiment);
        }

        InteractionController interactionController = (InteractionController) getSpringContext().getBean("interactionController");
        interactionController.newInteraction(publication, experiment);

        ParticipantImportController participantImportController = (ParticipantImportController) getSpringContext().getBean("participantImportController");

        List<ImportCandidate> candidates = new ArrayList<ImportCandidate>();

        // we get the first result that matches the primaryId. If none, get the first element
        for (String protein : proteins) {

            Set<ImportCandidate> proteinCandidates = participantImportController.importParticipant(protein);

            boolean candidateFound = false;

            for (ImportCandidate candidate : proteinCandidates) {
                if (candidate.getPrimaryAcs().contains(protein)) {
                    candidates.add(candidate);
                    candidateFound = true;
                    break;
                }
            }

            if (!candidateFound && !proteinCandidates.isEmpty()) {
                candidates.add(proteinCandidates.iterator().next());
            }
        }

        UserSessionController userSessionController = (UserSessionController) getSpringContext().getBean("userSessionController");

        // import the proteins
        Interaction interaction = interactionController.getInteraction();

        CvExperimentalRole unspecifiedExpRole = participantImportController.getCvExperimentalRole();
        CvBiologicalRole unspecifiedBioRole = participantImportController.getCvBiologicalRole();

        for (ImportCandidate candidate : candidates) {
            Interactor interactor = candidate.getInteractor();

            Component component = new Component(userSessionController.getUserInstitution(), interaction, interactor, unspecifiedExpRole, unspecifiedBioRole);
            interaction.addComponent(component);
        }

        // update the status of the controller
        interactionController.refreshExperimentLists();
        interactionController.refreshParticipants();
        interactionController.updateShortLabel();
        interactionController.setUnsavedChanges(true);

        return curateController.edit(interaction);
    }

    public String[] getProteins() {
        return proteins;
    }

    public void setProteins(String[] proteins) {
        this.proteins = proteins;
    }

    public String getProteinsAsString() {
        return StringUtils.join(proteins, ", ");
    }

    public String getPubRef() {
        return pubRef;
    }

    public void setPubRef(String pubRef) {
        this.pubRef = pubRef;
    }

    public Collection<Interaction> getInteractions() {
        return interactions;
    }

    public void setInteractions(Collection<Interaction> interactions) {
        this.interactions = interactions;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public List<SelectItem> getExperimentSelectItems() {
        return experimentSelectItems;
    }

    public void setExperimentSelectItems(List<SelectItem> experimentSelectItems) {
        this.experimentSelectItems = experimentSelectItems;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public BioSource getHostOrganism() {
        return hostOrganism;
    }

    public void setHostOrganism(BioSource hostOrganism) {
        this.hostOrganism = hostOrganism;
    }

    public CvInteraction getCvInteraction() {
        return cvInteraction;
    }

    public void setCvInteraction(CvInteraction cvInteraction) {
        this.cvInteraction = cvInteraction;
    }

    public CvIdentification getCvIdentification() {
        return cvIdentification;
    }

    public void setCvIdentification(CvIdentification cvIdentification) {
        this.cvIdentification = cvIdentification;
    }
}
