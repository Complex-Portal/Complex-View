package uk.ac.ebi.intact.services.search.controller;

import uk.ac.ebi.intact.services.search.JpaBaseController;

import javax.faces.component.UIComponent;
import java.util.List;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class AnnotatedObjectResultsController extends JpaBaseController {

    private UIComponent resultsTable;

    public AnnotatedObjectResultsController() {

    }

    public List getSelectedResults() {
        return getSelected(resultsTable);
    }

    public UIComponent getResultsTable() {
        return resultsTable;
    }

    public void setResultsTable(UIComponent resultsTable) {
        this.resultsTable = resultsTable;
    }
}