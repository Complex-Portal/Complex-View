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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.model.CvObject;

import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller("cvObjectPopulator")
@Lazy
public class CvObjectPopulatorImpl extends JpaAwareController implements CvObjectPopulator {

    private static final Log log = LogFactory.getLog( CvObjectPopulatorImpl.class );

    private List<CvObject> allCvObjects;
    private Map<CvKey,CvObject> allCvObjectMap;

    public CvObjectPopulatorImpl() {
    }


    @PostConstruct
    public void loadData() {
        refresh(null);
    }

    @Override
    @Transactional
    public void refresh(ActionEvent evt) {
        if (log.isDebugEnabled()) log.debug("Loading Controlled Vocabularies");

        allCvObjects = getDaoFactory().getCvObjectDao().getAll();

        allCvObjectMap = new HashMap<CvKey, CvObject>(allCvObjects.size());

        for (CvObject cvObject : allCvObjects) {
            if (cvObject.getIdentifier() != null) {
                CvKey key = new CvKey(cvObject.getIdentifier(), cvObject.getClass());
                allCvObjectMap.put(key, cvObject);
            }
        }
    }

    @Override
    public <T extends CvObject> T findCvObject(Class<T> clazz, String identifier) {
        CvKey key = new CvKey(identifier, clazz);

        if (allCvObjectMap.containsKey(key)) {
            return (T) allCvObjectMap.get(key);
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
}
