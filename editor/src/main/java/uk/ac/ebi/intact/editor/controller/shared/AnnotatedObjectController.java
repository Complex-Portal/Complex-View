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
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.editor.controller.cvobject.CvObjectService;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.Annotation;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.IntactObject;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;

import javax.faces.event.ActionEvent;
import java.util.*;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public abstract class AnnotatedObjectController extends JpaAwareController {



     @Autowired
    private CvObjectService cvObjectService;

    public AnnotatedObjectController() {
    }

    public abstract AnnotatedObject getAnnotatedObject();

    public void newAnnotation(ActionEvent evt) {
        getAnnotatedObject().addAnnotation(new Annotation(getIntactContext().getInstitution(), null));
    }

    public void addAnnotation(String topicIdOrLabel, String text) {
        CvTopic dataset = cvObjectService.findCvObject(CvTopic.class, topicIdOrLabel);

        Annotation annotation = new Annotation(getIntactContext().getInstitution(), dataset);
        annotation.setAnnotationText(text);

        getAnnotatedObject().addAnnotation(annotation);
    }

    public void replaceOrCreateAnnotation(String topicOrShortLabel, String text) {
        AnnotatedObject parent = getAnnotatedObject();

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
            addAnnotation(topicOrShortLabel, text);
        }
    }

    public void removeAnnotation(String topicIdOrLabel) {
        Iterator<Annotation> iterator = getAnnotatedObject().getAnnotations().iterator();

        while (iterator.hasNext()) {
            Annotation annotation = iterator.next();
            if (topicIdOrLabel.equals(annotation.getCvTopic().getIdentifier()) ||
                    topicIdOrLabel.equals(annotation.getCvTopic().getShortLabel())) {
                iterator.remove();
            }
        }
    }

    public void removeAnnotation(String topicIdOrLabel, String text) {
        Iterator<Annotation> iterator = getAnnotatedObject().getAnnotations().iterator();

        while (iterator.hasNext()) {
            Annotation annotation = iterator.next();
            if ((topicIdOrLabel.equals(annotation.getCvTopic().getIdentifier()) || topicIdOrLabel.equals(annotation.getCvTopic().getShortLabel()))
                    && text.equals(annotation.getAnnotationText())) {
                iterator.remove();
            }
        }
    }

    public void setAnnotation(String topicIdOrLabel, Object value) {
        if (value != null && !value.toString().isEmpty())  {
            replaceOrCreateAnnotation(topicIdOrLabel, value.toString());
        } else {
            removeAnnotation(topicIdOrLabel);
        }
    }

    public String findAnnotationText(AnnotatedObject parent, String topicId) {
        Annotation annotation = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel(parent, topicId);

        if (annotation != null) {
            return annotation.getAnnotationText();
        }

        return null;

    }

    public List<Annotation> getAnnotations() {
        final ArrayList<Annotation> annotations = new ArrayList<Annotation>(getAnnotatedObject().getAnnotations());
        Collections.sort(annotations, new Comparator<IntactObject>() {
            @Override
            public int compare(IntactObject o1, IntactObject o2) {
                if (o1.getAc() != null) return 1;
                return 0;
            }
        });
        return annotations;
    }


}
