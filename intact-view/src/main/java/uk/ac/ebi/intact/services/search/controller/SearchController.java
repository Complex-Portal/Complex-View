package uk.ac.ebi.intact.services.search.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.viewController.annotations.InitView;
import org.apache.myfaces.orchestra.viewController.annotations.ViewController;
import org.apache.myfaces.orchestra.viewController.annotations.PreRenderView;
import org.apache.myfaces.orchestra.conversation.ConversationUtils;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.persistence.svc.SearchService;
import uk.ac.ebi.intact.persistence.svc.impl.SimpleSearchService;
import uk.ac.ebi.intact.services.search.JpaBaseController;
import uk.ac.ebi.intact.services.search.SearchWebappException;
import uk.ac.ebi.intact.services.search.model.SearchResultDataModel;

import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.util.Map;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@ViewController(viewIds = "/search.xhtml")
public class SearchController extends JpaBaseController {

    private static final Log log = LogFactory.getLog(SearchController.class);

    private String searchQuery;
    private String searchClassName;
    private SearchResultDataModel searchResults;

    private ResultStats resultStats;

    public SearchController() {
        this.resultStats = new ResultStats();
    }

    @PreRenderView
    public void initialParams() {
        FacesContext context = FacesContext.getCurrentInstance();
        String queryParam = context.getExternalContext().getRequestParameterMap().get("query");
        String searchClassParam = context.getExternalContext().getRequestParameterMap().get("class");

        if (queryParam != null) {
            ConversationUtils.invalidateAndRestartCurrent();
            searchQuery = queryParam;
            searchClassName = searchClassFromParam(searchClassParam);
            doSearch(null);
        }

        if (searchQuery == null) {
            searchQuery = "*";
            doSearch(null);
        }
    }

    public void doSearch(ActionEvent evt) {
        if (searchClassName != null) {
            if (log.isDebugEnabled()) log.debug("Searching query: "+searchQuery+" - class: "+searchClassName);

            Class searchClass = null;
            try {
                searchClass = Class.forName(searchClassName);
            }
            catch (ClassNotFoundException e) {
                throw new SearchWebappException("Searchable class does not exist: " + searchClassName);
            }

            // if the search class is interactor, search with the interactor subtypes excluding the interaction
            if (InteractorImpl.class.isAssignableFrom(searchClass) && !Interaction.class.isAssignableFrom(searchClass)) { 
                this.searchResults = new SearchResultDataModel(new Class[] {ProteinImpl.class, SmallMoleculeImpl.class, NucleicAcidImpl.class}, searchQuery);
            } else {
                this.searchResults = new SearchResultDataModel(new Class[] {searchClass}, searchQuery);
            }

        } else { // count
            if (log.isDebugEnabled()) log.debug("Counting results for query: "+searchQuery);

            SearchService service = new SimpleSearchService();

            Map<Class<? extends Searchable>, Integer> counts =
                    service.count(SearchService.STANDARD_SEARCHABLES, searchQuery);

            final Integer cvObjectCount = counts.containsKey(CvObject.class) ? counts.get(CvObject.class) : 0;
            final Integer proteinCount = counts.containsKey(ProteinImpl.class) ? counts.get(ProteinImpl.class) : 0;
            final Integer smallMolCount = counts.containsKey(SmallMoleculeImpl.class) ? counts.get(SmallMoleculeImpl.class) : 0;
            final Integer nucAcidCount = counts.containsKey(NucleicAcidImpl.class) ? counts.get(NucleicAcidImpl.class) : 0;
            final Integer interactionCount = counts.containsKey(InteractionImpl.class) ? counts.get(InteractionImpl.class) : 0;
            final Integer experimentCount = counts.containsKey(Experiment.class) ? counts.get(Experiment.class) : 0;

            this.resultStats = new ResultStats(
                    cvObjectCount, proteinCount,
                    smallMolCount, nucAcidCount, interactionCount,
                    experimentCount);
        }
    }

    public void doSearchAll(ActionEvent evt) {
        searchClassName = null;
        doSearch(evt);
    }

    public void doSearchClass(ActionEvent evt) {
        UIParameter param = (UIParameter) evt.getComponent().getChildren().get(0);
        searchClassName = (String) param.getValue();
        doSearch(evt);
    }

    private String searchClassFromParam(String searchClassParam) {
        Class<? extends Searchable> searchClass = null;

        if ("experiment".equalsIgnoreCase(searchClassParam)) {
            searchClass = Experiment.class;
        } else if ("interaction".equalsIgnoreCase(searchClassParam)) {
            searchClass = InteractionImpl.class;
        } else if ("interactor".equalsIgnoreCase(searchClassParam)) {
            searchClass = InteractorImpl.class;
        } else if ("cv".equalsIgnoreCase(searchClassParam)) {
            searchClass = CvObject.class;
        } else if ("biosource".equalsIgnoreCase(searchClassParam)) {
            searchClass = BioSource.class;
        }

        if (searchClass == null) {
            return null;
        }

        return searchClass.getName();
    }

    public void testMe(ActionEvent evt) {
        System.out.println("TEST ME!!!");
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public String getSearchClassName() {
        return searchClassName;
    }

    public void setSearchClassName(String searchClassName) {
        this.searchClassName = searchClassName;
    }

    public ResultStats getResultStats() {
        return resultStats;
    }

    public SearchResultDataModel getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(SearchResultDataModel searchResults) {
        this.searchResults = searchResults;
    }
}
