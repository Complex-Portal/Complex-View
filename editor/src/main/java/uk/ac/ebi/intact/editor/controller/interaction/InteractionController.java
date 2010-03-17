/**
 * Copyright 2010 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.editor.controller.interaction;

import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.primefaces.model.LazyDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.editor.controller.experiment.ExperimentController;
import uk.ac.ebi.intact.editor.controller.publication.PublicationController;
import uk.ac.ebi.intact.editor.util.LazyDataModelFactory;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Publication;

import javax.faces.event.ComponentSystemEvent;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope("conversation.access")
@ConversationName("general")
public class InteractionController extends JpaAwareController {

    private Interaction interaction;
    private String ac;
    private LazyDataModel<Component> participantDataModel;

    public InteractionController() {
    }

    @Autowired
    private PublicationController publicationController;

    @Autowired
    private ExperimentController experimentController;

    @Transactional
    public void loadData(ComponentSystemEvent event) {
        if (ac != null) {
            if (interaction == null || !ac.equals(interaction.getAc())) {
                interaction = IntactContext.getCurrentInstance().getDaoFactory().getInteractionDao().getByAc(ac);

                participantDataModel = LazyDataModelFactory.createLazyDataModel(getCoreEntityManager(),
                    "select c from Component c where c.interaction.ac = '"+ac+"'",
                    "select count(c) from Component c where c.interaction.ac = '"+ac+"'");
            }
        } else {
            ac = interaction.getAc();
        }

        // check if the publication or experiment are null in their controlles (this happens when the interaction
        // page is loaded directly using a URL)
        if (publicationController.getPublication() == null) {
            Publication publication = interaction.getExperiments().iterator().next().getPublication();
            publicationController.setPublication(publication);
        }

        if (experimentController.getExperiment() == null) {
            experimentController.setExperiment(interaction.getExperiments().iterator().next());
        }
    }

    public String getAc() {
        if (ac == null && interaction != null) {
            return interaction.getAc();
        }
        return ac;
    }

    @Transactional
    public int countParticipantsByInteractionAc(String ac) {
        return getDaoFactory().getInteractionDao().countInteractorsByInteractionAc(ac);
    }

    public Experiment getFirstExperiment(Interaction interaction) {
        return interaction.getExperiments().iterator().next();
    }

    public void setAc(String ac) {
        this.ac = ac;
    }

    public Interaction getInteraction() {
        return interaction;
    }

    public void setInteraction(Interaction interaction) {
        this.interaction = interaction;
    }

    public LazyDataModel<Component> getParticipantDataModel() {
        return participantDataModel;
    }
}