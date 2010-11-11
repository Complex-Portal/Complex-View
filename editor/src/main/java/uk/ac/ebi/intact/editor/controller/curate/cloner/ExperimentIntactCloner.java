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

import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.clone.IntactClonerException;

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
        return super.cloneExperiment(experiment);
    }

    @Override
    public Interaction cloneInteraction(Interaction interaction) throws IntactClonerException {
        return super.cloneInteraction(interaction);
    }

    @Override
    protected AnnotatedObject cloneAnnotatedObjectCommon(AnnotatedObject<?, ?> ao, AnnotatedObject clone) throws IntactClonerException {

        if(clone==null){
            return null;
        }

        if (!cloneInteractions) {
            if (clone instanceof Interaction) {
                Interaction interaction = (Interaction)clone;
                interaction.getExperiments().clear();
            } else if (clone instanceof Experiment) {
                Experiment experiment = (Experiment)clone;
                experiment.getInteractions().clear();
            }
        }

        if (ao == clone) {
            return ao;
        }

        return super.cloneAnnotatedObjectCommon(ao, clone);
    }
}
