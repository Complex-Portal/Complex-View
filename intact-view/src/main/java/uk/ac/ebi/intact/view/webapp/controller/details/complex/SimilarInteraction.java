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

import com.google.common.collect.Sets;
import com.google.common.collect.Lists;

import java.util.Set;
import java.util.Collection;

/**
 * A row in our matrix of similar interactions.
*
* @author Samuel Kerrien (skerrien@ebi.ac.uk)
* @version $Id$
* @since 1.6.0
*/
public class SimilarInteraction extends SimpleInteractor {

    private Set<SimpleInteractor> members;
    
    private Set<SimpleInteractor> others;

    private int totalParticipantCount;

    public SimilarInteraction( String ac, String shortLabel, int totalParticipantCount ) {
        super( ac, shortLabel );
        members = Sets.newHashSet();
        others = Sets.newHashSet();
        this.totalParticipantCount = totalParticipantCount;
    }

    public int getMemberCount() {
        return members.size();
    }

    public int getOthersCount() {
        return others.size();
    }

    public int getTotalParticipantCount() {
        return totalParticipantCount;
    }

    public Collection<SimpleInteractor> getMembers() {
        return Lists.newArrayList( members );
    }

    public void addOthers( SimpleInteractor interactor ) {
        others.add( interactor );
    }

    public void addMember( SimpleInteractor interactor ) {
        members.add( interactor );
    }

    public Collection<SimpleInteractor> getOthers() {
        return Lists.newArrayList( others );
    }

    public boolean isMemberPresent( SimpleInteractor interactor ) {
        return members.contains( interactor );
    }
}
