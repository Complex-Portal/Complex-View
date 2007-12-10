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

import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.Interactor;

/**
 * Uses the alternativeIdentifiers of Interactor to create Label for the Node.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 1.6.0-Snapshot
 */
public class AlternativeLabelStrategy implements LabelStrategy {

    public String buildDefaultLabel( Interactor interactor ) {
        return buildLabel( interactor, "uniprotkb");
    }

    public String buildLabel( Interactor interactor, String database) {

        if (interactor.getAlternativeIdentifiers() != null && !interactor.getAlternativeIdentifiers().isEmpty()) {
            String id = null;
            for (CrossReference xref : interactor.getAlternativeIdentifiers()){
                if (id == null){
                    id =xref.getIdentifier().toLowerCase( );
                }
                if (xref.getDatabase().equals( database )){
                    id = xref.getIdentifier().toLowerCase();
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
            LabelStrategy strategy = new IdentifierLabelStrategy();
            return strategy.buildDefaultLabel( interactor );
        }
    }

}
