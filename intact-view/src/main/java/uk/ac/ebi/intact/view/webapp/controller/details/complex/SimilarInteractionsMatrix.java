/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.view.webapp.controller.details.complex;

import java.util.List;

/**
 * Centralization of all data nessary to build the similar interaction matrix.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 */
public class SimilarInteractionsMatrix {

    private SimpleInteractor investigatedInteraction;

    private List<SimilarInteraction> similarInteractions;

    private List<SimpleInteractor> members;

    public SimilarInteractionsMatrix( SimpleInteractor investigatedInteraction, List<SimilarInteraction> similarInteractions, List<SimpleInteractor> members ) {
        if ( investigatedInteraction == null ) {
            throw new IllegalArgumentException( "You must give a non null investigatedInteraction" );
        }

        if ( similarInteractions == null ) {
            throw new IllegalArgumentException( "You must give a non null similarInteractions" );
        }

        if ( members == null ) {
            throw new IllegalArgumentException( "You must give a non null members" );
        }

        this.investigatedInteraction = investigatedInteraction;
        this.similarInteractions = similarInteractions;
        this.members = members;
    }

    public SimpleInteractor getInvestigatedInteraction() {
        return investigatedInteraction;
    }

    public List<SimilarInteraction> getSimilarInteractions() {
        return similarInteractions;
    }

    public List<SimpleInteractor> getMembers() {
        return members;
    }

    public static boolean isMemberPresent( SimilarInteraction interaction, SimpleInteractor participant ) {
        return interaction.isMemberPresent( participant );
    }
}
