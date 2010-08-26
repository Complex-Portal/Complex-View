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
package uk.ac.ebi.intact.editor.component.inputbiosource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.controller.curate.organism.BioSourceService;
import uk.ac.ebi.intact.model.BioSource;

import javax.faces.component.FacesComponent;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIInput;
import javax.faces.component.UINamingContainer;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@FacesComponent("uk.ac.ebi.intact.editor.InputBioSource")
public class InputBioSource extends UIInput implements NamingContainer, Serializable {

    private static final Log log = LogFactory.getLog(InputBioSource.class);
    
    public InputBioSource() {
        if (log.isTraceEnabled()) log.trace("InputBioSource.InputBioSource - New instance ["+hashCode()+"]");
    }

    @Override
    public String getFamily() {
      return UINamingContainer.COMPONENT_FAMILY;
   }

    public void loadBioSources( ActionEvent evt ) {
        if (log.isTraceEnabled()) log.trace("Load Biosources");
        setQuery(null);

        BioSourceService bioSourceService = (BioSourceService) IntactContext.getCurrentInstance().getSpringContext().getBean("bioSourceService");
        List<BioSource> bioSources = bioSourceService.getAllBioSources();

        setBioSources(bioSources);
    }

    public void autoSearch(AjaxBehaviorEvent evt) {
        if (getQuery().length() >= 2) {
            search(null);
        }
    }

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


        List<BioSource> bioSources = jpaQuery.getResultList();
        setBioSources(bioSources);
    }

    private EntityManager getEntityManager() {
        return IntactContext.getCurrentInstance().getDaoFactory().getEntityManager();
    }

    public void selectBioSource( BioSource bioSource ) {
        setSelectedBioSource( bioSource );
    }

    public List<BioSource> getBioSources() {
        final List<BioSource> bioSources = (List<BioSource>) getStateHelper().eval("bioSources");

        return bioSources;
    }

    public void setBioSources( List<BioSource> bioSources ) {
        if (bioSources != null) {
            getStateHelper().put("bioSources", bioSources);
        }
    }

    public BioSource getSelectedBioSource() {
        return (BioSource) getStateHelper().eval("selectedBioSource");
    }

    public void setSelectedBioSource( BioSource selectedBioSource ) {
        getStateHelper().put("selectedBioSource", selectedBioSource);
    }

    public String getQuery() {
        return (String) getStateHelper().eval("query");
    }

    public void setQuery(String query) {
        getStateHelper().put("query", query);
    }  
}