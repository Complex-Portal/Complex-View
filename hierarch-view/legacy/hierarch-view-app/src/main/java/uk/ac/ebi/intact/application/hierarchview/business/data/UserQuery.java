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
package uk.ac.ebi.intact.application.hierarchview.business.data;

import org.apache.solr.client.solrj.SolrQuery;
import uk.ac.ebi.intact.application.hierarchview.business.graph.HVNetworkBuilder;

/**
 * Abstraction for the query.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class UserQuery {

    private SolrQuery solrQuery;
    private String query;

    public UserQuery(String query) {
        this.query = query;
        this.solrQuery = new SolrQuery(query)
                .setStart( 0 )
                .setRows( HVNetworkBuilder.getMaxInteractions() );
    }

    public UserQuery(SolrQuery solrQuery) {
        this.solrQuery = solrQuery;
        this.query = solrQuery.getQuery();
    }

    public SolrQuery getSolrQuery() {
        return solrQuery;
    }

    public String getQuery() {
        return query;
    }
}
