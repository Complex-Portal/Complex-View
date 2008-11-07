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

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.myfaces.trinidad.event.DisclosureEvent;
import org.apache.myfaces.trinidad.event.FocusEvent;
import org.apache.myfaces.trinidad.event.RowDisclosureEvent;
import org.apache.myfaces.trinidad.event.AttributeChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.bridges.ontologies.OntologyIndexSearcher;
import uk.ac.ebi.intact.view.webapp.controller.BaseController;
import uk.ac.ebi.intact.view.webapp.controller.application.AppConfigBean;
import uk.ac.ebi.intact.view.webapp.controller.application.IndexRequestController;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;
import uk.ac.ebi.intact.view.webapp.controller.search.SearchController;
import uk.ac.ebi.intact.view.webapp.controller.search.UserQuery;
import uk.ac.ebi.intact.view.webapp.util.WebappUtils;
import uk.ac.ebi.intact.binarysearch.webapp.generated.Index;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.List;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller("goBrowser")
@Scope("request")
public class GoBrowserController extends BaseController{

    @Autowired
    private IndexRequestController indexRequestController;

    @Autowired
    private SearchController searchController;

    private GoOntologyTreeModel goOntologyTreeModel;

    public GoBrowserController() {
    }

    @PostConstruct
    public void init() {
        final UserQuery userQuery = searchController.getUserQuery();

        String searchQuery = userQuery.getCurrentQuery();
        String luceneQuery = searchController.getResults().getResult().getLuceneQuery().toString();
        goOntologyTreeModel = new GoOntologyTreeModel(indexRequestController.getOntologyIndexSearcher(),
                                                      null,
                                                      null,
                                                      searchQuery,
                                                      luceneQuery);
    }

    public GoOntologyTreeModel getGoOntologyTreeModel() {
        return goOntologyTreeModel;
    }
}
