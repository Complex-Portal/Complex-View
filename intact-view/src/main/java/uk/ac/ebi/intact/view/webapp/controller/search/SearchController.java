package uk.ac.ebi.intact.view.webapp.controller.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.BooleanQuery;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.myfaces.orchestra.viewController.annotations.PreRenderView;
import org.apache.myfaces.orchestra.viewController.annotations.ViewController;
import org.apache.myfaces.trinidad.event.RangeChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.binarysearch.webapp.generated.SearchConfig;
import uk.ac.ebi.intact.view.webapp.IntactViewException;
import uk.ac.ebi.intact.view.webapp.controller.JpaBaseController;
import uk.ac.ebi.intact.view.webapp.controller.application.AppConfigBean;
import uk.ac.ebi.intact.view.webapp.controller.application.OlsBean;
import uk.ac.ebi.intact.view.webapp.model.SearchResultDataModel;
import uk.ac.ebi.intact.view.webapp.model.TooManyResultsException;
import uk.ac.ebi.intact.view.webapp.servlet.ExportServlet;
import uk.ac.ebi.intact.view.webapp.util.WebappUtils;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.io.IOException;

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
    private static final String MAX_RESULTS_INIT_PARAM = "psidev.MAX_SEARCH_RESULTS";

    // injected
    @Autowired
    private AppConfigBean appConfigBean;

    @Autowired
    private OlsBean olsBean;

    @Autowired
    private SearchControllerBindings bindings;

    private String searchQuery;
    private SearchResultDataModel searchResults;

     // vars
    private int pageSize = 30;

    private int totalResults;

    // status flags
    private String disclosedTabName;

    private boolean showProperties;
    private boolean expandedView;

    // results
    private uk.ac.ebi.intact.view.webapp.model.SearchResultDataModel results;

    // export
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

        if (queryParam != null) {
            searchQuery = queryParam;
            doBinarySearch(null);
        }

        if (searchQuery == null) {
            searchQuery = "*";
            doBinarySearch(null);
            disclosedTabName = "search";
        }
    }

    public String doBinarySearchAction() {
        doBinarySearch(null);
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

        String indexDirectory = WebappUtils.getDefaultIndex(appConfigBean.getConfig()).getLocation();

        try {
            results = new SearchResultDataModel(searchQuery, indexDirectory, pageSize);

            totalResults = results.getRowCount();
            if (log.isDebugEnabled()) log.debug("\tResults: " + results.getRowCount());

            disclosedTabName = "interactions";

        } catch (TooManyResultsException e) {
            addErrorMessage("Too many results found", "Please, refine your query");
        }

        if (bindings.getResultsDataTable() != null) {
            bindings.getResultsDataTable().setFirst(0);
        }
    }

    /**
     * Action that redirects to the export servlet
     * @return null, the lifecycle is shortcircuited
     */
    public String doExport() {
        // /export?query=#{searchBean.query}&amp;format=mitab&amp;sort=#{searchBean.sortColumn}&amp;asc=#{searchBean.sortAscending}

        // to go to an external URL, we need to shortcircuit the jsf lifecycle
        FacesContext context = FacesContext.getCurrentInstance();

        String exportUrl = context.getExternalContext().getRequestContextPath()+"/export?"+
                           ExportServlet.PARAM_QUERY + "=" + results.getSearchQuery() + "&" +
                           ExportServlet.PARAM_FORMAT + "=" + exportFormat + "&" +
                           ExportServlet.PARAM_SORT + "=" + results.getSortColumn() + "&" +
                           ExportServlet.PARAM_SORT_ASC + "=" + results.isAscending();

        // short-circuit the cycle to redirect to a external page
        try {
            context.responseComplete();
            context.getExternalContext().redirect(exportUrl);
        }
        catch (IOException e) {
            throw new IntactViewException(e);
        }

        return null;
    }

    public void rangeChanged(RangeChangeEvent evt) {
        results.setRowIndex(evt.getNewStart());
        bindings.getResultsDataTable().setFirst(evt.getNewStart());
        //results.fetchResults(evt.getNewStart(), 30);
    }

    public SearchConfig.Indexes.Index getDefaultIndex() {
        return WebappUtils.getDefaultIndex(appConfigBean.getConfig());
    }


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

    public SearchResultDataModel getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(SearchResultDataModel searchResults) {
        this.searchResults = searchResults;
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
}
