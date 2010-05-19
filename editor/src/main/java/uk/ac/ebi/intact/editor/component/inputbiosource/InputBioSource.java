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
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.LazyDataModel;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.util.LazyDataModelFactory;
import uk.ac.ebi.intact.model.BioSource;

import javax.faces.component.FacesComponent;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIInput;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@FacesComponent("uk.ac.ebi.intact.editor.InputBioSource")
public class InputBioSource extends UIInput implements NamingContainer {

    private static final Log log = LogFactory.getLog(InputBioSource.class);
    
    public InputBioSource() {
        if (log.isTraceEnabled()) log.trace("InputBioSource.InputBioSource");
    }

    @Override
    public String getFamily() {
      return UINamingContainer.COMPONENT_FAMILY;
   }

    @Override
    public void decode(FacesContext context) {
        if (log.isTraceEnabled()) log.trace("InputBioSource.decode");
        super.decode(context);
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        if (log.isTraceEnabled()) log.trace("InputBioSource.encodeBegin");
        super.encodeBegin(context);
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        if (log.isTraceEnabled()) log.trace("InputBioSource.encodeEnd");
        super.encodeEnd(context);
    }



    public void loadBioSources( ActionEvent evt ) {
        if (log.isTraceEnabled()) log.trace("LOAD BIOSOURCES");
        setQuery(null);
        LazyDataModel<BioSource> bioSources = LazyDataModelFactory.createLazyDataModel( getEntityManager(), "select b from BioSource b order by b.shortLabel",
                                                               "select count(b) from BioSource b" );

        setBioSources(bioSources);
    }

    public void search(ActionEvent evt) {
        String query = getQuery();

        if (log.isTraceEnabled()) log.trace("QUERY: "+query);

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

        LazyDataModel<BioSource> bioSources = LazyDataModelFactory.createLazyDataModel( getEntityManager(), "select b from BioSource b " + whereClause +
                " order by b.shortLabel",
                "select count(b) from BioSource b " + whereClause, params );
        setBioSources(bioSources);
    }

    private EntityManager getEntityManager() {
        return IntactContext.getCurrentInstance().getDaoFactory().getEntityManager();
    }

    public void selectBioSource( BioSource bioSource ) {
        setSelectedBioSource( bioSource );
    }

    public LazyDataModel<BioSource> getBioSources() {
        if (log.isTraceEnabled()) log.trace("InputBioSource.getBioSources ["+hashCode()+"]");

        final LazyDataModel<BioSource> bioSources = (LazyDataModel<BioSource>) getStateHelper().eval("biosources");

        if (log.isTraceEnabled()) log.trace("\tBIOSOURCES: "+((bioSources == null)? null : bioSources.getRowCount()));
        refreshTable(bioSources);

        return bioSources;
    }

    private void refreshTable(LazyDataModel<BioSource> bioSources) {
       if (bioSources != null) {
            DataTable dt = (DataTable) findComponent(getClientId()+":bioSourceTable");

            if (dt != null) {
                int rowIndex = dt.getRowIndex();
                if (log.isTraceEnabled()) log.trace("\tIndex: "+rowIndex);
                bioSources.setRowIndex(rowIndex);
                dt.setValue(bioSources);
            }
        }
    }

    public void setBioSources( LazyDataModel<BioSource> bioSources ) {
        if (log.isTraceEnabled()) log.trace("InputBioSource.setBioSources: "+((bioSources == null)? null : bioSources.getRowCount()));

        if (bioSources != null) {
            getStateHelper().put("biosources", bioSources);
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