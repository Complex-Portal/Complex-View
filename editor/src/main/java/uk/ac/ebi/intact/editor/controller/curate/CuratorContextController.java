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
package uk.ac.ebi.intact.editor.controller.curate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.editor.controller.BaseController;
import uk.ac.ebi.intact.editor.controller.curate.cvobject.CvObjectController;
import uk.ac.ebi.intact.editor.controller.curate.experiment.ExperimentController;
import uk.ac.ebi.intact.editor.controller.curate.feature.FeatureController;
import uk.ac.ebi.intact.editor.controller.curate.interaction.InteractionController;
import uk.ac.ebi.intact.editor.controller.curate.interactor.InteractorController;
import uk.ac.ebi.intact.editor.controller.curate.organism.BioSourceController;
import uk.ac.ebi.intact.editor.controller.curate.participant.ParticipantController;
import uk.ac.ebi.intact.editor.controller.curate.publication.PublicationController;
import uk.ac.ebi.intact.model.*;

import java.util.*;

/**
 * Keeps the changes for each annotated object by AC.
 * 
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope("session")
public class CuratorContextController extends BaseController {

    @Autowired
    private PersistenceController persistenceController;

    private Map<Object,UnsavedChangeManager> unsavedMap;

    public CuratorContextController() {
        this.unsavedMap = new HashMap<Object, UnsavedChangeManager>();
    }

    public UnsavedChangeManager getUnsavedChangeManager(Object key) {
        if (unsavedMap.containsKey(key)) {
            return unsavedMap.get(key);
        }

        UnsavedChangeManager unsavedChangeManager = new UnsavedChangeManager();

        if (key != null) {
            unsavedMap.put(key, unsavedChangeManager);
        }

        return unsavedChangeManager; 
    }

    public List<UnsavedChange> getAllChanges() {
        List<UnsavedChange> unsavedChanges = new ArrayList<UnsavedChange>(unsavedMap.size() * 2);

        for (UnsavedChangeManager ucm : unsavedMap.values()) {
            unsavedChanges.addAll(ucm.getChanges());
        }

        return unsavedChanges;
    }

    public String edit(IntactObject intactObject) {
        Class<?> iaClass = intactObject.getClass();

        String suffix = (intactObject.getAc() != null)? "?faces-redirect=true&includeViewParams=true" : "";

        if (Publication.class.isAssignableFrom(iaClass)) {
            PublicationController publicationController = (PublicationController) getSpringContext().getBean("publicationController");
            publicationController.setPublication((Publication)intactObject);
            return "/curate/publication"+suffix;
        } else if (Experiment.class.isAssignableFrom(iaClass)) {
            ExperimentController experimentController = (ExperimentController) getSpringContext().getBean("experimentController");
            experimentController.setExperiment((Experiment)intactObject);
            return "/curate/experiment"+suffix;
        } else if (Interaction.class.isAssignableFrom(iaClass)) {
            InteractionController interactionController = (InteractionController) getSpringContext().getBean("interactionController");
            interactionController.setInteraction((Interaction)intactObject);
            return "/curate/interaction"+suffix;
        } else if (Interactor.class.isAssignableFrom(iaClass)) {
            InteractorController interactorController = (InteractorController) getSpringContext().getBean("interactorController");
            interactorController.setInteractor((Interactor)intactObject);
            return "/curate/interactor"+suffix;
        } else if (Component.class.isAssignableFrom(iaClass)) {
            ParticipantController participantController = (ParticipantController) getSpringContext().getBean("participantController");
            participantController.setParticipant((Component) intactObject);
            return "/curate/participant"+suffix;
        } else if (Feature.class.isAssignableFrom(iaClass)) {
            FeatureController featureController = (FeatureController) getSpringContext().getBean("featureController");
            featureController.setFeature((Feature) intactObject);
            return "/curate/feature"+suffix;
        } else if (CvObject.class.isAssignableFrom(iaClass)) {
            CvObjectController cvObjectController = (CvObjectController) getSpringContext().getBean("cvObjectController");
            cvObjectController.setCvObject((CvObject) intactObject);
            return "/curate/cvobject"+suffix;
        } else if (BioSource.class.isAssignableFrom(iaClass)) {
            BioSourceController bioSourceController = (BioSourceController) getSpringContext().getBean("bioSourceController");
            bioSourceController.setBioSource((BioSource) intactObject);
            return "/curate/organism"+suffix;
        } else {
            throw new IllegalArgumentException("No view defined for object with type: "+iaClass);
        }
    }

    public void removeFromUnsaved(IntactObject object) {
        for (UnsavedChangeManager ucm : getUnsavedChangeManagers()) {
            ucm.removeFromUnsaved(object);
        }
    }

    public void removeFromUnsavedByAc(String ac) {
        for (UnsavedChangeManager ucm : getUnsavedChangeManagers()) {
            final Iterator<UnsavedChange> changeIterator = ucm.getChanges().iterator();

            while (changeIterator.hasNext()) {
                UnsavedChange unsavedChange = changeIterator.next();
                if (ac.equals(unsavedChange.getUnsavedObject().getAc())) {
                    changeIterator.remove();
                }
            }
        }
    }

    public Collection<UnsavedChangeManager> getUnsavedChangeManagers() {
        return unsavedMap.values();
    }

    public void clear() {
        unsavedMap.clear();
    }
}
