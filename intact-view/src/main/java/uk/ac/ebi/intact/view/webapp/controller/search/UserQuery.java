/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.view.webapp.controller.search;

import com.google.common.collect.Maps;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.solr.client.solrj.SolrQuery;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.dataexchange.psimi.solr.FieldNames;
import uk.ac.ebi.intact.view.webapp.controller.BaseController;
import uk.ac.ebi.intact.view.webapp.controller.browse.OntologyTermWrapper;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;
import uk.ac.ebi.intact.view.webapp.util.JsfUtils;
import uk.ac.ebi.intact.view.webapp.util.OntologyTerm;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * User query object wrapper.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller("userQuery")
@Scope("conversation.access")
@ConversationName("general")
public class UserQuery extends BaseController {

    private static final Log log = LogFactory.getLog( UserQuery.class );

    public static final String SESSION_SOLR_QUERY_KEY = "UserQuery.SOLR_QUERY";

    private static final String TERM_NAME_PARAM = "termName";
    public static final String STAR_QUERY = "*:*";

    @Autowired
    private FilterPopulatorController filterPopulator;

    @Autowired
    private IntactViewConfiguration intactViewConfiguration;

    @Autowired
    private ApplicationContext applicationContext;

    private String searchQuery = STAR_QUERY;
    private String ontologySearchQuery;

    private OntologyTerm ontologyTerm;

    private List<QueryToken> queryTokenList;

    private String[] datasets;
    private String[] sources;
    private String[] expansions;

    private String[] goTerms;
    private String[] chebiTerms;

    private Map<String, String> termMap = Maps.newHashMap();

    //for sorting and ordering
    private static final String DEFAULT_SORT_COLUMN = "rigid";
    private static final boolean DEFAULT_SORT_ORDER = true;

    private String userSortColumn = DEFAULT_SORT_COLUMN;
    private boolean userSortOrder = DEFAULT_SORT_ORDER;

    private int pageSize = 20;

    private boolean showNewFieldPanel;
    private QueryToken newQueryToken;

    private SearchField[] searchFields;

    private List<SelectItem> searchFieldSelectItems;
    private Map<String,SearchField> searchFieldsMap;

    private String searchBrowseName;

    private TreeNode selectedSearchTerm;

    public UserQuery() {
        this.queryTokenList = new ArrayList<QueryToken>();
    }

    @PostConstruct
    public void reset() {
        this.searchQuery = null;
        this.ontologySearchQuery = null;
        this.userSortColumn = DEFAULT_SORT_COLUMN;
        this.userSortOrder = DEFAULT_SORT_ORDER;

        clearFilters();

        initSearchFields();


    }

    public void clearFilters() {
        datasets = new String[0];
        sources = new String[0];
        expansions = new String[0];
        chebiTerms = new String[0];
        goTerms = new String[0];
        termMap.clear();
        queryTokenList.clear();
    }

    private void initSearchFields() {
        searchFields = new SearchField[]{
                new SearchField("", "All"),
                new SearchField(FieldNames.IDENTIFIER, "Participant Id"),
                new SearchField(FieldNames.INTERACTION_ID, "Interaction Id"),
                new SearchField(FieldNames.GENE_NAME, "Gene name"),
                new SearchField(FieldNames.DETMETHOD, "Interaction detection method", "detectionMethodBrowser"),
                new SearchField(FieldNames.TYPE, "Interaction type", "interactionTypeBrowser"),
                new SearchField("species", "Organism", "taxonomyBrowser"),
                new SearchField(FieldNames.PUBID, "Pubmed Id"),
                new SearchField(FieldNames.PUBAUTH, "Author"),
                new SearchField("biologicalRole", "Biological role", "biologicalRoleBrowser"),
                new SearchField("experimentalRole", "Experimental role", "experimentalRoleBrowser"),
                new SearchField("go_expanded_id", "GO", "goBrowser"),
                new SearchField("chebi_expanded_id", "ChEBI", "chebiBrowser"),
                new SearchField("intepro_expanded_id", "Interpro"),
                new SearchField("properties", "Participant cross-reference"),
                new SearchField(FieldNames.EXPANSION, "Expansion algorithm", filterPopulator.getExpansionSelectItems()),
                new SearchField(FieldNames.SOURCE, "Source", filterPopulator.getSourceSelectItems()),
                new SearchField(FieldNames.DATASET, "Dataset", filterPopulator.getDatasetSelectItems()),
                new SearchField(FieldNames.RIGID, "RIGID")
        };

        searchFieldSelectItems = new ArrayList<SelectItem>(searchFields.length);

        for (SearchField searchField : searchFields) {
            SelectItem selectItem = new SelectItem(searchField.getName(), searchField.getDisplayName());
            selectItem.setDisabled(searchField.isDisabled());
            searchFieldSelectItems.add(selectItem);
        }

        searchFieldsMap = new HashMap<String, SearchField>();

        for (SearchField field : searchFields) {
            searchFieldsMap.put(field.getName(), field);
        }
    }

    public void clearSearchFilters(ActionEvent evt) {
        clearFilters();
    }

    public SolrQuery createSolrQuery() {
        return createSolrQuery( true ); // by default include filters.
    }

    public SolrQuery createSolrQuery( final boolean includeFilters ) {

        if( searchQuery == null || searchQuery.trim().length() == 0 ||
                    searchQuery.equals("*") || searchQuery.equals("?")) {
            searchQuery = STAR_QUERY;
        }

        autoQuoteWhenNecessary();

        SolrQuery query = new SolrQuery( searchQuery );
        query.setSortField(userSortColumn, (userSortOrder)? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc);

        // store it in the HTTP Session - so it can be used by servlets (e.g. ExportServlet)
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(SESSION_SOLR_QUERY_KEY, query.toString());

        query.setRows(pageSize);

        return query;
    }

    private void autoQuoteWhenNecessary() {
        searchQuery = searchQuery.trim();

        if (!(searchQuery.startsWith("\"") && searchQuery.endsWith("\""))) {
            searchQuery = searchQuery.replaceAll("\\(", "( ");
            searchQuery = searchQuery.replaceAll("\\)", " )");

            String[] qtokens = searchQuery.split(" ");

            StringBuilder sb = new StringBuilder(searchQuery.length()+12);

            for (String qtoken : qtokens) {
                qtoken = qtoken.trim();

                if (qtoken.startsWith("MI:") ||
                        qtoken.startsWith("GO:")) {
                    qtoken = "\""+qtoken+"\"";
                }

                sb.append(qtoken).append(" ");
            }

            searchQuery = sb.toString().trim();

            searchQuery = searchQuery.replaceAll("\\( ", "(");
            searchQuery = searchQuery.replaceAll(" \\)", ")");
        }
    }

    public void prepareFromOntologySearch(ActionEvent evt) {
        if (ontologyTerm != null) {
            String query = ontologyTerm.getFieldName()+":\""+ontologyTerm.getIdentifier()+"\"";
            setSearchQuery(query);
        } else {
            setSearchQuery(buildSolrOntologyQuery(ontologySearchQuery));
        }
    }

    public String getDisplayQuery() {
        String query = "";

        if ( STAR_QUERY.equals(query)) {
            query = "*";
        }

        if ( termMap.containsKey( query ) ) {
            query = query + " (" + termMap.get( query ) + ")";
        }
        return query;
    }

    private String buildSolrOntologyQuery( String q ) {
        if (q == null) return "*:*";

        q = JsfUtils.surroundByQuotesIfMissing(q);
        return "detmethod:" + q + " OR type:" + q + " OR properties:" + q + " OR species:"+ q ;
    }

    public void doShowAddFieldPanel(ActionEvent evt) {
        showAddFieldsPanel();
        newQueryToken = new QueryToken("");
    }

    public void doAddFieldToQuery(ActionEvent evt) {
        doAddFieldToQuery(newQueryToken);
    }

    public void doAddFieldToQuery(QueryToken queryToken) {
        searchQuery = surroundByBracesIfNecessary(searchQuery);

        if (!isWildcardQuery(queryToken.getQuery())) {

            if (isWildcardQuery(searchQuery)) {
                searchQuery = queryToken.toQuerySyntax(true);
            } else {
                searchQuery = searchQuery + " " + queryToken.toQuerySyntax();
            }
        }

        hideAddFieldsPanel();
    }

    public void doAddTermToQuery(ActionEvent evt) {
        Map<String, String> requestParamsMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

        String operandStr = requestParamsMap.get("token_operand");
        BooleanOperand operand = BooleanOperand.valueOf(operandStr);
        boolean not = requestParamsMap.containsKey("token_not");
        String query = requestParamsMap.get("token_query");
        String field = requestParamsMap.get("token_field");

        QueryToken token = new QueryToken(query, field, operand);
        token.setNotQuery(not);

        doAddFieldToQuery(token);
    }

    public boolean isWildcardQuery() {
        return isWildcardQuery(searchQuery);
    }

    private boolean isWildcardQuery(String query) {
        return (query.trim().length() == 0 || "*".equals(query) || "*:*".equals(query));
    }

    private String surroundByBracesIfNecessary(String query) {
        if( query.matches( "\\(.*\\)" ) ) {
            return query;
        }

        // searching for space should be enough as it covers all 3 other cases, just left them for clarity sake.
        if  (query.contains(" ") || query.contains(" AND ") || query.contains(" OR ") || query.contains(" +")) {
            query = "("+query+")";
        }

        return query;
    }

    public void doCancelAddField(ActionEvent evt) {
        hideAddFieldsPanel();
    }

    private void showAddFieldsPanel() {
        showNewFieldPanel = true;
    }

    private void hideAddFieldsPanel() {
        newQueryToken = null;
        showNewFieldPanel = false;
    }


    private SolrQuery createSolrQueryForHierarchView() {
        // export all available rows
        return createSolrQuery( true ).setRows( 0 );
    }

    /**
     * Builds a String representation of a Solr query without size constraint. The query would return all document hit.
     * @return a non null string.
     */
    public String getSolrQueryString() {
        return getSolrQueryString( createSolrQuery( true ).setRows( 0 ));
    }

    private String getSolrQueryString( SolrQuery query ) {
        StringBuilder sb = new StringBuilder(128);
        boolean first=true;
        final Iterator<String> namesIterator = query.getParameterNamesIterator();
        while ( namesIterator.hasNext() ) {
            String paramName =  namesIterator.next();
            final String[] params = query.getParams( paramName );

            for (String param : params) {
                if (!first) sb.append('&');
                first=false;
                sb.append(paramName);
                sb.append('=');
                if( param != null ) {
                    sb.append( param );
                }
            }
        }
        return sb.toString();
    }

    public String getHierarchViewImageUrl() {
        return buildHierarchViewURL( intactViewConfiguration.getHierarchViewImageUrl() );
    }

    public String getHierarchViewSearchUrl() {
        return buildHierarchViewURL( intactViewConfiguration.getHierarchViewSearchUrl() );
    }

    public String getHierarchViewUrl() {
        return buildHierarchViewURL( intactViewConfiguration.getHierarchViewUrl() );
    }

    private String buildHierarchViewURL( String prefix ) {
        StringBuilder sb = new StringBuilder(256);

        sb.append(prefix);
        sb.append("?sq=");

        try {
            final SolrQuery solrQuery = createSolrQueryForHierarchView();
            final String q = getSolrQueryString( solrQuery );
            final String qe = URLEncoder.encode( q, "UTF-8" );
            sb.append( qe );
        } catch (UnsupportedEncodingException e) {
            // cannot happen
            throw new RuntimeException(e);
        }

        return sb.toString();
    }

    public boolean isUsingFilters() {
        final String[] filterQueries = createSolrQuery( true ).getFilterQueries();
        return (filterQueries != null && filterQueries.length > 0);
    }

    public void addGoTerm(ActionEvent evt) {
        String param = JsfUtils.getFirstParamValue(evt);
        String termName = (String)JsfUtils.getParameterValue( TERM_NAME_PARAM, evt);
        goTerms = (String[])ArrayUtils.add(goTerms, param);
        termMap.put( param,termName );

        addToTokenList(FieldNames.DB_GO, termName);
    }

    public void addChebiTerm(ActionEvent evt) {
        String param = JsfUtils.getFirstParamValue(evt);
        String termName = (String)JsfUtils.getParameterValue( TERM_NAME_PARAM, evt);
        chebiTerms = (String[]) ArrayUtils.add(chebiTerms, param);
        termMap.put( param,termName );

        addToTokenList(FieldNames.DB_CHEBI, termName);
    }

    public void doAddParamTermToQuery(ActionEvent evt) {
        String operand = (String)JsfUtils.getParameterValue( "operand", evt);
        String field = (String)JsfUtils.getParameterValue( "field", evt);
        String query = (String)JsfUtils.getParameterValue( "queryValue", evt);

        //termMap.put( param,termName );

        doAddParamToQuery(operand, field, query);
    }

    public void doAddParamToQuery(String operand, String field, String query) {
        doAddFieldToQuery(new QueryToken(query, field, BooleanOperand.valueOf(operand)));
    }

    public void doAddParamOntologyTermToQuery(ActionEvent evt) {
        String term = (String)JsfUtils.getParameterValue( "term", evt);

        this.ontologySearchQuery = term;
        prepareFromOntologySearch(evt);
    }

    public Collection<String> getDatasetsToInclude() {
        if (!containsNotSpecified(datasets)) {
            return Arrays.asList(datasets);
        }
        return Collections.EMPTY_LIST;
    }

    public boolean isCurrentOntologyQuery() {
        return (searchQuery == null && ontologySearchQuery != null);
    }

    public void onOntologySearchCheckboxChanged(ValueChangeEvent evt) {
        if (Boolean.FALSE.equals(evt.getNewValue())) {
            ontologySearchQuery = null;
        }
    }

    public void doSelectAllDatasets(ActionEvent evt) {
        setDatasets(filterPopulator.getDatasets().toArray(new String[filterPopulator.getDatasets().size()]));
    }

    public void doUnselectDatasets(ActionEvent evt) {
        datasets = new String[0];
    }

    public void doSelectCvTerm(NodeSelectEvent evt) {
        final OntologyTermWrapper data = (OntologyTermWrapper) evt.getTreeNode().getData();

        newQueryToken.setQuery(data.getTerm().getId());
    }

    private void addToTokenList(String fieldName, String value) {
        QueryToken token = new QueryToken(value, fieldName);

        if (!queryTokenList.contains(token)) {
            queryTokenList.add(token);
        }
    }

    private void addToTokenList(String fieldName, String[] values) {
        for (String value : values) {
           addToTokenList(fieldName, value);
        }
    }

    public String getSearchQuery() {
        if ("*:*".equals(searchQuery)) {
            searchQuery = "";
        }
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {

        if (searchQuery != null) {
            searchQuery = searchQuery.replaceAll(" and ", " AND ");
            searchQuery = searchQuery.replaceAll(" or ", " OR ");
            searchQuery = searchQuery.replaceAll(" not ", " NOT ");
            searchQuery = searchQuery.replaceAll("^not ", "NOT ");
        }

        this.searchQuery = searchQuery;
        this.ontologySearchQuery = null;
    }

    public void resetSearchQuery(){
        this.searchQuery = null;
    }

    public String getOntologySearchQuery() {
        return ontologySearchQuery;
    }

    public void setOntologySearchQuery(String ontologySearchQuery) {
        this.ontologySearchQuery = ontologySearchQuery;
    }

    public String[] getSources() {
        return sources;
    }

    public void setSources(String[] sources) {
        this.sources = sources;
    }

    public String[] getDatasets() {
        return datasets;
    }

    public void setDatasets(String[] datasets) {
        this.datasets = datasets;

        addToTokenList(FieldNames.DATASET, datasets);
    }

    public String[] getExpansions() {
        return expansions;
    }

    public void setExpansions(String[] expansions) {
        this.expansions = expansions;

        addToTokenList(FieldNames.DATASET, datasets);
    }

    public static boolean containsNotSpecified(String[] values) {
        for (String value : values) {
            if (isNotSpecified(value)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNotSpecified(String value) {
        return FilterPopulatorController.NOT_SPECIFIED_VALUE.equals(value);
    }

    public String getUserSortColumn() {
        return userSortColumn;
    }

    public void setUserSortColumn( String userSortColumn ) {
        this.userSortColumn = userSortColumn;
    }

    public boolean getUserSortOrder() {
        return userSortOrder;
    }

    public void setUserSortOrder( boolean userSortOrder ) {
        this.userSortOrder = userSortOrder;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String[] getGoTerms() {
        return goTerms;
    }

    public void setGoTerms(String[] goTerms) {
        this.goTerms = goTerms;
    }

    public String[] getChebiTerms() {
        return chebiTerms;
    }

    public void setChebiTerms(String[] chebiTerms) {
        this.chebiTerms = chebiTerms;
    }

    public List<SelectItem> getGoTermsSelectItems() {
        return createSelectItems(goTerms);
    }

    public List<SelectItem> getChebiTermsSelectItems() {
        return createSelectItems(chebiTerms);
    }

    private List<SelectItem> createSelectItems(String[] values) {
        List<SelectItem> selectItems = new ArrayList<SelectItem>(values.length);

        for ( String term : values ) {
            if ( termMap.containsKey( term ) ) {
                selectItems.add( new SelectItem( term, term + " (" + termMap.get( term ) + ")" ) );
            } else {
                selectItems.add( new SelectItem( term ) );
            }
        }

        return selectItems;
    }

    public Map<String, String> getTermMap() {
        return termMap;
    }

    public void setTermMap( Map<String, String> termMap ) {
        this.termMap = termMap;
    }

       public List<SelectItem> getSearchFieldSelectItems() {
        return searchFieldSelectItems;
    }

    public List<QueryToken> getQueryTokenList() {
        return queryTokenList;
    }

    public void setQueryTokenList(List<QueryToken> queryTokenList) {
        this.queryTokenList = queryTokenList;
    }

    public boolean isShowNewFieldPanel() {
        return showNewFieldPanel;
    }

    public QueryToken getNewQueryToken() {
        return newQueryToken;
    }

    public void setNewQueryToken(QueryToken newQueryToken) {
        this.newQueryToken = newQueryToken;
    }

    public Map<String, SearchField> getSearchFieldsMap() {
        return searchFieldsMap;
    }

    public String getSearchBrowseName() {
        return searchBrowseName;
    }

    public void setSearchBrowseName(String searchBrowseName) {
        this.searchBrowseName = searchBrowseName;
    }

    public OntologyTerm getOntologyTerm() {
        return ontologyTerm;
    }

    public void setOntologyTerm(OntologyTerm ontologyTerm) {
        this.ontologyTerm = ontologyTerm;
    }

    public TreeNode getSelectedSearchTerm() {
        return selectedSearchTerm;
    }

    public void setSelectedSearchTerm(TreeNode selectedSearchTerm) {
        this.selectedSearchTerm = selectedSearchTerm;
    }
}