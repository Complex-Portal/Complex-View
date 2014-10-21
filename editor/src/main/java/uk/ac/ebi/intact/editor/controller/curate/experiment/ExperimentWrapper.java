/**
 * Copyright 2012 The European Bioinformatics Institute, and others.
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

import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.FeatureUtils;
import uk.ac.ebi.intact.model.util.XrefUtils;

import java.util.*;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ExperimentWrapper {
    
    private Experiment experiment;
    private List<Interaction> interactions;
    private Map<String, List<Annotation>> interactionAnnotations;
    private Map<String, List<Xref>> interactionXrefs;
    private Map<String, List<Parameter>> interactionsParameters;

    private Map<String, List<Component>> componentsMap;
    private Map<String, List<FeatureSummary>> componentFeatures;

    public ExperimentWrapper(Experiment experiment) {
        this.experiment = experiment;

        interactions = new ArrayList<Interaction>(experiment.getInteractions());
        Collections.sort(interactions, new InteractionAlphabeticalOrder());

        interactionAnnotations = new HashMap<String, List<Annotation>>(interactions.size());
        interactionXrefs = new HashMap<String, List<Xref>>(interactions.size());
        interactionsParameters = new HashMap<String, List<Parameter>>(interactions.size());
        componentsMap = new HashMap<String, List<Component>>(interactions.size());
        componentFeatures = new HashMap<String, List<FeatureSummary>>(interactions.size() * 2);

        for (Interaction inter : interactions){
            String ac = inter.getAc() != null ? inter.getAc() : Integer.toString(inter.hashCode());
            interactionAnnotations.put(ac, new ArrayList<Annotation>(inter.getAnnotations()));
            interactionsParameters.put(ac, new ArrayList<Parameter>(inter.getParameters()));
            interactionXrefs.put(ac, new ArrayList<Xref>(inter.getXrefs()));
            componentsMap.put(ac, new ArrayList<Component>(sortedParticipants(inter)));

            for (Component comp : inter.getComponents()){
                String compAc = comp.getAc() != null ? comp.getAc() : Integer.toString(inter.hashCode());

                List<FeatureSummary> features = new ArrayList<FeatureSummary>(comp.getFeatures().size());
                componentFeatures.put(compAc, features);
                for (Feature f : comp.getFeatures()){
                    features.add(new FeatureSummary(featureAsString(f),
                            f.getBoundDomain() != null ? f.getBoundDomain().getShortLabel():null));
                }
            }
        }
    }

    public String featureAsString(Feature feature) {
        StringBuilder sb = new StringBuilder();
        sb.append(feature.getShortLabel());

        final Collection<Range> ranges = feature.getRanges();
        final Iterator<Range> iterator = ranges.iterator();

        while (iterator.hasNext()) {
            Range next = iterator.next();
            sb.append("[");
            sb.append(FeatureUtils.convertRangeIntoString(next));
            sb.append("]");

            if (iterator.hasNext()) sb.append(", ");
        }

        if (feature.getCvFeatureType() != null) {
            sb.append(" ");
            sb.append(feature.getCvFeatureType().getShortLabel());
        }

        return sb.toString();
    }


    public List<Component> sortedParticipants(Interaction interaction) {
        if (interaction == null ) return Collections.EMPTY_LIST;

        List<Component> components = new ArrayList<Component>(interaction.getComponents());
        Collections.sort(components, new ComponentOrder());
        return components;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public List<Interaction> getInteractions() {
        return interactions;
    }

    public List<Annotation> getInteractionAnnotations(Interaction interaction) {
        String ac = interaction.getAc() != null ? interaction.getAc() : Integer.toString(interaction.hashCode());

        return interactionAnnotations.get(ac);
    }

    public List<Xref> getInteractionXrefs(Interaction interaction) {
        String ac = interaction.getAc() != null ? interaction.getAc() : Integer.toString(interaction.hashCode());

        return interactionXrefs.get(ac);
    }

    public List<Parameter> getInteractionParameters(Interaction interaction) {
        String ac = interaction.getAc() != null ? interaction.getAc() : Integer.toString(interaction.hashCode());

        return interactionsParameters.get(ac);
    }

    public List<Component> getParticipants(Interaction interaction){
        String ac = interaction.getAc() != null ? interaction.getAc() : Integer.toString(interaction.hashCode());

        return componentsMap.get(ac);
    }

    public List<FeatureSummary> getFeatures(Component component){
        String ac = component.getAc() != null ? component.getAc() : Integer.toString(component.hashCode());

        return componentFeatures.get(ac);
    }

    private class InteractionAlphabeticalOrder implements Comparator<Interaction> {

        @Override
        public int compare(Interaction o1, Interaction o2) {
            return o1.getShortLabel().compareTo(o2.getShortLabel());
        }
    }
    
    private static class ComponentOrder implements Comparator<Component> {
        
        private static Map<String,Integer> rolePriorities = new HashMap<String, Integer>();
        
        static {
            rolePriorities.put(CvExperimentalRole.BAIT_PSI_REF, 1);
            rolePriorities.put(CvExperimentalRole.ENZYME_PSI_REF, 5);
            rolePriorities.put(CvExperimentalRole.ENZYME_TARGET, 10);
            rolePriorities.put(CvExperimentalRole.PREY_PSI_REF, 15);
        }
        
        @Override
        public int compare(Component o1, Component o2) {
            Integer priority1 = rolePriorities.get(experimentalRoleIdentifierFor(o1));
            Integer priority2 = rolePriorities.get(experimentalRoleIdentifierFor(o2));

            if (priority1 != null && priority2 == null) {
                return 1;
            }

            if (priority1 == null && priority2 != null) {
                return -1;
            }

            if (priority1 != null && !priority1.equals(priority2)) {
                return priority1.compareTo(priority2);
            }

            final Collection<InteractorXref> idXrefs1 = XrefUtils.getIdentityXrefs(o1.getInteractor());
            final Collection<InteractorXref> idXrefs2 = XrefUtils.getIdentityXrefs(o2.getInteractor());

            final InteractorXref idXref1 = (idXrefs1.isEmpty())? null : idXrefs1.iterator().next();
            final InteractorXref idXref2 = (idXrefs2.isEmpty())? null : idXrefs2.iterator().next();
            
            String id1 = (idXref1 != null)? idXref1.getPrimaryId() : "";
            String id2 = (idXref2 != null)? idXref2.getPrimaryId() : "";
            
            return id1.compareTo(id2);
        }

        private String experimentalRoleIdentifierFor(Component o1) {
            return o1.getExperimentalRoles().iterator().next().getIdentifier();
        }
    }
}
