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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.editor.controller.BaseController;
import uk.ac.ebi.intact.editor.controller.curate.organism.EditorOrganismService;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.model.extension.IntactOrganism;

import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 */
@Component
@Scope("conversation.access")
public class InputOrganismController extends BaseController {

    private static final Log log = LogFactory.getLog(InputOrganismController.class);

    private String query;
    private IntactOrganism selected;
    private List<IntactOrganism> bioSources;
    private String dialogId;

    public InputOrganismController() {
    }

    public void loadBioSources( ComponentSystemEvent evt) {
        if (log.isTraceEnabled()) log.trace("Load Biosources");
//        setQuery(null);
//
        if (query == null) {
            EditorOrganismService dao = ApplicationContextProvider.getBean("editorOrganismService");
            Collection<IntactOrganism> bioSources = dao.getAllOrganisms();

            setBioSources(new ArrayList<IntactOrganism>(bioSources));
        }
    }

    public void autoSearch(AjaxBehaviorEvent evt) {
        if (getQuery().length() >= 2) {
            search(null);
        }
    }

    @SuppressWarnings({"JpaQlInspection", "unchecked"})
    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void search(ActionEvent evt) {
        String query = getQuery();

        if (log.isTraceEnabled()) log.trace("Searching with query: "+query);

        if (query == null) {
            return;
        }

        EntityManager manager = ApplicationContextProvider.getBean("jamiEntityManager");

        Query jpaQuery = manager
                .createQuery("select b from IntactOrganism b " +
                        "where lower(b.commonName) like lower(:commonName) or " +
                        "lower(b.scientificName) like lower(:scientificName) or " +
                        "b.dbTaxid = :taxId");
        jpaQuery.setParameter("commonName", query+"%");
        jpaQuery.setParameter("scientificName", query+"%");
        jpaQuery.setParameter("taxId", query);


        this.bioSources = jpaQuery.getResultList();
    }

    public void selectBioSource( IntactOrganism bioSource ) {
        setSelectedBioSource( bioSource );
    }

    public List<IntactOrganism> getBioSources() {
        return bioSources;
    }

    public void setBioSources( List<IntactOrganism> bioSources ) {
        this.bioSources = bioSources;
    }

    public IntactOrganism getSelectedBioSource() {
        return selected;
    }

    public void setSelectedBioSource( IntactOrganism selectedBioSource ) {
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