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
package uk.ac.ebi.intact.view.webapp.controller.browse;

import org.apache.commons.collections.map.LRUMap;
import org.apache.myfaces.trinidad.model.TreeModel;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller("browserCache")
public class BrowserCache {

    private Map<String, TreeModel> termCountsCache;

    public BrowserCache() {
        this.termCountsCache = new LRUMap(10);
    }

    public TreeModel get(String fieldName, SolrQuery query) {
        String key = createKey(fieldName, query);
        return termCountsCache.get(key);
    }

    public boolean containsKey(String fieldName, SolrQuery query) {
        String key = createKey(fieldName, query);
        return termCountsCache.containsKey(key);
    }

    public TreeModel put(String fieldName, SolrQuery query, TreeModel counts) {
        String key = createKey(fieldName, query);
        return termCountsCache.put(key, counts);
    }

    protected String createKey(String fieldName, SolrQuery query) {
        return fieldName+"_"+query;
    }
}
