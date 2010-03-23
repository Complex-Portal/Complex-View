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
package uk.ac.ebi.intact.editor.controller.cvobject;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;

import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import java.util.*;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Lazy
public class CvObjectService extends JpaAwareController {

    private static final Log log = LogFactory.getLog( CvObjectService.class );

    private List<CvObject> allCvObjects;
    private Map<CvKey,CvObject> allCvObjectMap;

    private Collection<CvTopic> publicationTopics;
    private Collection<CvTopic> experimentTopics;
    private Collection<CvTopic> interactionTopics;
    private List<SelectItem> publicationTopicSelectItems;
    private List<SelectItem> experimentTopicSelectItems;
    private List<SelectItem> interactionTopicSelectItems;

    public CvObjectService() {
    }


    @PostConstruct
    public void loadData() {
        refresh(null);
    }

    @Transactional
    public void refresh(ActionEvent evt) {
        if (log.isDebugEnabled()) log.debug("Loading Controlled Vocabularies");

        final TransactionStatus transactionStatus = IntactContext.getCurrentInstance().getDataContext().beginTransaction();

        publicationTopicSelectItems = new ArrayList<SelectItem>();

        allCvObjects = getDaoFactory().getCvObjectDao().getAll();

        allCvObjectMap = new HashMap<CvKey, CvObject>(allCvObjects.size());

        Multimap<String, CvTopic> cvObjectsByUsedInClass = new HashMultimap<String, CvTopic>();

        for (CvObject cvObject : allCvObjects) {
            if (cvObject.getIdentifier() != null) {
                CvKey keyId = new CvKey(cvObject.getIdentifier(), cvObject.getClass());
                CvKey keyLabel = new CvKey(cvObject.getShortLabel(), cvObject.getClass());
                allCvObjectMap.put(keyId, cvObject);
                allCvObjectMap.put(keyLabel, cvObject);
            }

            if (cvObject instanceof CvTopic) {
                String[] usedInClasses = findUsedInClass(cvObject);

                for (String usedInClass : usedInClasses) {
                    cvObjectsByUsedInClass.put(usedInClass, (CvTopic) cvObject);
                }

                if (usedInClasses.length == 0) {
                    cvObjectsByUsedInClass.put("no_class", (CvTopic) cvObject);
                }
            }
        }

        publicationTopics = cvObjectsByUsedInClass.get(Experiment.class.getName());
        experimentTopics = cvObjectsByUsedInClass.get(Experiment.class.getName());
        interactionTopics = cvObjectsByUsedInClass.get(Interaction.class.getName());

        publicationTopicSelectItems = createSelectItems(publicationTopics, "-- Select topic --");
        experimentTopicSelectItems = createSelectItems(experimentTopics, "-- Select topic --");
        interactionTopicSelectItems = createSelectItems(interactionTopics, "-- Select topic --");

        IntactContext.getCurrentInstance().getDataContext().commitTransaction(transactionStatus);
    }

    private String[] findUsedInClass(CvObject cvObject) {
        final Annotation annotation = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel(cvObject, CvTopic.USED_IN_CLASS);

        if (annotation != null) {
            String annotText = annotation.getAnnotationText();
            annotText = annotText.replaceAll(" ", "");
            return annotText.split(",");
        } else {
            return new String[0];
        }
    }

    private List<SelectItem> createSelectItems(Collection<? extends CvObject> cvObjects, String noSelectionText) {
        List<SelectItem> selectItems = new ArrayList<SelectItem>(cvObjects.size());

        if (noSelectionText != null) {
            selectItems.add(new SelectItem(null, noSelectionText, noSelectionText, false, false, true));
        }

        for (CvObject cvObject: cvObjects) {
            selectItems.add(createSelectItem(cvObject));
        }

        return selectItems;
    }

    private SelectItem createSelectItem(CvObject cv) {
        boolean obsolete = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel(cv, CvTopic.OBSOLETE_MI_REF) != null;
        return new SelectItem(cv.getIdentifier(), cv.getShortLabel(), cv.getFullName(), obsolete);
    }

    public <T extends CvObject> T findCvObject(Class<T> clazz, String idOrLabel) {
        CvKey keyId = new CvKey(idOrLabel, clazz);
        CvKey keyLabel = new CvKey(idOrLabel, clazz);

        if (allCvObjectMap.containsKey(keyId)) {
            return (T) allCvObjectMap.get(keyId);
        } else if (allCvObjectMap.containsKey(keyLabel)) {
            return (T) allCvObjectMap.get(keyLabel);
        }

        return null;
    }


    private class CvKey {
        private String id;
        private String className;
        private String classSimpleName;

        private CvKey(String id, Class clazz) {
            this.id = id;
            this.className = clazz.getName();
            this.classSimpleName = clazz.getSimpleName();
        }

        public String getId() {
            return id;
        }

        public String getClassName() {
            return className;
        }

        public String getClassSimpleName() {
            return classSimpleName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CvKey cvKey = (CvKey) o;

            if (className != null ? !className.equals(cvKey.className) : cvKey.className != null) return false;
            if (id != null ? !id.equals(cvKey.id) : cvKey.id != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (className != null ? className.hashCode() : 0);
            return result;
        }
    }

    public List<SelectItem> getPublicationTopicSelectItems() {
        return publicationTopicSelectItems;
    }

    public List<SelectItem> getExperimentTopicSelectItems() {
        return experimentTopicSelectItems;
    }

    public List<SelectItem> getInteractionTopicSelectItems() {
        return interactionTopicSelectItems;
    }
}
