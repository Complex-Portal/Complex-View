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
package uk.ac.ebi.intact.editor.controller.admin;

import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.editor.controller.curate.institution.InstitutionService;
import uk.ac.ebi.intact.model.Institution;

import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;
import java.util.List;

/** *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "admin" )
public class InstitutionAdminController extends JpaAwareController {

    @Autowired
    private InstitutionService institutionService;

    private List<Institution> institutions;

    private Institution[] selectedInstitutions;

    public InstitutionAdminController() {

    }

    public void load(ComponentSystemEvent event) {
         institutions = institutionService.getAllInstitutions();
    }


    public void mergeSelected(ActionEvent evt) {
        addWarningMessage("This is not yet implemented. Sorry!", selectedInstitutions.length+" selected");
    }

    public List<Institution> getInstitutions() {
        return institutions;
    }

    public Institution[] getSelectedInstitutions() {
        return selectedInstitutions;
    }

    public void setSelectedInstitutions(Institution[] selectedInstitutions) {
        this.selectedInstitutions = selectedInstitutions;
    }
}
