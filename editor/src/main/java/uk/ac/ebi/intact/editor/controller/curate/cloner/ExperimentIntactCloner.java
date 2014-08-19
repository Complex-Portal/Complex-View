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
package uk.ac.ebi.intact.editor.controller.curate.cloner;

import uk.ac.ebi.intact.core.persister.IntactCore;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.clone.IntactClonerException;

import java.util.Collection;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ExperimentIntactCloner extends EditorIntactCloner {

    private boolean cloneInteractions;

    public ExperimentIntactCloner(boolean cloneInteractions) {
        super();
        this.cloneInteractions = cloneInteractions;
    }

    @Override
    public Experiment cloneExperiment(Experiment experiment) throws IntactClonerException {
        if (experiment == null) return null;
        Experiment clone = new Experiment();

        clonerManager.addClone(experiment, clone);

        clone.setCvIdentification(clone(experiment.getCvIdentification()));
        clone.setCvInteraction(clone(experiment.getCvInteraction()));
        clone.setBioSource(clone(experiment.getBioSource()));
        clone.setPublication(clone(experiment.getPublication()));

        if (isCollectionClonable(experiment.getInteractions()) && cloneInteractions) {
            Collection<Interaction> interactions = IntactCore.ensureInitializedInteractions(experiment);

            for (Interaction i : interactions) {
                Interaction inter = clone(i);
                inter.setOwner(experiment.getOwner());
                clone.addInteraction(inter);
            }
        }

        return clone;
    }

    // no need to override cloning interactions
    /*@Override
    public Interaction cloneInteraction(Interaction interaction) throws IntactClonerException {
        final Interaction clone = super.cloneInteraction( interaction );
        clone.getExperiments().clear();
        return clone;
    }*/

    @Override
    protected AnnotatedObject cloneAnnotatedObjectCommon(AnnotatedObject<?, ?> ao, AnnotatedObject clone) throws IntactClonerException {

        if(clone==null){
            return null;
        }

        // not optimized, do not need to clone interactions and then clear if we don't want to clone interactions. If we don't want to clone interactions, just don't clone them
        /*if (!cloneInteractions) {
            if (clone instanceof Interaction) {
                Interaction interaction = (Interaction)clone;
                interaction.getExperiments().clear();
            } else if (clone instanceof Experiment) {
                Experiment experiment = (Experiment)clone;
                experiment.getInteractions().clear();
            }
        } else if (clone instanceof Experiment) {

            final Experiment experiment = (Experiment) clone;

            for (Interaction interaction : experiment.getInteractions()) {
                interaction.getExperiments().add(experiment);
            }
        }*/

        if (ao == clone) {
            return ao;
        }

        return super.cloneAnnotatedObjectCommon(ao, clone);
    }
}
