/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.binarysearch.webapp.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.Sort;
import psidev.psi.mi.search.SearchResult;
import psidev.psi.mi.search.Searcher;
import psidev.psi.mi.search.engine.SearchEngineException;
import psidev.psi.mi.tab.PsimiTabColumn;

import javax.faces.model.DataModel;
import javax.faces.model.DataModelEvent;
import javax.faces.model.DataModelListener;
import java.io.Serializable;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class SearchResultDataModel extends DataModel implements Serializable {

    private static final Log log = LogFactory.getLog(SearchResultDataModel.class);

    private static final String DEFAULT_SORT_COLUMN = PsimiTabColumn.ID_A.getSortableColumnName();

    private String searchQuery;
    private String indexDirectory;

    private SearchResult result;
    private int rowIndex = -1;
    private int pageSize;

    private String sortColumn = DEFAULT_SORT_COLUMN;
    private boolean ascending = true;

    private long elapsedTimeMillis = -1;

    public SearchResultDataModel(String searchQuery, String indexDirectory, int pageSize, String sortColumn, boolean ascending) throws TooManyResults {
        this.searchQuery = searchQuery;
        this.indexDirectory = indexDirectory;
        this.pageSize = pageSize;
        this.sortColumn = sortColumn;
        this.ascending = ascending;

        try {
            setRowIndex(0);
            fetchResults(rowIndex, pageSize);
        }
        catch (SearchEngineException e) {
            throw new TooManyResults(e);
        }
    }

    public void fetchResults(Integer firstResult, Integer maxResults) throws SearchEngineException {

        Sort sort = new Sort(sortColumn, !ascending);

        long startTime = System.currentTimeMillis();

        this.result = Searcher.search(searchQuery, indexDirectory, firstResult, maxResults, sort);

        elapsedTimeMillis = System.currentTimeMillis() - startTime;
    }

    public boolean indexOutsideCurrentResults() {
        return rowIndex < result.getFirstResult() || rowIndex >= (result.getFirstResult() + result.getMaxResults());
    }

    public int getRowCount() {
        return result.getTotalCount();
    }

    public Object getRowData() {
        if (result == null) {
            return null;
        }

        if (indexOutsideCurrentResults()) {
            fetchResults(rowIndex, pageSize);
        }

        if (!isRowAvailable()) {
            throw new IllegalArgumentException("row is unavailable");
        }

        return result.getInteractions().get(rowIndex - result.getFirstResult());
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

    public void setWrappedData(Object data) {
        throw new UnsupportedOperationException("setWrappedData");
    }

    public SearchResult getResult() {
        return result;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public String getSortColumn() {
        return sortColumn;
    }

    public void setSortColumn(String sortColumn) {
        this.sortColumn = sortColumn;
    }

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    public long getElapsedTimeMillis() {
        return elapsedTimeMillis;
    }

    public double getElapsedTimeSecs() {
        return (double)elapsedTimeMillis/1000;
    }

    private Object key;

    public Object getRowKey()
    {
        return key;
    }

    public void setRowKey(Object key)
    {
       this.key = key;
    }
}