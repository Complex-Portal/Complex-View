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
package uk.ac.ebi.intact.view.webapp.controller.list;


import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.model.InteractorXref;
import uk.ac.ebi.intact.model.util.ProteinUtils;
import uk.ac.ebi.intact.view.webapp.model.InteractorWrapper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Controller for ProteinList View
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since 0.9
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "general" )
public class ProteinListController extends InteractorListController {

    public ProteinListController() {
    }

    public String[] getSelectedUniprotIds() {
        final InteractorWrapper[] selected = getSelected();

        if (selected == null) {
            return new String[0];
        }

        final List<InteractorWrapper> interactorWrappers = Arrays.asList(selected);

        Set<String> uniprotIds = new HashSet<String>();

        for (InteractorWrapper interactorWrapper : interactorWrappers) {
            Interactor interactor = interactorWrapper.getInteractor();

            final InteractorXref xref = ProteinUtils.getUniprotXref(interactor);

            if (xref != null) {
                uniprotIds.add(xref.getPrimaryId());
            }
        }

        return uniprotIds.toArray( new String[uniprotIds.size()] );
    }
}
