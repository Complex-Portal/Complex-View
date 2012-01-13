/**
 * Copyright 2012 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.editor.it.experiment;

import org.apache.commons.lang.StringUtils;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.primefaces.model.LazyDataModel;
import org.springframework.test.context.ContextConfiguration;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.core.unit.IntactMockBuilder;
import uk.ac.ebi.intact.core.util.DebugUtil;
import uk.ac.ebi.intact.editor.controller.curate.experiment.ExperimentWrapper;
import uk.ac.ebi.intact.editor.it.BaseIT;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.XrefUtils;

import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ExperimentWrapperIT extends BaseIT{
    
    private ExperimentWrapper experimentWrapper;
    
    @Before
    public void setupExperiment() {
        // experiment illustrated in story detailed_experiment_view.txt
        Experiment expren20111 = getDaoFactory().getExperimentDao().getByShortLabel("ren-2011-1");

        assertThat(expren20111, is(notNullValue()));
        
        experimentWrapper = new ExperimentWrapper(expren20111, getEntityManager());
    }

    @Test
    public void interactionsOrder() throws Exception {
        // Given interactions are sorted alphanumerically
        // When I am in the detailed page for experiment with label ren-2011-1
        LazyDataModel dataModel = (LazyDataModel) experimentWrapper.getInteractionsDataModel();
        List<Interaction> interactions = dataModel.load(0, 350, null, null, Collections.EMPTY_MAP);
        
        // Then they should be sorted alphanumerically: cwf8-prp46, dre4-luc7, prp17-sap61
        assertThat(theShortLabelsFor(interactions), is("cwf8-prp46, dre4-luc7, prp17-sap61"));
    }

    @Test
    public void participantsOrderWithBait() throws Exception {
        // Given bait participants should be displayed first and the rest should be sorted alphanumerically
        // When I look at the order of participants for interaction with label dre4-luc7
        // Then the participant order should be: Q09685, Q09882, Q9USM4
        assertThat(thePrimaryIdFor(participantsForInteraction("dre4-luc7")), is("Q09685, Q09882, Q9USM4"));
    }

    @Test
    public void participantsOrderAllNeutral() throws Exception {
        // Given participants without bait should be sorted alphanumerically
        // When I look at the order of participants for interaction with label cwf8-prp46
        // Then the participant order should be: Q09685, Q09882, Q9USM4
        assertThat(thePrimaryIdFor(participantsForInteraction("cwf8-prp46")), is("O13615, O14011, O59734"));
    }

    @Test
    public void participantsOrderEnzymeTarget() throws Exception {
        // Given enzyme participants should be displayed first and the rest should be sorted alphanumerically
        // When I look at the order of participants for interaction with label prp17-sap61
        // Then the participant order should be: O43071, O59706
        assertThat(thePrimaryIdFor(participantsForInteraction("prp17-sap61")), is("O43071, O59706"));
    }

    private List<Component> participantsForInteraction(String interactionLabel) {
        Interaction interaction = getDaoFactory().getInteractionDao().getByShortLabel(interactionLabel);
        return experimentWrapper.sortedParticipants(interaction);
    }


    private static String theShortLabelsFor(List<? extends AnnotatedObject> interactions) {
        return StringUtils.join(DebugUtil.labelList(interactions), ", ");
    }

    private static String thePrimaryIdFor(List<Component> participants) {
        List<String> ids = new ArrayList<String>(participants.size());
        
        for (Component participant : participants) {
           ids.add(XrefUtils.getIdentityXrefs(participant.getInteractor()).iterator().next().getPrimaryId());
        }
        
        return StringUtils.join(ids, ", ");
    }

}
