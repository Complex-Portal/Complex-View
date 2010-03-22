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
package uk.ac.ebi.intact.editor.controller.shared;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.editor.controller.cvobject.CvObjectPopulator;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.Annotation;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;

import java.util.Iterator;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Lazy
public class AnnotatedObjectHelper extends JpaAwareController {

    @Autowired
    private CvObjectPopulator cvObjectPopulator;

    public AnnotatedObjectHelper() {
    }

    public void addAnnotation(AnnotatedObject parent, String topicIdOrLabel, String text) {
        CvTopic dataset = cvObjectPopulator.findCvObject(CvTopic.class, topicIdOrLabel);

        Annotation annotation = new Annotation(getIntactContext().getInstitution(), dataset);
        annotation.setAnnotationText(text);

        parent.addAnnotation(annotation);
    }

    public void replaceOrCreateAnnotation(AnnotatedObject parent, String topicOrShortLabel, String text) {
        // modify if exists
        boolean exists = false;

        for (Annotation annotation : parent.getAnnotations()) {
            if (topicOrShortLabel.equals(annotation.getCvTopic().getIdentifier())
                    || topicOrShortLabel.equals(annotation.getCvTopic().getShortLabel())) {
                if (!text.equals(annotation.getAnnotationText())) {
                    annotation.setAnnotationText(text);
                }
                exists = true;
            }
        }

        // create if not exists
        if (!exists) {
            addAnnotation(parent, topicOrShortLabel, text);
        }
    }

    public void removeAnnotation(AnnotatedObject parent, String topicIdOrLabel) {
        Iterator<Annotation> iterator = parent.getAnnotations().iterator();

        while (iterator.hasNext()) {
            Annotation annotation = iterator.next();
            if (topicIdOrLabel.equals(annotation.getCvTopic().getIdentifier()) ||
                    topicIdOrLabel.equals(annotation.getCvTopic().getShortLabel())) {
                iterator.remove();
            }
        }
    }

    public void removeAnnotation(AnnotatedObject parent, String topicIdOrLabel, String text) {
        Iterator<Annotation> iterator = parent.getAnnotations().iterator();

        while (iterator.hasNext()) {
            Annotation annotation = iterator.next();
            if ((topicIdOrLabel.equals(annotation.getCvTopic().getIdentifier()) || topicIdOrLabel.equals(annotation.getCvTopic().getShortLabel()))
                    && text.equals(annotation.getAnnotationText())) {
                iterator.remove();
            }
        }
    }

    public void setAnnotation(AnnotatedObject parent, String topicIdOrLabel, Object value) {
        if (value != null && !value.toString().isEmpty())  {
            replaceOrCreateAnnotation(parent, topicIdOrLabel, value.toString());
        } else {
            removeAnnotation(parent, topicIdOrLabel);
        }
    }

    public String findAnnotationText(AnnotatedObject parent, String topicId) {
        Annotation annotation = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel(parent, topicId);

        if (annotation != null) {
            return annotation.getAnnotationText();
        }

        return null;

    }
}
