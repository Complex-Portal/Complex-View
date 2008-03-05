package uk.ac.ebi.intact.services.faces.athena.general;

import uk.ac.ebi.intact.services.faces.athena.BaseController;
import uk.ac.ebi.intact.services.faces.athena.model.IntactObjectCollectionModel;
import uk.ac.ebi.intact.model.Experiment;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ResultsController extends BaseController{

    private IntactObjectCollectionModel experimentsModel;

    public ResultsController() {
        experimentsModel = new IntactObjectCollectionModel(Experiment.class);
    }

    public IntactObjectCollectionModel getExperimentsModel() {
        return experimentsModel;
    }

    public void setExperimentsModel(IntactObjectCollectionModel experimentsModel) {
        this.experimentsModel = experimentsModel;
    }
}
