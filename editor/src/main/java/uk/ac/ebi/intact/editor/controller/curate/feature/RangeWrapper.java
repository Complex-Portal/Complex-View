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
package uk.ac.ebi.intact.editor.controller.curate.feature;

import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.controller.curate.cvobject.CvObjectService;
import uk.ac.ebi.intact.model.CvFuzzyType;
import uk.ac.ebi.intact.model.Range;
import uk.ac.ebi.intact.model.util.FeatureUtils;

import javax.faces.application.FacesMessage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.validator.ValidatorException;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class RangeWrapper {

    private Range range;
    private String sequence;
    private String rangeAsString;

    public RangeWrapper(Range range, String sequence) {
        this.range = range;
        this.rangeAsString = FeatureUtils.convertRangeIntoString(range);
        this.sequence = sequence;
    }

    public void onRangeAsStringChanged(AjaxBehaviorEvent evt) {
        Range newRange = FeatureUtils.createRangeFromString(rangeAsString, sequence);

        this.range.setDownStreamSequence(newRange.getDownStreamSequence());
        this.range.setUpStreamSequence(newRange.getUpStreamSequence());
        this.range.setFromCvFuzzyType(newRange.getFromCvFuzzyType());
        this.range.setFromIntervalStart(newRange.getFromIntervalStart());
        this.range.setFromIntervalEnd(newRange.getFromIntervalEnd());
        this.range.setToCvFuzzyType(newRange.getToCvFuzzyType());
        this.range.setToIntervalStart(newRange.getToIntervalStart());
        this.range.setToIntervalEnd(newRange.getToIntervalEnd());
        this.range.setFullSequence(newRange.getFullSequence());

        CvObjectService cvObjectService = (CvObjectService) IntactContext.getCurrentInstance().getSpringContext().getBean("cvObjectService");
        CvFuzzyType fromFuzzyType = cvObjectService.findCvObjectByIdentifier(CvFuzzyType.class, range.getFromCvFuzzyType().getIdentifier());
        CvFuzzyType toFuzzyType = cvObjectService.findCvObjectByIdentifier(CvFuzzyType.class, range.getToCvFuzzyType().getIdentifier());

        range.setFromCvFuzzyType(fromFuzzyType);
        range.setToCvFuzzyType(toFuzzyType);
    }

    public void onFuzzyTypeChanged(AjaxBehaviorEvent evt) {

        // reconvert positions if necessary (if status became undetermined, c-terminal region or n-terminal region position becomes 0, if n-terminal position =1 if c-terminal position = end of protein sequence or 0 if no protein sequence)
        FeatureUtils.correctRangePositionsAccordingToType(range, sequence);
        this.rangeAsString = FeatureUtils.convertRangeIntoString(range);
    }

    public void validateRange(FacesContext context, UIComponent component, Object value) throws ValidatorException {

        String rangeAsStr = (String) value;
        if (FeatureUtils.isABadRange(rangeAsStr, sequence)) {
            EditableValueHolder valueHolder = (EditableValueHolder) component;
            valueHolder.setValid(false);

            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid range", "Range syntax is invalid: "+rangeAsStr);
            throw new ValidatorException(message);
        }
    }

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public String getRangeAsString() {
        return rangeAsString;
    }

    public void setRangeAsString(String rangeAsString) {
        this.rangeAsString = rangeAsString;
    }

    public boolean isValidRange() {
        return !FeatureUtils.isABadRange(rangeAsString, sequence);
    }

    public String getBadRangeInfo() {
        return FeatureUtils.getBadRangeInfo(rangeAsString, sequence);
    }
}
