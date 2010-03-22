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

    public void addAnnotation(AnnotatedObject parent, String topicId, String text) {
        CvTopic dataset = cvObjectPopulator.findCvObject(CvTopic.class, topicId);

        Annotation annotation = new Annotation(getIntactContext().getInstitution(), dataset);
        annotation.setAnnotationText(text);

        parent.addAnnotation(annotation);
    }

    public void removeAnnotation(AnnotatedObject parent, String topicId, String text) {
        Iterator<Annotation> iterator = parent.getAnnotations().iterator();

        while (iterator.hasNext()) {
            Annotation annotation = iterator.next();
            if (topicId.equals(annotation.getCvTopic().getIdentifier()) && text.equals(annotation.getAnnotationText())) {
                iterator.remove();
            }
        }
    }
}
