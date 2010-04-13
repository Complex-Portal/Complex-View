/**
 * Copyright 2010 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.view.webapp;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.ac.ebi.intact.view.webapp.controller.BaseController;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;
import uk.ac.ebi.intact.view.webapp.controller.search.UserQuery;
import uk.ac.ebi.intact.view.webapp.model.LazySearchResultDataModel;

import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Component
@Scope("session")
public class TestController extends BaseController {

    @Autowired
    private IntactViewConfiguration config;

    private String query;
    private LazySearchResultDataModel results;

    public void search(ActionEvent evt) {
        SolrServer solrServer = config.getInteractionSolrServer();

        SolrQuery solrQuery = new SolrQuery( query );
        solrQuery.setSortField("rigid", SolrQuery.ORDER.asc);

        results = new LazySearchResultDataModel(solrServer, solrQuery);
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public DataModel getResults() {
        return results;
    }

    private UserQuery getUserQuery() {
        return (UserQuery) getBean("userQuery");
    }
}
