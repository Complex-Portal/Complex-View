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

import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.myfaces.trinidad.context.RequestContext;
import org.apache.myfaces.trinidad.model.TreeModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.view.webapp.controller.BaseController;
import uk.ac.ebi.intact.view.webapp.controller.browse.OntologyBrowserController;

import javax.faces.event.ActionEvent;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope("conversation.access")
@ConversationName("general")
public class SearchBrowseDialogController extends BaseController {

    private OntologyBrowserController ontologyBrowserController;

    public void doSelect(ActionEvent e) {
        Object returnValue = getParameterValue("termId");

        if (returnValue != null) {
            RequestContext.getCurrentInstance().returnFromDialog(returnValue, null);
        }
    }

    public void setBeanName(String beanName) {
        ontologyBrowserController = (OntologyBrowserController) getBean(beanName);
    }

    public TreeModel getTreeModel() {
        return ontologyBrowserController.getOntologyTreeModel();
    }

    public OntologyBrowserController getOntologyBrowserController() {
        return ontologyBrowserController;
    }


}
