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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.jami.model.extension.IntactComplex;
import uk.ac.ebi.intact.model.Publication;

import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "session" )
public class DashboardController extends JpaAwareController {

    @Autowired
    private DashboardQueryService queryService;

    public DashboardController() {
    }

    @SuppressWarnings("unchecked")
    public void loadData( ComponentSystemEvent event ) {
        if (!FacesContext.getCurrentInstance().isPostback()) {
            queryService.loadData();
            queryService.loadJamiData();
        }
    }

    public void refreshAllTables() {
        queryService.refreshTables();
        queryService.refreshJamiTables();
    }

    public LazyDataModel<Publication> getAllPublications() {
        return queryService.getAllPublications();
    }

    public LazyDataModel<Publication> getOwnedByUser() {
        return queryService.getOwnedByUser();
    }

    public LazyDataModel<Publication> getReviewedByUser() {
        return queryService.getReviewedByUser();
    }

    public boolean isHideAcceptedAndReleased() {
        return queryService.isHideAcceptedAndReleased();
    }

    public void setHideAcceptedAndReleased(boolean hideAcceptedAndReleased) {
        queryService.setHideAcceptedAndReleased(hideAcceptedAndReleased);
    }

    public String[] getStatusToShow() {
        return queryService.getStatusToShow();
    }

    public void setStatusToShow(String[] statusToShow) {
        queryService.setStatusToShow(statusToShow);
    }

    public LazyDataModel<IntactComplex> getAllComplexes() {
        return queryService.getAllComplexes();
    }

    public LazyDataModel<IntactComplex> getComplexesOwnedByUser() {
        return queryService.getComplexesOwnedByUser();
    }

    public LazyDataModel<IntactComplex> getComplexesReviewedByUser() {
        return queryService.getComplexesReviewedByUser();
    }

    public boolean isPublicationTableEnabled() {
        return queryService.isPublicationTableEnabled();
    }

    public boolean isComplexTableEnabled() {
        return queryService.isComplexTableEnabled();
    }
}
