/**
 * Copyright 2011 The European Bioinformatics Institute, and others.
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

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.dataexchange.psimi.solr.CoreNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrIndexer;
import uk.ac.ebi.intact.model.Publication;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
public class SolrServerTestController implements InitializingBean{

    private CommonsHttpSolrServer interactionsSolrServer;
    private StreamingUpdateSolrServer ontologiesSolrServer;
    private IntactSolrIndexer indexer;

    
    @Override
    public void afterPropertiesSet() throws Exception {
        this.interactionsSolrServer = new CommonsHttpSolrServer(Constants.SOLR_URL+"/"+CoreNames.CORE_PUB);
        this.ontologiesSolrServer = new StreamingUpdateSolrServer(Constants.SOLR_URL+"/"+CoreNames.CORE_ONTOLOGY_PUB, 4, 4);

        indexer = new IntactSolrIndexer( interactionsSolrServer, ontologiesSolrServer );
    }

    public IntactSolrIndexer getIndexer() {
        return indexer;
    }
}
