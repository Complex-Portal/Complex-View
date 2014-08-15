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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.jami.bridges.chebi.ChebiFetcher;
import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;
import psidev.psi.mi.jami.bridges.uniprot.UniprotGeneFetcher;
import psidev.psi.mi.jami.bridges.uniprot.UniprotProteinFetcher;
import psidev.psi.mi.jami.model.BioactiveEntity;
import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.Gene;
import psidev.psi.mi.jami.model.Participant;
import uk.ac.ebi.intact.editor.config.EditorConfig;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.editor.controller.curate.util.CheckIdentifier;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.context.IntactConfiguration;
import uk.ac.ebi.intact.jami.context.UserContext;
import uk.ac.ebi.intact.jami.dao.CvTermDao;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.dao.ModelledParticipantDao;
import uk.ac.ebi.intact.jami.model.extension.*;
import uk.ac.ebi.intact.jami.synchronizer.FinderException;
import uk.ac.ebi.intact.jami.synchronizer.PersisterException;
import uk.ac.ebi.intact.jami.synchronizer.SynchronizerException;
import uk.ac.ebi.intact.jami.utils.IntactUtils;

import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import java.util.*;

/**
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope("conversation.access")
@ConversationName("general")
public class ModelledParticipantImportController extends JpaAwareController {

    private static final Log log = LogFactory.getLog(ModelledParticipantImportController.class);

    @Autowired
    @Qualifier("proteinFetcher")
    private UniprotProteinFetcher uniprotProteinFetcher;

    @Autowired
    @Qualifier("bioactiveEntityFetcher")
    private ChebiFetcher chebiFetcher;

    @Autowired
    @Qualifier("geneFetcher")
    private UniprotGeneFetcher uniprotGeneFetcher;

    @Autowired
    private ComplexController interactionController;

    private List<ImportJamiCandidate> importCandidates;
    private List<String> queriesNoResults;
    private String[] participantsToImport = new String[0];

    private transient CvTerm cvBiologicalRole;
    private int minStoichiometry;
    private int maxStoichiometry;

    private final static String FEATURE_CHAIN = "PRO_";

    @PostConstruct
    public void init() {
        EditorConfig editorConfig = getEditorConfig();
        this.minStoichiometry = (int)editorConfig.getDefaultStoichiometry();
        this.maxStoichiometry = (int)editorConfig.getDefaultStoichiometry();
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void importParticipants(ActionEvent evt) {
        importCandidates = new ArrayList<ImportJamiCandidate>();
        queriesNoResults = new ArrayList<String>();

        IntactDao intactDao = getIntactDao();
        CvTermDao cvObjectService = intactDao.getCvTermDao();

        cvBiologicalRole = cvObjectService.getByMIIdentifier(Participant.UNSPECIFIED_ROLE, IntactUtils.BIOLOGICAL_ROLE_OBJCLASS);

        if (participantsToImport == null) {
            addErrorMessage("No participants to import", "Please add at least one identifier in the box");
            return;
        }

        for (String participantToImport : participantsToImport) {
            participantToImport = participantToImport.trim();

            if (participantToImport.isEmpty()) {
                continue;
            }

            // only import if the query has more than 4 chars (to avoid massive queries) {

            if (participantToImport.length() < 4) {
                queriesNoResults.add(participantToImport + " (short query - less than 4 chars.)");
            } else if (participantToImport.contains("*")) {
                queriesNoResults.add(participantToImport + " (wildcards not allowed)");
            } else {
                Set<ImportJamiCandidate> candidates = importParticipant(participantToImport);

                if (candidates.isEmpty()) {
                    queriesNoResults.add(participantToImport);
                } else {
                    importCandidates.addAll(candidates);
                }
            }
        }

        participantsToImport = new String[0];
    }

    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED)
    public void importSelected(ActionEvent evt) {
        for (ImportJamiCandidate candidate : importCandidates) {
            if (candidate.isSelected()) {
                final IntactComplex interaction = interactionController.getComplex();
                IntactModelledParticipant participant = toParticipant(candidate, interaction);
                interactionController.addParticipant(participant);

                interactionController.setUnsavedChanges(true);
            }
        }
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public Set<ImportJamiCandidate> importParticipant(String participantToImport) {
        if (participantToImport == null) {
            addErrorMessage("No participant to import", "Provide one or more accessions");
            return Collections.EMPTY_SET;
        }
        log.debug("Importing participant: " + participantToImport);

        Set<ImportJamiCandidate> candidates = importFromIntAct(participantToImport.toUpperCase());

        if (candidates.isEmpty()) {     //It is not an IntAct one

            CandidateType candidateType = detectCandidate(participantToImport.toUpperCase());

            try {
                switch (candidateType) {
                    case BIO_ACTIVE_ENTITY:
                        candidates = importFromChebi(participantToImport.toUpperCase());
                        break;
                    case GENE:
                        candidates = importFromSwissProtWithEnsemblId(participantToImport.toUpperCase());
                        break;
                    case PROTEIN:
                        candidates = importFromUniprot(participantToImport.toUpperCase());
                        break;
                }
            } catch (Exception e) {
                addErrorMessage("Cannot import participants", "Problem fetching participant: " + participantToImport);
                handleException(e);
                return Collections.EMPTY_SET;
            }

            // only pre-select those that match the query
            for (ImportJamiCandidate candidate : candidates) {
                candidate.setSelected(false);

                for (String primaryAc : candidate.getPrimaryAcs()) {
                    if (candidate.getQuery().equalsIgnoreCase(primaryAc)) {
                        candidate.setSelected(true);
                        break;
                    }
                    // for feature chains, in IntAct, we add the parent uniprot ac before the chain id so feature chains are never pre-selected
                    else if (candidate.isChain() && primaryAc.toUpperCase().contains(FEATURE_CHAIN)) {
                        int indexOfChain = primaryAc.indexOf(FEATURE_CHAIN);

                        String chain_ac = primaryAc.substring(indexOfChain);

                        if (candidate.getQuery().equalsIgnoreCase(chain_ac)) {
                            candidate.setSelected(true);
                            break;
                        }
                    }
                }
            }

            candidates.addAll(candidates);

        }

        return candidates;
    }

    private CandidateType detectCandidate(String candidateId) {
        if (CheckIdentifier.checkChebiId(candidateId)) {
            return CandidateType.BIO_ACTIVE_ENTITY;
        } else if (CheckIdentifier.checkEnsembleId(candidateId)) {
            return CandidateType.GENE;
        } else { //If the identifier is not one of the previous we suppose that is a UniprotKB
            return CandidateType.PROTEIN;
        }
    }

    private Set<ImportJamiCandidate> importFromIntAct(String participantToImport) {
        Set<ImportJamiCandidate> candidates = new HashSet<ImportJamiCandidate>();

        IntactConfiguration config = ApplicationContextProvider.getBean("intactJamiConfiguration");
        IntactDao intactDao = getIntactDao();
        final ModelledParticipantDao componentDao = intactDao.getModelledParticipantDao();
        final uk.ac.ebi.intact.jami.dao.InteractorDao<IntactInteractor> interactorDao = intactDao.getInteractorDao(IntactInteractor.class);

        // id
        if (participantToImport.startsWith(config.getAcPrefix())) {
            IntactInteractor interactor = interactorDao.getByAc(participantToImport);

            if (interactor != null) {
                candidates.add(toImportCandidate(participantToImport, interactor));
            } else {
                IntactModelledParticipant component = componentDao.getByAc(participantToImport);

                if (component != null) {
                    candidates.add(toImportCandidate(participantToImport, component.getInteractor()));
                }
            }
        } else {
            // identity xref
            Collection<IntactInteractor> interactorsByXref = interactorDao.getByXref(participantToImport);

            for (IntactInteractor interactorByXref : interactorsByXref) {
                for (psidev.psi.mi.jami.model.Xref ref : interactorByXref.getIdentifiers()){
                    if (psidev.psi.mi.jami.utils.XrefUtils.isXrefAnIdentifier(ref)){
                        candidates.add(toImportCandidate(participantToImport, interactorByXref));
                    }
                }
            }

            if (candidates.isEmpty()) {
                // shortLabel
                final Collection<IntactInteractor> interactorsByLabel = interactorDao.getByShortNameLike(participantToImport);

                for (IntactInteractor interactor : interactorsByLabel) {
                    candidates.add(toImportCandidate(participantToImport, interactor));
                }
            }
        }

        return candidates;
    }


    private Set<ImportJamiCandidate> importFromUniprot(String participantToImport) throws BridgeFailedException {
        Set<ImportJamiCandidate> candidates = new HashSet<ImportJamiCandidate>();

        final Collection<psidev.psi.mi.jami.model.Protein> uniprotProteins = uniprotProteinFetcher.fetchByIdentifier(participantToImport);

        for (psidev.psi.mi.jami.model.Protein uniprotProtein : uniprotProteins) {
            ImportJamiCandidate candidate = new ImportJamiCandidate(participantToImport, uniprotProtein);
            candidate.setSource("uniprotkb");
            candidate.setInteractor(uniprotProtein);
            candidates.add(candidate);
        }

        return candidates;
    }


    private Set<ImportJamiCandidate> importFromChebi(String participantToImport) throws BridgeFailedException {
        Set<ImportJamiCandidate> candidates = new HashSet<ImportJamiCandidate>();

        final Collection<BioactiveEntity> smallMolecule = chebiFetcher.fetchByIdentifier(participantToImport);
        for (BioactiveEntity entity : smallMolecule){
            ImportJamiCandidate candidate = toImportCandidate(participantToImport, entity);
            candidate.setSource("chebi");

            candidates.add(candidate);
        }

        return candidates;
    }

    private Set<ImportJamiCandidate> importFromSwissProtWithEnsemblId(String participantToImport) throws BridgeFailedException {
        Set<ImportJamiCandidate> candidates = new HashSet<ImportJamiCandidate>();

        final Collection<Gene> genes = uniprotGeneFetcher.fetchByIdentifier(participantToImport);

        for (Gene gene : genes) {
            ImportJamiCandidate candidate = toImportCandidate(participantToImport, gene);
            candidate.setSource("ensembl");

            candidates.add(candidate);
        }
        return candidates;
    }


    private ImportJamiCandidate toImportCandidate(String participantToImport, psidev.psi.mi.jami.model.Interactor interactor) {
        ImportJamiCandidate candidate = new ImportJamiCandidate(participantToImport, interactor);
        candidate.setSource("intact");

        final Collection<psidev.psi.mi.jami.model.Xref> identityXrefs = psidev.psi.mi.jami.utils.XrefUtils.collectAllXrefsHavingQualifier(interactor.getIdentifiers(),
                psidev.psi.mi.jami.model.Xref.IDENTITY_MI, psidev.psi.mi.jami.model.Xref.IDENTITY);

        if (!identityXrefs.isEmpty()) {
            List<String> ids = new ArrayList<String>(identityXrefs.size());

            for (psidev.psi.mi.jami.model.Xref xref : identityXrefs) {
                ids.add(xref.getId());
            }

            candidate.setPrimaryAcs(ids);
        }

        Collection<psidev.psi.mi.jami.model.Xref> secondaryAcs = psidev.psi.mi.jami.utils.XrefUtils.collectAllXrefsHavingQualifier(interactor.getIdentifiers(),
                psidev.psi.mi.jami.model.Xref.SECONDARY_MI, psidev.psi.mi.jami.model.Xref.SECONDARY);
        List<String> secondaryIds = new ArrayList<String>(secondaryAcs.size());

        for (psidev.psi.mi.jami.model.Xref xref : secondaryAcs) {
            secondaryIds.add(xref.getId());
        }

        candidate.setSecondaryAcs(secondaryIds);

        return candidate;
    }

    protected IntactModelledParticipant toParticipant(ImportJamiCandidate candidate, IntactComplex interaction) {
        psidev.psi.mi.jami.model.Interactor interactor = candidate.getInteractor();

        if (cvBiologicalRole == null) {
            IntactDao intactDao = getIntactDao();
            CvTermDao cvObjectService = intactDao.getCvTermDao();

            cvBiologicalRole = cvObjectService.getByMIIdentifier(Participant.UNSPECIFIED_ROLE, IntactUtils.BIOLOGICAL_ROLE_OBJCLASS);
        }

        IntactModelledParticipant component = null;
        try {
            component = new IntactModelledParticipant(getIntactDao().
                    getSynchronizerContext().
                    getInteractorSynchronizer().synchronize(interactor, true));
        } catch (FinderException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot import interactor: " + e.getMessage(), ExceptionUtils.getFullStackTrace(e));
        } catch (PersisterException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot import interactor: " + e.getMessage(), ExceptionUtils.getFullStackTrace(e));
        } catch (SynchronizerException e) {
            // clear cache
            getIntactDao().getSynchronizerContext().clearCache();
            addErrorMessage("Cannot import interactor: " + e.getMessage(), ExceptionUtils.getFullStackTrace(e));
        }
        UserContext jamiUserContext = ApplicationContextProvider.getBean("jamiUserContext");
        component.setCreator(jamiUserContext.getUserId());
        component.setUpdator(jamiUserContext.getUserId());
        component.setCreated(new Date());
        component.setUpdated(component.getCreated());
        component.setInteraction(interaction);
        component.setStoichiometry(new IntactStoichiometry(minStoichiometry,maxStoichiometry));
        component.setBiologicalRole(cvBiologicalRole);

        if (candidate.isChain() || candidate.isIsoform()) {
            Collection<String> parentAcs = new ArrayList<String>();

            if (interaction.getAc() != null) {
                parentAcs.add(interaction.getAc());

                addParentAcsTo(parentAcs, interaction);
            }
        }

        return component;
    }

    /**
     * Add all the parent acs of this interaction
     *
     * @param parentAcs
     * @param inter
     */
    protected void addParentAcsTo(Collection<String> parentAcs, IntactComplex inter) {
        if (inter.getAc() != null) {
            parentAcs.add(inter.getAc());
        }
    }


    public String[] getParticipantsToImport() {
        return participantsToImport;
    }

    public void setParticipantsToImport(String[] participantsToImport) {
        this.participantsToImport = participantsToImport;
    }

    public List<ImportJamiCandidate> getImportCandidates() {
        return importCandidates;
    }

    public void setImportCandidates(List<ImportJamiCandidate> importCandidates) {
        this.importCandidates = importCandidates;
    }

    public CvTerm getBiologicalRole() {
        return cvBiologicalRole;
    }

    public void setBiologicalRole(CvTerm cvBiologicalRole) {
        this.cvBiologicalRole = cvBiologicalRole;
    }

    public List<String> getQueriesNoResults() {
        return queriesNoResults;
    }

    public void setQueriesNoResults(List<String> queriesNoResults) {
        this.queriesNoResults = queriesNoResults;
    }

    public ComplexController getInteractionController() {
        return interactionController;
    }

    public void setInteractionController(ComplexController interactionController) {
        this.interactionController = interactionController;
    }

    public int getMinStoichiometry() {
        return minStoichiometry;
    }

    public void setMinStoichiometry(int stoichiometry) {
        this.minStoichiometry = stoichiometry;
        this.maxStoichiometry = Math.max(minStoichiometry, this.maxStoichiometry);
    }

    public int getMaxStoichiometry() {
        return maxStoichiometry;
    }

    public void setMaxStoichiometry(int stoichiometry) {
        this.maxStoichiometry = stoichiometry;
        this.minStoichiometry = Math.min(this.minStoichiometry, maxStoichiometry);
    }
}
