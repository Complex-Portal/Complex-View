package uk.ac.ebi.intact.binarysearch.webapp.view;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.BooleanQuery;
import org.apache.myfaces.orchestra.viewController.annotations.InitView;
import org.apache.myfaces.orchestra.viewController.annotations.ViewController;
import org.apache.myfaces.trinidad.component.UIXTable;
import org.apache.myfaces.trinidad.component.core.data.CoreSelectRangeChoiceBar;
import org.apache.myfaces.trinidad.event.PollEvent;
import org.apache.myfaces.trinidad.event.RangeChangeEvent;
import org.apache.myfaces.trinidad.event.DisclosureEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.binarysearch.webapp.SearchWebappException;
import uk.ac.ebi.intact.binarysearch.webapp.application.AppConfigBean;
import uk.ac.ebi.intact.binarysearch.webapp.application.OlsBean;
import uk.ac.ebi.intact.binarysearch.webapp.generated.SearchConfig;
import uk.ac.ebi.intact.binarysearch.webapp.model.SearchResultDataModel;
import uk.ac.ebi.intact.binarysearch.webapp.model.TooManyResults;
import uk.ac.ebi.intact.binarysearch.webapp.servlet.ExportServlet;
import uk.ac.ebi.intact.binarysearch.webapp.util.WebappUtils;
import uk.ac.ebi.intact.binarysearch.webapp.view.search.AdvancedSearch;
import uk.ac.ebi.intact.binarysearch.webapp.view.search.QueryHelper;
import uk.ac.ebi.intact.binarysearch.webapp.view.search.RelatedResults;
import uk.ac.ebi.intact.persistence.svc.impl.SimpleSearchService;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.ProteinImpl;
import uk.ac.ebi.intact.services.search.BaseController;
import uk.ac.ebi.intact.services.search.controller.SearchController;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.io.IOException;

/**
 * Main bean, that performs the searches
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller ("binarySearchBean")
@Scope("conversation.access")
@ViewController(viewIds = "binarysearch.xhtml")
public class BinarySearchController extends BaseController
{
    private static final Log log = LogFactory.getLog(BinarySearchController.class);

    private static final String ADV_SEARCH_PARAM = "advSearch";
    private static final String QUERY_PARAM = "query";
    private static final String VIEW_PARAM = "view";
    private static final String MAX_RESULTS_INIT_PARAM = "psidev.MAX_SEARCH_RESULTS";

    // injected
    @Autowired
    private AppConfigBean appConfigBean;

    @Autowired
    private OlsBean olsBean;

    @Autowired
    private SearchController searchController;

    // vars
    private AdvancedSearch advancedSearch;
    private RelatedResults relatedResults;
    private int pageSize = 30;

    // status flags
    private boolean advancedMode;
    private boolean searchDone;
    private boolean relatedPollEnabled;

    private boolean showProperties;
    private boolean expandedView;

    // bindings
    private UIXTable resultsDataTable;
    private CoreSelectRangeChoiceBar rangeChoiceBar;

    // results
    private SearchResultDataModel results;

    // export
    private String exportFormat;

    public BinarySearchController() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        int maxResults = Integer.parseInt(facesContext.getExternalContext().getInitParameter(MAX_RESULTS_INIT_PARAM));
        BooleanQuery.setMaxClauseCount(maxResults);

        this.advancedSearch = new AdvancedSearch();


    }

    @InitView
    public void loadFromParams() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        final String advSearchParam = facesContext.getExternalContext().getRequestParameterMap().get(ADV_SEARCH_PARAM);
        final String queryParam = facesContext.getExternalContext().getRequestParameterMap().get(QUERY_PARAM);
        final String viewParam = facesContext.getExternalContext().getRequestParameterMap().get(VIEW_PARAM);

        if (advSearchParam != null) {
            this.advancedMode = true;
        }

        if (viewParam != null) {
            if ("exp".equals(viewParam)) {
                expandedView = true;
            } 
        }

        if (queryParam != null) {
            searchController.setSearchQuery(queryParam);
            doSearch(null);
        }
    }

    public void doSearch(ActionEvent evt) {
        String query = searchController.getSearchQuery();

        relatedResults = null;

        // reset the status of the range choice bar
        if (rangeChoiceBar != null) {
            rangeChoiceBar.setFirst(0);
        }

        if (isAdvancedMode()) {
            query = QueryHelper.createQuery(advancedSearch, olsBean.getInteractionTypeTerms(), olsBean.getDetectionMethodTerms());
        } else {
            query = QueryHelper.prepareQuery(query);
        }

        if (log.isDebugEnabled()) log.debug("Searching: " + query);

        String indexDirectory = WebappUtils.getDefaultIndex(appConfigBean.getConfig()).getLocation();
        try {

            results = new SearchResultDataModel(query, indexDirectory, pageSize);

            if (log.isDebugEnabled()) log.debug("\tResults: " + results.getRowCount());
        }
        catch (TooManyResults e) {
            e.printStackTrace();
            //tooManyResults = true;
        }

        resultsDataTable.setFirst(0);

        relatedPollEnabled = true;
        searchDone = true;
    }

    public void doAdvancedSearch(ActionEvent evt) {
        advancedMode = true;
        doSearch(evt);
    }

    public void doCalculateRelatedResults(PollEvent evt) {
        relatedPollEnabled = false;
        doCalculateRelatedResults((ActionEvent)null);
    }

    public void doCalculateRelatedResults(ActionEvent evt) {
        String query = searchController.getSearchQuery();

        if (QueryHelper.isLuceneQuery(query)) {
            if (log.isDebugEnabled()) log.debug("Related results not calculated, cause it is a complex/lucene query");

            relatedResults = null;
            return;
        }
        try {
            if (log.isDebugEnabled()) log.debug("Calculating related results...");

            SimpleSearchService searchService = new SimpleSearchService();
            int numExperiments = searchService.count(Experiment.class, query);
            int numProteins = searchService.count(ProteinImpl.class, query);

            this.relatedResults = new RelatedResults();
            relatedResults.setNumOfExperiments(numExperiments);
            relatedResults.setNumOfInteractors(numProteins);

            if (log.isDebugEnabled()) log.debug("\tReturned "+numExperiments+" experiments and "+numProteins+" proteins");

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void doClearAdvancedForm(ActionEvent evt) {
        this.advancedSearch = new AdvancedSearch();
        this.advancedMode = true;
    }

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
            throw new SearchWebappException(e);
        }

        return null;
    }

    public void rangeChanged(RangeChangeEvent evt) {
        results.setRowIndex(evt.getNewStart());
        ((UIXTable)resultsDataTable).setFirst(evt.getNewStart());
        //results.fetchResults(evt.getNewStart(), 30);
    }

    public void forceSimpleMode(ActionEvent evt) {
        advancedMode = false;
    }

    public SearchConfig.Indexes.Index getDefaultIndex() {
        return WebappUtils.getDefaultIndex(appConfigBean.getConfig());
    }

    public void doDiscoleRelatedResults(DisclosureEvent evt) {
        System.out.println("DISCLOSING RELATED RESULTS!!!");

        searchController.doSearch(null);
    }

    public boolean isAdvancedMode()
    {
        return advancedMode;
    }

    public void setAdvancedMode(boolean advancedMode)
    {
        this.advancedMode = advancedMode;
    }

    public boolean isSearchDone()
    {
        return searchDone;
    }

    public void setSearchDone(boolean searchDone)
    {
        this.searchDone = searchDone;
    }

    public UIXTable getResultsDataTable()
    {
        return resultsDataTable;
    }

    public void setResultsDataTable(UIXTable resultsDataTable)
    {
        this.resultsDataTable = resultsDataTable;
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

    public AdvancedSearch getAdvancedSearch()
    {
        return advancedSearch;
    }

    public void setAdvancedSearch(AdvancedSearch advancedSearch)
    {
        this.advancedSearch = advancedSearch;
    }

    public RelatedResults getRelatedResults()
    {
        return relatedResults;
    }

    public void setRelatedResults(RelatedResults relatedResults)
    {
        this.relatedResults = relatedResults;
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

    public boolean isRelatedPollEnabled() {
        return relatedPollEnabled;
    }

    public void setRelatedPollEnabled(boolean relatedPollEnabled) {
        this.relatedPollEnabled = relatedPollEnabled;
    }

    public CoreSelectRangeChoiceBar getRangeChoiceBar() {
        return rangeChoiceBar;
    }

    public void setRangeChoiceBar(CoreSelectRangeChoiceBar rangeChoiceBar) {
        this.rangeChoiceBar = rangeChoiceBar;
    }
}
