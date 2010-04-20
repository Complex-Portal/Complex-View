package uk.ac.ebi.intact.view.webapp.controller.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.hupo.psi.mi.psicquic.registry.ServiceType;
import org.hupo.psi.mi.psicquic.registry.client.PsicquicRegistryClientException;
import org.hupo.psi.mi.psicquic.registry.client.registry.DefaultPsicquicRegistryClient;
import org.hupo.psi.mi.psicquic.registry.client.registry.PsicquicRegistryClient;
import org.hupo.psi.mi.psicquic.wsclient.UniversalPsicquicClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import psidev.psi.mi.search.SearchResult;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.model.CvInteractorType;
import uk.ac.ebi.intact.view.webapp.controller.ContextController;
import uk.ac.ebi.intact.view.webapp.controller.JpaBaseController;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;
import uk.ac.ebi.intact.view.webapp.controller.details.DetailsController;
import uk.ac.ebi.intact.view.webapp.controller.list.InteractorListController;
import uk.ac.ebi.intact.view.webapp.controller.moleculeview.MoleculeViewController;
import uk.ac.ebi.intact.view.webapp.model.InteractorSearchResultDataModel;
import uk.ac.ebi.intact.view.webapp.model.InteractorWrapper;
import uk.ac.ebi.intact.view.webapp.model.LazySearchResultDataModel;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ValueChangeEvent;
import java.util.*;

/**
 * Search controller.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller("searchBean")
@Scope("conversation.access")
@ConversationName("general")
public class SearchController extends JpaBaseController {

    private static final Log log = LogFactory.getLog(SearchController.class);

    public static final String TERMID_QUERY_PARAM = "termId";

    // table IDs
    public static final String INTERACTIONS_TABLE_ID = "interactionResults";
    public static final String PROTEINS_TABLE_ID = "proteinListResults";
    public static final String COMPOUNDS_TABLE_ID = "compoundListResults";
    public static final String NUCLEIC_ACID_TABLE_ID = "nucleicAcidListResults";

    @Autowired
    private IntactViewConfiguration intactViewConfiguration;

    @Autowired
    private SearchCache searchCache;

    @Autowired
    private ContextController contextController;

    private int totalResults;
    private int interactorTotalResults;
    private int proteinTotalResults;
    private int smallMoleculeTotalResults;

    private int nucleicAcidTotalResults;

    private int unfilteredTotalCount;

    private boolean showProperties;
    private boolean showAlternativeIds;
    private boolean showBrandNames;

    private boolean expandedView;

    // results
    private LazySearchResultDataModel results;
    private InteractorSearchResultDataModel proteinResults;
    private InteractorSearchResultDataModel smallMoleculeResults;
    private InteractorSearchResultDataModel nucleicAcidResults;

    // io
    private String exportFormat;

    // psicquic
    private List<ServiceType> services;
    private List<ServiceType> imexServices;
    private int countInOtherDatabases;
    private int countInOtherImexDatabases;
    private int otherDatabasesWithResults;
    private int otherImexDatabasesWithResults;
    private Map<String,UniversalPsicquicClient> psicquicClientCache;
    private boolean psicquicQueryRunning;

    //sorting
    private static final String DEFAULT_SORT_COLUMN = "rigid";
    private static final boolean DEFAULT_SORT_ORDER = true;

    private String userSortColumn = DEFAULT_SORT_COLUMN;
    //as the Sort constructor is Sort(String field, boolean reverse)
    private boolean ascending = DEFAULT_SORT_ORDER;

    public SearchController() {
    }

    public void searchOnLoad(ComponentSystemEvent evt) {
        FacesContext context = FacesContext.getCurrentInstance();

        UserQuery userQuery = getUserQuery();

        String statusParam = context.getExternalContext().getRequestParameterMap().get("status");

        if (statusParam != null && "exp".equals(statusParam)) {
            addWarningMessage("Session expired", "The user session was expired due to intactivity or the server being restarted");
        }

        SolrQuery solrQuery = userQuery.createSolrQuery();
        doBinarySearch( solrQuery );
    }

    public String doBinarySearchAction() {
        UserQuery userQuery = getUserQuery();
        SolrQuery solrQuery = userQuery.createSolrQuery();

        doBinarySearch( solrQuery );

        return "pages/interactions/interactions.xhtml?faces-redirect=true&includeViewParams=true";
    }

    public String doNewBinarySearch() {
        resetDetailControllers();

        UserQuery userQuery = getUserQuery();
        userQuery.setOntologySearchQuery(null);

        return doBinarySearchAction();
    }

    public void doBinarySearch(ActionEvent evt) {
        doBinarySearchAction();
    }

    public void doClearFilterAndSearch(ActionEvent evt) {
        UserQuery userQuery = getUserQuery();
        userQuery.clearFilters();
        doBinarySearch(evt);
    }

    public String doOntologySearchAction() {
        UserQuery userQuery = getUserQuery();
        final String query = userQuery.getOntologySearchQuery();

        if ( query == null) {
            addErrorMessage("The ontology query box was empty", "No search was submitted");
            return "search";
        }

        SolrQuery solrQuery = userQuery.createSolrQuery();

        doBinarySearch( solrQuery );

        return "interactions";
    }

    public String doNewOntologySearch() {
        UserQuery userQuery = getUserQuery();
        userQuery.resetSearchQuery();
        resetDetailControllers();
        return doOntologySearchAction();
    }

    private void resetDetailControllers() {
        DetailsController detailsController = (DetailsController) getBean("detailsBean");
        MoleculeViewController moleculeViewController = (MoleculeViewController) getBean("moleculeViewBean");

        detailsController.setInteraction(null);
        moleculeViewController.setInteractor(null);
    }

    public void doBinarySearch(SolrQuery solrQuery) {
        try {
            if ( log.isDebugEnabled() ) {log.debug( "\tSolrQuery:  "+ solrQuery.getQuery() );}

            results = createInteractionDataModel( solrQuery );

            totalResults = results.getRowCount();
            unfilteredTotalCount = countUnfilteredInteractions();

            if ( log.isDebugEnabled() ) log.debug( "\tResults: " + results.getRowCount() );

//            if ( totalResults == 0 ) {
//                addInfoMessage( "Your query didn't return any results in "+intactViewConfiguration.getWebappName(), "Try a different query" );
//            }

        } catch ( uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrException solrException ) {

            final String query = solrQuery.getQuery();
            if ( query != null && ( query.startsWith( "*" ) || query.startsWith( "?" ) ) ) {
                getUserQuery().setSearchQuery( "*:*" );
                addErrorMessage( "Your query '"+ query +"' is not correctly formatted",
                                 "Currently we do not support queries prefixed with wildcard characters such as '*' or '?'. " +
                                 "However, wildcard characters can be used anywhere else in one's query (eg. g?vin or gav* for gavin). " +
                                 "Please do reformat your query." );
            }
        }

        countInOtherDatabases = -1;
        countInOtherImexDatabases = -1;
    }

    public void doPsicquicQuery(ActionEvent evt) {
        if (psicquicQueryRunning) {
            return;
        }

        psicquicQueryRunning = true;

        try {
            countResultsInOtherDatabases();
        } catch (Exception e) {
            e.printStackTrace();
        }

        psicquicQueryRunning = false;
    }

    private void countResultsInOtherDatabases() throws PsicquicRegistryClientException {
        final String psicquicRegistryUrl = intactViewConfiguration.getPsicquicRegistryUrl();

        if (psicquicRegistryUrl == null || psicquicRegistryUrl.length() == 0) {
            return;
        }

        if (services == null) {
            PsicquicRegistryClient registryClient = new DefaultPsicquicRegistryClient(psicquicRegistryUrl);
            services = registryClient.listActiveServices();
        }

        countInOtherDatabases = 0;
        otherDatabasesWithResults = 0;
        countInOtherImexDatabases = 0;
        otherImexDatabasesWithResults = 0;

        imexServices = new ArrayList<ServiceType>(services.size());


        for (ServiceType service : services) {
            boolean isImexService = false;

            for (String tag : service.getTags()) {
                if ("MI:0959".equals(tag)) {
                    imexServices.add(service);
                    isImexService = true;
                    break;
                }
            }

            if (intactViewConfiguration.getWebappName().contains(service.getName())) {
                continue;
            }

            List<String> lines = null;
            try {
                String query = getUserQuery().getSearchQuery();

                if (query == null || query.length() == 0) {
                    query = "*";
                }

                int psicquicCount;
                int imexCount = 0;

                UniversalPsicquicClient client = getPsicquicClientFromCache(service);
                SearchResult<BinaryInteraction> psicquicResult = client.getByQuery(query, 0, 0);
                psicquicCount = psicquicResult.getTotalCount();


                if (isImexService) {
                    final String imexQuery = createImexQuery(query);
                    SearchResult<BinaryInteraction> imexResult = client.getByQuery(imexQuery, 0, 0);
                    imexCount = imexResult.getTotalCount();
                }

                countInOtherDatabases += psicquicCount;
                countInOtherImexDatabases += imexCount;

                if (psicquicCount > 0) {
                    otherDatabasesWithResults++;
                }

                if (imexCount > 0) {
                    otherImexDatabasesWithResults++;
                }

            } catch (Throwable e) {
                e.printStackTrace();
            }

        }
    }

    private String createImexQuery(String query) {
        String filter = "interaction_id:imex";

        if (query != null && query.trim().length() > 0 && !"*".equals(query.trim())) {
            return filter+" AND ("+query.trim()+")";
        } else {
            return filter;
        }
    }

    private UniversalPsicquicClient getPsicquicClientFromCache(ServiceType service) {
        if (psicquicClientCache == null) {
            psicquicClientCache = new HashMap<String, UniversalPsicquicClient>();
        }
        
        UniversalPsicquicClient client;

        if (psicquicClientCache.containsKey(service.getName())) {
            client = psicquicClientCache.get(service.getName());
        } else {
            client = new UniversalPsicquicClient(service.getSoapUrl());
            psicquicClientCache.put(service.getName(), client);
        }

        return client;
    }

    private LazySearchResultDataModel createInteractionDataModel(SolrQuery query) {
        if (searchCache.containsInteractionKey(query)) {
            return searchCache.getInteractionModel(query);
        }

        SolrServer solrServer = intactViewConfiguration.getInteractionSolrServer();
        return new LazySearchResultDataModel(solrServer, query);
    }

    public void doInteractorsSearch() {
        doProteinsSearch();
        doSmallMoleculeSearch();
        doNucleicAcidSearch();

        interactorTotalResults = smallMoleculeTotalResults + proteinTotalResults + nucleicAcidTotalResults;

    }

    private void doProteinsSearch() {
        proteinResults = doInteractorSearch(new String[] {CvInteractorType.PROTEIN_MI_REF, CvInteractorType.PEPTIDE_MI_REF});
        proteinTotalResults = proteinResults.getRowCount();
    }

    private void doSmallMoleculeSearch() {
        smallMoleculeResults = doInteractorSearch(CvInteractorType.SMALL_MOLECULE_MI_REF);
        smallMoleculeTotalResults = smallMoleculeResults.getRowCount();
    }

    private void doNucleicAcidSearch() {
        nucleicAcidResults = doInteractorSearch(new String[] {CvInteractorType.DNA_MI_REF, CvInteractorType.RNA_MI_REF});
        nucleicAcidTotalResults = nucleicAcidResults.getRowCount();
    }

    public InteractorSearchResultDataModel doInteractorSearch(String interactorTypeMi) {
        return doInteractorSearch(new String[] {interactorTypeMi});
    }

    public InteractorSearchResultDataModel doInteractorSearch(String[] interactorTypeMis) {
        UserQuery userQuery = getUserQuery();
        final SolrQuery solrQuery = userQuery.createSolrQuery();

        if (searchCache.containsInteractorKey(solrQuery, interactorTypeMis)) {
            return searchCache.getInteractorModel(solrQuery, interactorTypeMis);
        }

        if (log.isDebugEnabled()) log.debug("Searching interactors of type ("+ Arrays.toString(interactorTypeMis)+") for query: " + solrQuery);

        final InteractorSearchResultDataModel interactorResults
                = new InteractorSearchResultDataModel(intactViewConfiguration.getInteractionSolrServer(),
                                                      solrQuery,
                                                      interactorTypeMis);
        return interactorResults;
    }

    private int countUnfilteredInteractions() {
        UserQuery userQuery = getUserQuery();

        if( !userQuery.isUsingFilters() ) {
            return totalResults;
        }

        final SolrQuery solrQuery = userQuery.createSolrQuery( false );
        solrQuery.setRows( 0 );

        if ( log.isDebugEnabled() ) {
            log.debug( "getCountUnfilteredInteractions: '"+ solrQuery.toString() +"'" );
        }

        final LazySearchResultDataModel tempResults = createInteractionDataModel( solrQuery );
        return tempResults.getRowCount();
    }

    public void doSearchInteractionsFromCompoundListSelection(ActionEvent evt) {
        doSearchInteractionsFromListSelection((InteractorListController) getBean("compoundListController"));
    }

    public void doSearchInteractionsFromProteinListSelection(ActionEvent evt) {
        doSearchInteractionsFromListSelection((InteractorListController) getBean("proteinListController"));
    }

    public void doSearchInteractionsFromDnaListSelection( ActionEvent evt ) {
        doSearchInteractionsFromListSelection((InteractorListController) getBean("nucleicAcidListController"));
    }

    private void doSearchInteractionsFromListSelection(InteractorListController interactorListController) {
        final List<InteractorWrapper> selected = Arrays.asList(interactorListController.getSelected());

        if (selected.size() == 0) {
            return;
        }

        StringBuilder sb = new StringBuilder( selected.size() * 10 );
        sb.append("identifier:(");

        for (Iterator<InteractorWrapper> iterator = selected.iterator(); iterator.hasNext();) {
            InteractorWrapper interactorWrapper = iterator.next();

            sb.append(interactorWrapper.getInteractor().getAc());

            if (iterator.hasNext()) {
                sb.append(" ");
            }
        }

        sb.append(")");

        final String query = sb.toString();

        UserQuery userQuery = getUserQuery();
        userQuery.setSearchQuery( query );

        SolrQuery solrQuery = userQuery.createSolrQuery();

        doBinarySearch(solrQuery);
    }

    public String resetSearch() {
        UserQuery userQuery = getUserQuery();
        userQuery.reset();
        userQuery.setSearchQuery( "*:*" );
        return doNewBinarySearch();
    }

    public void userSort( ValueChangeEvent event ) {
        String sortableColumn = (String)event.getNewValue();

        if ( sortableColumn == null ) {
            sortableColumn = DEFAULT_SORT_COLUMN;
        }

        UserQuery userQuery = getUserQuery();

        userQuery.setUserSortColumn( sortableColumn );
        this.setUserSortColumn( sortableColumn );
        userQuery.setUserSortOrder(DEFAULT_SORT_ORDER);
        this.setAscending( DEFAULT_SORT_ORDER );

        SolrQuery solrQuery = userQuery.createSolrQuery();

        doBinarySearch( solrQuery );

    }

    public void userSortOrder( ValueChangeEvent event ) {
        Boolean sortOrder =  (Boolean) event.getNewValue();

        UserQuery userQuery = getUserQuery();

        if ( sortOrder == null ) {
            userQuery.setUserSortOrder( DEFAULT_SORT_ORDER );
        } else {
            userQuery.setUserSortColumn( this.getUserSortColumn());
            userQuery.setUserSortOrder( sortOrder );
            this.setAscending(sortOrder);

            SolrQuery solrQuery = userQuery.createSolrQuery();

            doBinarySearch( solrQuery );
        }

    }


    // Getters & Setters
    /////////////////////

    public LazySearchResultDataModel getResults() {
        return results;
    }

    public void setResults( LazySearchResultDataModel results ) {
        this.results = results;
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

    public boolean isShowBrandNames() {
        return showBrandNames;
    }

    public void setShowBrandNames( boolean showBrandNames ) {
        this.showBrandNames = showBrandNames;
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

    public InteractorSearchResultDataModel getProteinResults() {
        return proteinResults;
    }

    public void setProteinResults( InteractorSearchResultDataModel proteinResults) {
        this.proteinResults = proteinResults;
    }

    public int getSmallMoleculeTotalResults() {
        return smallMoleculeTotalResults;
    }

    public int getProteinTotalResults() {
        return proteinTotalResults;
    }

    public InteractorSearchResultDataModel getSmallMoleculeResults() {
        return smallMoleculeResults;
    }

    public String getUserSortColumn() {
        return userSortColumn;
    }

    public void setUserSortColumn( String userSortColumn ) {
        this.userSortColumn = userSortColumn;
    }

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending( boolean ascending ) {
        this.ascending = ascending;
    }

    public int getNucleicAcidTotalResults() {
        return nucleicAcidTotalResults;
    }

    public void setNucleicAcidTotalResults( int nucleicAcidTotalResults) {
        this.nucleicAcidTotalResults = nucleicAcidTotalResults;
    }

    public InteractorSearchResultDataModel getNucleicAcidResults() {
        return nucleicAcidResults;
    }

    public int getUnfilteredTotalCount() {
        return unfilteredTotalCount;
    }

    private UserQuery getUserQuery() {
        return (UserQuery) getBean("userQuery");
    }

    public int getCountInOtherDatabases() {
        return countInOtherDatabases;
    }

    public List<ServiceType> getServices() {
        return services;
    }

    public int getOtherDatabasesWithResults() {
        return otherDatabasesWithResults;
    }

    public boolean isPsicquicQueryRunning() {
        return psicquicQueryRunning;
    }

    public int getCountInOtherImexDatabases() {
        return countInOtherImexDatabases;
    }

    public int getOtherImexDatabasesWithResults() {
        return otherImexDatabasesWithResults;
    }

    public List<ServiceType> getImexServices() {
        return imexServices;
    }
}