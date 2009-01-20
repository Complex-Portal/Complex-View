/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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

import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope("session")
public class UserSessionConfig {

    @Autowired
    private IntactViewConfiguration intactViewConfiguration;

    @Autowired
    private AppConfigBean appConfigBean;

    public UserSessionConfig() {
    }

    public IntactViewConfiguration getIntactViewConfiguration() {
        return intactViewConfiguration;
    }

    public AppConfigBean getAppConfigBean() {
        return appConfigBean;
    }
}
