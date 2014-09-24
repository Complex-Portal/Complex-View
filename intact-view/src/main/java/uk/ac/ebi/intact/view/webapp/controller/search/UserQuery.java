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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.solr.client.solrj.SolrQuery;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import psidev.psi.mi.calimocho.solr.converter.SolrFieldName;
import uk.ac.ebi.intact.dataexchange.psimi.solr.FieldNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.InteractionOntologyTerm;
import uk.ac.ebi.intact.view.webapp.controller.BaseController;
import uk.ac.ebi.intact.view.webapp.controller.browse.OntologyTermWrapper;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;
import uk.ac.ebi.intact.view.webapp.util.JsfUtils;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
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
    public static final String STAR_QUERY = "*";

    @Autowired
    private FilterPopulatorController filterPopulator;

    @Autowired
    private IntactViewConfiguration intactViewConfiguration;

    private String searchQuery = STAR_QUERY;
    private String ontologySearchQuery;
    private String urlFriendlyQuery;

    private InteractionOntologyTerm ontologyTerm;

    private Map<String,String> longQueriesMap;

    private Map<String, String> termMap;

    //for sorting and ordering
    private static final String DEFAULT_SORT_COLUMN = FieldNames.INTACT_SCORE_NAME;
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

    private static final String ONTOLOGY_QUERY_PARAMETER_NAME = "ontologyQuery";
    private static final String INCLUDE_NEGATIVE_PARAMETER_NAME = "includeNegative";
    private static final String FILTER_SPOKE_PARAMETER_NAME = "filterSpoke";
    private static final String QUERY_PARAMETER_NAME = "query";

    private boolean includeNegative =false;
    private boolean filterSpoke=false;
    private boolean isOntologyQuery=false;

    public UserQuery() {
        this.longQueriesMap = new HashMap<String, String>();
        termMap = Maps.newHashMap();
    }

    @PostConstruct
    public void initializeSearchFieldsAndFilters() {
        reset();

        initSearchFields();
    }

    public void reset() {
        this.searchQuery = null;
        this.ontologySearchQuery = null;
        this.userSortColumn = DEFAULT_SORT_COLUMN;
        this.userSortOrder = DEFAULT_SORT_ORDER;
        this.selectedSearchTerm = null;
        this.newQueryToken = new QueryToken("");

        clearFilters();
    }

    public void clearFilters() {
        termMap.clear();
        isOntologyQuery=false;
        includeNegative =false;
        filterSpoke=false;
    }

    public void clearInteractionFilters() {
        includeNegative =false;
        filterSpoke=false;
    }

    private void initSearchFields() {
        searchFields = new SearchField[]{
                new SearchField("", "All"),
                new SearchField(FieldNames.ID, "Interactor id (Ex: P74565)"),
                new SearchField(FieldNames.ALIAS, "Interactor alias (Ex: KHDRBS1)"),
                new SearchField(FieldNames.IDENTIFIER, "Interactor id or alias"),
                new SearchField(FieldNames.INTERACTION_ID, "Interaction id (Ex: EBI-761050)"),
                new SearchField(FieldNames.GENE_NAME, "Gene name (Ex: BRCA2)"),
                new SearchField(FieldNames.DETMETHOD, "Interaction detection method (Ex: pull down)", "detectionMethodBrowser"),
                new SearchField(FieldNames.TYPE, "Interaction type (Ex: physical association)", "interactionTypeBrowser"),
                new SearchField(FieldNames.INTERACTOR_TYPE, "Interactor type (Ex: protein)", "interactorTypeBrowser"),
                new SearchField(FieldNames.INTERACTOR_DET_METHOD, "Interactor identification method (Ex: western blot)", "participantIdentificationMethodBrowser"),
                new SearchField(FieldNames.INTERACTION_ANNOTATIONS, "Interaction annotation (Ex: imex curation)", "annotationTopicBrowser"),
                new SearchField(FieldNames.INTERACTOR_FEATURE, "Interactor feature (Ex: binding site)", "featureTypeBrowser"),
                new SearchField(FieldNames.SPECIES, "Organism (Ex: human)", "taxonomyBrowser"),
                new SearchField(FieldNames.PUBID, "Publication id (Ex: 10837477)"),
                new SearchField(FieldNames.PUBAUTH, "Author (Ex: scott)"),
                new SearchField(FieldNames.BIOLOGICAL_ROLE, "Biological role (Ex : enzyme)", "biologicalRoleBrowser"),
                new SearchField(FieldNames.INTERACTOR_XREF+":\"go", "Interactor GO xref", "goBrowser"),
                new SearchField(FieldNames.INTERACTION_XREF+":\"go", "Interaction GO xref", "interactionGoBrowser"),
                new SearchField(FieldNames.ID+":\"chebi", "ChEBI", "chebiBrowser"),
                new SearchField(FieldNames.INTERACTOR_XREF+":\"interpro", "Interpro"),
                new SearchField(FieldNames.INTERACTOR_XREF, "Interactor xref (Ex: GO:0005794)"),
                new SearchField(FieldNames.INTERACTION_XREF, "Interaction xref (Ex: GO:0005634)"),
                new SearchField(FieldNames.COMPLEX_EXPANSION, "Complex expansion", filterPopulator.getExpansionSelectItems()),
                new SearchField(FieldNames.SOURCE, "Source (Ex: i2d)", filterPopulator.getSourceSelectItems()),
                new SearchField(FieldNames.UPDATE_DATE, "Last update date (Ex: [YYYYMMDD TO YYYYMMDD])"),
                new SearchField(FieldNames.INTACT_SCORE_NAME, "Intact MI score (Ex: [0.5 TO 1])"),
                new SearchField(FieldNames.NEGATIVE, "Negative interaction", filterPopulator.getNegativeSelectItems()),
                new SearchField(FieldNames.INTERACTOR_STOICHIOMETRY, "Stoichiometry", filterPopulator.getStoichiometrySelectItems()),
                new SearchField(FieldNames.INTERACTION_PARAMETERS, "Interaction parameter", filterPopulator.getParametersSelectItems()),
                new SearchField(FieldNames.INTERACTION_ANNOTATIONS+":\"dataset", "Dataset", filterPopulator.getDatasetSelectItems()),
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

        this.newQueryToken = new QueryToken("");
    }

    public SolrQuery createSolrQuery( ) {

        if( searchQuery == null || searchQuery.trim().length() == 0 ||
                searchQuery.equals("*") || searchQuery.equals("?")) {
            searchQuery = STAR_QUERY;
        }

        autoQuoteWhenNecessary();

        SolrQuery query = new SolrQuery( searchQuery );
        query.setSortField(userSortColumn, (userSortOrder)? SolrQuery.ORDER.desc : SolrQuery.ORDER.asc);
        query.addSortField(FieldNames.BINARY, SolrQuery.ORDER.asc);

        if (includeNegative){
            query.addFilterQuery(FieldNames.NEGATIVE+":(true OR false)");
        }
        if (filterSpoke){
            query.addFilterQuery(FieldNames.COMPLEX_EXPANSION+":\"-\"");
        }

        // add default parameters if nor already there
        query.setParam("defType", "edismax");
        query.setParam("mm", "1");
        if (isOntologyQuery){
            query.setParam("qf", SolrFieldName.identifier.toString()+" "+SolrFieldName.xref.toString()+" "+SolrFieldName.pxref.toString()+" "+SolrFieldName.species.toString()+" "+SolrFieldName.detmethod.toString()+" "+SolrFieldName.type.toString()+" "+SolrFieldName.pbiorole.toString()
                    +" "+SolrFieldName.ptype.toString()+" "+SolrFieldName.ftype.toString()+" "+SolrFieldName.pmethod.toString()+" "+SolrFieldName.annot.toString()+" "+SolrFieldName.pubid.toString()+" "+SolrFieldName.pubauth.toString()+" "+SolrFieldName.interaction_id.toString());
        }
        else {
            query.setParam("qf", SolrFieldName.identifier.toString()+" "+SolrFieldName.pubid.toString()+" "
                    +SolrFieldName.pubauth.toString()+" "+SolrFieldName.species.toString()+" "+SolrFieldName.detmethod.toString()
                    +" "+SolrFieldName.type.toString()+" "+SolrFieldName.interaction_id.toString()+" "+SolrFieldName.xref.toString()
                    +" "+SolrFieldName.pxref.toString());
        }

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
                String qtokenLowerCase = qtoken.toLowerCase();
                if (qtokenLowerCase.startsWith("MI:") ||
                        qtokenLowerCase.startsWith("GO:") ||
                        qtokenLowerCase.startsWith("CHEBI:")) {
                    qtoken = "\""+qtoken+"\"";
                }

                sb.append(qtoken).append(" ");
            }

            searchQuery = sb.toString().trim();
        }
    }

    public void prepareFromOntologySearch(ActionEvent evt) {
        if (ontologyTerm != null) {
            String query = ontologyTerm.getResults().getSearchField()+":\""+ontologyTerm.getIdentifier()+"\"";
            setSearchQuery(query);
        } else {
            isOntologyQuery=true;
            setSearchQuery(JsfUtils.surroundByQuotesIfMissing(ontologySearchQuery));
        }
    }

    public void doShowAddFieldPanel(ActionEvent evt) {
        showAddFieldsPanel();

        newQueryToken = new QueryToken("");
    }

    public void doAddFieldToQuery(ActionEvent evt) {
        doAddFieldToQuery(newQueryToken);
    }

    public void doAddFieldToQuery(QueryToken queryToken) {
        String query = searchQuery;

        if (!isWildcardQuery(queryToken.getQuery())) {
            // the new field is the new query
            if (isWildcardQuery(searchQuery)) {
                final boolean excludeOperand = true;
                query = queryToken.toQuerySyntax(excludeOperand);
            }
            // add the new field in the query
            else {
                query =  surroundByBraces(query) + " " + queryToken.toQuerySyntax();
            }
        }

        setSearchQuery(query);

        hideAddFieldsPanel();
    }

    public boolean isWildcardQuery() {
        return isWildcardQuery(searchQuery);
    }

    private boolean isWildcardQuery(String query) {
        return (query == null || query.trim().length() == 0 || "*".equals(query) || "*:*".equals(query));
    }

    private String surroundByBraces(String query) {

        if (query == null){
            return null;
        }
        return "("+query+")";
    }

    public void doCancelAddField(ActionEvent evt) {
        hideAddFieldsPanel();
        this.newQueryToken = new QueryToken("");
    }

    public void doClearSearchField(ActionEvent evt) {
        clearFilters();
        setSearchQuery("");
        this.newQueryToken = new QueryToken("");
    }

    private void showAddFieldsPanel() {
        showNewFieldPanel = true;
    }

    private void hideAddFieldsPanel() {
        showNewFieldPanel = false;
    }


    private SolrQuery createSolrQueryForHierarchView() {
        // export all available rows
        return createSolrQuery( ).setRows(0);
    }

    /**
     * Builds a String representation of a Solr query without size constraint. The query would return all document hit.
     * @return a non null string.
     */
    public String getSolrQueryString() {
        return getSolrQueryString(createSolrQuery().setRows(0));
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
        return buildHierarchViewURL(intactViewConfiguration.getHierarchViewImageUrl());
    }

    public String getHierarchViewSearchUrl() {
        return buildHierarchViewURL(intactViewConfiguration.getHierarchViewSearchUrl());
    }

    public String getHierarchViewUrl() {
        return buildHierarchViewURL(intactViewConfiguration.getHierarchViewUrl());
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

    public void doAddParamOntologyTermToQuery(ActionEvent evt) {
        String term = (String)JsfUtils.getParameterValue( "term", evt);

        this.ontologySearchQuery = term;
        prepareFromOntologySearch(evt);
    }

    public void doAddParamToQuery(String operand, String field, String query) {
        doAddFieldToQuery(new QueryToken(query, field, BooleanOperand.valueOf(operand), operand));
    }

    public void doSelectCvTerm(NodeSelectEvent evt) {
        final OntologyTermWrapper data = (OntologyTermWrapper) evt.getTreeNode().getData();

        if (data.isUseName()){
            newQueryToken.setQuery(data.getTerm().getName());
        }
        else {
            newQueryToken.setQuery(data.getTerm().getId());
        }
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {

        this.searchQuery = searchQuery;
        this.ontologySearchQuery = null;

        if (searchQuery != null && !searchQuery.equals(urlFriendlyQuery)) {
            urlFriendlyQuery = prepareUrlFriendlyQuery(searchQuery);
        }
    }

    public String getUrlFriendlyQuery() {
        return urlFriendlyQuery;
    }

    public void setUrlFriendlyQuery(String query) {
        urlFriendlyQuery = prepareUrlFriendlyQuery(query);

        if (query.startsWith("longquery:")) {
            query = longQueriesMap.get(query);
        }

        setSearchQuery(query);
    }

    private String prepareUrlFriendlyQuery(String searchQuery) {
        String friendlyQuery;

        if (searchQuery.length() > 200) {
            friendlyQuery = "longquery:" + System.nanoTime();
            longQueriesMap.put(friendlyQuery, searchQuery.trim());
        } else {
            friendlyQuery = searchQuery.trim();
        }

        return friendlyQuery;
    }

    public void resetSearchQuery(){
        this.searchQuery = null;
        this.urlFriendlyQuery = null;
    }

    public String getOntologySearchQuery() {
        return ontologySearchQuery;
    }

    public void setOntologySearchQuery(String ontologySearchQuery) {
        this.ontologySearchQuery = ontologySearchQuery;
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

    public void setUpQueryParameters(ActionEvent event) {
        for (Map.Entry<String, Object> entry : event.getComponent().getAttributes().entrySet()){
            if (QUERY_PARAMETER_NAME.equals(entry.getKey())){
                setSearchQuery((String) entry.getValue());
            }
            else if (INCLUDE_NEGATIVE_PARAMETER_NAME.equals(entry.getKey())){
                setIncludeNegative(Boolean.parseBoolean((String) entry.getValue()));
            }
            else if (FILTER_SPOKE_PARAMETER_NAME.equals(entry.getKey())){
                setFilterSpoke(Boolean.parseBoolean((String) entry.getValue()));
            }
            else if (ONTOLOGY_QUERY_PARAMETER_NAME.equals(entry.getKey())){
                setOntologyQuery(Boolean.parseBoolean((String) entry.getValue()));
            }
        }
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

    public InteractionOntologyTerm getOntologyTerm() {
        return ontologyTerm;
    }

    public void setOntologyTerm(InteractionOntologyTerm ontologyTerm) {
        this.ontologyTerm = ontologyTerm;
    }

    public TreeNode getSelectedSearchTerm() {
        return selectedSearchTerm;
    }

    public void setSelectedSearchTerm(TreeNode selectedSearchTerm) {
        this.selectedSearchTerm = selectedSearchTerm;
    }

    public boolean isIncludeNegative() {
        return includeNegative;
    }

    public void setIncludeNegative(boolean includeNegative) {
        this.includeNegative = includeNegative;
    }

    public boolean isFilterSpoke() {
        return filterSpoke;
    }

    public void setFilterSpoke(boolean filterSpoke) {
        this.filterSpoke = filterSpoke;
    }

    public boolean isOntologyQuery() {
        return isOntologyQuery;
    }

    public void setOntologyQuery(boolean ontologyQuery) {
        isOntologyQuery = ontologyQuery;
    }

    public String getOntologyQueryParameterName() {
        return ONTOLOGY_QUERY_PARAMETER_NAME;
    }

    public String getIncludeNegativeParameterName() {
        return INCLUDE_NEGATIVE_PARAMETER_NAME;
    }

    public String getFilterSpokeParameterName() {
        return FILTER_SPOKE_PARAMETER_NAME;
    }

    public String getQueryParameterName() {
        return QUERY_PARAMETER_NAME;
    }
}