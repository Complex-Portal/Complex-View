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
import org.apache.myfaces.trinidad.model.SortCriterion;
import org.apache.myfaces.trinidad.model.SortableModel;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrSearcher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.SolrSearchResult;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;

import javax.faces.model.DataModelEvent;
import javax.faces.model.DataModelListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class SolrSearchResultDataModel extends SortableModel implements Serializable {

    private static final Log log = LogFactory.getLog(SolrSearchResultDataModel.class);

    private static String DEFAULT_SORT_COLUMN = "relevancescore";

    private SolrQuery solrQuery;
    private SolrServer solrServer;

    private SolrSearchResult result;
    private int rowIndex = -1;
    private int firstResult = 0;

    private String sortColumn = DEFAULT_SORT_COLUMN;
    private SolrQuery.ORDER sortOrder = SolrQuery.ORDER.asc;

    private Map<String,SolrQuery.ORDER> columnSorts;


    public SolrSearchResultDataModel(SolrServer solrServer, SolrQuery solrQuery) {
        if (solrQuery == null) {
            throw new IllegalArgumentException("Trying to create data model with a null SolrQuery");
        }

        this.solrServer = solrServer;
        this.solrQuery = solrQuery.getCopy();

        columnSorts = new HashMap<String, SolrQuery.ORDER>(16);

        setRowIndex(0);
        fetchResults();

        setWrappedData(result);
    }

    protected void fetchResults() {
        if (solrQuery == null) {
            throw new IllegalStateException("Trying to fetch results for a null SolrQuery");
        }

        solrQuery.setStart(firstResult);

        for (Map.Entry<String,SolrQuery.ORDER> colSortEntry : columnSorts.entrySet()) {
            solrQuery.addSortField(colSortEntry.getKey(), colSortEntry.getValue());
        }

        if (log.isDebugEnabled()) log.debug("Fetching results: "+solrQuery);

        IntactSolrSearcher searcher = new IntactSolrSearcher(solrServer);
        result = searcher.search(solrQuery);
    }

    public int getRowCount() {
        return Long.valueOf(result.getTotalCount()).intValue();
    }

    public Object getRowData() {
        if (result == null) {
            return null;
        }

        if (!isRowWithinResultRange()) {
            firstResult = getRowIndex();
            fetchResults();
        }

        if (!isRowAvailable()) {
            throw new IllegalArgumentException("row is unavailable");
        }

        List<IntactBinaryInteraction> interactions = new ArrayList<IntactBinaryInteraction>(result.getBinaryInteractionList());

        final IntactBinaryInteraction binaryInteraction = interactions.get(rowIndex - solrQuery.getStart());
        return binaryInteraction;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public Object getWrappedData() {
        return result;
    }

    public boolean isRowAvailable() {
        if (result == null) {
            return false;
        }

        return rowIndex >= 0 && rowIndex < result.getTotalCount();
    }

    protected boolean isRowWithinResultRange() {
        return (getRowIndex() >= firstResult) && (getRowIndex() < (firstResult+solrQuery.getRows()));
    }

    public void setRowIndex(int rowIndex) {
        if (rowIndex < -1) {
            throw new IllegalArgumentException("illegal rowIndex " + rowIndex);
        }
        int oldRowIndex = rowIndex;
        this.rowIndex = rowIndex;
        if (result != null && oldRowIndex != this.rowIndex) {
            Object data = isRowAvailable() ? getRowData() : null;
            DataModelEvent event = new DataModelEvent(this, this.rowIndex, data);
            DataModelListener[] listeners = getDataModelListeners();
            for (int i = 0; i < listeners.length; i++) {
                listeners[i].rowSelected(event);
            }
        }
    }

    @Override
    public void setSortCriteria(List<SortCriterion> criteria) {
        if ((criteria == null) || (criteria.isEmpty())) {
            this.sortColumn = DEFAULT_SORT_COLUMN;
            columnSorts.clear();
        }
        else {
            // only use the first criterion
            SortCriterion criterion = criteria.get(0);

            this.sortColumn = criterion.getProperty();

            if (columnSorts.containsKey(sortColumn)) {
                SolrQuery.ORDER currentOrder = columnSorts.get(sortColumn);
                sortOrder = currentOrder.reverse();
            } else {
                sortOrder = SolrQuery.ORDER.asc;
            }
            columnSorts.put(sortColumn, sortOrder);

            if (log.isDebugEnabled())
                log.debug("\tSorting by '" + criterion.getProperty() + "' " + (criterion.isAscending() ? "ASC" : "DESC"));
        }

        fetchResults();
    }

    /**
     * Checks to see if the underlying collection is sortable by the given property.
     *
     * @param property The name of the property to sort the underlying collection by.
     * @return true, if the property implements java.lang.Comparable
     */
    @Override
    public boolean isSortable(String property) {
        return true;
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


    public SolrSearchResult getResult() {
        return result;
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
