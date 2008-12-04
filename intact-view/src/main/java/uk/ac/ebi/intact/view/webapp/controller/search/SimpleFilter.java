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

/**
 * This simple filter contain only one value.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class SimpleFilter implements SearchFilter {

    private String field;
    private String value;
    private String displayValue;
    private boolean clearable;
    private boolean negated;

    public SimpleFilter(String value) {
        this(value, null, true);
    }

    public SimpleFilter(String value, boolean clearable) {
        this(value, null, clearable, value);
    }

    public SimpleFilter(String value, String field, boolean clearable) {
        this(value, field, clearable, value);
    }


    public SimpleFilter(String value, String field, boolean clearable, String displayValue) {
        this.value = value;
        this.field = field;
        this.clearable = clearable;
        this.displayValue = displayValue;
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }

    public boolean isClearable() {
        return clearable;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public boolean isNegated() {
        return negated;
    }

    public void setNegated(boolean negated) {
        this.negated = negated;
    }

    public String toLuceneSyntax() {
        StringBuilder sb = new StringBuilder(value.length()+32);

        if (value == null) return "";

        String modifiedValue = value;

        if (field != null) {
            sb.append(field).append(":");
            modifiedValue = UserQueryUtils.escapeIfNecessary(value);
        }

        sb.append(modifiedValue);

        return sb.toString();
    }

    public String toDisplay() {
        StringBuilder sb = new StringBuilder(displayValue.length()+6);

        if (isNegated()) sb.append("NOT ");

        sb.append(UserQueryUtils.escapeIfNecessary(displayValue));

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleFilter that = (SimpleFilter) o;

        if (field != null ? !field.equals(that.field) : that.field != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 31 * (field != null ? field.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return toDisplay();
    }


}
