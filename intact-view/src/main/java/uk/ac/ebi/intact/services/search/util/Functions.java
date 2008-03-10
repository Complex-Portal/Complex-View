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
package uk.ac.ebi.intact.services.search.util;

import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.ProteinUtils;
import uk.ac.ebi.intact.services.search.model.*;

/**
 * Functions to be used in the UI
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public final class Functions {

    private Functions() {
    }

    /**
     * Calculates which ID to pass to Dasty, depending on the interactor type
     */
    public static String getIdentifierForDasty(Interactor interactor) {
        if (interactor instanceof Protein && ProteinUtils.isFromUniprot((Protein) interactor)) {
            return ProteinUtils.getUniprotXref((Protein)interactor).getPrimaryId();
        }

        return interactor.getAc();
    }

    public static <T extends AnnotatedObject> AnnotatedObjectWrapper<T> wrap(T ao) {
        AnnotatedObjectWrapper aod;
        
        if (ao instanceof Interaction) {
            aod = new InteractionWrapper((Interaction) ao);
        } else if (ao instanceof Interactor) {
            aod = new InteractorWrapper((Interactor) ao);
        } else if (ao instanceof Experiment) {
            aod = new ExperimentWrapper((Experiment) ao);
        } else if (ao instanceof CvObject) {
            aod = new CvObjectWrapper((CvObject) ao);
        } else {
            aod = new AnnotatedObjectWrapper(ao);
        }

        return aod;
    }
    
}
