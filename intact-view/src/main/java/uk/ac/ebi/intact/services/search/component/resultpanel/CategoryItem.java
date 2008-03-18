package uk.ac.ebi.intact.services.search.component.resultpanel;

import javax.faces.model.DataModel;
import java.util.Collection;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 */
public class CategoryItem {

    private String category;
    private Object data;

    public CategoryItem(String category, Object data) {
        this.category = category;
        this.data = data;
    }

    public String getCategory() {
        return category;
    }

    public Object getData() {
        return data;
    }

    public int getSize() {
        int size = 0;

        if (data instanceof Collection) {
            size = ((Collection)data).size();
        } else if (data instanceof DataModel) {
            size = ((DataModel)data).getRowCount();
        }

        return size;
    }
}
