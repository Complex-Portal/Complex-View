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
package uk.ac.ebi.intact.editor.controller.experiment;

import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.primefaces.model.LazyDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.controller.publication.PublicationController;
import uk.ac.ebi.intact.editor.controller.shared.AnnotatedObjectController;
import uk.ac.ebi.intact.editor.util.LazyDataModelFactory;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Interaction;

import javax.faces.event.ComponentSystemEvent;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope("conversation.access")
@ConversationName("general")
public class ExperimentController extends AnnotatedObjectController {

    private Experiment experiment;
    private String ac;
    private LazyDataModel<Interaction> interactionDataModel;

    @Autowired
    private PublicationController publicationController;

    public ExperimentController() {
    }

    @Override
    public AnnotatedObject getAnnotatedObject() {
        return getExperiment();
    }

    public void loadData(ComponentSystemEvent event) {
        if (ac != null) {
            if (experiment == null || !ac.equals(experiment.getAc())) {
                experiment = IntactContext.getCurrentInstance().getDaoFactory().getExperimentDao().getByAc(ac);

                interactionDataModel = LazyDataModelFactory.createLazyDataModel(getCoreEntityManager(),
                    "select i from InteractionImpl i join i.experiments as exp where exp.ac = '"+ac+"'",
                    "select count(i) from InteractionImpl i join i.experiments as exp where exp.ac = '"+ac+"'");
            }
        } else if (experiment != null) {
            ac = experiment.getAc();
        }

        if (experiment != null && publicationController.getPublication() == null) {
            publicationController.setPublication(experiment.getPublication());
        }
    }

    public int countInteractionsByExperimentAc(String ac) {
        return getDaoFactory().getExperimentDao().countInteractionsForExperimentWithAc(ac);
    }

    public String getAc() {
        if (ac == null && experiment != null) {
            return experiment.getAc();
        }
        return ac;
    }

    public void setAc(String ac) {
        this.ac = ac;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public LazyDataModel<Interaction> getInteractionDataModel() {
        return interactionDataModel;
    }
}