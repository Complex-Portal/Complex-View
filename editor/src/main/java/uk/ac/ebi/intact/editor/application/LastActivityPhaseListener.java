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
package uk.ac.ebi.intact.editor.application;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ConfigurableApplicationContext;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.controller.UserSessionController;
import uk.ac.ebi.intact.model.user.User;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

/**
 * Records the last activity for a user
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class LastActivityPhaseListener implements PhaseListener {

    private static final Log log = LogFactory.getLog(LastActivityPhaseListener.class);

    @Override
    public void afterPhase(PhaseEvent event) {

    }

    @Override
    public void beforePhase(PhaseEvent event) {
        final ConfigurableApplicationContext springContext = IntactContext.getCurrentInstance().getSpringContext();
        UserSessionController userSessionController = (UserSessionController) springContext.getBean("userSessionController");

        User user = userSessionController.getCurrentUser();

        if (user != null) {
            if (log.isDebugEnabled()) log.debug("Notifying last activity for user: "+user.getLogin());

            userSessionController.notifyLastActivity();
        }
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.APPLY_REQUEST_VALUES;
    }
}
