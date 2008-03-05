package uk.ac.ebi.intact.services.search.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.trinidad.model.SortCriterion;
import org.apache.myfaces.trinidad.model.SortableModel;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.persistence.svc.impl.SimpleSearchService;

import javax.faces.model.DataModelEvent;
import javax.faces.model.DataModelListener;
import java.io.Serializable;
import java.util.*;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class SearchResultDataModel extends SortableModel implements Serializable {

    private static final Log log = LogFactory.getLog(SearchResultDataModel.class);

    private static final int DEFAULT_MAX_RESULTS = 25;

    private Class<? extends Searchable>[] searchableClasses;
    private String searchQuery;
    private List results;
    private int rowIndex = -1;
    private int firstResult = 0;
    private int maxResults = DEFAULT_MAX_RESULTS;
    private int rowCount = 0;

    private SimpleSearchService searchService;

    public SearchResultDataModel(Class<? extends Searchable>[] searchableClasses, String searchQuery) {
        this.searchableClasses = searchableClasses;
        this.searchQuery = searchQuery;
        this.searchService = new SimpleSearchService();

        Map<Class<? extends Searchable>,Integer> map = searchService.count(searchableClasses, searchQuery);

        for (Integer count : map.values()) {
            this.rowCount += count;
        }

        fetchResults();
        setWrappedData(results);
    }

    protected void fetchResults() {
        if (log.isDebugEnabled()) log.debug("Fetching results: "+searchQuery+" on "+ Arrays.asList(searchableClasses));

        List<SortCriterion> sortCriteria = super.getSortCriteria();

        if (!sortCriteria.isEmpty()) {
            // only use the first criterion
            SortCriterion criterion = sortCriteria.get(0);

            searchService.setSortProperty(criterion.getProperty());
            searchService.setSortAsc(criterion.isAscending());

            if (log.isDebugEnabled()) log.debug("\tSorting by '"+criterion.getProperty()+"' "+(criterion.isAscending()? "ASC" : "DESC"));
        }

        Collection<?> searchResults = searchService.search(searchableClasses, searchQuery, firstResult, maxResults);
        results = new ArrayList(searchResults.size());

        // wrap if necessary
        if (Interaction.class.isAssignableFrom(searchableClasses[0])) {
            for (Object objInteractor : searchResults) {
                results.add(new InteractionDecorator((Interaction)objInteractor));
            }
        } else if (Interactor.class.isAssignableFrom(searchableClasses[0])) {
            for (Object objInteractor : searchResults) {
                results.add(new InteractorDecorator((Interactor)objInteractor));
            }
        } else if (Experiment.class.isAssignableFrom(searchableClasses[0])) {
            for (Object objInteractor : searchResults) {
                results.add(new ExperimentDecorator((Experiment)objInteractor));
            }
        } else if (CvObject.class.isAssignableFrom(searchableClasses[0])) {
            for (Object objInteractor : searchResults) {
                results.add(new CvObjectDecorator((CvObject)objInteractor));
            }
        } else {
            results.addAll(searchResults);
        }

        if (log.isDebugEnabled()) log.debug("Results returned: "+results.size()+" - Row index: "+rowIndex+" - Row count: "+rowCount);
    }

    public int getRowCount() {
        return rowCount;
    }

    public Object getRowData() {
        if (results == null) {
            return null;
        }

        if (!isRowAvailable()) {
            throw new IllegalArgumentException("row is unavailable");
        }

        if (!isRowWithinResultRange()) {
            firstResult = getRowIndex();
            fetchResults();
        }

        return results.get(rowIndex - firstResult);
    }

    @Override
    public void setSortCriteria(List<SortCriterion> criteria) {
        super.setSortCriteria(criteria);
        fetchResults();
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public Object getWrappedData() {
        return results;
    }

    protected boolean isRowWithinResultRange() {
        return (getRowIndex() >= firstResult) && (getRowIndex() < (firstResult+maxResults));
    }

    public boolean isRowAvailable() {
        if (results == null || results.isEmpty()) {
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
        if (results != null && oldRowIndex != this.rowIndex) {
            Object data = isRowAvailable() ? getRowData() : null;
            DataModelEvent event = new DataModelEvent(this, this.rowIndex, data);
            DataModelListener[] listeners = getDataModelListeners();
            for (DataModelListener listener : listeners) {
                listener.rowSelected(event);
            }
        }
    }

}