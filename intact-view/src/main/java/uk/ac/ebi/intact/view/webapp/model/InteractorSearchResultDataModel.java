/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.view.webapp.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.trinidad.model.SortableModel;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import psidev.psi.mi.search.engine.SearchEngineException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrSearcher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.InteractorIdCount;
import uk.ac.ebi.intact.model.Interactor;

import javax.faces.model.DataModelEvent;
import javax.faces.model.DataModelListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InteractorSearchResultDataModel extends SortableModel implements Serializable {

    private static final Log log = LogFactory.getLog(SolrSearchResultDataModel.class);

    private static String DEFAULT_SORT_COLUMN = "relevancescore";

    private SolrQuery solrQuery;
    private SolrServer solrServer;

    private String interactorTypeMi;

    private List<InteractorIdCount> idCounts;

    private int rowIndex = -1;
    private int firstResult = 0;

    private String sortColumn = DEFAULT_SORT_COLUMN;
    private SolrQuery.ORDER sortOrder = SolrQuery.ORDER.asc;


    public InteractorSearchResultDataModel(SolrServer solrServer, SolrQuery solrQuery, String interactorTypeMi)  {
        this.solrServer = solrServer;
        this.solrQuery = solrQuery;
        this.interactorTypeMi = interactorTypeMi;

        setRowIndex(0);
        fetchResults();

        setWrappedData(idCounts);
    }

    protected void fetchResults() throws SearchEngineException {
        if (solrQuery == null) {
            throw new IllegalStateException("Trying to fetch results for a null SolrQuery");
        }

        if (log.isDebugEnabled()) log.debug("Fetching interactor for query: "+solrQuery);

        

        IntactSolrSearcher searcher = new IntactSolrSearcher(solrServer);
        idCounts = new ArrayList<InteractorIdCount>(searcher.searchInteractors(solrQuery, interactorTypeMi));
    }

    public int getRowCount() {
        return idCounts.size();
    }

    public Object getRowData() {
        if (idCounts == null) {
            return null;
        }

        if (!isRowAvailable()) {
            throw new IllegalArgumentException("row is unavailable");
        }

        InteractorIdCount idCount = idCounts.get(getRowIndex());

        Interactor interactor = IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                .getInteractorDao().getByAc(idCount.getAc());

        return new InteractorWrapper(interactor, idCount.getCount());
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public Object getWrappedData() {
        return idCounts;
    }

    public boolean isRowAvailable() {
        if (idCounts == null) {
            return false;
        }

        return rowIndex >= 0 && rowIndex < getRowCount();
    }

    public void setRowIndex(int rowIndex) {
        if (rowIndex < -1) {
            throw new IllegalArgumentException("illegal rowIndex " + rowIndex);
        }
        int oldRowIndex = rowIndex;
        this.rowIndex = rowIndex;
        if (idCounts != null && oldRowIndex != this.rowIndex) {
            Object data = isRowAvailable() ? getRowData() : null;
            DataModelEvent event = new DataModelEvent(this, this.rowIndex, data);
            DataModelListener[] listeners = getDataModelListeners();
            for (int i = 0; i < listeners.length; i++) {
                listeners[i].rowSelected(event);
            }
        }
    }

    @Override
    public Object getRowKey() {
        return isRowAvailable()
               ? getRowIndex()
               : null;
    }

    @Override
    public void setRowKey(Object key) {
        if (key == null) {
            setRowIndex(-1);
        } else {
            setRowIndex((Integer) key);
        }
    }


    public List<InteractorIdCount> getInteractorIdCounts() {
        return idCounts;
    }

    public SolrQuery getSearchQuery() {
        return solrQuery;
    }

    public String getSortColumn() {
        return sortColumn;
    }

    public void setSortColumn(String sortColumn) {
        this.sortColumn = sortColumn;
    }

    public int getFirstResult() {
        return firstResult;
    }

    public boolean isAscending() {
        return (sortOrder == SolrQuery.ORDER.asc);
    }
}