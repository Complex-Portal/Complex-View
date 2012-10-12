package uk.ac.ebi.intact.view.webapp.model;

import org.hibernate.Hibernate;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.view.webapp.util.Functions;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Participant wrapper for the interactions details
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>02/10/12</pre>
 */

public class ParticipantWrapper {

    private String participantAc;
    private Collection<ComponentXref> participantXrefs;
    private Collection<Annotation> participantAnnotations;
    private float participantStoichiometry;
    private Collection<Parameter> parameters;
    private Collection<ComponentConfidence> confidences;
    private Collection<Feature> features;

    private String interactorAc;
    private String interactorShortLabel;
    private String interactorFullName;
    private Collection<InteractorXref> interactorXrefs;
    private Collection<InteractorAlias> interactorAliases;
    private CvInteractorType interactorType;

    private String bioSourceAc;
    private String bioSourceShortLabel;
    private String bioSourceFullName;
    private Collection<Annotation> bioSourceAnnotations;
    private Collection<BioSourceXref> bioSourceXrefs;

    private String expressedInAc;
    private String expressedInShortLabel;
    private String expressedInFullName;
    private Collection<Annotation> expressedInAnnotations;
    private Collection<BioSourceXref> expressedInXrefs;

    private CvExperimentalRole experimentalRole;
    private CvBiologicalRole biologicalRole;

    public ParticipantWrapper(Component component){
        participantAc = component.getAc();
        participantXrefs = new ArrayList<ComponentXref>(component.getXrefs());
        participantAnnotations = new ArrayList<Annotation>(component.getAnnotations());
        participantStoichiometry = component.getStoichiometry();
        parameters = new ArrayList<Parameter>(component.getParameters());
        confidences = new ArrayList<ComponentConfidence>(component.getConfidences());
        features = new ArrayList<Feature>(component.getFeatures().size());
        for (Feature feature : component.getFeatures()){
            Hibernate.initialize(feature.getRanges());
            Hibernate.initialize(feature.getAnnotations());
            Hibernate.initialize(feature.getXrefs());
            features.add(feature);
        }

        Interactor interactor = component.getInteractor();

        interactorShortLabel = interactor.getAc();
        interactorFullName = interactor.getFullName();
        interactorXrefs = Functions.extractIdentityXrefs(interactor.getXrefs());
        interactorAliases = new ArrayList<InteractorAlias>(interactor.getAliases());
        interactorType = interactor.getCvInteractorType();
        Hibernate.initialize(interactorType.getAnnotations());
        Hibernate.initialize(interactorType.getXrefs());

        this.interactorAc = interactor.getAc();

        BioSource bioSource = interactor.getBioSource();
        if (bioSource != null){
            bioSourceAc = bioSource.getAc();
            bioSourceShortLabel = bioSource.getShortLabel();
            bioSourceFullName = bioSource.getFullName();
            bioSourceAnnotations = new ArrayList<Annotation>(bioSource.getAnnotations());
            bioSourceXrefs = new ArrayList<BioSourceXref>(bioSource.getXrefs());
        }

        BioSource expressedIn = component.getExpressedIn();
        if (expressedIn != null){
            expressedInAc = expressedIn.getAc();
            expressedInShortLabel = expressedIn.getShortLabel();
            expressedInFullName = expressedIn.getFullName();
            expressedInAnnotations = new ArrayList<Annotation>(expressedIn.getAnnotations());
            expressedInXrefs = new ArrayList<BioSourceXref>(expressedIn.getXrefs());
        }

        experimentalRole = component.getCvExperimentalRole();
        Hibernate.initialize(experimentalRole.getAnnotations());
        Hibernate.initialize(experimentalRole.getXrefs());

        biologicalRole = component.getCvBiologicalRole();
        Hibernate.initialize(biologicalRole.getAnnotations());
        Hibernate.initialize(biologicalRole.getXrefs());
    }

    public String getInteractorShortLabel() {
        return interactorShortLabel;
    }

    public Collection<InteractorXref> getInteractorXrefs() {
        return interactorXrefs;
    }

    public Collection<InteractorAlias> getInteractorAliases() {
        return interactorAliases;
    }

    public String getInteractorFullName() {
        return interactorFullName;
    }

    public String getBioSourceAc() {
        return bioSourceAc;
    }

    public String getBioSourceShortLabel() {
        return bioSourceShortLabel;
    }

    public String getBioSourceFullName() {
        return bioSourceFullName;
    }

    public Collection<Annotation> getBioSourceAnnotations() {
        return bioSourceAnnotations;
    }

    public Collection<BioSourceXref> getBioSourceXrefs() {
        return bioSourceXrefs;
    }

    public String getParticipantAc() {
        return participantAc;
    }

    public Collection<ComponentXref> getParticipantXrefs() {
        return participantXrefs;
    }

    public Collection<Annotation> getParticipantAnnotations() {
        return participantAnnotations;
    }

    public float getParticipantStoichiometry() {
        return participantStoichiometry;
    }

    public Collection<Parameter> getParameters() {
        return parameters;
    }

    public CvInteractorType getInteractorType() {
        return interactorType;
    }

    public Collection<Feature> getFeatures() {
        return features;
    }

    public String getExpressedInAc() {
        return expressedInAc;
    }

    public String getExpressedInShortLabel() {
        return expressedInShortLabel;
    }

    public String getExpressedInFullName() {
        return expressedInFullName;
    }

    public Collection<Annotation> getExpressedInAnnotations() {
        return expressedInAnnotations;
    }

    public Collection<BioSourceXref> getExpressedInXrefs() {
        return expressedInXrefs;
    }

    public CvExperimentalRole getExperimentalRole() {
        return experimentalRole;
    }

    public CvBiologicalRole getBiologicalRole() {
        return biologicalRole;
    }

    public Collection<ComponentConfidence> getConfidences() {
        return confidences;
    }

    public String getInteractorAc() {
        return interactorAc;
    }
}
