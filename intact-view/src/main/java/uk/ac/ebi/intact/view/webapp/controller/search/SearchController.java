package uk.ac.ebi.intact.view.webapp.controller.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.index.Term;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.myfaces.orchestra.viewController.annotations.PreRenderView;
import org.apache.myfaces.orchestra.viewController.annotations.ViewController;
import org.apache.myfaces.trinidad.event.RangeChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.binarysearch.webapp.generated.SearchConfig;
import uk.ac.ebi.intact.binarysearch.webapp.generated.Index;
import uk.ac.ebi.intact.view.webapp.IntactViewException;
import uk.ac.ebi.intact.view.webapp.io.BinaryInteractionsExporter;
import uk.ac.ebi.intact.view.webapp.controller.JpaBaseController;
import uk.ac.ebi.intact.view.webapp.controller.application.AppConfigBean;
import uk.ac.ebi.intact.view.webapp.controller.application.OlsBean;
import uk.ac.ebi.intact.view.webapp.model.SearchResultDataModel;
import uk.ac.ebi.intact.view.webapp.model.TooManyResultsException;
import uk.ac.ebi.intact.view.webapp.servlet.ExportServlet;
import uk.ac.ebi.intact.view.webapp.util.WebappUtils;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.util.List;
import java.util.Iterator;

import psidev.psi.mi.tab.model.CrossReference;

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

    private static final String MAX_RESULTS_INIT_PARAM = "intact.MAX_SEARCH_RESULTS";

    // injected
    @Autowired
    private AppConfigBean appConfigBean;

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
        FacesContext facesContext = FacesContext.getCurrentInstance();
        int maxResults = Integer.parseInt(facesContext.getExternalContext().getInitParameter(MAX_RESULTS_INIT_PARAM));
        BooleanQuery.setMaxClauseCount(maxResults);
    }

    @PreRenderView
    public void initialParams() {
        FacesContext context = FacesContext.getCurrentInstance();
        String queryParam = context.getExternalContext().getRequestParameterMap().get(QUERY_PARAM);
        String tabParam = context.getExternalContext().getRequestParameterMap().get(TAB_PARAM);

        if (queryParam != null) {
            searchQuery = queryParam;
            doBinarySearch(null);
        }

        if (searchQuery == null) {
            searchQuery = "*";
            doBinarySearch(null);
            disclosedTabName = "search";
        }

        if (tabParam != null) {
            disclosedTabName = tabParam;
        }
    }

    public String doBinarySearchAction() {
        doBinarySearch(null);
        return "main";
    }

    public String doOntologySearchAction() {
        searchQuery = ontologySearchQuery;
        doBinarySearch(null);
        searchQuery="*";
        return "main";
    }

    public void doBinarySearch(ActionEvent evt) {
        // reset the status of the range choice bar
        if (bindings.getRangeChoiceBar() != null) {
            bindings.getRangeChoiceBar().setFirst(0);
        }

        if (log.isDebugEnabled()) log.debug("Searching (raw): " + searchQuery);

        searchQuery = QueryHelper.prepareQuery(searchQuery);

        if (log.isDebugEnabled()) log.debug("Searching (prepared query): " + searchQuery);

        final SearchConfig config = appConfigBean.getConfig();

        if (config == null) {
            addErrorMessage("Configuration file was not found", "Run the first time setup or check the application configuration");
            return;
        }

        String indexDirectory = WebappUtils.getDefaultIndex(config).getLocation();

        try {
            results = new SearchResultDataModel(searchQuery, indexDirectory, pageSize);

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
        }

        if (bindings.getResultsDataTable() != null) {
            bindings.getResultsDataTable().setFirst(0);
        }

        doInteractorSearch(evt);
    }

    public void doInteractorSearch(ActionEvent evt) {
        // reset the status of the range choice bar
        if (interactorBindings.getRangeChoiceBar() != null) {
            interactorBindings.getRangeChoiceBar().setFirst(0);
        }

        if (log.isDebugEnabled()) log.debug("Searching interactors for: " + searchQuery);

        String indexDirectory = WebappUtils.getDefaultInteractorIndex(appConfigBean.getConfig()).getLocation();

        try {
            interactorResults = new SearchResultDataModel(searchQuery, indexDirectory, pageSize);
        } catch (TooManyResultsException e) {
            addErrorMessage("Too many interactors found", "Please, refine your query");
        }

        interactorTotalResults = interactorResults.getRowCount();

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
        
        doBinarySearch(evt);

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
