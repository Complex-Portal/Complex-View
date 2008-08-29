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
package uk.ac.ebi.intact.view.webapp.controller.moleculeview;

import org.springframework.stereotype.Controller;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.myfaces.orchestra.viewController.annotations.ViewController;
import org.apache.myfaces.orchestra.viewController.annotations.PreRenderView;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.view.webapp.controller.JpaBaseController;
import uk.ac.ebi.intact.view.webapp.controller.search.SearchController;

import javax.faces.context.FacesContext;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller("moleculeViewBean")
@Scope("conversation.access")
@ConversationName("general")
public class MoleculeViewController extends JpaBaseController{

    private Interactor interactor;


    public MoleculeViewController() {

    }

    public void setInteractorAc(String ac) {
        interactor = getDaoFactory().getInteractorDao().getByAc(ac);
    }

    public Interactor getInteractor() {
        return interactor;
    }

    public void setInteractor(Interactor interactor) {
        this.interactor = interactor;
    }
}
