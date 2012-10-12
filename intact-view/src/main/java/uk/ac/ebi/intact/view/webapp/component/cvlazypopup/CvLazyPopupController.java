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
package uk.ac.ebi.intact.view.webapp.component.cvlazypopup;

import org.primefaces.event.CloseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.ac.ebi.intact.model.CvObject;
import uk.ac.ebi.intact.view.webapp.controller.application.CvObjectService;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Component
@Scope("request")
public class CvLazyPopupController {

    private boolean dialogRendered;

    @Autowired
    private CvObjectService cvObjectService;

    public CvLazyPopupController() {
    }

    public CvObject fetch(String className, String identifier) {
       return cvObjectService.loadByIdentifier(className, identifier);
    }

    public void closeDialog(CloseEvent evt) {
        dialogRendered = false;
    }

    public boolean isDialogRendered() {
        return dialogRendered;
    }

    public void setDialogRendered(boolean dialogRendered) {
        this.dialogRendered = dialogRendered;
    }
}
