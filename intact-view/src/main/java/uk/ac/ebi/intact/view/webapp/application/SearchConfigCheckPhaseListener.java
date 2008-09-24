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
package uk.ac.ebi.intact.view.webapp.application;

import uk.ac.ebi.intact.view.webapp.controller.SearchWebappException;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;
import uk.ac.ebi.intact.view.webapp.util.WebappUtils;

import javax.faces.context.ExternalContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class SearchConfigCheckPhaseListener implements PhaseListener {

    private static final String FIRST_TIME_CONFIG_XHTML = "first_time_config.xhtml";

    public void afterPhase(PhaseEvent event) {
        // nothing
    }

    public void beforePhase(PhaseEvent event) {
        final ExternalContext externalContext = event.getFacesContext().getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();

        if (request.getRequestURL().toString().endsWith(FIRST_TIME_CONFIG_XHTML)) {
            return;
        }

        IntactViewConfiguration intactViewConfiguration = WebappUtils.getIntactViewConfiguration(event.getFacesContext());

        final String configFile = intactViewConfiguration.getConfigFile();

        if (configFile == null || !new File(configFile).exists()) {
            HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();

            String absoluteContextPath = request.getScheme() + "://" +
                                         request.getServerName() + ":" +
                                         request.getServerPort() +
                                         request.getContextPath();

            try {
                response.sendRedirect(absoluteContextPath + "/" + FIRST_TIME_CONFIG_XHTML);
            } catch (IOException e) {
                throw new SearchWebappException("Cannot redirect to fist time config", e);
            }

            event.getFacesContext().responseComplete();
        }

    }

    public PhaseId getPhaseId() {
        return PhaseId.RESTORE_VIEW;
    }
}
