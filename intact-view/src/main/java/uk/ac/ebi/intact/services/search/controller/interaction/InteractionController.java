package uk.ac.ebi.intact.services.search.controller.interaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.viewController.annotations.InitView;
import org.apache.myfaces.orchestra.viewController.annotations.ViewController;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Annotation;
import uk.ac.ebi.intact.services.search.JpaBaseController;
import uk.ac.ebi.intact.services.search.model.InteractionWrapper;

import javax.faces.context.FacesContext;
import javax.faces.component.UIComponent;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@ViewController(viewIds = "/pages/interaction/interaction.xhtml")
public class InteractionController extends JpaBaseController {

    private static final Log log = LogFactory.getLog(InteractionController.class);

    private Interaction currentInteraction;
    private InteractionWrapper currentInteractionWrapped;

    private UIComponent componentsTable;

    public InteractionController() {

    }

    @InitView
    public void loadViewFromParameter() {
        FacesContext context = FacesContext.getCurrentInstance();
        String acParam = context.getExternalContext().getRequestParameterMap().get("ac");

        if (log.isDebugEnabled()) log.debug("Loading interactor in view: "+acParam);

        if (acParam != null) {
            currentInteraction = getDaoFactory().getInteractionDao().getByAc(acParam);

            // preload the annotations
            System.out.println(currentInteraction.getAnnotations());
        }
    }

    public Interaction getCurrentInteraction() {
        return currentInteraction;
    }

    public void setCurrentInteraction(Interaction currentInteraction) {
        this.currentInteraction = currentInteraction;
    }

    public UIComponent getComponentsTable() {
        return componentsTable;
    }

    public void setComponentsTable(UIComponent componentsTable) {
        this.componentsTable = componentsTable;
    }

    public InteractionWrapper getCurrentInteractionWrapped() {
        if (currentInteractionWrapped == null) {
            currentInteractionWrapped = new InteractionWrapper(currentInteraction);
        }
        return currentInteractionWrapped;
    }

    public void setCurrentInteractionWrapped(InteractionWrapper currentInteractionWrapped) {
        this.currentInteractionWrapped = currentInteractionWrapped;
    }
}