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

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.jami.exception.IllegalRangeException;
import psidev.psi.mi.jami.utils.RangeUtils;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.extension.ModelledRange;
import uk.ac.ebi.intact.jami.model.extension.ModelledResultingSequence;
import uk.ac.ebi.intact.jami.synchronizer.FinderException;
import uk.ac.ebi.intact.jami.synchronizer.PersisterException;
import uk.ac.ebi.intact.jami.synchronizer.SynchronizerException;

import javax.faces.application.FacesMessage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.validator.ValidatorException;

/**
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 */
public class ModelledRangeWrapper {

    private ModelledRange range;
    private String sequence;
    private String rangeAsString;

    public ModelledRangeWrapper(ModelledRange range, String sequence) {
        this.range = range;
        this.rangeAsString = RangeUtils.convertRangeToString(range);
        this.sequence = sequence;
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void onRangeAsStringChanged(AjaxBehaviorEvent evt) throws PersisterException, FinderException, SynchronizerException, IllegalRangeException {
        psidev.psi.mi.jami.model.Range newRange = RangeUtils.createRangeFromString(rangeAsString, false);
        newRange.setResultingSequence(new ModelledResultingSequence(RangeUtils.extractRangeSequence(newRange, this.sequence), null));

        IntactDao dao = ApplicationContextProvider.getBean("intactDao");
        this.range = dao.getSynchronizerContext().getModelledRangeSynchronizer().synchronize(newRange, false);
    }

    public void onFuzzyTypeChanged(AjaxBehaviorEvent evt) {
        this.rangeAsString = RangeUtils.convertRangeToString(range);
    }

    public void validateRange(FacesContext context, UIComponent component, Object value) throws ValidatorException {

        String rangeAsStr = (String) value;
        try {
            psidev.psi.mi.jami.model.Range newRange = RangeUtils.createRangeFromString(rangeAsStr, false);
            if (!RangeUtils.validateRange(newRange, sequence).isEmpty()) {
                EditableValueHolder valueHolder = (EditableValueHolder) component;
                valueHolder.setValid(false);

                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid range", "Range syntax is invalid: "+rangeAsStr);
                throw new ValidatorException(message);
            }
        } catch (IllegalRangeException e) {
            EditableValueHolder valueHolder = (EditableValueHolder) component;
            valueHolder.setValid(false);

            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid range", "Range syntax is invalid: "+rangeAsStr);
            throw new ValidatorException(message);
        }
    }

    public ModelledRange getRange() {
        return range;
    }

    public void setRange(ModelledRange range) {
        this.range = range;
    }

    public String getRangeAsString() {
        return rangeAsString;
    }

    public void setRangeAsString(String rangeAsString) {
        this.rangeAsString = rangeAsString;
    }
}
