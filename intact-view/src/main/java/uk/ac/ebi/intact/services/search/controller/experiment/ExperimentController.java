package uk.ac.ebi.intact.services.search.controller.experiment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.viewController.annotations.InitView;
import org.apache.myfaces.orchestra.viewController.annotations.ViewController;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.services.search.JpaBaseController;
import uk.ac.ebi.intact.services.search.model.InteractionWrapper;
import uk.ac.ebi.intact.services.search.model.ExperimentWrapper;

import javax.faces.component.UIComponent;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@ViewController(viewIds = "/pages/experiment/experiment.xhtml")
public class ExperimentController extends JpaBaseController {

    private static final Log log = LogFactory.getLog(ExperimentController.class);

    private Experiment currentExperiment;
    private ExperimentWrapper currentExperimentWrapped;

    private UIComponent interactionsTable;

    public ExperimentController() {

    }

    @InitView
    public void loadViewFromParameter() {
        String acParam = getParameterValue("ac", "experimentAc");

        if (log.isDebugEnabled()) log.debug("Loading experiment in view: "+acParam);

        if (acParam != null) {
            currentExperiment = getDaoFactory().getExperimentDao().getByAc(acParam);
        }
    }

    public Experiment getCurrentExperiment() {
        return currentExperiment;
    }

    public void setCurrentExperiment(Experiment currentExperiment) {
        this.currentExperiment = currentExperiment;
    }

    public UIComponent getInteractionsTable() {
        return interactionsTable;
    }

    public void setInteractionsTable(UIComponent interactionsTable) {
        this.interactionsTable = interactionsTable;
    }

    public ExperimentWrapper getCurrentExperimentWrapped() {
        if (currentExperimentWrapped == null) {
            currentExperimentWrapped = new ExperimentWrapper(currentExperiment);
        }
        return currentExperimentWrapped;
    }

    public void setCurrentExperimentWrapped(ExperimentWrapper currentExperimentWrapped) {
        this.currentExperimentWrapped = currentExperimentWrapped;
    }
}