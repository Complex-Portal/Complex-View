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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.apache.solr.client.solrj.SolrQuery;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.bridges.ontologies.OntologyIndexSearcher;
import uk.ac.ebi.intact.view.webapp.controller.BaseController;
import uk.ac.ebi.intact.view.webapp.controller.search.SearchController;
import uk.ac.ebi.intact.view.webapp.controller.search.UserQuery;
import uk.ac.ebi.intact.view.webapp.util.GoOntologyTerm;

import javax.annotation.PostConstruct;

/**
 * Controller for GoBrowsing
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller("goBrowser")
@Scope("request")
public class GoBrowserController extends BaseController{

    @Autowired
    private SearchController searchController;

    @Autowired
    private UserQuery userQuery;

    private OntologyTreeModel goOntologyTreeModel;

    public GoBrowserController() {
    }

    @PostConstruct
    public void init() {
        SolrQuery query = userQuery.createSolrQuery();

        // TODO fix this
        String luceneQuery = "";
        String searchQuery = null;
        OntologyIndexSearcher searcher = null;
        //String luceneQuery = searchController.getResults().getResult().getLuceneQuery().toString();

        final OntologyTerm goOntologyRoot = new GoOntologyTerm(searcher);
        goOntologyTreeModel = new OntologyTreeModel(goOntologyRoot,
                                                      null,
                                                      null,
                                                      searchQuery,
                                                      luceneQuery);
    }

    public OntologyTreeModel getOntologyTreeModel() {
        return goOntologyTreeModel;
    }
}
