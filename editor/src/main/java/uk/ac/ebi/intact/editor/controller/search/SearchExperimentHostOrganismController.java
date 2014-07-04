package uk.ac.ebi.intact.editor.controller.search;

import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.primefaces.model.LazyDataModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.editor.util.LazyDataModelFactory;
import uk.ac.ebi.intact.model.Experiment;

import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;

/**
 * Created with IntelliJ IDEA.
 * User: ntoro
 * Date: 26/03/2013
 * Time: 11:21
 * To change this template use File | Settings | File Templates.
 */
@Controller("experimentHostOrganismController")
@Scope( "conversation.access" )
@ConversationName("search")
@SuppressWarnings("unchecked")
public class SearchExperimentHostOrganismController extends JpaAwareController {

	private String ac;
	private String shortLabel;
	private String numExperiments;

	private LazyDataModel<Experiment> experiments = null;

	public String getAc() {
		return ac;
	}

	public void setAc(String ac) {
		this.ac = ac;
	}

    @Transactional(value = "transactionManager", readOnly = true)
	public void loadData(ComponentSystemEvent evt) {
		if (!FacesContext.getCurrentInstance().isPostback()) {

			if (ac != null) {
				experiments = LazyDataModelFactory.createLazyDataModel(
						IntactContext.getCurrentInstance().getDaoFactory().getExperimentDao().getByHostOrganism(ac)
				);
			}
		}
	}

	public LazyDataModel<Experiment> getExperiments() {
		return experiments;
	}

	public void setExperiments(LazyDataModel<Experiment> experiments) {
		this.experiments = experiments;
	}

	public String getShortLabel() {
		return shortLabel;
	}

	public void setShortLabel(String shortLabel) {
		this.shortLabel = shortLabel;
	}

	public void setNumExperiments(String numExperiments) {
		this.numExperiments = numExperiments;
	}

	public String getNumExperiments() {
		return numExperiments;
	}
}

