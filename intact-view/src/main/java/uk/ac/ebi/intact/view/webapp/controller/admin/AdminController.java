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
package uk.ac.ebi.intact.view.webapp.controller.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.view.webapp.controller.application.AppConfigBean;
import uk.ac.ebi.intact.view.webapp.controller.application.CvObjectService;
import uk.ac.ebi.intact.view.webapp.controller.application.StatisticsController;
import uk.ac.ebi.intact.view.webapp.controller.search.FilterPopulatorController;

import java.io.IOException;

/**
 * Allows to do some actions to administrate the application.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
public class AdminController {

    private static final Log log = LogFactory.getLog( AdminController.class );

    @Autowired
    private AppConfigBean appConfigBean;

    @Autowired
    private FilterPopulatorController filterPopulatorController;

    @Autowired
    private StatisticsController statisticsController;

    @Autowired
    private CvObjectService cvObjectService;

    public AdminController() {

    }

    public void reload() throws IOException {
        if (log.isInfoEnabled()) {
            log.info("Reloading application lists and filters");
        }

        filterPopulatorController.loadFilters();
        statisticsController.calculateStats();
        cvObjectService.clear();
    }

}
