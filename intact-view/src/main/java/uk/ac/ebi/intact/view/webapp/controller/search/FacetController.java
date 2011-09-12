/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.dataexchange.psimi.solr.FieldNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.SolrSearchResult;
import uk.ac.ebi.intact.view.webapp.controller.search.facet.ExpansionCount;
import uk.ac.ebi.intact.view.webapp.controller.search.facet.InteractorTypeCount;
import uk.ac.ebi.intact.view.webapp.model.LazySearchResultDataModel;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope("request")
public class FacetController {

    private static final Log log = LogFactory.getLog( FacetController.class );

    @Autowired
    private SearchController searchController;

    public FacetController() {

    }

    public ExpansionCount getExpansionCount() {
        FacetField facetField = getFacetField(FieldNames.EXPANSION);
        return new ExpansionCount(facetField);
    }

    public InteractorTypeCount getInteractorTypeCount() {
        FacetField facetField = getFacetField("interactorType_id");
        return new InteractorTypeCount(facetField);
    }

    private FacetField getFacetField(String field) {
        System.out.println( "FacetController.getFacetField(\""+ field +"\")" );
        LazySearchResultDataModel model = searchController.getResults();

        if (model == null) {
            return null;
        } else {
            log.error( "LazySearchResultDataModel is null" );
        }

        final SolrSearchResult result = model.getResult();
        if( result != null ) {
            QueryResponse queryResponse = result.getQueryResponse();
            return queryResponse.getFacetField(field);
        } else {
            log.error( "SolrSearchResult is null" );
        }

        return null;
    }
}
