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
package uk.ac.ebi.intact.editor.controller.curate.interaction;

import uk.ac.ebi.intact.jami.model.extension.IntactModelledFeature;

/**
* Feature wrapper for modelled features
*
* @author Marine Dumousseau (marine@ebi.ac.uk)
* @version $Id$
*/
public class ModelledFeatureWrapper {

    private IntactModelledFeature feature;
    private boolean selected;

    public ModelledFeatureWrapper(IntactModelledFeature feature) {
        this.feature = feature;
    }

    public IntactModelledFeature getFeature() {
        return feature;
    }

    public void setFeature(IntactModelledFeature feature) {
        this.feature = feature;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
