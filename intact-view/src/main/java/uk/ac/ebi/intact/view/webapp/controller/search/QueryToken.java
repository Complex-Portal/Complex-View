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

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class QueryToken {

    private String field;
    private String query;
    private BooleanOperand operand;
    private boolean notQuery;
    private String booleanString;

    public QueryToken(String query) {
        this(query, null);
    }

    public QueryToken(String query, String field) {
        this(query, field, BooleanOperand.AND, "AND");
    }

    public QueryToken(String query, String field, BooleanOperand operand) {
        this.query = query;
        this.field = field;
        this.operand = operand;
        this.booleanString = this.operand.toString();
    }

    public QueryToken(String query, String field, BooleanOperand operand, String operandStr) {
        this.query = query;
        this.field = field;
        this.operand = operand;
        this.booleanString = operandStr;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public BooleanOperand getOperand() {
        return operand;
    }

    public void setOperand(BooleanOperand operand) {
        if (operand != null){
            this.operand = operand;
            this.booleanString = operand.toString();
        }
        else {
            this.operand = BooleanOperand.AND;
            this.booleanString = "AND";
        }
    }

    public boolean isNotQuery() {
        return notQuery;
    }

    public void setNotQuery(boolean notQuery) {
        this.notQuery = notQuery;
    }

    public String getOperandStr() {
        return booleanString;
    }

    public void setOperandStr(String booleanStr) {

        if (booleanStr != null){
            this.booleanString = booleanStr;
            operand = BooleanOperand.valueOf(booleanStr);
        }
        else {
            this.operand = BooleanOperand.AND;
            this.booleanString = "AND";
        }
    }

    public String toQuerySyntax() {
        return toQuerySyntax(false);
    }

    public String toQuerySyntax(boolean excludeOperand) {
        StringBuffer queryString = new StringBuffer();

        if (excludeOperand) {
            queryString.append(isNotQuery() ? "NOT " : "");
        } else {
            queryString.append((operand == BooleanOperand.AND) ? (isNotQuery() ? "AND NOT " : "AND ") : (isNotQuery() ? "OR NOT " : "OR "));
        }

        queryString.append((field != null? field+":" : ""));

        // close any opened parenthesis in field name. For instance : interaction_id:"GO
        if (field.contains("\"")){

            queryString.append(query).append("\"");
        }
        else {
            escapeIfNecessary(query, queryString);
        }
        return queryString.toString();
    }

    @Override
    public String toString() {
        return toQuerySyntax();
    }

    public void escapeIfNecessary(String query, StringBuffer queryString) {
        if (query.equals(UserQuery.STAR_QUERY)){
            queryString.append(query);
            return;
        }

        // range query, do nothing
        if (query.startsWith("[") && query.endsWith("]")){
            queryString.append(query);
        }
        else if (query.contains(" ") ||
                query.contains(":") ||
                query.contains("(") ||
                query.contains(")") ||
                query.contains("-") ||
                query.contains("+")) {

            // deal with wild search
            if (query.contains("*")){
                queryString.append(query.toLowerCase()
                        .replaceAll(" ", "\\\\ ")
                        .replaceAll(":", "\\\\:")
                        .replaceAll("\\(", "\\\\(")
                        .replaceAll("\\)", "\\\\)")
                        .replaceAll("-", "\\\\-")
                        .replaceAll("\\+", "\\\\+"));
            }
            else if (query.contains("(") ||
                    query.contains(")") ||
                    query.contains("-") ||
                    query.contains("+")){
                queryString.append(query);
            }
            else {
                queryString.append("\""+query+"\"");
            }
        }
        else if (query.contains("*")){
            queryString.append(query.toLowerCase());
        }
        else {
            queryString.append(query);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueryToken that = (QueryToken) o;

        if (notQuery != that.notQuery) return false;
        if (field != null ? !field.equals(that.field) : that.field != null) return false;
        if (operand != that.operand) return false;
        if (query != null ? !query.equals(that.query) : that.query != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = field != null ? field.hashCode() : 0;
        result = 31 * result + (query != null ? query.hashCode() : 0);
        result = 31 * result + (operand != null ? operand.hashCode() : 0);
        result = 31 * result + (notQuery ? 1 : 0);
        return result;
    }
}
