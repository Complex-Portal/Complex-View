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
package uk.ac.ebi.intact.editor.controller.curate.cloner;

import psidev.psi.mi.jami.model.*;
import uk.ac.ebi.intact.editor.controller.admin.UserManagerController;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.model.extension.*;

import java.util.Date;

/**
 * Editor specific cloning routine for biological complexes.
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id: InteractionIntactCloner.java 14783 2010-07-29 12:52:28Z brunoaranda $
 * @since 2.0.1-SNAPSHOT
 */
public class ParticipantJamiCloner {


    public static ModelledParticipant cloneParticipant(Participant participant) {
        IntactModelledParticipant clone = new IntactModelledParticipant(participant.getInteractor());
        clone.setCreated(new Date());
        clone.setUpdated(clone.getCreated());
        // set current user
        UserManagerController userController = ApplicationContextProvider.getBean("userManagerController");
        clone.setCreator(userController.getCurrentJamiUser().getLogin());
        clone.setUpdator(userController.getCurrentJamiUser().getLogin());
        clone.setBiologicalRole(participant.getBiologicalRole());
        if (participant.getInteraction() instanceof Complex){
            clone.setInteraction((Complex)participant.getInteraction());
        }
        clone.setStoichiometry(new IntactStoichiometry(participant.getStoichiometry().getMinValue(), participant.getStoichiometry().getMaxValue()));

        for (Object obj : participant.getAliases()){
            Alias alias = (Alias)obj;
            clone.getAliases().add(new ModelledParticipantAlias(alias.getType(), alias.getName()));
        }

        for (Object obj : participant.getXrefs()){
            Xref ref = (Xref)obj;
            clone.getXrefs().add(new ModelledParticipantXref(ref.getDatabase(), ref.getId(), ref.getVersion(), ref.getQualifier()));
        }

        for (Object obj : participant.getAnnotations()){
            Annotation annotation = (Annotation)obj;
            clone.getAnnotations().add(new ModelledParticipantAnnotation(annotation.getTopic(), annotation.getValue()));
        }

        for (Object obj : participant.getFeatures()){
            Feature feature = (Feature)obj;
            ModelledFeature r = FeatureJamiCloner.cloneFeature(feature);
            clone.addFeature(r);
        }

        if (participant.getInteraction() instanceof ModelledInteraction){
            clone.setInteraction((ModelledInteraction)participant.getInteraction());
        }

        // don't need to add it to the feature component because it is already done by the cloner
        return clone;
    }
}

