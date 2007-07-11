/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.binarysearch.webapp.view.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.tab.PsimiTabColumn;
import uk.ac.ebi.intact.binarysearch.webapp.application.OlsBean;
import uk.ac.ebi.intact.util.ols.Term;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class QueryHelper
{
    private static Log log = LogFactory.getLog(QueryHelper.class);
    private static final String WILDCARD = "*";

    public static String prepareQuery(String query) {
        if (query == null) {
            throw new NullPointerException("query");
        }

        if (query.length() == 0) {
            return WILDCARD;
        }

        if (isLuceneQuery(query)) {
            return query;
        }

        AdvancedSearch as = new AdvancedSearch();
        as.setConjunction(false);
        as.setDetectionMethod(query);
        as.setIdentifier(query);
        as.setInteractionType(query);
        as.setPubFirstAuthor(query);
        as.setPubId(query);
        as.setTaxid(query);

        return createQuery(as, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    }

    public static boolean isLuceneQuery(String query) {
        return query.contains(":");
    }

    /**
     * Creates a query String from an AdvancedSearch object
     *
     * @param search
     * @param interactionTypeTerms
     * @param detectionMethodTerms
     *
     * @return
     */
    public static String createQuery(AdvancedSearch search, List<Term> interactionTypeTerms, List<Term> detectionMethodTerms) {

        if (search == null) {
            throw new NullPointerException("search");
        }

        if (interactionTypeTerms == null) {
            throw new NullPointerException("interactionTypeTerms");
        }

        if (detectionMethodTerms == null) {
            throw new NullPointerException("detectionMethodTerms");
        }

        List<String> subQueries = new ArrayList<String>();

        createSubQuery(subQueries, search.getIdentifier(), "identifiers");

        createSubQuery(subQueries, search.getPubId(), PsimiTabColumn.PUB_ID.getShortName());
        createSubQuery(subQueries, search.getPubFirstAuthor(), PsimiTabColumn.PUB_1ST_AUTHORS.getShortName());
        createSubQuery(subQueries, search.getTaxid(), "species");

        // interaction types
        Term interactionTypeTerm = termForValue(search.getInteractionType(), interactionTypeTerms);

        if (interactionTypeTerm != null) {
            createTermSubQuery(subQueries, interactionTypeTerm, search.isIncludeInteractionTypeChildren(), PsimiTabColumn.INTERACTION_TYPES.getShortName());
        } else {
            createSubQuery(subQueries, putInQuotes(search.getInteractionType()), PsimiTabColumn.INTERACTION_TYPES.getShortName());
        }

        // detection methods
        Term detectionMethodTerm = termForValue(search.getDetectionMethod(), detectionMethodTerms);

        if (detectionMethodTerm != null) {
            createTermSubQuery(subQueries, detectionMethodTerm, search.isIncludeDetectionMethodChildren(), PsimiTabColumn.INTER_DETECTION_METHODS.getShortName());
        } else {
            createSubQuery(subQueries, putInQuotes(search.getDetectionMethod()), PsimiTabColumn.INTER_DETECTION_METHODS.getShortName());
        }


        StringBuffer sb = new StringBuffer();
        String junction = (search.isConjunction()) ? " AND " : " OR ";
        for (Iterator<String> iterator = subQueries.iterator(); iterator.hasNext();) {
            String subQuery = iterator.next();
            sb.append(subQuery);

            if (iterator.hasNext()) {
                sb.append(junction);
            }
        }

        String query = sb.toString();
        if (query.length() == 0) {
            query = WILDCARD;
        }

        return query;
    }

    private static void createSubQuery(List<String> subQueryList, String value, String... colShortNames) {
        if (value != null && value.trim().length() > 0) {
            if (colShortNames.length == 1) {
                subQueryList.add(formatSubQuery(colShortNames[0], value));
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("(");
                for (int i = 0; i < colShortNames.length; i++) {
                    if (i > 0) {
                        sb.append(" OR ");
                    }
                    sb.append(formatSubQuery(colShortNames[i], value));
                }
                sb.append(")");

                subQueryList.add(sb.toString());
            }
        }
    }

    private static void createTermSubQuery(List<String> subQueryList, Term term, boolean includeChildren, String colShortName) {
        if (includeChildren) {
            List<Term> termWithChildren = OlsBean.childrenFor(term, new ArrayList<Term>());
            termWithChildren.add(term);
            createSubQuery(subQueryList, termsToValue(termWithChildren, colShortName), colShortName);
        } else {
            createSubQuery(subQueryList, putInQuotes(term.getName()), colShortName);
        }
    }

    private static String putInQuotes(String value) {
        if (value == null) return null;

        if (value.length() == 0) return value;

        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value;
        }
        return "\"" + value + "\"";
    }


    /**
     * Returns the term for the value provided. It will return null if no term exists for that value (e.g. the user
     * provides a new value or a combination of different values)
     */
    private static Term termForValue(String value, List<Term> terms) {
        if (value == null) {
            return null;
        }

        for (Term term : terms) {
            if (value.equals(term.getName())) {
                return term;
            }
        }

        return null;
    }

    /**
     * Converts a list of terms to a lucene accepted value
     *
     * @param terms the terms to process
     *
     * @return the terms as string and separated with "OR"
     */
    private static String termsToValue(List<Term> terms, String shortcolName) {
        if (terms.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < terms.size(); i++) {
            Term term = terms.get(i);

            if (i > 0) {
                sb.append(" OR " + shortcolName + ":");
            }

            sb.append(putInQuotes(term.getName()));
        }

        return sb.toString();
    }

    /**
     * Splits the subquery by its spaces and prepends the column short name to each token
     * If the passed value contains quotes, no split is done
     *
     * @param colShortName The column short name to use in the lucene search
     * @param value        the value to process
     *
     * @return query for the value
     */
    private static String formatSubQuery(String colShortName, String value) {
        if (value.contains("\"")) {
            return colShortName + ":" + value;
        }

        String[] tokens = value.split(" ");

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];

            // ignore "AND" and "OR"
            if (token.equalsIgnoreCase("AND") || token.equalsIgnoreCase("OR")) {
                continue;
            }

            // append OR between tokens
            if (i > 0) {
                sb.append(" OR ");
            }

            // append the token prepending the col name
            sb.append(colShortName + ":" + token);
        }

        return sb.toString();
    }
}