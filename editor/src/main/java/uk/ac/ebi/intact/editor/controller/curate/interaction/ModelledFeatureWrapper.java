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

import psidev.psi.mi.jami.model.ModelledFeature;
import psidev.psi.mi.jami.model.Range;
import psidev.psi.mi.jami.utils.RangeUtils;
import uk.ac.ebi.intact.jami.model.extension.IntactModelledFeature;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Feature wrapper for modelled features
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 */
public class ModelledFeatureWrapper {

    private IntactModelledFeature feature;
    private boolean selected;
    private String ranges=null;
    private List<ModelledFeature> linkedFeatures;

    public ModelledFeatureWrapper(IntactModelledFeature feature) {
        this.feature = feature;

        initialiseRangesAsString();
        this.linkedFeatures = new ArrayList<ModelledFeature>(this.feature.getLinkedFeatures());
    }

    private void initialiseRangesAsString(){

        StringBuffer buffer = new StringBuffer();
        buffer.append("[");

        Iterator<Range> rangeIterator = feature.getRanges().iterator();
        while (rangeIterator.hasNext()){
            Range range = rangeIterator.next();
            buffer.append(RangeUtils.convertRangeToString(range));
            if (rangeIterator.hasNext()){
                buffer.append(", ");
            }
        }
        buffer.append("]");
        this.ranges = buffer.toString();
    }

    public IntactModelledFeature getFeature() {
        return feature;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getRanges() {
        return ranges;
    }

    public List<ModelledFeature> getLinkedFeatures() {
        return linkedFeatures;
    }
}
