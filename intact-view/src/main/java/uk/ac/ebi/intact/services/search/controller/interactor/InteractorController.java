package uk.ac.ebi.intact.services.search.controller.interactor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.viewController.annotations.InitView;
import org.apache.myfaces.orchestra.viewController.annotations.ViewController;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.services.search.SearchBaseController;
import uk.ac.ebi.intact.services.search.model.InteractorWrapper;

import javax.faces.context.FacesContext;
import javax.faces.component.UIComponent;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@ViewController(viewIds = "/pages/interactor/interactor.xhtml")
public class InteractorController extends SearchBaseController {

    private static final Log log = LogFactory.getLog(InteractorController.class);

    private Interactor currentInteractor;
    private InteractorWrapper currentInteractorWrapped;

    private UIComponent interactionsTable;

    public InteractorController() {

    }

    @InitView
    public void loadViewFromParameter() {
        FacesContext context = FacesContext.getCurrentInstance();
        String acParam = context.getExternalContext().getRequestParameterMap().get("ac");

        if (log.isDebugEnabled()) log.debug("Loading interactor in view: "+acParam);

        if (acParam != null) {
            currentInteractor = getDaoFactory().getInteractorDao().getByAc(acParam);
        }
    }

    public Interactor getCurrentInteractor() {
        return currentInteractor;
    }

    public void setCurrentInteractor(Interactor currentInteractor) {
        this.currentInteractor = currentInteractor;
    }

    public UIComponent getInteractionsTable() {
        return interactionsTable;
    }

    public void setInteractionsTable(UIComponent interactionsTable) {
        this.interactionsTable = interactionsTable;
    }

    public InteractorWrapper getCurrentInteractorWrapped() {
        if (currentInteractorWrapped == null) {
            currentInteractorWrapped = new InteractorWrapper(currentInteractor);
        }
        return currentInteractorWrapped;
    }

    public void setCurrentInteractorWrapped(InteractorWrapper currentInteractorWrapped) {
        this.currentInteractorWrapped = currentInteractorWrapped;
    }
}
