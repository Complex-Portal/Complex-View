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
package uk.ac.ebi.intact.services.search.component.resultpanel;

import com.google.common.collect.Multimap;
import com.google.common.collect.HashMultimap;

import java.util.Collection;
import java.util.ArrayList;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class CategoryModel {

    private Multimap<String, CategoryItem> resultItemsByCateory;

    public CategoryModel(Collection<CategoryItem> categoryItems) {
        resultItemsByCateory = new HashMultimap<String, CategoryItem>();

        for (CategoryItem ri : categoryItems) {
            resultItemsByCateory.put(ri.getCategory(), ri);
        }
    }

    public Collection<CategoryItem> getResultItems() {
        return new ArrayList<CategoryItem>(resultItemsByCateory.values());
    }

    public Collection<CategoryItem> getResultItemsByCategory(String category) {
        return new ArrayList<CategoryItem>(resultItemsByCateory.get(category));
    }
}
