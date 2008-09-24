package uk.ac.ebi.intact.view.webapp.controller.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.BooleanQuery;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.myfaces.orchestra.viewController.annotations.PreRenderView;
import org.apache.myfaces.orchestra.viewController.annotations.ViewController;
import org.apache.myfaces.trinidad.event.RangeChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.binarysearch.webapp.generated.Index;
import uk.ac.ebi.intact.binarysearch.webapp.generated.SearchConfig;
import uk.ac.ebi.intact.view.webapp.controller.JpaBaseController;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;
import uk.ac.ebi.intact.view.webapp.controller.application.AppConfigBean;
import uk.ac.ebi.intact.view.webapp.controller.application.OntologyBean;
import uk.ac.ebi.intact.view.webapp.model.SearchResultDataModel;
import uk.ac.ebi.intact.view.webapp.model.TooManyResultsException;
import uk.ac.ebi.intact.view.webapp.util.WebappUtils;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.el.ValueBinding;
import javax.annotation.PostConstruct;
import java.util.*;

import psidev.psi.mi.tab.model.*;

/**
 * Main search controller
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller("searchBean")
@Scope("conversation.access")
@ConversationName("general")
@ViewController(viewIds = {"/main.xhtml"})
public class SearchController extends JpaBaseController {

    private static final Log log = LogFactory.getLog(SearchController.class);

    private static final String QUERY_PARAM = "query";
    private static final String TAB_PARAM = "tab";

    // injected
    @Autowired
    private AppConfigBean appConfigBean;

    @Autowired
    private OntologyBean ontologyBean;

    @Autowired
    private IntactViewConfiguration intactViewConfiguration;

    @Autowired
    @Qualifier("searchBindings")  
    private SearchControllerBindings bindings;

    @Autowired
    @Qualifier("interactorSearchBindings")
    private InteractorSearchControllerBindings interactorBindings;

    private String searchQuery;
    private String ontologySearchQuery;

     // vars
    private int pageSize = 30;

    private int totalResults;
    private int interactorTotalResults;

    // status flags
    private String disclosedTabName;

    private boolean showProperties;
    private boolean expandedView;

    // results
    private SearchResultDataModel results;
    private SearchResultDataModel interactorResults;

    // io
    private String exportFormat;


    public SearchController() {
    }

    @PostConstruct
    public void lala() {
        BooleanQuery.setMaxClauseCount(intactViewConfiguration.getLuceneMaxCombinations());
    }

    @PreRenderView
    public void initialParams() {
        FacesContext context = FacesContext.getCurrentInstance();
        String queryParam = context.getExternalContext().getRequestParameterMap().get(QUERY_PARAM);
        String tabParam = context.getExternalContext().getRequestParameterMap().get(TAB_PARAM);

        if (queryParam != null) {
            searchQuery = queryParam;
            doBinarySearch(searchQuery);
        }

        if (searchQuery == null) {
            searchQuery = "*";
            doBinarySearch(searchQuery);
            disclosedTabName = "search";
        }

        if (tabParam != null) {
            disclosedTabName = tabParam;
        }
    }

    public String doBinarySearchAction() {
        doBinarySearch(searchQuery);
        return "main";
    }

    public String doOntologySearchAction() {

        String formattedQuery = OntologyBean.prepareOntologyQueryForLucene( ontologySearchQuery );
        if ( log.isDebugEnabled() ) {
            log.debug( " ontologySearchQuery " +ontologySearchQuery);
            log.debug( " formattedQuery " +formattedQuery);
        }
        doBinarySearch( formattedQuery );
        return "main";
    }

    private void doBinarySearch(String query) {
        // reset the status of the range choice bar
        if (bindings.getRangeChoiceBar() != null) {
            bindings.getRangeChoiceBar().setFirst(0);
        }

        if (log.isDebugEnabled()) log.debug("Searching (raw): " + query);

        query = QueryHelper.prepareQuery(query);

        if (log.isDebugEnabled()) log.debug("Searching (prepared query): " + query);

        final SearchConfig config = appConfigBean.getConfig();

        if (config == null) {
            addErrorMessage("Configuration file was not found", "Run the first time setup or check the application configuration");
            return;
        }

        String indexDirectory = WebappUtils.getDefaultIndex(config).getLocation();

        try {
            results = new SearchResultDataModel(query, indexDirectory, pageSize);

            totalResults = results.getRowCount();

            if (log.isDebugEnabled()) log.debug("\tResults: " + results.getRowCount());

            if (totalResults == 0) {
                addErrorMessage("Your query didn't return any results", "Use a different query");
                disclosedTabName = "search";
            } else {
               disclosedTabName = "interactions";
            }

        } catch (TooManyResultsException e) {
            addErrorMessage("Too many results found", "Please, refine your query");
            e.printStackTrace();
        }

        if (bindings.getResultsDataTable() != null) {
            bindings.getResultsDataTable().setFirst(0);
        }

        doInteractorSearch(query);
    }

    public void doInteractorSearch(String query) {
        // reset the status of the range choice bar
        if (interactorBindings.getRangeChoiceBar() != null) {
            interactorBindings.getRangeChoiceBar().setFirst(0);
        }

        if (log.isDebugEnabled()) log.debug("Searching interactors for: " + query);

        String indexDirectory = WebappUtils.getDefaultInteractorIndex(appConfigBean.getConfig()).getLocation();

        try {
            interactorResults = new SearchResultDataModel(query, indexDirectory, pageSize);
            interactorTotalResults = interactorResults.getRowCount();
        } catch (TooManyResultsException e) {
            addErrorMessage("Too many interactors found", "Please, refine your query");
            interactorTotalResults = 0;
        }

        if (interactorBindings.getResultsDataTable() != null) {
            interactorBindings.getResultsDataTable().setFirst(0);
        }
    }

    public void rangeChanged(RangeChangeEvent evt) {
        results.setRowIndex(evt.getNewStart());
        bindings.getResultsDataTable().setFirst(evt.getNewStart());
    }

    public Index getDefaultIndex() {
        return WebappUtils.getDefaultIndex(appConfigBean.getConfig());
    }

    public void doSearchInteractionsFromListSelection(ActionEvent evt) {
        final List<IntactBinaryInteraction> selected = getSelected(interactorBindings.getResultsDataTable());

        if (selected.size() == 0) {
            return;
        }

        StringBuilder sb = new StringBuilder(selected.size()*10);
        sb.append("id:(");

        for (Iterator<IntactBinaryInteraction> iterator = selected.iterator(); iterator.hasNext();) {
            IntactBinaryInteraction intactBinaryInteraction = iterator.next();

            String identifier = null;

            for (CrossReference xref : intactBinaryInteraction.getInteractorA().getIdentifiers()) {
                if ("intact".equals(xref.getDatabase())) {
                    identifier = xref.getIdentifier();
                }
            }

            sb.append(identifier);

            if (iterator.hasNext()) {
                sb.append(" ");
            }
        }

        sb.append(")");

        searchQuery = sb.toString();
        
        doBinarySearch(searchQuery);

        interactorBindings.getResultsDataTable().setSelectedRowKeys(null);
    }

    public Index getDefaultInteractorIndex() {
        return WebappUtils.getDefaultInteractorIndex(appConfigBean.getConfig());
    }

    public String doDiscloseMoleculeViewTab() {
        disclosedTabName = "molecule";
        return "main";
    }

    // Getters & Setters
    /////////////////////

    public SearchResultDataModel getResults()
    {
        return results;
    }

    public void setResults(SearchResultDataModel results)
    {
        this.results = results;
    }

    public int getPageSize()
    {
        return pageSize;
    }

    public void setPageSize(int pageSize)
    {
        this.pageSize = pageSize;
    }

    public boolean isShowProperties() {
        return showProperties;
    }

    public void setShowProperties(boolean showProperties) {
        this.showProperties = showProperties;
    }

    public boolean isExpandedView() {
        return expandedView;
    }

    public void setExpandedView(boolean expandedView) {
        this.expandedView = expandedView;
    }

    public String getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(String exportFormat) {
        this.exportFormat = exportFormat;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public SearchControllerBindings getBindings() {
        return bindings;
    }

    public String getDisclosedTabName() {
        return disclosedTabName;
    }

    public void setDisclosedTabName(String disclosedTabName) {
        this.disclosedTabName = disclosedTabName;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public InteractorSearchControllerBindings getInteractorBindings() {
        return interactorBindings;
    }

    public void setInteractorBindings(InteractorSearchControllerBindings interactorBindings) {
        this.interactorBindings = interactorBindings;
    }

    public int getInteractorTotalResults() {
        return interactorTotalResults;
    }

    public void setInteractorTotalResults(int interactorTotalResults) {
        this.interactorTotalResults = interactorTotalResults;
    }

    public SearchResultDataModel getInteractorResults() {
        return interactorResults;
    }

    public void setInteractorResults(SearchResultDataModel interactorResults) {
        this.interactorResults = interactorResults;
    }

    public String getOntologySearchQuery() {
        return ontologySearchQuery;
    }

    public void setOntologySearchQuery( String ontologySearchQuery ) {
        this.ontologySearchQuery = ontologySearchQuery;
    }
}
