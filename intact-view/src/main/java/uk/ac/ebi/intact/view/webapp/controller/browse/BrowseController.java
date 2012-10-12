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
package uk.ac.ebi.intact.view.webapp.controller.browse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.hupo.psi.mi.psicquic.model.PsicquicSolrServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.dataexchange.psimi.solr.FieldNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrSearchResult;
import uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrSearcher;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.view.webapp.IntactViewException;
import uk.ac.ebi.intact.view.webapp.application.PsicquicThreadConfig;
import uk.ac.ebi.intact.view.webapp.controller.JpaBaseController;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;
import uk.ac.ebi.intact.view.webapp.controller.search.SearchController;
import uk.ac.ebi.intact.view.webapp.controller.search.UserQuery;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Controller for the browse tab.
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1-SNAPSHOT
 */
@Controller( "browseBean" )
@Scope( "conversation.access" )
@ConversationName( "general" )
public class BrowseController extends JpaBaseController {

    private static final Log log = LogFactory.getLog( BrowseController.class );

    //identifier separators
    public static final String INTERPRO_SEPERATOR = ",";
    public static final String CHROMOSOME_SEPERATOR = ";id=";
    public static final String EXPRESSION_SEPERATOR = "+";

    private int maxSize = 200;
    private int maxSizeRNAExpression = 125;

    private Set<String> uniprotAcs;

    //browsing
    private String interproIdentifierList;
    private String chromosomalLocationIdentifierList;
    private String mRNAExpressionIdentifierList;
    private String[] reactomeIdentifierList;

    private UserQuery userQuery;
    private IntactViewConfiguration intactViewConfiguration;
    private IntactSolrSearcher solrSearcher;

    private String currentQuery;
    private SearchController searchController;
    private ExecutorService executorService;

    @Autowired
    private PsicquicThreadConfig psicquicThreadConfig;

    public BrowseController() {
    }

    @PostConstruct
    public void initialSearch() {
        this.currentQuery = null;

        if (executorService == null){

            executorService = psicquicThreadConfig.getExecutorService();
        }
    }

    public void searchOnLoad(ComponentSystemEvent evt) {
        if (userQuery == null){
            userQuery = (UserQuery) getBean("userQuery");
        }
        if (searchController == null){
            searchController = (SearchController) getBean("searchBean");
        }
        if (!FacesContext.getCurrentInstance().isPostback()) {

            buildListOfIdentifiers();
        }
    }

    private void buildListOfIdentifiers() {

        if (!hasLoadedUniprotAcs(searchController.getCurrentQuery())){

            Callable<Set<String>> uniprotAcsRunnable = createBrowserInteractorListRunnable(searchController.getCurrentQuery() != null ? searchController.getCurrentQuery() : UserQuery.STAR_QUERY, getSolrSearcher(), userQuery.isFilterSpoke(), userQuery.isIncludeNegative());
            Future<Set<String>> uniprotAcsFuture = executorService.submit(uniprotAcsRunnable);

            if (!searchController.hasLoadedInteractorResults()){
                searchController.doInteractorsSearch();
            }
            checkAndResumeBrowserInteractorListTasks(uniprotAcsFuture, searchController.getCurrentQuery());
        }
    }

    public Callable<Set<String>> createBrowserInteractorListRunnable(final String userQuery, final IntactSolrSearcher solrSearcher, final boolean filterSpoke, final boolean filterNegative) {
        return new Callable<Set<String>>() {
            public Set<String> call() {

                return collectUniprotAcs(userQuery, solrSearcher, filterSpoke, filterNegative);
            }
        };
    }

    public void checkAndResumeBrowserInteractorListTasks(Future<Set<String>> uniprotAcsFuture, String query) {

        try {
            Set<String> uniprotAcs = uniprotAcsFuture.get();
            initializeAllLists(uniprotAcs);
            this.currentQuery = query;

        } catch (InterruptedException e) {
            log.error("The intact browser search was interrupted, we cancel the task.", e);
            if (!uniprotAcsFuture.isCancelled()){
                uniprotAcsFuture.cancel(true);
            }
        } catch (ExecutionException e) {
            log.error("The intact browser search could not be executed, we cancel the task.", e);
            if (!uniprotAcsFuture.isCancelled()){
                uniprotAcsFuture.cancel(true);
            }
        }
    }

    public IntactSolrSearcher getSolrSearcher() {
        if (solrSearcher == null){
            if (intactViewConfiguration == null){
                intactViewConfiguration = (IntactViewConfiguration) getBean("intactViewConfiguration");
                final SolrServer solrServer = intactViewConfiguration.getInteractionSolrServer();
                solrSearcher = new IntactSolrSearcher(solrServer);
            }
        }
        return  solrSearcher;
    }

    public void initializeAllLists(Set<String> uniprotAcs) {
        if (uniprotAcs != null){
            this.uniprotAcs = uniprotAcs;

            initializeAllListsFromUniprotAcs();
        }
    }

    public Set<String> collectUniprotAcs(final String userQuery, final IntactSolrSearcher solrSearcher, final boolean filterSpoke, final boolean filterNegative) {
        int numberUniprotProcessed = 0;
        int first = 0;
        Set<String> uniprotAcs = new HashSet<String>();
        final String uniprotFieldNameA = FieldNames.ID_A_FACET;
        final String uniprotFieldNameB = FieldNames.ID_B_FACET;

        boolean hasMoreAcs = true;
        while (hasMoreAcs && numberUniprotProcessed < maxSize){
            try {
                String [] facetFields = buildFacetFields(uniprotFieldNameA, uniprotFieldNameB, numberUniprotProcessed);
                String query = userQuery != null ? userQuery : UserQuery.STAR_QUERY;

                String [] queryFilters = null;
                if (filterNegative && filterSpoke){
                    queryFilters = new String[]{FieldNames.ID_FACET+":uniprotkb\\:*", FieldNames.NEGATIVE+":false", FieldNames.COMPLEX_EXPANSION+":\"-\""};
                }
                else if (filterNegative){
                    queryFilters = new String[]{FieldNames.ID_FACET+":uniprotkb\\:*", FieldNames.NEGATIVE+":false"};
                }
                else if (filterSpoke){
                    queryFilters = new String[]{FieldNames.ID_FACET+":uniprotkb\\:*", FieldNames.COMPLEX_EXPANSION+":\"-\""};
                }
                else{
                    queryFilters = new String[]{FieldNames.ID_FACET+":uniprotkb\\:*"};
                }

                IntactSolrSearchResult result = solrSearcher.searchWithFacets(query, 0, 0, PsicquicSolrServer.RETURN_TYPE_COUNT, queryFilters, facetFields, first, maxSize);

                List<FacetField> facetFieldList = result.getFacetFieldList();
                if (facetFieldList == null || facetFieldList.isEmpty()){
                    hasMoreAcs = false;
                }

                for (FacetField facet : facetFieldList){
                    if (facet.getValueCount() > 0){
                        // collect uniprot ids
                        for (FacetField.Count count : facet.getValues()){
                            if (numberUniprotProcessed < maxSize){
                                String uniprotkbPrefix= CvDatabase.UNIPROT+":";
                                // only process uniprot ids
                                if (count.getName().startsWith(uniprotkbPrefix)){
                                    if (uniprotAcs.add(count.getName().substring(uniprotkbPrefix.length()))){
                                        numberUniprotProcessed++;
                                    }
                                }
                            }
                        }
                    }
                    else {
                        hasMoreAcs = false;
                    }
                }

                first+=maxSize;

            } catch (Exception e) {
                log.error("Problem loading uniprot ACs",e);

                initializeAllLists(new HashSet<String>());
                throw new IntactViewException("Problem loading uniprot ACs", e);
            }
        }

        return uniprotAcs;
    }

    private void initializeAllListsFromUniprotAcs() {
        if (log.isDebugEnabled()) log.debug("Browse uniprot ACs: "+uniprotAcs);

        Set<String> uniprotAcsFormRNAExpression = createSubsetUniprotAcsForRNAExpression();

        this.interproIdentifierList = appendIdentifiers( uniprotAcs, INTERPRO_SEPERATOR);
        this.chromosomalLocationIdentifierList = appendIdentifiers( uniprotAcs, CHROMOSOME_SEPERATOR);
        this.mRNAExpressionIdentifierList = appendIdentifiers( uniprotAcsFormRNAExpression, EXPRESSION_SEPERATOR);
        this.reactomeIdentifierList =  uniprotAcs.toArray( new String[uniprotAcs.size()] );
    }

    private Set<String> createSubsetUniprotAcsForRNAExpression() {
        Set<String> uniprotAcsFormRNAExpression = new HashSet<String>(maxSizeRNAExpression);
        int numberProcessed = 0;
        Iterator<String> uniprotAcsIterator = uniprotAcs.iterator();
        while (numberProcessed < maxSizeRNAExpression && uniprotAcsIterator.hasNext()){
            uniprotAcsFormRNAExpression.add(uniprotAcsIterator.next());
            numberProcessed++;
        }
        return uniprotAcsFormRNAExpression;
    }

    private String[] buildFacetFields(String uniprotFieldNameA, String uniprotFieldNameB, int numberUniprotProcessed) {
        String[] facetFields;

        // collect uniprot ids
        if (numberUniprotProcessed < 200){
            facetFields = new String[]{uniprotFieldNameA, uniprotFieldNameB};
        }
        else {
            facetFields = new String[]{};
        }
        return facetFields;
    }

    private String appendIdentifiers( Collection<String> uniqueIdentifiers, String separator ) {
        if ( uniqueIdentifiers != null && separator != null && !uniqueIdentifiers.isEmpty()) {
            return StringUtils.join( uniqueIdentifiers, separator );
        }

        return "";
    }

    public Set<String> getUniprotAcs() {
        return uniprotAcs;
    }

    public String getInterproIdentifierList() {
        return interproIdentifierList;
    }

    public void setInterproIdentifierList( String interproIdentifierList ) {
        this.interproIdentifierList = interproIdentifierList;
    }

    public String getChromosomalLocationIdentifierList() {
        return chromosomalLocationIdentifierList;
    }

    public void setChromosomalLocationIdentifierList( String chromosomalLocationIdentifierList ) {
        this.chromosomalLocationIdentifierList = chromosomalLocationIdentifierList;
    }

    public String getMRNAExpressionIdentifierList() {
        return mRNAExpressionIdentifierList;
    }

    public void setMRNAExpressionIdentifierList( String mRNAExpressionIdentifierList ) {
        this.mRNAExpressionIdentifierList = mRNAExpressionIdentifierList;
    }

    public String[] getReactomeIdentifierList() {
        return reactomeIdentifierList;
    }

    public void setReactomeIdentifierList( String[] reactomeIdentifierList ) {
        this.reactomeIdentifierList = reactomeIdentifierList;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public int getMaxSizeRNAExpression() {
        return maxSizeRNAExpression;
    }

    public boolean hasLoadedUniprotAcs(String userQuery) {

        if ((this.currentQuery == null && userQuery != null) || (userQuery != null && !userQuery.equals(this.currentQuery))){
            return false;
        }
        return true;
    }
}
