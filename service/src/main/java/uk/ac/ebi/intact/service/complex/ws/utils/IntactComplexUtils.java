package uk.ac.ebi.intact.service.complex.ws.utils;

import psidev.psi.mi.jami.model.*;
import psidev.psi.mi.jami.utils.AliasUtils;
import psidev.psi.mi.jami.utils.AnnotationUtils;
import psidev.psi.mi.jami.utils.RangeUtils;
import uk.ac.ebi.intact.jami.model.extension.IntactComplex;
import uk.ac.ebi.intact.jami.model.extension.IntactInteractor;
import uk.ac.ebi.intact.jami.model.extension.IntactModelledParticipant;
import uk.ac.ebi.intact.jami.model.extension.InteractorXref;
import uk.ac.ebi.intact.service.complex.ws.model.ComplexDetails;
import uk.ac.ebi.intact.service.complex.ws.model.ComplexDetailsCrossReferences;
import uk.ac.ebi.intact.service.complex.ws.model.ComplexDetailsFeatures;
import uk.ac.ebi.intact.service.complex.ws.model.ComplexDetailsParticipants;

import java.util.*;

/**
 * Created by maitesin on 09/12/2014.
 */
public class IntactComplexUtils {

    public static final String COMPLEX_PROPERTIES = "properties";
    public static final String COMPLEX_PROPERTIES_MI = "MI:0629";

    public static final String COMPLEX_DISEASE = "disease";
    public static final String COMPLEX_DISEASE_MI = "MI:0617";

    public static final String COMPLEX_LIGAND = "ligand";
    public static final String COMPLEX_LIGAND_IA = "IA:2738";

    public static final String COMPLEX_ASSEMBLY = "complex-assembly";
    public static final String COMPLEX_ASSEMBLY_IA = "IA:2783";

    public static final String CURATED_COMPLEX = "curated-complex";
    public static final String CURATED_COMPLEX_IA = "IA:0285";

    public static final String INTACT = "intact";
    public static final String INTACT_MI = "MI:0469";

    public static final String SEARCH = "search-url";
    public static final String SEARCH_MI = "MI:0615";

    public static List<String> getComplexSynonyms(IntactComplex complex) {
        List<String> synosyms = new ArrayList<String>();
        for (Alias alias : AliasUtils.collectAllAliasesHavingType(complex.getAliases(), Alias.COMPLEX_SYNONYM_MI, Alias.COMPLEX_SYNONYM)) {
            synosyms.add(alias.getName());
        }
        return synosyms;
    }

    public static String getComplexName(IntactComplex complex){
        String name = complex.getRecommendedName();
        if (name != null) return name;
        name = complex.getSystematicName();
        if (name != null) return name;
        List<String> synonyms = getComplexSynonyms(complex);
        if (! synonyms.isEmpty()) return synonyms.get(0);
        return complex.getShortName();
    }


    // This method fills the cross references table for the view
    public static void setCrossReferences(IntactComplex complex, ComplexDetails details) {
        Collection<ComplexDetailsCrossReferences> crossReferences = details.getCrossReferences();
        ComplexDetailsCrossReferences cross;
        for (Xref xref : complex.getXrefs()) {
            cross = createCrossReference(xref);
            crossReferences.add(cross);
        }
        for (Xref xref : complex.getIdentifiers()) {
            // We does not want to show us a cross references. That does not make sense.
            if (! xref.getDatabase().getShortName().equals(INTACT)) {
                cross = createCrossReference(xref);
                crossReferences.add(cross);
            }
        }
    }

    private static ComplexDetailsCrossReferences createCrossReference(Xref xref) {
        ComplexDetailsCrossReferences cross = new ComplexDetailsCrossReferences();
        if (xref.getDatabase() != null) {
            cross.setDatabase(xref.getDatabase().getFullName());
            if (xref.getDatabase() instanceof OntologyTerm) {
                OntologyTerm ontologyTerm = (OntologyTerm) xref.getDatabase();
                if (ontologyTerm.getDefinition() != null)
                    cross.setDbdefinition(ontologyTerm.getDefinition());
            }
            cross.setDbMI(xref.getDatabase().getMIIdentifier());
        }
        if (xref.getQualifier() != null) {
            cross.setQualifier(xref.getQualifier().getFullName());
            if (xref.getQualifier() instanceof OntologyTerm){
                OntologyTerm ontologyTerm = (OntologyTerm) xref.getQualifier();
                if (ontologyTerm.getDefinition() != null)
                    cross.setQualifierDefinition(ontologyTerm.getDefinition());
            }
            cross.setQualifierMI(xref.getQualifier().getMIIdentifier());
        }
        cross.setIdentifier(xref.getId());
        Annotation searchUrl = AnnotationUtils.collectFirstAnnotationWithTopic(xref.getDatabase().getAnnotations(), SEARCH_MI, SEARCH);
        if (searchUrl != null) {
            cross.setSearchURL(searchUrl.getValue().replaceAll("\\$*\\{ac\\}",cross.getIdentifier()));
        }
        if (xref instanceof InteractorXref) {
            InteractorXref interactorXref = (InteractorXref) xref;
            if (interactorXref.getSecondaryId() != null) cross.setDescription(interactorXref.getSecondaryId());
        }
        return cross;
    }

    // This method fills the participants table for the view
    public static void setParticipants(IntactComplex complex, ComplexDetails details) {
        Collection<ComplexDetailsParticipants> participants = details.getParticipants();
        ComplexDetailsParticipants part;
        for (ModelledParticipant participant : mergeParticipants(complex.getParticipants())) { //Use ModelledParticipant
            part = new ComplexDetailsParticipants();
            Interactor interactor = participant.getInteractor();

            if (interactor != null) {
                setInteractorType(part, interactor);
                part.setDescription(interactor.getFullName());
                part.setInteractorAC(((IntactModelledParticipant) participant).getAc());
                if (interactor instanceof Protein) {
                    Protein protein = (Protein) interactor;
                    Alias alias = AliasUtils.collectFirstAliasWithType(protein.getAliases(), Alias.COMPLEX_SYNONYM_MI, Alias.COMPLEX_SYNONYM);
                    part.setName(alias != null ? alias.getName() : protein.getGeneName());
                    part.setIdentifier(protein.getPreferredIdentifier().getId());
                }
                else if (interactor instanceof BioactiveEntity) {
                    BioactiveEntity bioactiveEntity = (BioactiveEntity) interactor;
                    part.setName(bioactiveEntity.getShortName());
                    part.setIdentifier(bioactiveEntity.getChebi());
                }
                else {
                    part.setName(interactor.getShortName());
                    part.setIdentifier(interactor.getFullName());
                }
                Annotation searchUrl = AnnotationUtils.collectFirstAnnotationWithTopic(interactor.getPreferredIdentifier().getDatabase().getAnnotations(), SEARCH_MI, SEARCH);
                if (searchUrl != null) {
                    part.setIdentifierLink(searchUrl.getValue().replaceAll("\\$*\\{ac\\}", part.getIdentifier()));
                }
                if (participant.getStoichiometry().getMinValue() == 0 && participant.getStoichiometry().getMaxValue() == 0)
                    part.setStochiometry(null);
                else
                    part.setStochiometry(participant.getStoichiometry().toString());
                if (participant.getBiologicalRole() != null) {
                    setBiologicalRole(part, participant);
                }
            }
            setFeatures(part, participant);
            participants.add(part);
        }
    }

    private static Collection<ModelledParticipant> mergeParticipants(Collection<ModelledParticipant> participants) {
        if (participants.size() > 1) {
            Comparator<ModelledParticipant> comparator = new Comparator<ModelledParticipant>() {
                @Override
                public int compare(ModelledParticipant o1, ModelledParticipant o2) {
                    return (((IntactInteractor)o1.getInteractor()).getAc().compareTo(((IntactInteractor)o2.getInteractor()).getAc()));
                }
            };
            List<ModelledParticipant> participantList = (List<ModelledParticipant>) participants;
            Collections.sort(participantList, comparator);
            Collection<ModelledParticipant> merged = new ArrayList<ModelledParticipant>();
            ModelledParticipant aux = participantList.get(0);
            int stochiometry = 0;
            for (ModelledParticipant participant : participantList) {
                if (((IntactInteractor)aux.getInteractor()).getAc().equals(((IntactInteractor) participant.getInteractor()).getAc())) {
                    //Same
                    stochiometry += participant.getStoichiometry().getMinValue();
                }
                else {
                    //Different
                    aux.setStoichiometry(stochiometry);
                    merged.add(aux);
                    aux = participant;
                    stochiometry = aux.getStoichiometry().getMinValue();
                }
            }
            aux.setStoichiometry(stochiometry);
            merged.add(aux);
            return merged;
        }
        else {
            return participants;
        }
    }

    // this method fills the linked features and the other features cells in the participants table
    protected static void setFeatures(ComplexDetailsParticipants part, Participant participant) {
        for (Feature feature : (List<Feature>) participant.getFeatures()) {
            if (feature.getLinkedFeatures().size() != 0) {
                for (Feature linked : (List<Feature>) feature.getLinkedFeatures()) {
                    ComplexDetailsFeatures complexDetailsFeatures = createFeature(linked);
                    part.getLinkedFeatures().add(complexDetailsFeatures);
                }
            }
            else {
                ComplexDetailsFeatures complexDetailsFeatures = createFeature(feature);
                part.getOtherFeatures().add(complexDetailsFeatures);
            }
        }
    }

    private static ComplexDetailsFeatures createFeature(Feature feature) {
        ComplexDetailsFeatures complexDetailsFeatures = new ComplexDetailsFeatures();
        complexDetailsFeatures.setFeatureType(feature.getType().getShortName());
        if (feature.getType() instanceof OntologyTerm) {
            OntologyTerm ontologyTerm = (OntologyTerm) feature.getType();
            if (ontologyTerm.getDefinition() != null)
                complexDetailsFeatures.setFeatureTypeDefinition(ontologyTerm.getDefinition());
        }
        complexDetailsFeatures.setFeatureTypeMI(feature.getType().getMIIdentifier());
        complexDetailsFeatures.setParticipantId(feature.getParticipant().getInteractor().getPreferredIdentifier().getId());
        for (Range range : (List<Range>) feature.getRanges()) {
            complexDetailsFeatures.getRanges().add(RangeUtils.convertRangeToString(range));
        }
        return complexDetailsFeatures;
    }

    // This method sets the interactor type information
    protected static void setInteractorType(ComplexDetailsParticipants part, Interactor interactor) {
        CvTerm term = interactor.getInteractorType();
        part.setInteractorType(term.getFullName());
        part.setInteractorTypeMI(term.getMIIdentifier());
        if (term instanceof OntologyTerm) {
            OntologyTerm ontologyTerm = (OntologyTerm) term;
            if (ontologyTerm.getDefinition() != null)
                part.setInteractorTypeDefinition(ontologyTerm.getDefinition());
        }
    }

    // This method sets the biological role information
    protected static void setBiologicalRole(ComplexDetailsParticipants part, Participant participant) {
        CvTerm term = participant.getBiologicalRole();
        part.setBioRole(term.getFullName());
        part.setBioRoleMI(term.getMIIdentifier());
        if (term instanceof OntologyTerm) {
            OntologyTerm ontologyTerm = (OntologyTerm) term;
            if (ontologyTerm.getDefinition() != null)
                part.setBioRoleDefinition(ontologyTerm.getDefinition());
        }
    }

    //
    // ALIASES
    //
    public static String getSystematicName(IntactComplex complex) {
        return complex.getSystematicName();
    }

    //Retrieve all the synosyms of the complex
    public static List<String> getSynonyms(IntactComplex complex) {
        List<String> synosyms = new ArrayList<String>();
        for (Alias alias : AliasUtils.collectAllAliasesHavingType(complex.getAliases(), Alias.COMPLEX_SYNONYM_MI, Alias.COMPLEX_SYNONYM)) {
            synosyms.add(alias.getName());
        }
        return synosyms;
    }

    public static String getName(IntactComplex complex) {
        String name = complex.getRecommendedName();
        if (name != null) return name;
        name = complex.getSystematicName();
        if (name != null) return name;
        List<String> synonyms = getSynonyms(complex);
        if (synonyms != Collections.EMPTY_LIST) return synonyms.get(0);
        return complex.getShortName();
    }

    //
    // SPECIES
    //
    //
    public static String getSpeciesName(IntactComplex complex) {
        return complex.getOrganism().getScientificName();
    }

    public static String getSpeciesTaxId(IntactComplex complex) {
        return Integer.toString(complex.getOrganism().getTaxId());
    }

    //
    // ANNOTATIONS
    //
    public static String getProperties(IntactComplex complex) {
        Annotation annotation = AnnotationUtils.collectFirstAnnotationWithTopic(complex.getAnnotations(), COMPLEX_PROPERTIES_MI, COMPLEX_PROPERTIES);
        if (annotation != null)
            return annotation.getValue();
        else
            return null;
    }

    public static String getDisease(IntactComplex complex) {
        Annotation annotation = AnnotationUtils.collectFirstAnnotationWithTopic(complex.getAnnotations(), COMPLEX_DISEASE_MI, COMPLEX_DISEASE);
        if (annotation != null)
            return annotation.getValue();
        else
            return null;
    }

    public static String getLigand(IntactComplex complex) {
        Annotation annotation = AnnotationUtils.collectFirstAnnotationWithTopic(complex.getAnnotations(), COMPLEX_LIGAND_IA, COMPLEX_LIGAND);
        if (annotation != null)
            return annotation.getValue();
        else
            return null;
    }

    public static String getComplexAssembly(IntactComplex complex) {
        Annotation annotation = AnnotationUtils.collectFirstAnnotationWithTopic(complex.getAnnotations(), COMPLEX_ASSEMBLY_IA, COMPLEX_ASSEMBLY);
        if (annotation != null)
            return annotation.getValue();
        else
            return null;
    }

    public static String getFunction(IntactComplex complex) {
        Annotation annotation = AnnotationUtils.collectFirstAnnotationWithTopic(complex.getAnnotations(), CURATED_COMPLEX_IA, CURATED_COMPLEX);
        if (annotation != null)
            return annotation.getValue();
        else
            return null;
    }

}
