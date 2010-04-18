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
package uk.ac.ebi.intact.view.webapp.controller.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.model.CvObject;
import uk.ac.ebi.intact.view.webapp.IntactViewException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
public class CvObjectService {

    @Autowired
    private DaoFactory daoFactory;

    private Map<String,CvObject> cvObjectsByAc;
    private Map<String,CvObject> cvObjectsByIdentifier;

    public CvObjectService() {
        cvObjectsByAc = new HashMap<String, CvObject>();
        cvObjectsByIdentifier = new HashMap<String, CvObject>();
    }

    public void clear() {
        cvObjectsByAc.clear();
        cvObjectsByIdentifier.clear();
    }

    public CvObject loadByAc(String ac) {
        CvObject cvObject;

        if (cvObjectsByAc.containsKey(ac)) {
            cvObject = cvObjectsByAc.get(ac);
        } else {
            cvObject = daoFactory.getCvObjectDao().getByAc(ac);
            cvObjectsByAc.put(ac, cvObject);
        }

        return cvObject;
    }

    public CvObject loadByIdentifier(String className, String identifier) {
        CvObject cvObject;

        final Class cvClass;
        try {
            cvClass = Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new IntactViewException("Class not found: "+className, e);
        }

        String key = className+"::"+identifier;

        if (cvObjectsByIdentifier.containsKey(key)) {
            cvObject = cvObjectsByIdentifier.get(key);
        } else {
            cvObject = daoFactory.getCvObjectDao(cvClass).getByPsiMiRef(identifier);
            cvObjectsByIdentifier.put(key, cvObject);
        }

        return cvObject;
    }
}
