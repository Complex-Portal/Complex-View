package uk.ac.ebi.intact.editor.controller.search;

import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.Interaction;

import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ntoro
 * Date: 19/03/2013
 * Time: 11:22
 * To change this template use File | Settings | File Templates.
 */
@Controller
@Scope( "conversation.access" )
@ConversationName("search")
public class SearchInteractionsMoleculeController {

	private String ac;
	private List<Interaction> interactions = null;
	private String shortLabel;
	private String numInteractions;

	public String getAc() {
		return ac;
	}

	public void setAc(String ac) {
		this.ac = ac;
	}

	@Transactional(readOnly = true)
	public void loadData(ComponentSystemEvent evt) {
		if (!FacesContext.getCurrentInstance().isPostback()) {

			if (ac != null) {
				interactions = IntactContext.getCurrentInstance().getDaoFactory().getInteractionDao().getInteractionsByInteractorAc(ac);
			}
		}
	}

	public List<Interaction> getInteractions() {
		return interactions;
	}

	public void setInteractions(List<Interaction> interactions) {
		this.interactions = interactions;
	}

	public String getShortLabel() {
		return shortLabel;
	}

	public void setShortLabel(String shortLabel) {
		this.shortLabel = shortLabel;
	}

	public void setNumInteractions(String numInteractions) {
		this.numInteractions = numInteractions;
	}

	public String getNumInteractions() {
		return numInteractions;
	}
}
