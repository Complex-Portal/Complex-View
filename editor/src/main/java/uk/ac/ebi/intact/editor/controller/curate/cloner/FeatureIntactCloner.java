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

import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.Feature;
import uk.ac.ebi.intact.model.IntactObject;
import uk.ac.ebi.intact.model.clone.IntactClonerException;

/**
 * Editor specific cloning routine for interactions.
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id: InteractionIntactCloner.java 14783 2010-07-29 12:52:28Z brunoaranda $
 * @since 2.0.1-SNAPSHOT
 */
public class FeatureIntactCloner extends EditorIntactCloner {


    @Override
    public Component cloneComponent(Component component) throws IntactClonerException {
        return component;
    }

    @Override
    public Feature cloneFeature(Feature feature) throws IntactClonerException {
        Feature clone = super.cloneFeature(feature);
        // don't need to add it to the feature component because it is already done by the cloner
        return clone;
    }

    @Override
    protected IntactObject cloneIntactObjectCommon( IntactObject ao, IntactObject clone ) throws IntactClonerException {

        if (clone == null || clone instanceof Feature) {
            return null;
        }

        return super.cloneIntactObjectCommon( ao, clone );
    }
}

