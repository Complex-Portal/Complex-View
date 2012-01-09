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

import uk.ac.ebi.intact.editor.util.LazyDataModelFactory;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.XrefUtils;

import javax.faces.model.DataModel;
import javax.persistence.EntityManager;
import java.util.*;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ExperimentWrapper {
    
    private Experiment experiment;
    private DataModel interactionsDataModel;
    
    public ExperimentWrapper(Experiment experiment, EntityManager entityManager) {
        this.experiment = experiment;
        
        if (experiment.getAc() != null) {
            this.interactionsDataModel = LazyDataModelFactory.createLazyDataModel(entityManager,
                    "select i from InteractionImpl i join i.experiments as exp where exp.ac = '"+experiment.getAc()+"' order by i.shortLabel",
                    "select count(i) from InteractionImpl i join i.experiments as exp where exp.ac = '"+experiment.getAc()+"'");
        } else {
            List<Interaction> sortedInteractions = new ArrayList<Interaction>(experiment.getInteractions());
            Collections.sort(sortedInteractions, new InteractionAlphabeticalOrder());
            this.interactionsDataModel = LazyDataModelFactory.createLazyDataModel(experiment.getInteractions());
        }
    }
    
    public List<Component> sortedParticipants(Interaction interaction) {
        List<Component> components = new ArrayList<Component>(interaction.getComponents());
        Collections.sort(components, new ComponentOrder());
        return components;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public DataModel getInteractionsDataModel() {
        return interactionsDataModel;
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
