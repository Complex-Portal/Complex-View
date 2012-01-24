package uk.ac.ebi.intact.editor.util;

import org.primefaces.model.SelectableDataModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class SelectableCollectionDataModel<T> implements SelectableDataModel<T> {
    
    private List<T> list;

    public SelectableCollectionDataModel(Collection<T> list) {
        this.list = new ArrayList<T>(list);
    }

    @Override
    public Object getRowKey(T object) {
        int i = 0;
        
        for (T element : list) {
            if (object == element) {
                return String.valueOf(i);
            }
            i++;
        }

        return null;
    }

    @Override
    public T getRowData(String rowKey) {
        return list.get(Integer.parseInt(rowKey));
    }
    
    public int size() {
        return list.size();
    }
}
