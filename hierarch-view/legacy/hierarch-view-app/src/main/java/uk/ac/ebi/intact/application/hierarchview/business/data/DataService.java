/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.application.hierarchview.business.data;

import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.application.hierarchview.exception.HierarchViewDataException;
import uk.ac.ebi.intact.application.hierarchview.exception.MultipleResultException;
import uk.ac.ebi.intact.application.hierarchview.exception.ProteinNotFoundException;

import java.util.Collection;

/**
 * Interface define getting data for building Graph.
 *
 * @author Nadin Neuhauser
 * @version $Id
 * @since 1.6.0-SNAPSHOT
 */
public interface DataService {

    Collection<String> getCentralProteins();

    Collection<BinaryInteraction> getBinaryInteractionsByQueryString( String query ) throws HierarchViewDataException, MultipleResultException, ProteinNotFoundException;

    String getDbName() throws HierarchViewDataException;

}
