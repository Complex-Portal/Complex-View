package uk.ac.ebi.intact.binarysearch.webapp.view;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.BooleanQuery;
import org.apache.myfaces.component.html.ext.HtmlDataTable;
import org.apache.myfaces.trinidad.component.UIXTable;
import org.apache.myfaces.orchestra.viewController.annotations.ViewController;
import org.apache.myfaces.orchestra.viewController.annotations.InitView;
import org.springframework.stereotype.Controller;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import psidev.psi.mi.tab.PsimiTabColumn;
import uk.ac.ebi.intact.binarysearch.webapp.application.OlsBean;
import uk.ac.ebi.intact.binarysearch.webapp.application.AppConfigBean;
import uk.ac.ebi.intact.binarysearch.webapp.generated.SearchConfig;
import uk.ac.ebi.intact.binarysearch.webapp.model.SearchResultDataModel;
import uk.ac.ebi.intact.binarysearch.webapp.model.TooManyResults;
import uk.ac.ebi.intact.binarysearch.webapp.view.search.AdvancedSearch;
import uk.ac.ebi.intact.binarysearch.webapp.view.search.QueryHelper;
import uk.ac.ebi.intact.binarysearch.webapp.view.search.RelatedResults;
import uk.ac.ebi.intact.search.wsclient.SearchServiceClient;

import javax.faces.component.UIData;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.io.Serializable;
import java.util.Map;

/**
 * Main bean, that performs the searches
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller ("searchBean")
@Scope("conversation.flash")
@ViewController(viewIds = "/search.xhtml")
public class SearchBean implements Serializable
{
    private static final Log log = LogFactory.getLog(SearchBean.class);

    private static final String ADV_SEARCH_PARAM = "advSearch";
    private static final String QUERY_PARAM = "query";
    private static final String MAX_RESULTS_INIT_PARAM = "psidev.MAX_SEARCH_RESULTS";

    // injected
    @Autowired
    private AppConfigBean appConfigBean;

    @Autowired
    private OlsBean olsBean;

    // vars
    private String query;
    private AdvancedSearch advancedSearch;
    private RelatedResults relatedResults;
    private int pageSize = 50;

    // status flags
    private boolean advancedMode;
    private boolean searchDone;

    // results
    private UIComponent resultsDataTable;
    private SearchResultDataModel results;

    private String sortColumn;
    private boolean sortAscending;

    public SearchBean() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        int maxResults = Integer.parseInt(facesContext.getExternalContext().getInitParameter(MAX_RESULTS_INIT_PARAM));
        BooleanQuery.setMaxClauseCount(maxResults);

        this.advancedSearch = new AdvancedSearch();


    }

    @InitView
    public void loadFromParams() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        final String advSearchParam = facesContext.getExternalContext().getRequestParameterMap().get("advSearch");
        final String queryParam = facesContext.getExternalContext().getRequestParameterMap().get("query");

        if (advSearchParam != null) {
            this.advancedMode = true;
        }

        if (queryParam != null) {
            this.query = queryParam;
            doSearch(null);
        }
    }

    public void doSearch(ActionEvent evt) {
        relatedResults = null;

        if (isAdvancedMode()) {
            query = QueryHelper.createQuery(advancedSearch, olsBean.getInteractionTypeTerms(), olsBean.getDetectionMethodTerms());
        } else {
            query = QueryHelper.prepareQuery(query);
        }

        if (log.isDebugEnabled()) log.debug("Searching: " + query);

        String indexDirectory = getDefaultIndex().getLocation();
        try {
            if (sortColumn == null) {
                sortColumn = PsimiTabColumn.ID_A.getSortableColumnName();
            }

            results = new SearchResultDataModel(query, indexDirectory, pageSize, sortColumn, sortAscending);

            if (log.isDebugEnabled()) log.debug("\tResults: " + results.getRowCount());
        }
        catch (TooManyResults e) {
            e.printStackTrace();
            //tooManyResults = true;
        }

        if (resultsDataTable instanceof UIXTable) {
            ((UIXTable)resultsDataTable).setFirst(0);
        }


        searchDone = true;
    }

    public void doAdvancedSearch(ActionEvent evt) {
        advancedMode = true;
        doSearch(evt);
    }

    public void doCalculateRelatedResults(ActionEvent evt) {
        if (QueryHelper.isLuceneQuery(query)) {
            if (log.isDebugEnabled()) log.debug("Related results not calculated, cause it is a complex/lucene query");

            relatedResults = null;
            return;
        }
        try {
            if (log.isDebugEnabled()) log.debug("Calculating related results...");

            SearchServiceClient client = new SearchServiceClient();

            int numExperiments = client.getSearchPort().countExperimentsUsingIntactQuery(query);
            int numProteins = client.getSearchPort().countProteinsUsingIntactQuery(query);

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

    public void scrollerAction(ActionEvent evt) {
        doSearch(evt);

        searchDone = true;
    }

    public void forceSimpleMode(ActionEvent evt) {
        advancedMode = false;
    }

    public SearchConfig.Indexes.Index getDefaultIndex() {

        for (SearchConfig.Indexes.Index index : appConfigBean.getConfig().getIndexes().getIndex()) {
            if (index.isDefault()) {
                return index;
            }
        }

        return null;
    }

    public String getQuery()
    {
        return query;
    }

    public void setQuery(String query)
    {
        this.query = query;
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

    public UIComponent getResultsDataTable()
    {
        return resultsDataTable;
    }

    public void setResultsDataTable(UIComponent resultsDataTable)
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

    public String getSortColumn()
    {
        return sortColumn;
    }

    public void setSortColumn(String sortColumn)
    {
        this.sortColumn = sortColumn;
    }

    public boolean isSortAscending()
    {
        return sortAscending;
    }

    public void setSortAscending(boolean sortAscending)
    {
        this.sortAscending = sortAscending;
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
}
