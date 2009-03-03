/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.view.webapp.controller.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.view.webapp.controller.JpaBaseController;
import uk.ac.ebi.intact.view.webapp.controller.config.ColourPalette;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
public class IconGenerator extends JpaBaseController{

    private static final Log log = LogFactory.getLog( IconGenerator.class );

    private Map<String,ColouredCv> colourMap;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private ColourPalette colourPalette;

    public IconGenerator() {
        colourMap = new HashMap<String,ColouredCv>(24);
    }

    @PostConstruct
    public void prepareColours() {
        if (log.isInfoEnabled()) log.info("Preparing simple icons for CVs");

        final List<String> proteinTypeLabels = listProteinTypeLabels();
        Collections.sort(proteinTypeLabels);

        for (String label : proteinTypeLabels) {
            String colour = colourPalette.getNextGrey();

            colourMap.put(label, new ColouredCv(label, colour));
        }

        final List<String> expRoleLabels = listExpRoleLabels();
        Collections.sort(expRoleLabels);

        for (String label : expRoleLabels) {
            String colour = colourPalette.getNextRed();

            colourMap.put(label, new ColouredCv(label, colour));
        }

        final List<String> bioRoleLabels = listBioRoleLabels();
        Collections.sort(expRoleLabels);

        for (String label : bioRoleLabels) {
            String colour = colourPalette.getNextGreen();

            colourMap.put(label, new ColouredCv(label, colour));
        }
    }

    public Map<String, ColouredCv> getColourMap() {
        return colourMap;
    }

    private List<String> listProteinTypeLabels() {
        Query query = entityManagerFactory.createEntityManager()
                .createQuery("select distinct i.cvInteractorType.shortLabel from InteractorImpl i");
        return query.getResultList();
    }

    private List<String> listExpRoleLabels() {
        Query query = entityManagerFactory.createEntityManager()
                .createQuery("select distinct expRole.shortLabel from Component c inner join c.experimentalRoles as expRole");
        return query.getResultList();
    }

    private List<String> listBioRoleLabels() {
        Query query = entityManagerFactory.createEntityManager()
                .createQuery("select distinct c.cvBiologicalRole.shortLabel from Component c");
        return query.getResultList();
    }

    public class ColouredCv {

        private String colourHex;
        private String text;
        private String initials;

        private ColouredCv(String text, String colourHex) {
            this.text = text;
            this.colourHex = colourHex;
            this.initials = text.substring(0,2).toUpperCase();
        }

        public String getColourHex() {
            return colourHex;
        }

        public String getText() {
            return text;
        }

        public String getInitials() {
            return initials;
        }
    }

}
