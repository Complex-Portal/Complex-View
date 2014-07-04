package uk.ac.ebi.intact.editor.controller.search;

import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.primefaces.model.LazyDataModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.editor.util.LazyDataModelFactory;
import uk.ac.ebi.intact.model.Component;

import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;

/**
 * Created with IntelliJ IDEA.
 * User: ntoro
 * Date: 26/03/2013
 * Time: 11:21
 * To change this template use File | Settings | File Templates.
 */
@Controller("expressInOrganismController")
@Scope( "conversation.access" )
@ConversationName("search")
@SuppressWarnings("unchecked")
public class SearchParticipantExpressInOrganismController extends JpaAwareController {

	private String ac;
	private String shortLabel;
	private String numParticipants;

	private LazyDataModel<Component> participants = null;


	public String getAc() {
		return ac;
	}

	public void setAc(String ac) {
		this.ac = ac;
	}

    @Transactional(value = "transactionManager", readOnly = true)
	public void loadData(ComponentSystemEvent evt) {
		if (!FacesContext.getCurrentInstance().isPostback()) {

//				interactors = IntactContext.getCurrentInstance().getDaoFactory().getInteractorDao().getByBioSourceAc(ac);
//				final HashMap<String, String> params = Maps.<String, String>newHashMap();
//				params.put( "ac", ac );
//
//				participants = LazyDataModelFactory.createLazyDataModel(getCoreEntityManager(),
//
//						"select distinct p " +
//								"from Component p " +
//								"where p.expressedInAc = :ac ",
//
//						"select count(distinct p) " +
//								"from Component p " +
//								"where p.expressedInAc = :ac ",
//
//						params, "p", "updated", false);

			if (ac != null) {
				participants = LazyDataModelFactory.createLazyDataModel(
						IntactContext.getCurrentInstance().getDaoFactory().getComponentDao().getByExpressedIn(ac)
				);
			}
		}
	}

	public LazyDataModel<Component> getParticipants() {
		return participants;
	}

	public void setParticipants(LazyDataModel<Component> participants) {
		this.participants = participants;
	}

	public String getShortLabel() {
		return shortLabel;
	}

	public void setShortLabel(String shortLabel) {
		this.shortLabel = shortLabel;
	}

	public void setNumParticipants(String numParticipants) {
		this.numParticipants = numParticipants;
	}

	public String getNumParticipants() {
		return numParticipants;
	}


}

