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
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.model.Annotation;
import uk.ac.ebi.intact.model.CvObject;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.view.webapp.IntactViewException;
import uk.ac.ebi.intact.view.webapp.controller.JpaBaseController;
import uk.ac.ebi.intact.view.webapp.controller.application.CvObjectService;

import javax.faces.context.FacesContext;
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

    private String identifier;
    private String className;
    @Autowired
    private CvObjectService cvObjectService;

    public CvObjectController() {
    }

    @Transactional(readOnly = true)
    public void load(ComponentSystemEvent evt) {
        if (!FacesContext.getCurrentInstance().isPostback()) {
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
            }else if (identifier != null){
                if (className == null){
                   className = "CvObject";
                }
                this.cv = loadByIdentifier(className, identifier);
            }
        }
    }

    private CvObject loadByIdentifier(String className, String identifier) {
        CvObject cvObject;

        final Class cvClass;
        try {
            cvClass = Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new IntactViewException("Class not found: "+className, e);
        }

        cvObject = getDaoFactory().getCvObjectDao(cvClass).getByPsiMiRef(identifier);
        Hibernate.initialize(cvObject.getXrefs());
        Hibernate.initialize(cvObject.getAnnotations());

        return cvObject;
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

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
