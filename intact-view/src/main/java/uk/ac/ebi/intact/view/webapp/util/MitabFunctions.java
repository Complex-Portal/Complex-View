/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.view.webapp.util;

import uk.ac.ebi.intact.psimitab.model.ExtendedInteractor;
import uk.ac.ebi.intact.model.Interactor;

/**
 * Functions to be used in the UI to control the display in interactions_tab
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1-SNAPSHOT
 */
public final class MitabFunctions {

    private static final String PROTEIN_MI_REF = "MI:0326";
    private static final String SMALLMOLECULE_MI_REF = "MI:0328";

    private MitabFunctions() {
    }

    public static boolean isProtein( ExtendedInteractor interactor ) {

        if ( interactor.getInteractorType() != null ) {
            return ( PROTEIN_MI_REF.equals( interactor.getInteractorType().getIdentifier() ) );
        }

        return false;
    }

    public static boolean isSmallMolecule( ExtendedInteractor interactor ) {

        if ( interactor.getInteractorType() != null ) {
            return ( SMALLMOLECULE_MI_REF.equals( interactor.getInteractorType().getIdentifier() ) );
        }

        return false;
    }




}
