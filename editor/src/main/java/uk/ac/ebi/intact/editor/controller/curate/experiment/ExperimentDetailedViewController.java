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

import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.model.util.FeatureUtils;

import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import java.util.*;

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

	private List<String> annotationTopicsForExpOverview =
			Arrays.asList(
					CvTopic.ACCEPTED,
					CvTopic.TO_BE_REVIEWED,
					CvTopic.ON_HOLD,
					CvTopic.HIDDEN,
					CvTopic.COMMENT_MI_REF,
					CvTopic.REMARK_INTERNAL,
					CvTopic.CORRECTION_COMMENT,
					CvTopic.COMMENT,
					"MI:0591", //experiment description
					"MI:0627", //experiment modification
					"MI:0633" //data-processing
					);

    @Autowired
    private ExperimentController experimentController;

    public ExperimentDetailedViewController() {
    }

    public void loadData( ComponentSystemEvent event ) {
        if (!FacesContext.getCurrentInstance().isPostback()) {
            if (experimentController.getExperiment() != null) {
                Experiment experiment = experimentController.getExperiment();

                this.experimentWrapper = new ExperimentWrapper(experiment, getDaoFactory().getEntityManager());
                ac = experiment.getAc();
            }
            else if (ac != null) {
                Experiment experiment = getDaoFactory().getExperimentDao().getByAc(ac);
                if (isComplexExperiment(experiment)){
                   experiment = null;
                }

                if (experiment != null) {
                    this.experimentWrapper = new ExperimentWrapper(experiment, getDaoFactory().getEntityManager());
                    experimentController.setExperiment(experiment);
                } else {
                    addErrorMessage("No experiment with this AC", "Verify the URL");
                }
            }
        }

    }

    /**
     * This method is for backward compatibility only. We exclude all experiments that were created for complex curation and that should all have 'inferred by curator'
     * as interaction detection method
     */
    protected boolean isComplexExperiment(Experiment experiment) {
        if (experiment != null){
            if (experiment.getCvInteraction() != null &&
                    CvInteraction.INFERRED_BY_CURATOR_MI_REF.equals(experiment.getCvInteraction().getIdentifier())){
                return true;
            }
        }
        return false;
    }

	//Now in experiment_overview we show all the annotations in the interaction not only these two
//	@Deprecated
//    public String figureLegendForInteraction(Interaction interaction) {
//        return findAnnotationText(interaction, "MI:0599");
//    }
//
//	@Deprecated
//    public String commentForInteraction(Interaction interaction) {
//        return findAnnotationText(interaction, CvTopic.COMMENT_MI_REF);
//    }

//	@Deprecated
//    public String commentForInteraction(Interaction interaction) {
//        return findAnnotationText(interaction, CvTopic.COMMENT_MI_REF);
//    }

	public List<Annotation> experimentAnnotationsByOverviewCriteria(Experiment experiment) {

		if (experiment == null) return null;

		List<Annotation> annotations = new ArrayList<Annotation>();
		for(String topic: annotationTopicsForExpOverview){
			Annotation annotation = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel(experiment, topic);
			if(annotation!=null){
				annotations.add(annotation);
			}
		}
		return annotations;
	}


	public static String parameterAsString(Parameter param){
		if (param == null){
			return null;
		}

		String value = null;

		if (param.getFactor() != null) {
			value = String.valueOf(param.getFactor());

			if ((param.getExponent() != null && param.getExponent() != 0) || (param.getBase() !=null && param.getBase() != 10)) {
				value = param.getFactor() + "x" + param.getBase() + "^" + param.getExponent();
			}
			if (param.getUncertainty()!=null && param.getUncertainty() != 0.0) {
				value = value + " ~" + param.getUncertainty();
			}
		}
		return value;
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

//    @Deprecated
//    private String findAnnotationText(Interaction interaction, String miOrLabel) {
//        if (interaction == null) return null;
//
//        final Annotation annotation = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel(interaction, miOrLabel);
//
//        if (annotation != null) {
//            return annotation.getAnnotationText();
//        }
//
//        return null;
//    }

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
