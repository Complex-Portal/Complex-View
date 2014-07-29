package uk.ac.ebi.intact.editor.controller.search;

import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.primefaces.model.LazyDataModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.editor.util.LazyDataModelFactory;
import uk.ac.ebi.intact.model.InteractorImpl;

import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;

/**
 * Created with IntelliJ IDEA.
 * User: ntoro
 * Date: 26/03/2013
 * Time: 11:21
 * To change this template use File | Settings | File Templates.
 */
@Controller("interactorOrganismController")
@Scope( "conversation.access" )
@ConversationName("search")
@SuppressWarnings("unchecked")
public class SearchInteractorOrganismController extends JpaAwareController {

	private String ac;
	private String shortLabel;
	private String numInteractors;

	private LazyDataModel<InteractorImpl> interactors = null;

	public String getAc() {
		return ac;
	}

	public void setAc(String ac) {
		this.ac = ac;
	}

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
	public void loadData(ComponentSystemEvent evt) {
		if (!FacesContext.getCurrentInstance().isPostback()) {

			if (ac != null) {
				interactors = LazyDataModelFactory.createLazyDataModel(
						IntactContext.getCurrentInstance().getDaoFactory().getInteractorDao().getByBioSourceAc(ac)
				);
			}
		}
	}

	public LazyDataModel<InteractorImpl> getInteractors() {
		return interactors;
	}

	public void setInteractors(LazyDataModel<InteractorImpl> interactors) {
		this.interactors = interactors;
	}

	public String getShortLabel() {
		return shortLabel;
	}

	public void setShortLabel(String shortLabel) {
		this.shortLabel = shortLabel;
	}

	public void setNumInteractors(String numInteractors) {
		this.numInteractors = numInteractors;
	}

	public String getNumInteractors() {
		return numInteractors;
	}


}

