package uk.ac.ebi.intact.services.search.controller;

import javax.faces.event.ActionEvent;
import java.util.List;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InteractorResultsController extends AnnotatedObjectResultsController {

    public void exportSelectedToHierarchView(ActionEvent action) {
        List selected = getSelectedResults();

        System.out.println("SELECTED: " + selected.size());
    }

    public void exportSelectedToInterPro(ActionEvent action) {
        List selected = getSelectedResults();

        System.out.println("SELECTED: " + selected.size());
    }

}
