package uk.ac.ebi.intact.services.search.controller.interactor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.myfaces.orchestra.viewController.annotations.PreRenderView;
import org.apache.myfaces.orchestra.viewController.annotations.ViewController;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.services.search.JpaBaseController;
import uk.ac.ebi.intact.services.search.model.InteractorWrapper;

import javax.faces.component.UIComponent;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller("interactorBean")
@Scope("conversation.access")
@ConversationName("general")
@ViewController(viewIds = "/pages/interactor/interactor.xhtml")
public class InteractorController extends JpaBaseController {

    private static final Log log = LogFactory.getLog(InteractorController.class);

    private Interactor currentInteractor;
    private InteractorWrapper currentInteractorWrapped;

    private UIComponent interactionsTable;

    private int pdbImageSize = 500;
    private String pdbDisplay = "bio";

    public InteractorController() {

    }

    @PreRenderView
    public void loadViewFromParameter() {
        String acParam = getParameterValue("ac", "interactorAc");

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

    public int getPdbImageSize() {
        return pdbImageSize;
    }

    public void setPdbImageSize(int pdbImageSize) {
        this.pdbImageSize = pdbImageSize;
    }

    public String getPdbDisplay() {
        return pdbDisplay;
    }

    public void setPdbDisplay(String pdbDisplay) {
        this.pdbDisplay = pdbDisplay;
    }
}
