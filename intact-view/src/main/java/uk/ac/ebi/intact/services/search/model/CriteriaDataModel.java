package uk.ac.ebi.intact.services.search.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.trinidad.model.SortCriterion;
import org.apache.myfaces.trinidad.model.SortableModel;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Order;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.services.search.util.Functions;

import javax.faces.model.DataModelEvent;
import javax.faces.model.DataModelListener;
import javax.persistence.Query;
import java.io.*;
import java.util.*;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class CriteriaDataModel extends SortableModel implements Serializable {

    private static final Log log = LogFactory.getLog(SearchResultDataModel.class);

    private static final int DEFAULT_MAX_RESULTS = 25;

    private DetachedCriteria detachedCriteria;
    private List results;
    private int rowIndex = -1;
    private int firstResult = 0;
    private int maxResults = DEFAULT_MAX_RESULTS;
    private int rowCount = 0;


    public CriteriaDataModel(DetachedCriteria detachedCriteria) {
        this.detachedCriteria = detachedCriteria;

        rowCount = count(detachedCriteria);

        fetchResults();
        setWrappedData(results);
    }

    protected void fetchResults() {
        Criteria criteria = createCriteria(detachedCriteria);
        List<SortCriterion> sortCriteria = super.getSortCriteria();

        if (!sortCriteria.isEmpty()) {
            // only use the first criterion
            SortCriterion criterion = sortCriteria.get(0);

            if (criterion.isAscending()) {
                criteria.addOrder(Order.asc(criterion.getProperty()));
            } else {
                criteria.addOrder(Order.desc(criterion.getProperty()));
            }

            if (log.isDebugEnabled()) log.debug("\tSorting by '"+criterion.getProperty()+"' "+(criterion.isAscending()? "ASC" : "DESC"));
        }

        Collection<?> searchResults = criteria.list();
        results = new ArrayList(searchResults.size());

        // wrap if necessary
        for (Object obj : searchResults) {
            if (obj instanceof AnnotatedObject) {
                results.add(Functions.wrap((AnnotatedObject)obj)) ;
            } else {
                results.add(obj);
            }
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

    private int count(DetachedCriteria detachedCriteria) {
        Criteria countCriteria = createCriteria(detachedCriteria);
        countCriteria.setProjection(Projections.rowCount());

        return (Integer) countCriteria.uniqueResult();
    }

    private Criteria createCriteria(DetachedCriteria detachedCriteria) {
        DetachedCriteria criteriaCopy = copy(detachedCriteria);
        Criteria criteria = criteriaCopy.getExecutableCriteria(IntactContext.getCurrentInstance().getDataContext().getSession());
        criteria.setFirstResult(firstResult);
        criteria.setMaxResults(maxResults);
        return criteria;
    }

    protected DetachedCriteria copy(DetachedCriteria criteria) {
        try {
            ByteArrayOutputStream baostream = new ByteArrayOutputStream();
            ObjectOutputStream oostream = new ObjectOutputStream(baostream);
            oostream.writeObject(criteria);
            oostream.flush();
            oostream.close();
            ByteArrayInputStream baistream = new ByteArrayInputStream(baostream.toByteArray());
            ObjectInputStream oistream = new ObjectInputStream(baistream);
            DetachedCriteria copy = (DetachedCriteria)oistream.readObject();
            oistream.close();
            return copy;
        } catch(Throwable t) {
            throw new HibernateException(t);
        }
    }

}