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
package uk.ac.ebi.intact.view.webapp.controller.search;

import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Iterator;

/**
 * This filter contains a disjunction of filters (filters with <code>OR</code> operand).
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class DisjunctionSearchFilter implements SearchFilter, FilterContainer {

    private Collection<SearchFilter> filters;
    private String field;
    private boolean clearable;
    private boolean negated;
    
    public DisjunctionSearchFilter(Collection<SearchFilter> filters) {
        this(filters, null);
    }

    public DisjunctionSearchFilter(Collection<SearchFilter> filters, String field) {
        this(filters, field, true);
    }

    public DisjunctionSearchFilter(Collection<SearchFilter> filters, String field, boolean clearable) {
        this.filters = filters;
        this.field = field;
        this.clearable = clearable;
    }

    @Override
    public String toLuceneSyntax() {
        if (filters.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder(filters.size() * 64);

        if (filters.size() > 1) sb.append("(");

        for (Iterator<SearchFilter> searchFilterIterator = filters.iterator(); searchFilterIterator.hasNext();) {
            SearchFilter filter = searchFilterIterator.next();

             if (field != null) {
                sb.append(field).append(":");
            }

            final String filterLucene = UserQueryUtils.escapeIfNecessary(filter.getValue());

            if (filterLucene.contains(" ")) sb.append("(");

            sb.append(filterLucene);

            if (filterLucene.contains(" ")) sb.append(")");

            if (searchFilterIterator.hasNext()) {
                sb.append(" ");
            }
        }


        if (filters.size() > 1) sb.append(")");

        return sb.toString();
    }

    @Override
    public String toDisplay() {
        if (filters.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder(filters.size()*64);

        if (isNegated()) sb.append("NOT ");

        if (filters.size() > 1) sb.append("(");

        sb.append(StringUtils.join(filters, " OR "));

        if (filters.size() > 1) sb.append(")");

        return sb.toString();
    }

    @Override
    public String getField() {
        return field;
    }

    @Override
    public String getValue() {
        return filters.toString();
    }

    @Override
    public boolean isClearable() {
        return clearable;
    }

    @Override
    public String getDisplayValue() {
        return filters.toString();
    }

    @Override
    public Collection<SearchFilter> getFilters() {
        return filters;
    }

    @Override
    public String toString() {
        return toDisplay();
    }

    public boolean isNegated() {
        return negated;
    }

    public void setNegated(boolean negated) {
        this.negated = negated;
    }
}
