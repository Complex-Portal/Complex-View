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
import uk.ac.ebi.intact.dataexchange.enricher.standard.InteractorEnricher;
import uk.ac.ebi.intact.editor.controller.BaseController;
import uk.ac.ebi.intact.editor.controller.curate.EnricherService;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.ProteinUtils;
import uk.ac.ebi.intact.uniprot.model.UniprotProtein;
import uk.ac.ebi.intact.uniprot.service.UniprotRemoteService;

import javax.faces.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    @Autowired
    private EnricherService enricherService;

    private List<ImportCandidate> importCandidates;
    private List<String> queriesNoResults;
    private String[] participantsToImport;

    private CvExperimentalRole cvExperimentalRole;
    private CvBiologicalRole cvBiologicalRole;

    public void importParticipants( ActionEvent evt ) {
        importCandidates = new ArrayList<ImportCandidate>();
        queriesNoResults = new ArrayList<String>();

        final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();
        cvExperimentalRole = daoFactory.getCvObjectDao(CvExperimentalRole.class).getByPsiMiRef(CvExperimentalRole.UNSPECIFIED_PSI_REF);
        cvBiologicalRole = daoFactory.getCvObjectDao(CvBiologicalRole.class).getByPsiMiRef(CvBiologicalRole.UNSPECIFIED_PSI_REF);

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

                if (log.isDebugEnabled()) log.debug("Adding to the interaction: "+participant);

                interaction.addComponent(participant);
            }
        }
    }



    private List<ImportCandidate> importParticipant(String participantToImport) {
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
                // uniprot AC
                Collection<ProteinImpl> proteins = proteinDao.getByUniprotId(participantToImport);

                for (Protein protein : proteins) {
                    candidates.add(toImportCandidate(participantToImport, protein));
                }
            }
        }

        return candidates;
    }



    private List<ImportCandidate> importFromUniprot(String participantToImport) {
        List<ImportCandidate> candidates = new ArrayList<ImportCandidate>();

        final Collection<UniprotProtein> uniprotProteins = uniprotRemoteService.retrieve(participantToImport);

        for (UniprotProtein uniprotProtein : uniprotProteins) {
            ImportCandidate candidate = new ImportCandidate(participantToImport, uniprotProtein);
            candidate.setSource("uniprotkb");
            candidates.add(candidate);
        }

        return candidates;
    }

    private ImportCandidate toImportCandidate(String participantToImport, Interactor interactor) {
        ImportCandidate candidate = new ImportCandidate(participantToImport, interactor);
        candidate.setSource(IntactContext.getCurrentInstance().getInstitution().getShortLabel());
        candidate.setPrimaryAc(ProteinUtils.getUniprotXref(interactor).getPrimaryId());

        List<String> secondaryAcs = new ArrayList<String>();

        for (Xref xref : interactor.getXrefs()) {
            if (CvDatabase.UNIPROT_MI_REF.equals(xref.getCvDatabase().getIdentifier())) {
                if (xref.getCvXrefQualifier() != null && CvXrefQualifier.SECONDARY_AC_MI_REF.equals(xref.getCvXrefQualifier().getIdentifier())) {
                    secondaryAcs.add(xref.getPrimaryId());
                }
            }
        }

        candidate.setSecondaryAcs(secondaryAcs);

        if (interactor.getBioSource() != null) {
            candidate.setOrganism(interactor.getBioSource().getShortLabel());
        }

        return candidate;
    }

    private Component toParticipant(ImportCandidate candidate, Interaction interaction) {
        Interactor interactor;

        if (candidate.getInteractor() != null) {
            interactor = candidate.getInteractor();
        } else {
            interactor = toProtein(candidate);
        }

        Component component = new Component(IntactContext.getCurrentInstance().getInstitution(),
                interaction, interactor, cvExperimentalRole, cvBiologicalRole );
        return component;
    }

    private Interactor toProtein(ImportCandidate candidate) {
        final Institution owner = IntactContext.getCurrentInstance().getInstitution();
        final UniprotProtein uniprotProtein = candidate.getUniprotProtein();

        CvInteractorType type = IntactContext.getCurrentInstance().getDaoFactory().getCvObjectDao(CvInteractorType.class)
                .getByPsiMiRef(CvInteractorType.PROTEIN_MI_REF);

        BioSource organism = new BioSource(owner, uniprotProtein.getOrganism().getName(), String.valueOf(uniprotProtein.getOrganism().getTaxid()));
        Protein protein = new ProteinImpl(owner, organism, uniprotProtein.getId().toLowerCase(), type);

        InteractorEnricher interactorEnricher = enricherService.getInteractorEnricher();
        interactorEnricher.enrich(protein);

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
}