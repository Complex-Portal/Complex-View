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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.ComponentDao;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.core.persistence.dao.InteractorDao;
import uk.ac.ebi.intact.core.persistence.dao.ProteinDao;
import uk.ac.ebi.intact.editor.config.EditorConfig;
import uk.ac.ebi.intact.editor.controller.BaseController;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.XrefUtils;
import uk.ac.ebi.intact.uniprot.model.UniprotProtein;
import uk.ac.ebi.intact.uniprot.service.UniprotRemoteService;

import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import java.util.*;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "general" )
public class ParticipantImportController extends BaseController {

    private static final Log log = LogFactory.getLog( ParticipantImportController.class );

    @Autowired
    private UniprotRemoteService uniprotRemoteService;

    @Autowired
    private InteractionController interactionController;

    //@Autowired
    //private EnricherService enricherService;

    private List<ImportCandidate> importCandidates;
    private List<String> queriesNoResults;
    private String[] participantsToImport = new String[0];

    private CvExperimentalRole cvExperimentalRole;
    private CvBiologicalRole cvBiologicalRole;
    private BioSource expressedIn;
    private CvExperimentalPreparation cvExperimentalPreparation;
    private float stoichiometry;

    @PostConstruct
    public void init() {
        EditorConfig editorConfig = getEditorConfig();
        stoichiometry = editorConfig.getDefaultStoichiometry();
    }

    public void importParticipants( ActionEvent evt ) {
        importCandidates = new ArrayList<ImportCandidate>();
        queriesNoResults = new ArrayList<String>();

        final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();
        cvExperimentalRole = daoFactory.getCvObjectDao(CvExperimentalRole.class).getByPsiMiRef(CvExperimentalRole.UNSPECIFIED_PSI_REF);
        cvBiologicalRole = daoFactory.getCvObjectDao(CvBiologicalRole.class).getByPsiMiRef(CvBiologicalRole.UNSPECIFIED_PSI_REF);

        if (participantsToImport == null) {
            addErrorMessage("No participants to import", "Please add at least one identifier in the box");
            return;
        }

        for (String participantToImport : participantsToImport) {
            List<ImportCandidate> candidates = importParticipant(participantToImport);

            if (candidates.isEmpty()) {
                queriesNoResults.add(participantToImport);
            } else {
                importCandidates.addAll(candidates);
            }
        }
    }

    public void addSelectedToInteraction(ActionEvent evt) {
        for (ImportCandidate candidate : importCandidates) {
            if (candidate.isSelected()) {
                final Interaction interaction = interactionController.getInteraction();
                Component participant = toParticipant(candidate, interaction);
                interactionController.addParticipant(participant);

                interactionController.setUnsavedChanges(true);
            }
        }
    }



    public List<ImportCandidate> importParticipant(String participantToImport) {
        if (participantToImport == null) {
            addErrorMessage("No participant to import", "Provide one");
            return Collections.EMPTY_LIST;
        }
        log.debug( "Importing participant: "+ participantToImport );

        List<ImportCandidate> candidates = importFromIntAct(participantToImport);

        if (candidates.isEmpty()) {
            candidates.addAll(importFromUniprot(participantToImport));
        }

        return candidates;
    }

    private List<ImportCandidate> importFromIntAct(String participantToImport) {
        List<ImportCandidate> candidates = new ArrayList<ImportCandidate>();

        final IntactContext context = IntactContext.getCurrentInstance();
        final ComponentDao componentDao = context.getDaoFactory().getComponentDao();
        final InteractorDao<InteractorImpl> interactorDao = context.getDaoFactory().getInteractorDao();
        final ProteinDao proteinDao = context.getDaoFactory().getProteinDao();

        // id
        if (participantToImport.startsWith(context.getConfig().getAcPrefix())) {
            Interactor interactor = interactorDao.getByAc(participantToImport);

            if (interactor != null) {
                candidates.add(toImportCandidate(participantToImport, interactor));
            } else {
                Component component = componentDao.getByAc(participantToImport);

                if (component != null) {
                    candidates.add(toImportCandidate(participantToImport, component.getInteractor()));
                }
            }
        } else {
            // shortLabel
            final Interactor interactorByLabel = interactorDao.getByShortLabel(participantToImport);

            if (interactorByLabel != null) {
                candidates.add(toImportCandidate(participantToImport, interactorByLabel));
            } else {
                final Interactor interactorByXref = interactorDao.getByXref(participantToImport);

                if (interactorByXref != null) {
                    candidates.add(toImportCandidate(participantToImport, interactorByXref));
                }
            }

//            if (candidates.isEmpty()) {
                // uniprot AC
                Collection<ProteinImpl> proteins = proteinDao.getByUniprotId(participantToImport);

                for (Protein protein : proteins) {
                    candidates.add(toImportCandidate(participantToImport, protein));
                }
//            }
        }

        return candidates;
    }



    private Set<ImportCandidate> importFromUniprot(String participantToImport) {
        Set<ImportCandidate> candidates = new HashSet<ImportCandidate>();

        final Collection<UniprotProtein> uniprotProteins = uniprotRemoteService.retrieve(participantToImport);

        for (UniprotProtein uniprotProtein : uniprotProteins) {
            ImportCandidate candidate = new ImportCandidate(participantToImport, uniprotProtein);
            candidate.setSource("uniprotkb");
            candidate.setInteractor(toProtein(candidate));
            candidates.add(candidate);
        }

        return candidates;
    }

    private ImportCandidate toImportCandidate(String participantToImport, Interactor interactor) {
        ImportCandidate candidate = new ImportCandidate(participantToImport, interactor);
        candidate.setSource(IntactContext.getCurrentInstance().getInstitution().getShortLabel());

        final Collection<InteractorXref> identityXrefs = XrefUtils.getIdentityXrefs(interactor);

        if (!identityXrefs.isEmpty()) {
            List<String> ids = new ArrayList<String>(identityXrefs.size());

            for (InteractorXref xref : identityXrefs) {
                ids.add(xref.getPrimaryId());
            }

            candidate.setPrimaryAcs(ids);
        }

        List<String> secondaryAcs = new ArrayList<String>();

        for (Xref xref : interactor.getXrefs()) {
            if (xref.getCvXrefQualifier() != null && CvXrefQualifier.SECONDARY_AC_MI_REF.equals(xref.getCvXrefQualifier().getIdentifier())) {
                secondaryAcs.add(xref.getPrimaryId());
            }
        }

        candidate.setSecondaryAcs(secondaryAcs);

        return candidate;
    }

    private Component toParticipant(ImportCandidate candidate, Interaction interaction) {
        Interactor interactor = candidate.getInteractor();

//        if (candidate.getInteractor() != null) {
//            interactor = candidate.getInteractor();
//        } else {
//            interactor = toProtein(candidate);
//        }

        Component component = new Component(IntactContext.getCurrentInstance().getInstitution(),
                interaction, interactor, cvExperimentalRole, cvBiologicalRole );
        component.setExpressedIn(expressedIn);
        component.setStoichiometry(stoichiometry);

        if (cvExperimentalPreparation != null) {
            component.setExperimentalPreparations(Collections.singleton(cvExperimentalPreparation));
        }

        return component;
    }

    private Interactor toProtein(ImportCandidate candidate) {
        final Institution owner = IntactContext.getCurrentInstance().getInstitution();
        final UniprotProtein uniprotProtein = candidate.getUniprotProtein();

        final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();
        CvInteractorType type = daoFactory.getCvObjectDao(CvInteractorType.class).getByPsiMiRef(CvInteractorType.PROTEIN_MI_REF);
        CvDatabase uniprotkb = daoFactory.getCvObjectDao(CvDatabase.class).getByPsiMiRef(CvDatabase.UNIPROT_MI_REF);
        CvXrefQualifier identity = daoFactory.getCvObjectDao(CvXrefQualifier.class).getByPsiMiRef(CvXrefQualifier.IDENTITY_MI_REF);

        BioSource organism = new BioSource(owner, uniprotProtein.getOrganism().getName(), String.valueOf(uniprotProtein.getOrganism().getTaxid()));
        Protein protein = new ProteinImpl(owner, organism, uniprotProtein.getId().toLowerCase(), type);
        protein.setFullName(uniprotProtein.getDescription());

        InteractorXref xref = new InteractorXref(owner, uniprotkb, candidate.getPrimaryAcs().iterator().next(), identity);
        protein.addXref(xref);

//        InteractorEnricher interactorEnricher = enricherService.getInteractorEnricher();
//        interactorEnricher.enrich(protein);

        return protein;
    }

    public String[] getParticipantsToImport() {
        return participantsToImport;
    }

    public void setParticipantsToImport(String[] participantsToImport) {
        this.participantsToImport = participantsToImport;
    }

    public List<ImportCandidate> getImportCandidates() {
        return importCandidates;
    }

    public void setImportCandidates(List<ImportCandidate> importCandidates) {
        this.importCandidates = importCandidates;
    }

    public CvExperimentalRole getCvExperimentalRole() {
        return cvExperimentalRole;
    }

    public void setCvExperimentalRole(CvExperimentalRole cvExperimentalRole) {
        this.cvExperimentalRole = cvExperimentalRole;
    }

    public CvBiologicalRole getCvBiologicalRole() {
        return cvBiologicalRole;
    }

    public void setCvBiologicalRole(CvBiologicalRole cvBiologicalRole) {
        this.cvBiologicalRole = cvBiologicalRole;
    }

    public List<String> getQueriesNoResults() {
        return queriesNoResults;
    }

    public void setQueriesNoResults(List<String> queriesNoResults) {
        this.queriesNoResults = queriesNoResults;
    }

    public InteractionController getInteractionController() {
        return interactionController;
    }

    public void setInteractionController(InteractionController interactionController) {
        this.interactionController = interactionController;
    }

    public BioSource getExpressedIn() {
        return expressedIn;
    }

    public void setExpressedIn(BioSource expressedIn) {
        this.expressedIn = expressedIn;
    }

    public CvExperimentalPreparation getCvExperimentalPreparation() {
        return cvExperimentalPreparation;
    }

    public void setCvExperimentalPreparation(CvExperimentalPreparation cvExperimentalPreparation) {
        this.cvExperimentalPreparation = cvExperimentalPreparation;
    }

    public float getStoichiometry() {
        return stoichiometry;
    }

    public void setStoichiometry(float stoichiometry) {
        this.stoichiometry = stoichiometry;
    }
}