package uk.ac.ebi.intact.editor.controller.publication;

import javax.faces.model.SelectItem;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public interface DatasetPopulator {

    List<String> getAllDatasets();

    List<SelectItem> getAllDatasetSelectItems();

    SelectItem createSelectItem(String dataset);
}
