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
package uk.ac.ebi.intact.editor.controller.dashboard;

import org.primefaces.model.LazyDataModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.editor.controller.UserSessionController;
import uk.ac.ebi.intact.editor.util.LazyDataModelFactory;
import uk.ac.ebi.intact.model.Publication;

import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "session" )
public class DashboardController extends JpaAwareController {

    private LazyDataModel<Publication> allPublications;
    private LazyDataModel<Publication> createdByUser;
    private LazyDataModel<Publication> updatedByUser;

    public DashboardController() {
    }

    @SuppressWarnings("unchecked")
    public void loadData( ComponentSystemEvent event ) {
        UserSessionController userSessionController = (UserSessionController) getSpringContext().getBean("userSessionController");
        final String userId = userSessionController.getCurrentUser().getLogin().toUpperCase();

        allPublications = LazyDataModelFactory.createLazyDataModel( getCoreEntityManager(),
                                                                    "select p from Publication p", "p", "updated", false);
        
        createdByUser = LazyDataModelFactory.createLazyDataModel( getCoreEntityManager(),
                                                                    "select p from Publication p where p.creator = '"+userId+"'", "p", "updated", false);

        updatedByUser = LazyDataModelFactory.createLazyDataModel( getCoreEntityManager(),
                                                                    "select p from Publication p where p.updator = '"+userId+"'", "p", "updated", false);
    }

    public void firePanicException(ActionEvent evt) {
        throw new IllegalStateException("Relaaax, don't panic!");
    }

    public LazyDataModel<Publication> getAllPublications() {
        return allPublications;
    }

    public LazyDataModel<Publication> getCreatedByUser() {
        return createdByUser;
    }

    public LazyDataModel<Publication> getUpdatedByUser() {
        return updatedByUser;
    }
}
