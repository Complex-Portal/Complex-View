package uk.ac.ebi.intact.view.webapp.controller.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.BooleanQuery;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.myfaces.orchestra.viewController.annotations.PreRenderView;
import org.apache.myfaces.orchestra.viewController.annotations.ViewController;
import org.apache.myfaces.trinidad.event.RangeChangeEvent;
import org.apache.myfaces.trinidad.component.UIXTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.binarysearch.webapp.generated.Index;
import uk.ac.ebi.intact.binarysearch.webapp.generated.SearchConfig;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.view.webapp.controller.JpaBaseController;
import uk.ac.ebi.intact.view.webapp.controller.application.AppConfigBean;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;
import uk.ac.ebi.intact.view.webapp.model.SearchResultDataModel;
import uk.ac.ebi.intact.view.webapp.model.TooManyResultsException;
import uk.ac.ebi.intact.view.webapp.util.WebappUtils;
import uk.ac.ebi.intact.model.CvInteractorType;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

/**
 * Main search controller
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller("searchBean")
@Scope("conversation.access")
@ConversationName("general")
@ViewController(viewIds = {"/main.xhtml",
                           "/pages/search/search.xhtml",
                           "/pages/interactions/interactions.xhtml",
                           "/pages/list/protein_list.xhtml",
                           "/pages/list/compound_list.xhtml",
                           "/pages/molecule/molecule.xhtml",
                           "/pages/graph/graph.xhtml",
                           "/pages/browse/browse.xhtml",
                           "/pages/browse/gobrowser.xhtml"})
public class SearchController extends JpaBaseController {

    private static final Log log = LogFactory.getLog(SearchController.class);

    private static final String QUERY_PARAM = "query";
    private static final String TAB_PARAM = "tab";
    private static final String ONTOLOGY_QUERY_PARAM = "ontologyQuery";

    // table IDs
    public static final String INTERACTIONS_TABLE_ID = "interactionResults";
    public static final String PROTEINS_TABLE_ID = "proteinListResults";
    public static final String COMPOUNDS_TABLE_ID = "compoundListResults";


    // injected
    @Autowired
    private AppConfigBean appConfigBean;

    @Autowired
    private IntactViewConfiguration intactViewConfiguration;

    private String searchQuery;
    private String ontologySearchQuery;
    private String displayQuery;
    private boolean currentOntologyQuery;

     // vars
    private int pageSize = 30;

    private int totalResults;
    private int interactorTotalResults;
    private int smallMoleculeTotalResults;

    // status flags
    private String disclosedTabName;

    private boolean showProperties;
    private boolean showAlternativeIds;

    private boolean expandedView;

    // results
    private SearchResultDataModel results;
    private SearchResultDataModel interactorResults;
    private SearchResultDataModel smallMoleculeResults;

    // io
    private String exportFormat;


    public SearchController() {
    }

    @PostConstruct
    public void postInstantiation() {
        BooleanQuery.setMaxClauseCount(intactViewConfiguration.getLuceneMaxCombinations());
    }

    @PreRenderView
    public void initialParams() {
        FacesContext context = FacesContext.getCurrentInstance();

        String queryParam = context.getExternalContext().getRequestParameterMap().get(QUERY_PARAM);
        String ontologyQueryParam = context.getExternalContext().getRequestParameterMap().get(ONTOLOGY_QUERY_PARAM);
        String tabParam = context.getExternalContext().getRequestParameterMap().get(TAB_PARAM);

        if (queryParam != null) {
            displayQuery = queryParam;
            searchQuery = queryParam;
            doBinarySearch(searchQuery);
        }

        if ( ontologyQueryParam != null ) {
            doOntologySearch( ontologyQueryParam );
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
        displayQuery = searchQuery;
        setCurrentOntologyQuery( false );
        doBinarySearch(searchQuery);
        return "interactions";
    }

    public String doOntologySearchAction() {
        if (ontologySearchQuery == null) {
            addErrorMessage("The ontology query box was empty", "No search was submitted");
            return "interactions";
        }
        setCurrentOntologyQuery( true );
        doOntologySearch( ontologySearchQuery );
        return "interactions";
    }

    public void doOntologySearch(String ontologySearch) {
        displayQuery = ontologySearch;

        String formattedQuery = prepareOntologyQuery(ontologySearch);
        doBinarySearch( formattedQuery );
    }

    private String prepareOntologyQuery(String ontologySearchQuery) {
        String identifier = (ontologySearchQuery.startsWith("\""))? ontologySearchQuery : "\""+ontologySearchQuery+"\"";
        return "detmethod:"+identifier+" type:"+identifier+" properties:"+identifier;
    }

    public void doBinarySearch(ActionEvent evt) {
        doBinarySearch(searchQuery);
    }

    private void doBinarySearch(String query) {
        // reset the status of the range choice bar
        //if (bindings.getRangeChoiceBar() != null) {
        //    bindings.getRangeChoiceBar().setFirst(0);
        //}

        if (log.isDebugEnabled()) log.debug("Searching interactions (raw): " + query);

        query = QueryHelper.prepareQuery(query);

        if (log.isDebugEnabled()) log.debug("Searching interactions (prepared query): " + query);

        final SearchConfig config = appConfigBean.getConfig();

        if (config == null) {
            addErrorMessage("Configuration file was not found", "Run the first time setup or check the application configuration");
            return;
        }

        String indexDirectory = WebappUtils.getDefaultInteractionIndex(config).getLocation();

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

        //if (bindings.getResultsDataTable() != null) {
        //    bindings.getResultsDataTable().setFirst(0);
        //}

        doInteractorSearch(query);
    }

    public void doInteractorSearch(String query) {
        doProteinsSearch(query);
        doSmallMoleculeSearch(query);
    }

    public void doProteinsSearch(String query) {
        // reset the status of the range choice bar
        if (log.isDebugEnabled()) log.debug("Searching interactors (raw): " + query);

        query = QueryHelper.prepareInteractorQuery(query, CvInteractorType.PROTEIN_MI_REF);

        if (log.isDebugEnabled()) log.debug("Searching interactors (prepared query): " + query);

        String indexDirectory = WebappUtils.getDefaultInteractorIndex(appConfigBean.getConfig()).getLocation();

        try {
            interactorResults = new SearchResultDataModel(query, indexDirectory, pageSize);
            interactorTotalResults = interactorResults.getRowCount();
        } catch (TooManyResultsException e) {
            addErrorMessage("Too many interactors found", "Please, refine your query");
            interactorTotalResults = 0;
        }
    }

    public void doSmallMoleculeSearch(String query) {
        // reset the status of the range choice bar
        if (log.isDebugEnabled()) log.debug("Searching small molecules (raw): " + query);

        query = QueryHelper.prepareInteractorQuery(query, CvInteractorType.SMALL_MOLECULE_MI_REF);

        if (log.isDebugEnabled()) log.debug("Searching small molecules (prepared query): " + query);

        String indexDirectory = WebappUtils.getDefaultInteractorIndex(appConfigBean.getConfig()).getLocation();

        try {
            smallMoleculeResults = new SearchResultDataModel(query, indexDirectory, pageSize);
            smallMoleculeTotalResults = smallMoleculeResults.getRowCount();
        } catch (TooManyResultsException e) {
            addErrorMessage("Too many small molecules found", "Please, refine your query");
            interactorTotalResults = 0;
        }
    }

    public void rangeChanged(RangeChangeEvent evt) {
        results.setRowIndex(evt.getNewStart());

        UIXTable table = (UIXTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(INTERACTIONS_TABLE_ID);
        table.setFirst(evt.getNewStart());

        refreshTable(INTERACTIONS_TABLE_ID, results);
    }

    public Index getDefaultIndex() {
        return WebappUtils.getDefaultInteractionIndex(appConfigBean.getConfig());
    }

    public void doSearchInteractionsFromCompoundListSelection(ActionEvent evt) {
        doSearchInteractionsFromListSelection( COMPOUNDS_TABLE_ID, "intact" );
    }

    public void doSearchInteractionsFromProteinListSelection(ActionEvent evt) {
        doSearchInteractionsFromListSelection( PROTEINS_TABLE_ID, "intact" );
    }

    private void doSearchInteractionsFromListSelection(String tableName, String database ) {
        final List<IntactBinaryInteraction> selected = getSelected(tableName);

        if (selected.size() == 0) {
            return;
        }

        StringBuilder sb = new StringBuilder(selected.size()*10);
        sb.append("id:(");

        for (Iterator<IntactBinaryInteraction> iterator = selected.iterator(); iterator.hasNext();) {
            IntactBinaryInteraction intactBinaryInteraction = iterator.next();

            String identifier = null;

            for (CrossReference xref : intactBinaryInteraction.getInteractorA().getIdentifiers()) {
                if (database.equals(xref.getDatabase())) {
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
        displayQuery = searchQuery;

        doBinarySearch(searchQuery);

        resetSelection(tableName);
    }

    public Index getDefaultInteractorIndex() {
        return WebappUtils.getDefaultInteractorIndex(appConfigBean.getConfig());
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

    public boolean isShowAlternativeIds() {
        return showAlternativeIds;
    }

    public void setShowAlternativeIds(boolean showAlternativeIds) {
        this.showAlternativeIds = showAlternativeIds;
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

    public int getSmallMoleculeTotalResults() {
        return smallMoleculeTotalResults;
    }

    public SearchResultDataModel getSmallMoleculeResults() {
        return smallMoleculeResults;
    }

    public String getOntologySearchQuery() {
        return ontologySearchQuery;
    }

    public void setOntologySearchQuery( String ontologySearchQuery ) {
        this.ontologySearchQuery = ontologySearchQuery;
    }

    public String getDisplayQuery() {
        return displayQuery;
    }

    public void setDisplayQuery( String displayQuery ) {
        this.displayQuery = displayQuery;
    }

    public boolean isCurrentOntologyQuery() {
        return currentOntologyQuery;
    }

    public void setCurrentOntologyQuery( boolean currentOntologyQuery ) {
        this.currentOntologyQuery = currentOntologyQuery;
    }
}