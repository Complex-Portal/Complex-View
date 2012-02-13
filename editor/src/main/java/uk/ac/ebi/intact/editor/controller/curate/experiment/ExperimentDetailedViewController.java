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
package uk.ac.ebi.intact.editor.controller.curate.experiment;

import org.apache.commons.lang.StringUtils;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.model.util.FeatureUtils;
import uk.ac.ebi.intact.model.util.XrefUtils;

import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "general" )
public class ExperimentDetailedViewController extends JpaAwareController {
    
    private String ac;
    private ExperimentWrapper experimentWrapper;

    @Autowired
    private ExperimentController experimentController;

    public ExperimentDetailedViewController() {
    }

    public void loadData( ComponentSystemEvent event ) {
        if (!FacesContext.getCurrentInstance().isPostback()) {
            if (ac != null) {
                Experiment experiment = getDaoFactory().getExperimentDao().getByAc(ac);

                if (experiment != null) {
                    this.experimentWrapper = new ExperimentWrapper(experiment, getDaoFactory().getEntityManager());
                    experimentController.setExperiment(experiment);
                } else {
                    addErrorMessage("No experiment with this AC", "Verify the URL");
                }
            }
        }
        
    }

    public String figureLegendForInteraction(Interaction interaction) {
        return findAnnotationText(interaction, "MI:0599");
    }

    public String commentForInteraction(Interaction interaction) {
        return findAnnotationText(interaction, CvTopic.COMMENT_MI_REF);
    }
    
    public String featureAsString(Feature feature) {
        StringBuilder sb = new StringBuilder();
        sb.append(feature.getShortLabel());

        final Collection<Range> ranges = feature.getRanges();
        final Iterator<Range> iterator = ranges.iterator();

        while (iterator.hasNext()) {
            Range next = iterator.next();
            sb.append("[");
            sb.append(FeatureUtils.convertRangeIntoString(next));
            sb.append("]");

            if (iterator.hasNext()) sb.append(", ");
        }

        if (feature.getCvFeatureType() != null) {
            sb.append(" ");
            sb.append(feature.getCvFeatureType().getShortLabel());
        }

        return sb.toString();
    }

    private String findAnnotationText(Interaction interaction, String miOrLabel) {
        if (interaction == null) return null;

        final Annotation annotation = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel(interaction, miOrLabel);

        if (annotation != null) {
            return annotation.getAnnotationText();
        }

        return null;
    }

    public String getAc() {
        return ac;
    }

    public void setAc(String ac) {
        this.ac = ac;
    }

    public ExperimentWrapper getExperimentWrapper() {
        return experimentWrapper;
    }

    
}
