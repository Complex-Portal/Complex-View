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

import org.primefaces.model.LazyDataModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.editor.util.LazyDataModelFactory;
import uk.ac.ebi.intact.model.BioSource;

import javax.faces.event.ActionEvent;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "conversation.access" )
public class InputBioSourceController {

    @PersistenceContext( unitName = "intact-core-default" )
    private EntityManager entityManager;

    private LazyDataModel<BioSource> bioSources;

    private BioSource selectedBioSource;

    private String query;

    public InputBioSourceController() {
    }

    public void loadBioSources( ActionEvent evt ) {
        query = null;
        bioSources = LazyDataModelFactory.createLazyDataModel( entityManager, "select b from BioSource b order by b.shortLabel",
                                                               "select count(b) from BioSource b" );
    }

    public void search(ActionEvent evt) {
        if (query == null) {
            return;
        }
        
        Map<String,String> params = new HashMap<String,String>();
        params.put("shortLabel", query+"%");
        params.put("fullName", query+"%");
        params.put("taxId", query);

        String whereClause = "where lower(b.shortLabel) like lower(:shortLabel) or " +
                "lower(b.fullName) like lower(:fullName) or " +
                "b.taxId = :taxId";

        bioSources = LazyDataModelFactory.createLazyDataModel( entityManager, "select b from BioSource b " + whereClause +
                " order by b.shortLabel",
                "select count(b) from BioSource b " + whereClause, params );
    }

    public void selectBioSource( BioSource bioSource ) {
        setSelectedBioSource( bioSource );
    }

    public LazyDataModel<BioSource> getBioSources() {
        return bioSources;
    }

    public void setBioSources( LazyDataModel<BioSource> bioSources ) {
        this.bioSources = bioSources;
    }

    public BioSource getSelectedBioSource() {
        return selectedBioSource;
    }

    public void setSelectedBioSource( BioSource selectedBioSource ) {
        this.selectedBioSource = selectedBioSource;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
