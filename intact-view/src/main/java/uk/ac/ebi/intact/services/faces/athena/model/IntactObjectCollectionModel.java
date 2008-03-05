package uk.ac.ebi.intact.services.faces.athena.model;

import org.apache.myfaces.trinidad.model.SortableModel;
import uk.ac.ebi.intact.model.IntactObject;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.persistence.dao.IntactObjectDao;

import javax.faces.model.DataModelEvent;
import javax.faces.model.DataModelListener;
import java.util.List;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class IntactObjectCollectionModel extends SortableModel {

    private static final int DEFAULT_MAX_RESULTS = 50;

    private Class<? extends IntactObject> intactObjectClass;
    private List<? extends IntactObject> intactObjects;
    private int rowIndex = -1;
    private int firstResult = 0;
    private int maxResults = DEFAULT_MAX_RESULTS;
    private int rowCount = -1;

    public IntactObjectCollectionModel(Class<? extends IntactObject> intactObjectClass) {
        this.intactObjectClass = intactObjectClass;

        fetchResults();
        setWrappedData(intactObjects);
    }

    protected void fetchResults() {
        this.rowCount = getDao().countAll();
        this.intactObjects = getDao().getAll(firstResult, maxResults);
    }

    private IntactObjectDao<? extends IntactObject> getDao() {
        return IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getIntactObjectDao(intactObjectClass);
    }

    public int getRowCount() {
        return rowCount;
    }

    public Object getRowData() {
        if (intactObjects == null) {
            return null;
        }

        if (!isRowAvailable()) {
            throw new IllegalArgumentException("row is unavailable");
        }

        if (!isRowWithinResultRange()) {
            firstResult = getRowIndex();
            fetchResults();
        }

        return intactObjects.get(rowIndex - firstResult);
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public Object getWrappedData() {
        return intactObjects;
    }

    protected boolean isRowWithinResultRange() {
        return (getRowIndex() >= firstResult) && (getRowIndex() < (firstResult+maxResults));
    }

    public boolean isRowAvailable() {
        if (intactObjects == null || intactObjects.isEmpty()) {
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
        if (intactObjects != null && oldRowIndex != this.rowIndex) {
            Object data = isRowAvailable() ? getRowData() : null;
            DataModelEvent event = new DataModelEvent(this, this.rowIndex, data);
            DataModelListener[] listeners = getDataModelListeners();
            for (DataModelListener listener : listeners) {
                listener.rowSelected(event);
            }
        }
    }

}
