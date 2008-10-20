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
import org.apache.myfaces.trinidad.event.DisclosureEvent;
import org.apache.myfaces.trinidad.event.FocusEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.bridges.ontologies.OntologyIndexSearcher;
import uk.ac.ebi.intact.view.webapp.controller.BaseController;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;
import uk.ac.ebi.intact.view.webapp.controller.search.SearchController;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

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
    private IntactViewConfiguration configuration;

    @Autowired
    private SearchController searchController;

    private OntologyIndexSearcher ontologyIndexSearcher;
    private IndexSearcher interactionIndexSearcher;
    private IndexSearcher interactorIndexSearcher;

    private GoOntologyTreeModel goOntologyTreeModel;


    public GoBrowserController() {
    }

    @PostConstruct
    public void init() {
        try {
            ontologyIndexSearcher = new OntologyIndexSearcher(configuration.getDefaultOntologiesIndexLocation());
            interactionIndexSearcher = new IndexSearcher(configuration.getDefaultIndexLocation());
            interactorIndexSearcher = new IndexSearcher(configuration.getDefaultInteractorIndexLocation());
        } catch (Exception e) {
            addErrorMessage("Problem creating ontology index searcher", e.getMessage());
        }

        String searchQuery = searchController.getSearchQuery();

        if ("*".equals(searchQuery) || "?".equals(searchQuery)) {
            searchQuery = "";
        }

        goOntologyTreeModel = new GoOntologyTreeModel(ontologyIndexSearcher, interactionIndexSearcher, interactorIndexSearcher, searchQuery);

    }


    @PreDestroy
    public void destroy() {
        try {
            interactionIndexSearcher.close();
            interactorIndexSearcher.close();
            ontologyIndexSearcher.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processFocus(FocusEvent evt) {
        System.out.println("FOCUSING: "+evt.getSource());
        System.out.println("\tevt: "+evt);
    }

    public GoOntologyTreeModel getGoOntologyTreeModel() {
        return goOntologyTreeModel;
    }
}
