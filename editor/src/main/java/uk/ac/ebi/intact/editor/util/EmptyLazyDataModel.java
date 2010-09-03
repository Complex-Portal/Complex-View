package uk.ac.ebi.intact.editor.util;

import org.primefaces.model.LazyDataModel;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class EmptyLazyDataModel<T> extends LazyDataModel<T> {

    @Override
    public List<T> load(int first, int pageSize, String sortField, boolean sortOrder, Map<String, String> filters) {
        return Collections.EMPTY_LIST;
    }

}
