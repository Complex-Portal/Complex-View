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
package uk.ac.ebi.intact.editor.application;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.util.DebugUtil;
import uk.ac.ebi.intact.editor.controller.curate.AnnotatedObjectController;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class HistoryPhaseListener implements PhaseListener {

    private static final Log log = LogFactory.getLog( HistoryPhaseListener.class );

    @Override
    public void afterPhase(PhaseEvent event) {
        if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
            final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            final HttpServletRequest req = (HttpServletRequest) externalContext.getRequest();
            final String requestUrl = req.getRequestURL().toString();
            if (log.isDebugEnabled()) log.debug("\t"+requestUrl);
        }

        Collection<AnnotatedObjectController> aocs = IntactContext.getCurrentInstance().getSpringContext()
                .getBeansOfType(AnnotatedObjectController.class).values();

        for (AnnotatedObjectController aoc : aocs) {
            String ao = aoc.getAnnotatedObject() != null? DebugUtil.annotatedObjectToString(aoc.getAnnotatedObject(), false) : null;
            log.debug("\t\t"+aoc.getClass().getSimpleName()+" - "+ao);
        }
    }

    @Override
    public void beforePhase(PhaseEvent event) {
        // nothing
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RENDER_RESPONSE;
    }

}
