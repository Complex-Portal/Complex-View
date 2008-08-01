package uk.ac.ebi.intact.view.webapp.controller.search;

import org.apache.myfaces.trinidad.component.UIXTable;
import org.apache.myfaces.trinidad.component.core.data.CoreSelectRangeChoiceBar;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller("searchBindings")
@Scope("request")
public class SearchControllerBindings {

    // bindings
    private UIXTable resultsDataTable;
    private CoreSelectRangeChoiceBar rangeChoiceBar;

    public UIXTable getResultsDataTable() {
        return resultsDataTable;
    }

    public void setResultsDataTable(UIXTable resultsDataTable) {
        this.resultsDataTable = resultsDataTable;
    }

    public CoreSelectRangeChoiceBar getRangeChoiceBar() {
        return rangeChoiceBar;
    }

    public void setRangeChoiceBar(CoreSelectRangeChoiceBar rangeChoiceBar) {
        this.rangeChoiceBar = rangeChoiceBar;
    }
}
