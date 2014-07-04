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
import org.primefaces.model.DualListModel;
import org.primefaces.model.SelectableDataModelWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.AnnotatedObjectDao;
import uk.ac.ebi.intact.core.persistence.util.InstitutionMerger;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.editor.controller.curate.institution.InstitutionService;
import uk.ac.ebi.intact.editor.util.SelectableCollectionDataModel;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.OwnedAnnotatedObject;
import uk.ac.ebi.intact.model.user.User;

import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.model.DataModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "general" )
public class InstitutionAdminController extends JpaAwareController {

    @Autowired
    private InstitutionService institutionService;

    private List<Institution> institutions;

    private Institution[] selectedInstitutions;
    private Institution mergeDestinationInstitution;

    private DualListModel<User> usersDualListModel;
    private DataModel<Institution> institutionsDataModel;

    public InstitutionAdminController() {

    }

    public void load(ComponentSystemEvent event) {
        institutionService.refresh(null);
        institutions = institutionService.getAllInstitutions();

        List<User> usersAvailable = getDaoFactory().getUserDao().getAll();
        List<User> usersSelected = new ArrayList<User>();

        institutionsDataModel = new SelectableDataModelWrapper(new SelectableCollectionDataModel<Institution>(institutions), institutions);

        usersDualListModel = new DualListModel<User>(usersAvailable, usersSelected);

    }

    @Transactional(value = "transactionManager")
    public void mergeSelected(ActionEvent evt) {
        if (mergeDestinationInstitution == null) {
            addErrorMessage("Destination institution not selected", "Select one in the drop down list");
            return;
        }

        InstitutionMerger merger = new InstitutionMerger();
        int updated = merger.merge(selectedInstitutions, mergeDestinationInstitution, true);

        addInfoMessage(selectedInstitutions.length + " iInstitutions merged", updated + " annotated objects updated");

        institutionService.refresh(null);
    }

    @Transactional(value = "transactionManager")
    public void deleteSelected(ActionEvent evt) {
        for (Institution selectedInstitution : selectedInstitutions) {
            getDaoFactory().getInstitutionDao().deleteByAc(selectedInstitution.getAc());
        }
    }

    @Transactional(value = "transactionManager")
    public void fixAnnotatedObjectOwners(ActionEvent evt) {
        if (usersDualListModel.getTarget().isEmpty()) {
            addErrorMessage("No users selected", "Add some users to fix using the picklist");
            return;
        }

        IntactContext intactContext = IntactContext.getCurrentInstance();

        Map<String, AnnotatedObjectDao> annotatedObjectDaoMap = intactContext.getSpringContext().getBeansOfType(AnnotatedObjectDao.class);

        int updatedCount = 0;

        for (User userToFix : usersDualListModel.getTarget()) {
            Institution userInstitution = ((UserAdminController) getSpringContext().getBean("userAdminController")).getInstitution(userToFix);

            if (userInstitution != null) {

                for (AnnotatedObjectDao annotatedObjectDao : annotatedObjectDaoMap.values()) {
                    if (OwnedAnnotatedObject.class.isAssignableFrom(annotatedObjectDao.getEntityClass())) {
                        int replaced = annotatedObjectDao.replaceInstitution(userInstitution, userToFix.getLogin().toUpperCase());

                        updatedCount += replaced;
                    }
                }
            }
        }

        addInfoMessage("Users object ownership fixed", "Updated annotated objects: "+updatedCount);
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

    public Institution getMergeDestinationInstitution() {
        return mergeDestinationInstitution;
    }

    public void setMergeDestinationInstitution(Institution mergeDestinationInstitution) {
        this.mergeDestinationInstitution = mergeDestinationInstitution;
    }

    public DualListModel<User> getUsersDualListModel() {
        return usersDualListModel;
    }

    public void setUsersDualListModel(DualListModel<User> usersDualListModel) {
        this.usersDualListModel = usersDualListModel;
    }

    public DataModel<Institution> getInstitutionsDataModel() {
        return institutionsDataModel;
    }
}
