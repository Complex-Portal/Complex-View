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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.context.DataContext;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.view.webapp.application.SpringInitializedService;
import uk.ac.ebi.intact.view.webapp.controller.JpaBaseController;
import uk.ac.ebi.intact.view.webapp.controller.config.ColourPalette;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Icon Generator.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
public class IconGeneratorImpl extends SpringInitializedService implements IconGenerator {

    private static final Log log = LogFactory.getLog( IconGeneratorImpl.class );

    private Map<String,ColouredCv> typeColourMap;
    private Map<String,ColouredCv> expRoleColourMap;
    private Map<String,ColouredCv> bioRoleColourMap;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private ColourPalette colourPalette;

    public IconGeneratorImpl() {
        typeColourMap = new HashMap<String,ColouredCv>(24);
        expRoleColourMap = new HashMap<String,ColouredCv>(24);
        bioRoleColourMap = new HashMap<String,ColouredCv>(24);
    }

    @Override
    public void initialize() {
        if (typeColourMap.isEmpty() || expRoleColourMap.isEmpty() || bioRoleColourMap.isEmpty()){
            if (log.isInfoEnabled()) log.info("Preparing simple icons for CVs");

            final List<Object[]> proteinTypeLabels = listProteinTypeLabels();
            //Collections.sort(proteinTypeLabels);

            for (Object[] protType : proteinTypeLabels) {
                String colour = colourPalette.getNextGrey();

                String label = protType[1].toString();
                String description = protType[0].toString();

                typeColourMap.put(label, new ColouredCv(label, colour, description));
            }

            final List<Object[]> expRoleLabels = listExpRoleLabels();
            //Collections.sort(expRoleLabels);

            for (Object[] expRole : expRoleLabels) {
                String colour = colourPalette.getNextRed();

                String label = expRole[1].toString();
                String description = expRole[0].toString();

                expRoleColourMap.put(label, new ColouredCv(label, colour, description));
            }

            final List<Object[]> bioRoleLabels = listBioRoleLabels();
            //Collections.sort(expRoleLabels);

            for (Object[] bioRole : bioRoleLabels) {
                String colour = colourPalette.getNextGreen();

                String label = bioRole[1].toString();
                String description = bioRole[0].toString();

                bioRoleColourMap.put(label, new ColouredCv(label, colour, description));
            }
        }
    }

    public synchronized void reload() {
        if (log.isInfoEnabled()) log.info("Preparing simple icons for CVs");

        final List<Object[]> proteinTypeLabels = listProteinTypeLabels();
        //Collections.sort(proteinTypeLabels);

        for (Object[] protType : proteinTypeLabels) {
            String colour = colourPalette.getNextGrey();

            String label = protType[1].toString();
            String description = protType[0].toString();

            typeColourMap.put(label, new ColouredCv(label, colour, description));
        }

        final List<Object[]> expRoleLabels = listExpRoleLabels();
        //Collections.sort(expRoleLabels);

        for (Object[] expRole : expRoleLabels) {
            String colour = colourPalette.getNextRed();

            String label = expRole[1].toString();
            String description = expRole[0].toString();

            expRoleColourMap.put(label, new ColouredCv(label, colour, description));
        }

        final List<Object[]> bioRoleLabels = listBioRoleLabels();
        //Collections.sort(expRoleLabels);

        for (Object[] bioRole : bioRoleLabels) {
            String colour = colourPalette.getNextGreen();

            String label = bioRole[1].toString();
            String description = bioRole[0].toString();

            bioRoleColourMap.put(label, new ColouredCv(label, colour, description));
        }
    }

    public Map<String, ColouredCv> getTypeColourMap() {
        return typeColourMap;
    }

    public Map<String, ColouredCv> getExpRoleColourMap() {
        return expRoleColourMap;
    }

    public Map<String, ColouredCv> getBioRoleColourMap() {
        return bioRoleColourMap;
    }

    private List<Object[]> listProteinTypeLabels() {
        Query query = entityManagerFactory.createEntityManager()
                .createQuery("select distinct i.cvInteractorType.shortLabel, i.cvInteractorType.identifier from InteractorImpl i");
        return query.getResultList();
    }

    private List<Object[]> listExpRoleLabels() {
        Query query = entityManagerFactory.createEntityManager()
                .createQuery("select distinct expRole.shortLabel, expRole.identifier from Component c inner join c.experimentalRoles as expRole");
        return query.getResultList();
    }

    private List<Object[]> listBioRoleLabels() {
        Query query = entityManagerFactory.createEntityManager()
                .createQuery("select distinct c.cvBiologicalRole.shortLabel, c.cvBiologicalRole.identifier from Component c");
        return query.getResultList();
    }

    public class ColouredCv {

        private String colourHex;
        private String text;
        private String initials;
        private String description;

        private ColouredCv(String text, String colourHex, String description) {
            this.text = text;
            this.colourHex = colourHex;
            this.description = description;

            if("prey".equals( description )){
                description = "PY";
            }

            this.initials = description.substring(0,2).toUpperCase();
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

        public String getDescription() {
            return description;
        }
    }

}
