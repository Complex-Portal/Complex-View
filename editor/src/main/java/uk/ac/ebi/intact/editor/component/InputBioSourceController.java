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
package uk.ac.ebi.intact.editor.component;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.controller.BaseController;
import uk.ac.ebi.intact.editor.controller.curate.organism.BioSourceService;
import uk.ac.ebi.intact.model.BioSource;

import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.persistence.Query;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Component
@Scope("conversation.access")
public class InputBioSourceController extends BaseController {

    private static final Log log = LogFactory.getLog(InputBioSourceController.class);

    private String query;
    private BioSource selected;
    private List<BioSource> bioSources;
    private String dialogId;

    public InputBioSourceController() {
    }


    public void loadBioSources( ComponentSystemEvent evt) {
        if (log.isTraceEnabled()) log.trace("Load Biosources");
//        setQuery(null);
//
        if (query == null) {
            BioSourceService bioSourceService = (BioSourceService) IntactContext.getCurrentInstance().getSpringContext().getBean("bioSourceService");
            List<BioSource> bioSources = bioSourceService.getAllBioSources();

            setBioSources(bioSources);
        }
    }

    public void autoSearch(AjaxBehaviorEvent evt) {
        if (getQuery().length() >= 2) {
            search(null);
        }
    }

    @SuppressWarnings({"JpaQlInspection", "unchecked"})
    public void search(ActionEvent evt) {
        String query = getQuery();

        if (log.isTraceEnabled()) log.trace("Searching with query: "+query);

        if (query == null) {
            return;
        }

        Query jpaQuery = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager()
                .createQuery("select b from BioSource b " +
                        "where lower(b.shortLabel) like lower(:shortLabel) or " +
                        "lower(b.fullName) like lower(:fullName) or " +
                        "b.taxId = :taxId");
        jpaQuery.setParameter("shortLabel", query+"%");
        jpaQuery.setParameter("fullName", query+"%");
        jpaQuery.setParameter("taxId", query);


        this.bioSources = jpaQuery.getResultList();
    }

    public void selectBioSource( BioSource bioSource ) {
        setSelectedBioSource( bioSource );
    }

    public List<BioSource> getBioSources() {
        return bioSources;
    }

    public void setBioSources( List<BioSource> bioSources ) {
        this.bioSources = bioSources;
    }

    public BioSource getSelectedBioSource() {
        return selected;
    }

    public void setSelectedBioSource( BioSource selectedBioSource ) {
        this.selected = selectedBioSource;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getDialogId() {
        return dialogId;
    }

    public void setDialogId(String dialogId) {
        this.dialogId = dialogId;
    }
}