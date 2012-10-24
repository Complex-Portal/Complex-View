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
package uk.ac.ebi.intact.view.webapp.controller.search.facet;

import org.apache.solr.client.solrj.response.FacetField;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains the counts for faceted searches.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public abstract class AbstractCount {

    private static final String MISSING_KEY = "missing";

    private Map<String,Long> counts;

    public AbstractCount() {
        counts = new HashMap<String, Long>();
    }

    public AbstractCount(FacetField facetField) {
        this();

        if (facetField != null) {
            if (facetField.getValues() != null){
                for (FacetField.Count c : facetField.getValues()) {
                    String key = c.getName();
                    if (key == null) {
                        key = MISSING_KEY;
                    }
                    counts.put(key, c.getCount());
                }
            }
        }
    }

    public long getPhysicalCount() {
       return getCount(MISSING_KEY);
    }

    protected long getCount(String name) {
        if (counts.containsKey(name)) {
            return counts.get(name);
        }

        return 0;
    }

    public Map<String, Long> getCounts() {
        return counts;
    }
}