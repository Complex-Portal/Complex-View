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
import org.apache.myfaces.trinidad.event.DisclosureEvent;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.FacetParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.view.webapp.controller.JpaBaseController;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;
import uk.ac.ebi.intact.view.webapp.controller.search.SearchController;
import uk.ac.ebi.intact.view.webapp.controller.search.UserQuery;
import uk.ac.ebi.intact.view.webapp.util.ExternalDbLinker;

import javax.faces.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    @Autowired
    private IntactViewConfiguration intactViewConfig;

    @Autowired
    private ExternalDbLinker dbLinker;

    private int maxSize = 200;

    private List<String> uniprotAcs;
    private List<String> geneNames;

    //browsing
    private String interproIdentifierList;
    private String chromosomalLocationIdentifierList;
    private String mRNAExpressionIdentifierList;
    private String[] reactomeIdentifierList;

   
    public BrowseController() {
    }

     /**
     * A DiscloserListener that generates the urls for all the links in the browse page
     *
     * @param evt DisclosureEvent
     */
    public void createListofIdentifiers( DisclosureEvent evt ) {
         SearchController searchController = (SearchController) getBean("searchBean");
         searchController.onListDisclosureChanged(evt);
         buildListOfIdentifiers();
    }

    public void createListofIdentifiers( ActionEvent evt ) {
        buildListOfIdentifiers();
    }

    public String createListofIdentifiersAndBrowse() {
        buildListOfIdentifiers();
        SearchController searchController = (SearchController) getBean("searchBean");
        searchController.doInteractorsSearch();
        return "browse";
    }

    private void buildListOfIdentifiers() {
        uniprotAcs = new ArrayList<String>(maxSize);
        geneNames = new ArrayList<String>(maxSize);

        final String uniprotFieldName = "uniprotkb_id";
        final String geneNameFieldName = "geneName";

        UserQuery userQuery = (UserQuery) getBean("userQuery");

        SolrQuery query = userQuery.createSolrQuery();
        query.setRows(0);
        query.setFacet(true);
        query.setFacetLimit(maxSize);
        query.setFacetMinCount(1);
        query.setFacetSort( FacetParams.FACET_SORT_COUNT);
        query.addFacetField(uniprotFieldName);
        query.addFacetField(geneNameFieldName);

        final SolrServer solrServer = intactViewConfig.getInteractionSolrServer();
        QueryResponse queryResponse;

        try {
            if (log.isDebugEnabled()) log.debug("Loading browsing id space using query: "+query);

            queryResponse = solrServer.query(query);
        } catch ( SolrServerException e) {
            addErrorMessage("Problem loading uniprot ACs", e.getMessage());
            e.printStackTrace();
            return;
        }

        final FacetField uniprotField = queryResponse.getFacetField(uniprotFieldName);

        if (uniprotField != null && uniprotField.getValues() != null) {
             for (FacetField.Count c : uniprotField.getValues()) {
                 uniprotAcs.add(c.getName());
             }
        }

        final FacetField geneNameField = queryResponse.getFacetField(geneNameFieldName);

        if (geneNameField != null && geneNameField.getValues() != null) {
             for (FacetField.Count c : geneNameField.getValues()) {
                 geneNames.add(c.getName());
             }
        }

        if (log.isDebugEnabled()) log.debug("Browse uniprot ACs: "+uniprotAcs);
        if (log.isDebugEnabled()) log.debug("Browse gene names: "+geneNames);

        this.interproIdentifierList = appendIdentifiers( uniprotAcs, ExternalDbLinker.INTERPRO_SEPERATOR);
        this.chromosomalLocationIdentifierList = appendIdentifiers( uniprotAcs, ExternalDbLinker.CHROMOSOME_SEPERATOR);
        this.mRNAExpressionIdentifierList = appendIdentifiers( uniprotAcs, ExternalDbLinker.EXPRESSION_SEPERATOR);
        this.reactomeIdentifierList =  uniprotAcs.toArray( new String[uniprotAcs.size()] );
    }

    private String appendIdentifiers( Collection<String> uniqueIdentifiers, String separator ) {
        if ( uniqueIdentifiers != null && separator != null && !uniqueIdentifiers.isEmpty()) {
            return StringUtils.join( uniqueIdentifiers, separator );
        }

        return "";
    }

    public List<String> getUniprotAcs() {
        return uniprotAcs;
    }

    public List<String> getGeneNames() {
        return geneNames;
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

    public void goReactome( ActionEvent evt ) {
        String[] selected = reactomeIdentifierList;
        //the carriage return has to be escaped as it is used in the JavaScript
        dbLinker.reactomeLinker( dbLinker.REACTOMEURL, "\\r", selected, "/view/pages/browse/browse.xhtml" );
    }

    public int getMaxSize() {
        return maxSize;
    }
}
