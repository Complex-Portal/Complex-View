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

import org.apache.commons.collections.map.LRUMap;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.view.webapp.model.InteractorSearchResultDataModel;
import uk.ac.ebi.intact.view.webapp.model.LazySearchResultDataModel;

import java.util.Arrays;
import java.util.Map;

/**
 * Caches search results.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller("searchCache")
public class SearchCache {

    private Map<String, LazySearchResultDataModel> interactionCache;
    private Map<String, InteractorSearchResultDataModel> interactorCache;

    public SearchCache() {
        this.interactionCache = new LRUMap(30);
        this.interactorCache = new LRUMap(30);
    }

    public LazySearchResultDataModel getInteractionModel(SolrQuery query) {
        String key = createKey(query, null);
        return interactionCache.get(key);
    }

    public boolean containsInteractionKey(SolrQuery query) {
        String key = createKey(query, null);
        return interactionCache.containsKey(key);
    }

    public LazySearchResultDataModel putInteraction(SolrQuery query, LazySearchResultDataModel dataModel) {
        String key = createKey(query, null);
        return interactionCache.put(key, dataModel);
    }

    public InteractorSearchResultDataModel getInteractorModel(SolrQuery query, String[] miType) {
        String key = createKey(query, miType);
        return interactorCache.get(key);
    }

    public boolean containsInteractorKey(SolrQuery query, String[] miType) {
        String key = createKey(query, miType);
        return interactorCache.containsKey(key);
    }

    public InteractorSearchResultDataModel putInteractorModel(SolrQuery query, String[] miType, InteractorSearchResultDataModel dataModel) {
        String key = createKey(query, miType);
        return interactorCache.put(key, dataModel);
    }

    protected String createKey(SolrQuery query, String[] miType) {
        return query+"_"+ Arrays.toString(miType);
    }
}