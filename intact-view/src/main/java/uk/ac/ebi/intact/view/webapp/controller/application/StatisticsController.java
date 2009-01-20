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
package uk.ac.ebi.intact.view.webapp.controller.application;

import uk.ac.ebi.intact.view.webapp.controller.JpaBaseController;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;
import uk.ac.ebi.intact.view.webapp.util.WebappUtils;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Container for the application statistics (e.g. database counts)
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
public class StatisticsController extends JpaBaseController {

    private static final Log log = LogFactory.getLog( StatisticsController.class );

    @Autowired
    private IntactViewConfiguration intactViewConfiguration;

    private int binaryInteractionCount;
    private int proteinCount;
    private int experimentCount;
    private int cvTermsCount;

    public StatisticsController() {

    }

    @PostConstruct
    public void calculateStats() throws IOException {
        if (log.isInfoEnabled()) log.info("Calculating statistics");

        // index stats
        binaryInteractionCount = WebappUtils.countItemsInIndex(intactViewConfiguration.getDefaultIndexLocation());

        // database stats
        DaoFactory daoFactory = getDaoFactory();

        proteinCount = daoFactory.getProteinDao().countAll();
        experimentCount = daoFactory.getExperimentDao().countAll();
        cvTermsCount = daoFactory.getCvObjectDao().countAll();
    }

    public int getCvTermsCount() {
        return cvTermsCount;
    }

    public int getBinaryInteractionCount() {
        return binaryInteractionCount;
    }

    public int getProteinCount() {
        return proteinCount;
    }

    public int getExperimentCount() {
        return experimentCount;
    }
}
