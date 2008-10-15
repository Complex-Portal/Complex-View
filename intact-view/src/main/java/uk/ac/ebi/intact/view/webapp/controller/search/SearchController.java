package uk.ac.ebi.intact.view.webapp.controller.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.BooleanQuery;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.myfaces.orchestra.viewController.annotations.PreRenderView;
import org.apache.myfaces.orchestra.viewController.annotations.ViewController;
import org.apache.myfaces.trinidad.event.DisclosureEvent;
import org.apache.myfaces.trinidad.event.RangeChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import psidev.psi.mi.tab.model.Alias;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.binarysearch.webapp.generated.Index;
import uk.ac.ebi.intact.binarysearch.webapp.generated.SearchConfig;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.view.webapp.controller.JpaBaseController;
import uk.ac.ebi.intact.view.webapp.controller.application.AppConfigBean;
import uk.ac.ebi.intact.view.webapp.controller.application.OntologyBean;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;
import uk.ac.ebi.intact.view.webapp.model.SearchResultDataModel;
import uk.ac.ebi.intact.view.webapp.model.TooManyResultsException;
import uk.ac.ebi.intact.view.webapp.util.WebappUtils;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.util.*;

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
    private static final String ONTOLOGY_QUERY_PARAM = "ontologyQuery";

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
    private String displayQuery;

     // vars
    private int pageSize = 30;

    private int totalResults;
    private int interactorTotalResults;

    // status flags
    private String disclosedTabName;

    private boolean showProperties;
    private boolean showAlternativeIds;

    private boolean expandedView;

    // results
    private SearchResultDataModel results;
    private SearchResultDataModel interactorResults;

    // io
    private String exportFormat;

    //browsing
    private String interproURL;
    private String chromosomalLocationURL;
    private String mRNAExpressionURL;


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
        doBinarySearch(searchQuery);
        return "main";
    }

    public String doOntologySearchAction() {
        if (ontologySearchQuery == null) {
            addErrorMessage("The ontology query box was empty", "No search was submitted");
            return "main";
        }
        doOntologySearch( ontologySearchQuery );
        return "main";
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
        return WebappUtils.getDefaultInteractionIndex(appConfigBean.getConfig());
    }

    private Set<String> prepareUniqueListofIdentifiers() {

        Set<String> uniqueIdentifiers = new HashSet<String>();
        List<IntactBinaryInteraction> results;

        if ( getInteractorResults() != null ) {
            results = getInteractorResults().getResult().getData();


            for ( IntactBinaryInteraction result : results ) {
                final Collection<CrossReference> crossReferences = result.getInteractorB().getIdentifiers();

                for ( CrossReference xRef : crossReferences ) {

                    if ( "uniprotkb".equals( xRef.getDatabase() ) ) {
                        uniqueIdentifiers.add( xRef.getIdentifier() );
                    }
                }
            }
        }
        return uniqueIdentifiers;
    }


    private Set<String> prepareUniqueListOfGeneNames() {

        Set<String> uniqueGeneNames = new HashSet<String>();
        List<IntactBinaryInteraction> results;
        
        if ( getInteractorResults() != null ) {
            results = getInteractorResults().getResult().getData();

            for ( IntactBinaryInteraction result : results ) {
                final Collection<Alias> aliases = result.getInteractorB().getAliases();

                for ( Alias alias : aliases ) {
                    uniqueGeneNames.add( alias.getName() );
                }
            }
        }
        return uniqueGeneNames;
    }

    /**
     * A DiscloserListener that generates the urls for all the links in the browse page
     * @param evt    DisclosureEvent
     */
    public void generateURLsForBrowse( DisclosureEvent evt ) {

     this.interproURL = generateURLsForGivenFormat(prepareUniqueListofIdentifiers(),"ac=",false,",");
     this.chromosomalLocationURL = generateURLsForGivenFormat(prepareUniqueListofIdentifiers(),"id=",true,";");
     this.mRNAExpressionURL =   generateURLFormRNAExpression(prepareUniqueListOfGeneNames(),"+",",");
    }

    /**
     * Generates urls for a given database using the formatting criterias
     * @param prefix     the prefix eg:ac=, id=
     * @param repeatPrefix   whether prefix is repeated or not
     * @param seperator      eg: , or ;
     * @return  the url
     */
    private String generateURLsForGivenFormat( Set<String> uniqueIdentifiers, String prefix, boolean repeatPrefix, String seperator ) {

        StringBuilder sb = new StringBuilder( 2000 );

        for ( String identifier : uniqueIdentifiers ) {
            if ( repeatPrefix ) {
                sb.append( prefix ).append( identifier ).append( seperator );
            } else {
                sb.append( identifier ).append( seperator );

            }
        }

        if ( !repeatPrefix ) {
            sb.insert( 0, prefix );
        }

        if ( sb.toString().endsWith( seperator ) ) {
            sb.deleteCharAt( sb.length() - 1 );
        }

        String url = sb.toString();
        if ( log.isDebugEnabled() ) {
            log.debug( ( "  url " + url + "  length " + url.length() ) );
        }
        return url;

    }

    public String generateURLFormRNAExpression( Set<String> uniqueGeneNames, String prefix, String seperator ) {

        StringBuilder sb = new StringBuilder( 2000 );

        for ( String geneName : uniqueGeneNames ) {
            sb.append( prefix ).append( geneName ).append( seperator );
        }

        if ( sb.toString().startsWith( prefix ) ) {
            sb.deleteCharAt( 0 );
        }

        if ( sb.toString().endsWith( seperator ) ) {
            sb.deleteCharAt( sb.length() - 1 );
        }

        String url = sb.toString();
        if ( log.isDebugEnabled() ) {
            log.debug( ( "  url " + url + "  length " + url.length() ) );
        }
        return url;
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
        displayQuery = searchQuery;
        
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

    public String getDisplayQuery() {
        return displayQuery;
    }

    public void setDisplayQuery( String displayQuery ) {
        this.displayQuery = displayQuery;
    }

    public String getInterproURL() {
        return interproURL;
    }

    public void setInterproURL( String interproURL ) {
        this.interproURL = interproURL;
    }

    public String getChromosomalLocationURL() {
        return chromosomalLocationURL;
    }

    public void setChromosomalLocationURL( String chromosomalLocationURL ) {
        this.chromosomalLocationURL = chromosomalLocationURL;
    }

    public String getMRNAExpressionURL() {
        return mRNAExpressionURL;
    }

    public void setMRNAExpressionURL( String mRNAExpressionURL ) {
        this.mRNAExpressionURL = mRNAExpressionURL;
    }
}
