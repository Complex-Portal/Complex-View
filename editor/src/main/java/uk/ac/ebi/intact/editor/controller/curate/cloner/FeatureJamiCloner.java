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
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.context.UserContext;
import uk.ac.ebi.intact.jami.model.extension.*;

import java.util.Date;

/**
 * Editor specific cloning routine for complex features.
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id: InteractionIntactCloner.java 14783 2010-07-29 12:52:28Z brunoaranda $
 * @since 2.0.1-SNAPSHOT
 */
public class FeatureJamiCloner {


    public static ModelledFeature cloneFeature(Feature feature) {
        IntactModelledFeature clone = new IntactModelledFeature();
        clone.setCreated(new Date());
        clone.setUpdated(clone.getCreated());
        UserContext jamiUserContext = ApplicationContextProvider.getBean("jamiUserContext");
        clone.setCreator(jamiUserContext.getUserId());
        clone.setUpdator(jamiUserContext.getUserId());
        clone.setShortName(feature.getShortName());
        clone.setFullName(feature.getFullName());
        clone.setRole(feature.getRole());
        clone.setType(feature.getType());
        if (feature.getParticipant() instanceof ModelledParticipant){
            clone.setParticipant((ModelledParticipant)feature.getParticipant());
        }

        for (Object obj : feature.getAliases()){
            Alias alias = (Alias)obj;
            clone.getAliases().add(new ModelledFeatureAlias(alias.getType(), alias.getName()));
        }

        for (Object obj: feature.getIdentifiers()){
            Xref ref = (Xref)obj;
            clone.getIdentifiers().add(new ModelledFeatureXref(ref.getDatabase(), ref.getId(), ref.getVersion(), ref.getQualifier()));
        }

        for (Object obj : feature.getXrefs()){
            Xref ref = (Xref)obj;
            clone.getXrefs().add(new ModelledFeatureXref(ref.getDatabase(), ref.getId(), ref.getVersion(), ref.getQualifier()));
        }

        for (Object obj : feature.getAnnotations()){
            Annotation annotation = (Annotation)obj;
            clone.getAnnotations().add(new ModelledFeatureAnnotation(annotation.getTopic(), annotation.getValue()));
        }

        for (Object obj : feature.getRanges()){
            Range range = (Range)obj;
            ModelledRange r = new ModelledRange(range.getStart(), range.getEnd(), range.isLink(), range.getResultingSequence());
            clone.getRanges().add(r);
        }

        // don't need to add it to the feature component because it is already done by the cloner
        return clone;
    }
}

