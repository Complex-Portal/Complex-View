/*
 * Copyright 2001-2007 The European Bioinformatics Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.application.editor.util;

import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.clone.IntactClonerException;
import uk.ac.ebi.intact.model.clone.IntactCloner;
import uk.ac.ebi.intact.context.IntactContext;
import org.joda.time.DateTime;

import java.util.Date;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class EditorIntactCloner extends IntactCloner {

    public EditorIntactCloner() {
        setExcludeACs(true);
    }
    
    @Override
    public BioSource cloneBioSource(BioSource bioSource) throws IntactClonerException {
        return bioSource;
    }

    @Override
    public Institution cloneInstitution(Institution institution) throws IntactClonerException {
        return institution;
    }

    @Override
    protected AnnotatedObject cloneAnnotatedObjectCommon(AnnotatedObject<?, ?> ao, AnnotatedObject clone) throws IntactClonerException {

        if(clone==null){
            return null;
        }

        if (clone instanceof Interaction) {
            Interaction interaction = (Interaction)clone;
            interaction.getExperiments().clear();
        } else if (clone instanceof Experiment) {
            Experiment experiment = (Experiment)clone;
            experiment.getInteractions().clear();
        }

        if (ao == clone) {
            return ao;
        }

        return super.cloneAnnotatedObjectCommon(ao, clone);
    }
}