package uk.ac.ebi.intact.view.webapp.controller.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.BooleanQuery;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.myfaces.orchestra.viewController.annotations.PreRenderView;
import org.apache.myfaces.orchestra.viewController.annotations.ViewController;
import org.apache.myfaces.trinidad.component.UIXTable;
import org.apache.myfaces.trinidad.event.RangeChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.binarysearch.webapp.generated.Index;
import uk.ac.ebi.intact.binarysearch.webapp.generated.SearchConfig;
import uk.ac.ebi.intact.model.CvInteractorType;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.view.webapp.controller.JpaBaseController;
import uk.ac.ebi.intact.view.webapp.controller.application.AppConfigBean;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;
import uk.ac.ebi.intact.view.webapp.model.SearchResultDataModel;
import uk.ac.ebi.intact.view.webapp.model.TooManyResultsException;
import uk.ac.ebi.intact.view.webapp.util.WebappUtils;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Search controller.
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


    private UserQuery userQuery;

     // vars
    private int pageSize = 30;

    private int totalResults;
    private int interactorTotalResults;
    private int proteinTotalResults;
    private int smallMoleculeTotalResults;

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
        userQuery = new UserQuery();
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

        if (queryParam != null) {
            userQuery.setDisplayQuery( queryParam );
            userQuery.setSearchQuery( queryParam );
            doBinarySearch( userQuery );
        }

        if ( ontologyQueryParam != null ) {
            doOntologySearch( ontologyQueryParam );
        }

        if (userQuery.getSearchQuery() == null) {
            userQuery.setSearchQuery( "*" );
            userQuery.setDisplayQuery( "*" );
            doBinarySearch(userQuery);
        }
    }

    public String doBinarySearchAction() {
        userQuery.getFilters().clear();
        userQuery.setDisplayQuery( userQuery.getSearchQuery() );
        userQuery.setCurrentOntologyQuery( false );
        doBinarySearch( userQuery );

        return "interactions";
    }

    public String doOntologySearchAction() {
        final String query = userQuery.getOntologySearchQuery();

        if ( query == null) {
            addErrorMessage("The ontology query box was empty", "No search was submitted");
            return "search";
        }

        doOntologySearch(query);

        return "interactions";
    }

    public void doOntologySearch(String ontologySearch) {
        userQuery.setCurrentOntologyQuery( true );
        userQuery.setDisplayQuery( ontologySearch );

        final String formattedQuery = prepareOntologyQuery(ontologySearch);
        userQuery.setSearchQuery( formattedQuery );
        doBinarySearch( userQuery );
    }

    private String prepareOntologyQuery(String ontologySearchQuery) {
        String identifier = (ontologySearchQuery.startsWith("\""))? ontologySearchQuery : "\""+ontologySearchQuery+"\"";
        return "detmethod:"+identifier+" type:"+identifier+" properties:"+identifier;
    }

    public void doBinarySearch(ActionEvent evt) {
        userQuery.getFilters().clear();
        doBinarySearch( userQuery );
    }

    public void doFilteredBinarySearch(ActionEvent evt) {
        final Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String query = params.get("query");
        String termId = params.get("termId");
        if( query == null || termId == null ) {
            throw new IllegalStateException( "Query or TermId was null. termId:"+termId+" query:"+query );
        }
        userQuery.setSearchQuery( query );
        userQuery.processIncomingFilter( termId );

        doBinarySearch( userQuery );
    }

    private void doBinarySearch(UserQuery userQuery) {

        String query = userQuery.getInteractionSearchQuery();

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
            }

        } catch (TooManyResultsException e) {
            addErrorMessage("Too many results found", "Please, refine your query");
            e.printStackTrace();
        }

        doInteractorSearch(userQuery.getInteractorSearchQuery());
    }

    public void doInteractorSearch(String query) {
        doProteinsSearch(query);
        doSmallMoleculeSearch(query);
        interactorTotalResults = smallMoleculeTotalResults + proteinTotalResults;
    }

    private void doProteinsSearch(String query) {
        // reset the status of the range choice bar
        if (log.isDebugEnabled()) log.debug("Searching proteins (raw): " + query);

        query = QueryHelper.prepareInteractorQuery(query, CvInteractorType.PROTEIN_MI_REF);

        if (log.isDebugEnabled()) log.debug("Searching proteins (prepared query): " + query);

        String indexDirectory = WebappUtils.getDefaultInteractorIndex(appConfigBean.getConfig()).getLocation();

        try {
            interactorResults = new SearchResultDataModel(query, indexDirectory, pageSize);
            proteinTotalResults = interactorResults.getRowCount();
        } catch (TooManyResultsException e) {
            addErrorMessage("Too many proteins found", "Please, refine your query");
            proteinTotalResults = 0;
            interactorTotalResults = 0;
        }
    }

    private void doSmallMoleculeSearch(String query) {
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
            smallMoleculeTotalResults = 0;
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

        StringBuilder sb = new StringBuilder( selected.size() * 10 );
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

        final String query = sb.toString();
        userQuery.setSearchQuery( query );
        userQuery.setDisplayQuery( query );

        doBinarySearch(userQuery);

        resetSelection(tableName);
    }

    public Index getDefaultInteractorIndex() {
        return WebappUtils.getDefaultInteractorIndex(appConfigBean.getConfig());
    }

    public void resetSearch(ActionEvent event) {
        final String query = "*";
        userQuery.setSearchQuery( query );
        userQuery.setDisplayQuery( query );
        userQuery.setCurrentOntologyQuery( false );
        userQuery.setOntologySearchQuery(null);
        doBinarySearch( userQuery );
    }

    // Getters & Setters
    /////////////////////

    public SearchResultDataModel getResults() {
        return results;
    }

    public void setResults( SearchResultDataModel results ) {
        this.results = results;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize( int pageSize ) {
        this.pageSize = pageSize;
    }

    public boolean isShowProperties() {
        return showProperties;
    }

    public void setShowProperties( boolean showProperties ) {
        this.showProperties = showProperties;
    }

    public boolean isShowAlternativeIds() {
        return showAlternativeIds;
    }

    public void setShowAlternativeIds( boolean showAlternativeIds ) {
        this.showAlternativeIds = showAlternativeIds;
    }

    public boolean isExpandedView() {
        return expandedView;
    }

    public void setExpandedView( boolean expandedView ) {
        this.expandedView = expandedView;
    }

    public String getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat( String exportFormat ) {
        this.exportFormat = exportFormat;
    }

    public UserQuery getUserQuery() {
        return userQuery;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults( int totalResults ) {
        this.totalResults = totalResults;
    }

    public int getInteractorTotalResults() {
        return interactorTotalResults;
    }

    public void setInteractorTotalResults( int interactorTotalResults ) {
        this.interactorTotalResults = interactorTotalResults;
    }

    public SearchResultDataModel getInteractorResults() {
        return interactorResults;
    }

    public void setInteractorResults( SearchResultDataModel interactorResults ) {
        this.interactorResults = interactorResults;
    }

    public int getSmallMoleculeTotalResults() {
        return smallMoleculeTotalResults;
    }

    public int getProteinTotalResults() {
        return proteinTotalResults;
    }

    public SearchResultDataModel getSmallMoleculeResults() {
        return smallMoleculeResults;
    }
}