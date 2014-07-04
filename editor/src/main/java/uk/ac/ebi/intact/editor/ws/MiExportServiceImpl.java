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
package uk.ac.ebi.intact.editor.ws;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.BinaryInteractionImpl;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.OrganismImpl;
import psidev.psi.mi.xml.PsimiXmlForm;
import psidev.psi.mi.xml.converter.ConverterContext;
import psidev.psi.mi.xml.model.Entry;
import psidev.psi.mi.xml.model.EntrySet;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared.EntryConverter;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.PublicationUtils;
import uk.ac.ebi.intact.psimitab.converters.Intact2BinaryInteractionConverter;
import uk.ac.ebi.intact.psimitab.converters.converters.ExperimentConverter;
import uk.ac.ebi.intact.psimitab.converters.converters.PublicationConverter;
import uk.ac.ebi.intact.psimitab.converters.expansion.ExpansionStrategy;
import uk.ac.ebi.intact.psimitab.converters.expansion.NotExpandableInteractionException;
import uk.ac.ebi.intact.psimitab.converters.expansion.SpokeWithoutBaitExpansion;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.util.*;

/**
 * TODO comment this class header.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class MiExportServiceImpl implements MiExportService {
    private final static String RELEASED_EVT_ID = "PL:0028";

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public Object exportPublication(final String ac, final String format) {
        Response response = null;
        try {
            String responseType = "application/x-download";
            String extension = calculateFileExtension(format);
            StreamingOutput output = null;

            if (format.equals("xml254") || format.equals("html") || format.equals("json")){
                output = new IntactEntryStreamingOutput(format) {
                    @Override
                    public EntrySet createIntactEntry() {
                        return createEntrySetFromPublication(ac);
                    }
                };
            }
            else if (format.equals("sda")){
                output = new IntactEntryStreamingOutput(format) {
                    @Override
                    public Publication createIntactEntry() {
                        return IntactContext.getCurrentInstance().getDaoFactory().getPublicationDao().getByAc(ac);
                    }
                };
            }
            else {
                output = new IntactEntryStreamingOutput(format) {
                    @Override
                    public Collection<psidev.psi.mi.tab.model.BinaryInteraction> createIntactEntry() {
                        return createBinaryInteractionsFromPublication(ac);
                    }
                };
            }

            response = Response.status(200).type(responseType).header("Content-Disposition", "attachment; filename="+ac+"."+extension).entity(output).build();
        } catch (Throwable e) {
            throw new RuntimeException("Problem exporting publication: "+ac, e);
        }

        return response;
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public Object exportExperiment(final String ac, final String format) {
        Response response = null;
        try {
            String responseType = "application/x-download";
            String extension = calculateFileExtension(format);
            StreamingOutput output = null;

            if (format.equals("xml254") || format.equals("html") || format.equals("json")){
                output = new IntactEntryStreamingOutput(format) {
                    @Override
                    public EntrySet createIntactEntry() {
                        return createEntrySetFromExperiment(ac);
                    }
                };
            }
            else if (format.equals("sda")){
                output = new IntactEntryStreamingOutput(format) {
                    @Override
                    public Experiment createIntactEntry() {
                        return IntactContext.getCurrentInstance().getDaoFactory().getExperimentDao().getByAc(ac);
                    }
                };
            }
            else {
                output = new IntactEntryStreamingOutput(format) {
                    @Override
                    public Collection<psidev.psi.mi.tab.model.BinaryInteraction> createIntactEntry() {
                        return createBinaryInteractionsFromExperiment(ac);
                    }
                };
            }

            response = Response.status(200).type(responseType).header("Content-Disposition", "attachment; filename="+ac+"."+extension).entity(output).build();
        } catch (Throwable e) {
            throw new RuntimeException("Problem exporting experiment: "+ac, e);
        }

        return response;
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public Object exportInteraction(final String ac, final String format) {
        Response response = null;
        try {
            String responseType = "application/x-download";
            String extension = calculateFileExtension(format);
            StreamingOutput output = null;

            if (format.equals("xml254") || format.equals("html") || format.equals("json")){
                output = new IntactEntryStreamingOutput(format) {
                    @Override
                    public EntrySet createIntactEntry() {
                        return createEntrySetFromInteraction(ac);
                    }
                };
            }
            else if (format.equals("sda")){
                output = new IntactEntryStreamingOutput(format) {
                    @Override
                    public Interaction createIntactEntry() {
                        return IntactContext.getCurrentInstance().getDaoFactory().getInteractionDao().getByAc(ac);
                    }
                };
            }
            else {
                output = new IntactEntryStreamingOutput(format) {
                    @Override
                    public Collection<psidev.psi.mi.tab.model.BinaryInteraction> createIntactEntry() {
                        return createBinaryInteractionsFromInteraction(ac);
                    }
                };
            }

            response = Response.status(200).type(responseType).header("Content-Disposition", "attachment; filename="+ac+"."+extension).entity(output).build();
        } catch (Throwable e) {
            throw new RuntimeException("Problem exporting interaction: "+ac, e);
        }

        return response;
    }

    private String calculateResponseType(String format) {
        String responseType;

        if (format.contains("xml")) {
            responseType = "application/xml";
        } else if (format.contains("tab")) {
            responseType = "text/plain";
        } else if (format.contains("html")) {
            responseType = "text/html";
        } else if (format.contains("json")) {
            responseType = "text/json";
        } else if (format.contains("graphml")) {
            responseType = "text/plain";
        } else {
            throw new IllegalArgumentException("Unexpected format: "+format);
        }
        return responseType;
    }

    private String calculateFileExtension(String format) {
        String responseType;

        if (format.contains("xml")) {
            responseType = "xml";
        } else if (format.contains("tab")) {
            responseType = "txt";
        } else if (format.contains("html")) {
            responseType = "html";
        } else if (format.contains("json")) {
            responseType = "txt";
        } else if (format.contains("graphml")) {
            responseType = "xml";
        }
        else if (format.contains("sda")) {
            responseType = "html";
        } else {
            throw new IllegalArgumentException("Unexpected format: "+format);
        }
        return responseType;
    }


    private EntrySet createEntrySetFromPublication(String pubAc){
        // we export compact xml only and excludes hidden annotations
        uk.ac.ebi.intact.dataexchange.psimi.xml.converter.ConverterContext.getInstance().setGenerateExpandedXml(false);
        ConverterContext.getInstance().getConverterConfig().setXmlForm(PsimiXmlForm.FORM_COMPACT);
        uk.ac.ebi.intact.dataexchange.psimi.xml.converter.ConverterContext.getInstance().getAnnotationConfig().setExcludeHiddenTopics(true);


        // reattach the publication object to the entity manager because connection may have been closed after reading the object
        Publication publication = IntactContext.getCurrentInstance().getDaoFactory().getPublicationDao().getByAc(pubAc);

        // if the publication does not have any experiments, we skip it
        if (publication == null || publication.getExperiments().isEmpty()){
            return new EntrySet(Collections.EMPTY_LIST, 2, 5, 4);
        }

        // iterator of experiments
        Iterator<Experiment> iterator = publication.getExperiments().iterator();

        // the released date of the publication
        Date releasedDate;
        LifecycleEvent evt = PublicationUtils.getLastEventOfType(publication, RELEASED_EVT_ID);
        if (evt == null){
            releasedDate = new Date();
        }
        else {
            releasedDate = evt.getWhen();
        }

        Set<String> interactorsAcs = new HashSet<String>();

        IntactEntry intactEntry = new IntactEntry();
        // set institution
        intactEntry.setInstitution(publication.getOwner());
        // set release date
        intactEntry.setReleasedDate(releasedDate);

        // convert experiments in one to several publication entry(ies)
        while (iterator.hasNext()){
            // the processed experiment
            Experiment exp = iterator.next();

            // add experiment to intact entry
            intactEntry.getExperimentsList().add(exp);
            // add all the interactions to the currentIntactEntry
            for (Interaction inter : exp.getInteractions()){
                processInteractionForEntrySet(inter, interactorsAcs, intactEntry);
            }
        }

        EntrySet entrySet = createEntrySetFromIntactEntry(intactEntry);

        IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().clear();

        return entrySet;
    }

    private Collection<psidev.psi.mi.tab.model.BinaryInteraction> createBinaryInteractionsFromPublication(String pubAc){
        // reattach the publication object to the entity manager because connection may have been closed after reading the object
        Publication publication = IntactContext.getCurrentInstance().getDaoFactory().getPublicationDao().getByAc(pubAc);

        // if the publication does not have any experiments, we skip it
        if (publication == null || publication.getExperiments().isEmpty()){
            return Collections.EMPTY_LIST;
        }

        PublicationConverter publicationConverter = new PublicationConverter();
        ExperimentConverter experimentConverter = new ExperimentConverter();
        ExpansionStrategy expansionStrategy = new SpokeWithoutBaitExpansion(false, false);
        Intact2BinaryInteractionConverter intactInteractionConverter = new Intact2BinaryInteractionConverter(expansionStrategy);

        // convert the publication
        BinaryInteractionImpl binaryTemplate = new BinaryInteractionImpl();

        publicationConverter.intactToMitab(publication, binaryTemplate);

        // iterator of experiments
        Iterator<Experiment> iterator = publication.getExperiments().iterator();

        Collection<psidev.psi.mi.tab.model.BinaryInteraction> binaryInteractions = new ArrayList<psidev.psi.mi.tab.model.BinaryInteraction>();

        // convert experiments in one to several publication entry(ies)
        while (iterator.hasNext()){
            // clear previous experiment details
            binaryTemplate.setDetectionMethods(new ArrayList<CrossReference>());
            binaryTemplate.setHostOrganism(new OrganismImpl());

            // the processed experiment
            Experiment exp = iterator.next();

            // convert experiment details
            experimentConverter.intactToMitab(exp, binaryTemplate, true, false);

            for (Interaction interaction : exp.getInteractions()){
                try {
                    processBinaryInteractionsFor(exp, experimentConverter, intactInteractionConverter, binaryTemplate, binaryInteractions, interaction);

                } catch (Exception e) {
                    throw new RuntimeException("Problem exporting publication: "+pubAc, e);
                }
            }
        }

        IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().clear();

        return binaryInteractions;
    }

    private EntrySet createEntrySetFromExperiment(String expAc){
        // we export compact xml only and excludes hidden annotations
        uk.ac.ebi.intact.dataexchange.psimi.xml.converter.ConverterContext.getInstance().setGenerateExpandedXml(false);
        ConverterContext.getInstance().getConverterConfig().setXmlForm(PsimiXmlForm.FORM_COMPACT);
        uk.ac.ebi.intact.dataexchange.psimi.xml.converter.ConverterContext.getInstance().getAnnotationConfig().setExcludeHiddenTopics(true);


        Experiment experiment = IntactContext.getCurrentInstance().getDaoFactory().getExperimentDao().getByAc(expAc);

        // if the experiment does not have any interactions, we skip it
        if (experiment == null || experiment.getInteractions().isEmpty()){
            return new EntrySet(Collections.EMPTY_LIST, 2, 5, 4);
        }

        Publication pub = experiment.getPublication();

        // the released date of the publication
        Date releasedDate;
        if (pub != null){
            LifecycleEvent evt = PublicationUtils.getLastEventOfType(pub, RELEASED_EVT_ID);
            if (evt == null){
                releasedDate = new Date();
            }
            else {
                releasedDate = evt.getWhen();
            }
        }
        else {
            releasedDate = new Date();
        }

        Set<String> interactorsAcs = new HashSet<String>();

        IntactEntry intactEntry = new IntactEntry();
        // set institution
        intactEntry.setInstitution(experiment.getOwner());
        // set release date
        intactEntry.setReleasedDate(releasedDate);

        // convert experiments in one to several publication entry(ies)
        // add experiment to intact entry
        intactEntry.getExperimentsList().add(experiment);
        // add all the interactions to the currentIntactEntry
        for (Interaction inter : experiment.getInteractions()){
            intactEntry.getInteractions().add(inter);
            for (Component comp : inter.getComponents()){
                Interactor interactor = comp.getInteractor();
                Collection<Interactor> interactors = intactEntry.getInteractorsList();

                if (interactor != null && !interactorsAcs.contains(interactor.getAc())){
                    interactors.add(interactor);
                    interactorsAcs.add(interactor.getAc());
                }
            }
        }

        EntrySet entrySet = createEntrySetFromIntactEntry(intactEntry);

        IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().clear();

        return entrySet;
    }

    private Collection<psidev.psi.mi.tab.model.BinaryInteraction> createBinaryInteractionsFromExperiment(String expAc){
        // reattach the publication object to the entity manager because connection may have been closed after reading the object
        Experiment experiment = IntactContext.getCurrentInstance().getDaoFactory().getExperimentDao().getByAc(expAc);

        // if the experiment does not have any interactions, we skip it
        if (experiment == null || experiment.getInteractions().isEmpty()){
            return Collections.EMPTY_LIST;
        }

        PublicationConverter publicationConverter = new PublicationConverter();
        ExperimentConverter experimentConverter = new ExperimentConverter();
        ExpansionStrategy expansionStrategy = new SpokeWithoutBaitExpansion(false, false);
        Intact2BinaryInteractionConverter intactInteractionConverter = new Intact2BinaryInteractionConverter(expansionStrategy);

        Publication pub = experiment.getPublication();

        // convert the publication
        BinaryInteractionImpl binaryTemplate = new BinaryInteractionImpl();

        if (pub != null){
            publicationConverter.intactToMitab(pub, binaryTemplate);
        }

        Collection<psidev.psi.mi.tab.model.BinaryInteraction> binaryInteractions = new ArrayList<psidev.psi.mi.tab.model.BinaryInteraction>();

        // convert experiment details
        experimentConverter.intactToMitab(experiment, binaryTemplate, true, false);

        for (Interaction interaction : experiment.getInteractions()){
            try {
                processBinaryInteractionsFor(experiment, experimentConverter, intactInteractionConverter, binaryTemplate, binaryInteractions, interaction);

            } catch (Exception e) {
                throw new RuntimeException("Problem exporting experiment: "+expAc, e);
            }
        }

        IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().clear();

        return binaryInteractions;
    }

    private void processBinaryInteractionsFor(Experiment experiment, ExperimentConverter experimentConverter, Intact2BinaryInteractionConverter intactInteractionConverter, BinaryInteractionImpl binaryTemplate, Collection<BinaryInteraction> binaryInteractions, Interaction interaction) throws NotExpandableInteractionException {
        Collection<BinaryInteraction> binaryChunk = intactInteractionConverter.convert(interaction);

        if (binaryChunk != null && !binaryChunk.isEmpty()){
            boolean isFirst = true;

            for (BinaryInteraction binary : binaryChunk){
                // we override all the shared collections of the expanded binary interactions excepted annotations (can come from publication and interactions)
                // experiment details
                binary.setDetectionMethods(binaryTemplate.getDetectionMethods());
                binary.setHostOrganism(binaryTemplate.getHostOrganism());

                if (binary.getInteractorA() != null && binary.getInteractorA().getParticipantIdentificationMethods().isEmpty()){
                    experimentConverter.addParticipantDetectionMethodForInteractor(experiment,  binary.getInteractorA());
                }
                if (binary.getInteractorB() != null && binary.getInteractorB().getParticipantIdentificationMethods().isEmpty()){
                    experimentConverter.addParticipantDetectionMethodForInteractor(experiment,  binary.getInteractorB());
                }

                // publication details
                binary.setPublications(binaryTemplate.getPublications());
                binary.setAuthors(binaryTemplate.getAuthors());
                binary.setSourceDatabases(binaryTemplate.getSourceDatabases());
                binary.setCreationDate(binaryTemplate.getCreationDate());

                // we don't need to update all the interactions as they all share the same annotation collection
                if (isFirst){
                    binary.getAnnotations().addAll(binaryTemplate.getAnnotations());
                }

                isFirst = false;
            }
            binaryInteractions.addAll(binaryChunk);
        }
    }

    private EntrySet createEntrySetFromInteraction(String intAc){
        // we export compact xml only and excludes hidden annotations
        uk.ac.ebi.intact.dataexchange.psimi.xml.converter.ConverterContext.getInstance().setGenerateExpandedXml(false);
        ConverterContext.getInstance().getConverterConfig().setXmlForm(PsimiXmlForm.FORM_COMPACT);
        uk.ac.ebi.intact.dataexchange.psimi.xml.converter.ConverterContext.getInstance().getAnnotationConfig().setExcludeHiddenTopics(true);

        Interaction interaction = IntactContext.getCurrentInstance().getDaoFactory().getInteractionDao().getByAc(intAc);

        // if the interaction does not have any participants, we skip it
        if (interaction == null || interaction.getComponents().isEmpty()){
            return new EntrySet(Collections.EMPTY_LIST, 2, 5, 4);
        }

        // the released date of the publication
        Date releasedDate = new Date();

        Set<String> interactorsAcs = new HashSet<String>();

        IntactEntry intactEntry = new IntactEntry();
        // set institution
        intactEntry.setInstitution(interaction.getOwner());
        // set release date
        intactEntry.setReleasedDate(releasedDate);

        // convert experiments in one to several publication entry(ies)
        // add experiment to intact entry
        intactEntry.getExperimentsList().addAll(interaction.getExperiments());
        // add all the interactions to the currentIntactEntry
        processInteractionForEntrySet(interaction, interactorsAcs, intactEntry);

        EntrySet entrySet = createEntrySetFromIntactEntry(intactEntry);

        IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().clear();

        return entrySet;
    }

    private void processInteractionForEntrySet(Interaction interaction, Set<String> interactorsAcs, IntactEntry intactEntry) {
        intactEntry.getInteractions().add(interaction);
        for (Component comp : interaction.getComponents()){
            Interactor interactor = comp.getInteractor();
            Collection<Interactor> interactors = intactEntry.getInteractorsList();

            if (interactor != null && !interactorsAcs.contains(interactor.getAc())){
                interactors.add(interactor);
                interactorsAcs.add(interactor.getAc());
            }
        }
    }

    private EntrySet createEntrySetFromIntactEntry(IntactEntry intactEntry) {

        EntryConverter entryConverter = new EntryConverter();
        entryConverter.setCheckInitializedCollections(false);

        Entry xmlEntry = entryConverter.intactToPsi(intactEntry);

        return new EntrySet(Arrays.asList(xmlEntry), 2, 5, 4);
    }

    private Collection<psidev.psi.mi.tab.model.BinaryInteraction> createBinaryInteractionsFromInteraction(String intAc){
        Interaction interaction = IntactContext.getCurrentInstance().getDaoFactory().getInteractionDao().getByAc(intAc);

        // if the interaction does not have any participants, we skip it
        if (interaction == null || interaction.getComponents().isEmpty()){
            return Collections.EMPTY_LIST;
        }

        ExpansionStrategy expansionStrategy = new SpokeWithoutBaitExpansion(true, true);
        Intact2BinaryInteractionConverter intactInteractionConverter = new Intact2BinaryInteractionConverter(expansionStrategy);

        Collection<psidev.psi.mi.tab.model.BinaryInteraction> binaryInteractions = new ArrayList<psidev.psi.mi.tab.model.BinaryInteraction>();

        try {
            Collection<psidev.psi.mi.tab.model.BinaryInteraction> binaryChunk = intactInteractionConverter.convert(interaction);

            if (binaryChunk != null && !binaryChunk.isEmpty()){

                binaryInteractions.addAll(binaryChunk);
            }

        } catch (Exception e) {
            throw new RuntimeException("Problem exporting interaction: "+intAc, e);
        }

        IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().clear();

        return binaryInteractions;
    }
}
