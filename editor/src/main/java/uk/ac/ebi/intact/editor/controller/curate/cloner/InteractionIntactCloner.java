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
import uk.ac.ebi.intact.model.clone.IntactClonerException;

/**
 * Editor specific cloning routine for interactions.
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1-SNAPSHOT
 */
public class InteractionIntactCloner extends EditorIntactCloner {

    private Experiment defaultExperiment;

    public InteractionIntactCloner(Experiment defaultExperiment) {
        this.defaultExperiment = defaultExperiment;
    }

    /**
     * This cloner will be used by the editor to clone the
     * interaction. As some experiment has 1000s of experiment,
     * the system unnecessarily attempts to clone experiments,
     * which is not needed.
     *
     * @param experiment, the experiment to be cloned
     * @return null (experiment is not cloned)
     * @throws IntactClonerException
     */
    @Override
    public Experiment cloneExperiment( Experiment experiment ) throws IntactClonerException {
        return defaultExperiment;
    }

    @Override
    protected AnnotatedObject cloneAnnotatedObjectCommon(AnnotatedObject<?, ?> ao, AnnotatedObject clone) throws IntactClonerException {
        if (clone == null ) {
            return null;
        }

        if (clone instanceof Experiment) {
            return clone;
        }

        return super.cloneAnnotatedObjectCommon( ao, clone );
    }
}

