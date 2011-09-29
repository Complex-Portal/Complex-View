/**
 * Copyright 2011 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.view.webapp.controller.details;

import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.model.Annotation;
import uk.ac.ebi.intact.model.CvObject;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.view.webapp.controller.JpaBaseController;

import javax.faces.event.ComponentSystemEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope("conversation.access")
@ConversationName( "general" )
public class CvObjectController extends JpaBaseController {

    private CvObject cv;
    private String ac;

    private String cvDescription;
    private List<Annotation> cvAnnotations;

    public CvObjectController() {
    }

    @Transactional(readOnly = true)
    public void load(ComponentSystemEvent evt) {
        cvAnnotations = new ArrayList<Annotation>();

        if (ac != null) {
            cv = getDaoFactory().getCvObjectDao().getByAc(ac);

            final Annotation annotation = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel(cv, CvTopic.DEFINITION);

            if (annotation != null) {
                cvDescription = annotation.getAnnotationText();
            }

            for (Annotation annot : cv.getAnnotations()) {
                if (!annot.getCvTopic().getShortLabel().equals(CvTopic.USED_IN_CLASS)) {
                    cvAnnotations.add(annot);
                }
            }

        } else if (cv != null) {
            ac = cv.getAc();
        }
    }

    public String getCvDescription() {
        return cvDescription;
    }

    public CvObject getCv() {
        return cv;
    }

    public void setCv(CvObject cv) {
        this.cv = cv;
        setAc(cv.getAc());
    }

    public String getAc() {
        return ac;
    }

    public void setAc(String ac) {
        this.ac = ac;
    }

    public List<Annotation> getCvAnnotations() {
        return cvAnnotations;
    }
}
