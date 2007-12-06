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
package uk.ac.ebi.intact.service.graph.binary.label;

import psidev.psi.mi.tab.model.Alias;
import psidev.psi.mi.tab.model.Interactor;

/**
 * Uses the aliases of Interactor to create Label for the Node.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 1.6.0-Snapshot
 */
public class AliasLabelBuilder implements LabelBuilder {

    public String buildDefaultLabel( Interactor interactor ) {

        if (interactor.getAliases() != null && !interactor.getAliases().isEmpty()) {
            String id = null;

            for ( Alias alias : interactor.getAliases()){
                if (id == null){
                    id =alias.getName().toLowerCase( );
                    break;
                }
            }

            if (interactor.getOrganism() != null &&  !interactor.getOrganism().getIdentifiers().isEmpty() ) {
                String organism = interactor.getOrganism().getIdentifiers().iterator().next().getText();
                if (organism != null){
                    return id.concat( "_".concat(organism));
                }
            }
            return id;

        } else {
            LabelBuilder builder = new IdentifierLabelBuilder();
            return builder.buildDefaultLabel( interactor );
        }
    }

    public String buildLabel( Interactor interactor, String database) {
        return buildDefaultLabel( interactor );
    }

}
