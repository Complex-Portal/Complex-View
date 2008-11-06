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
package uk.ac.ebi.intact.view.webapp.controller.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.tab.model.builder.MitabDocumentDefinition;

import uk.ac.ebi.intact.util.ols.Term;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Query utilities.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class QueryHelper {



    private static final Log log = LogFactory.getLog( QueryHelper.class );

    private static final String WILDCARD = "*";

    public static String prepareQuery(String query) {
        if (query == null) {
            throw new NullPointerException("You must give a non null query");
        }

        if (query.length() == 0) {
            return WILDCARD;
        }

//        if( queryNeedsEscaping( query ) ) {
//            query = "\""+ query +"\"";
//            if ( log.isDebugEnabled() ) {
//                log.debug( "This looks like a GO or CHEBI identifier, we are escaping to: " + query );
//            }
//        }
        
        return query;
    }

    public static String prepareInteractorQuery(String query, String interactorTypeMi) {
        query = prepareQuery(query);

        StringBuilder sb = new StringBuilder();
        if (query != null && !query.equals(WILDCARD)) {
            sb.append("+(").append(query).append(") +");
        }
        sb.append("typeA:").append("\"").append(interactorTypeMi).append("\"");

        return sb.toString();
    }





    private static String escapeQueryIfNecessary( String query ) {

        String processedQuery = query;

        final String[] subQueries = query.split( " " );
        switch( subQueries.length ) {

            case 0:
                // query seems empty, do nothing
                break;
            case 1:
                // single parameter, proceed...
                String subQuery = subQueries[0];

                boolean negative = false;
                if( subQuery.startsWith( "-" )) {
                    negative = true;
                    // remove leading '-'
                    subQuery = subQuery.substring( 1 );
                }

                if( subQuery.toLowerCase(  ).startsWith( "go:" )
                    || subQuery.toLowerCase(  ).startsWith( "chebi:" ) ) {
                    processedQuery = (negative ? "-" : "") + "\""+ subQuery +"\"";
                }

                if (log.isDebugEnabled()) log.debug("The query "+ query +" was escaped: " + processedQuery );

                break;
            default:
                // more than 1, currently ignore
        }

        return processedQuery;
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

        MitabDocumentDefinition docDef = new MitabDocumentDefinition();

        QueryStringBufferDecorator sb = new QueryStringBufferDecorator(new StringBuffer());
        if (isValidValue(search.getIdentifier())) {
            sb.append(concatFieldAndValue("identifiers", search.getIdentifier()));
        }

        if (isValidValue(search.getPubId())) {
            sb.appendOperand(search);
            sb.append(concatFieldAndValue(docDef.getColumnDefinition(MitabDocumentDefinition.PUB_ID).getShortName(),
                                          search.getPubId()));
        }
        if (isValidValue(search.getPubFirstAuthor())) {
            sb.appendOperand(search);
            sb.append(concatFieldAndValue(docDef.getColumnDefinition(MitabDocumentDefinition.PUB_AUTH).getShortName(),
                                          search.getPubFirstAuthor()));
        }
        if (isValidValue(search.getInteractionId())) {
            sb.appendOperand(search);
            sb.append(concatFieldAndValue(docDef.getColumnDefinition(MitabDocumentDefinition.INTERACTION_ID).getShortName(),
                                          search.getInteractionId()));
        }
        if (isValidValue(search.getTaxid())) {
            sb.appendOperand(search);
            sb.append(concatFieldAndValue("species", search.getTaxid()));
        }


        // interaction types
        if (isValidValue(search.getInteractionType())) {
            sb.appendOperand(search);

            Term interactionTypeTerm = termForValue(search.getInteractionType(), interactionTypeTerms);



            if (interactionTypeTerm != null) {
                sb.append(concatFieldAndTerm(docDef.getColumnDefinition(MitabDocumentDefinition.INT_TYPE).getShortName(),
                                             interactionTypeTerm, search.isIncludeInteractionTypeChildren()));
            } else {
                sb.append(concatFieldAndValue(docDef.getColumnDefinition(MitabDocumentDefinition.INT_TYPE).getShortName(),
                                              search.getInteractionType()));

            }
        }

        if (isValidValue(search.getDetectionMethod())) {
            sb.appendOperand(search);

            // detection methods
            Term detectionMethodTerm = termForValue(search.getDetectionMethod(), detectionMethodTerms);

            if (detectionMethodTerm != null) {
                sb.append(concatFieldAndTerm(docDef.getColumnDefinition(MitabDocumentDefinition.INT_DET_METHOD).getShortName(),
                                             detectionMethodTerm, search.isIncludeDetectionMethodChildren()));
            } else {
                sb.append(concatFieldAndValue(docDef.getColumnDefinition(MitabDocumentDefinition.INT_DET_METHOD).getShortName(),
                                              search.getDetectionMethod()));
            }
        }

        String query = sb.toString();
        if (query.length() == 0) {
            query = WILDCARD;
        }

        return query;
    }

    private static String concatFieldAndValue(String fieldName, String value) {
        return fieldName + ":" + value;
    }



    private static String concatFieldAndTerm(String fieldName, Term term, boolean includeChildren) {
        List<Term> terms = new ArrayList<Term>();
        terms.add(term);

        if (includeChildren) {
            //terms.addAll(OlsBean.childrenFor(term, new ArrayList<Term>()));
        }

        return fieldName + ":(" + termsToValue(terms) + ")";
    }

    private static String termsToValue(List<Term> terms) {
        StringBuilder sb = new StringBuilder();

        for (Iterator<Term> iterator = terms.iterator(); iterator.hasNext();) {
            Term term = iterator.next();

            String termName = (term.getExactSynonim() != null)? term.getExactSynonim() : term.getName();
            sb.append(putInQuotes(termName));

            if (iterator.hasNext()) {
                sb.append(" ");
            }
        }

        return sb.toString();
    }

    private static String putInQuotes(String value) {
        if (value == null) return null;

        if (value.length() == 0) return value;

        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value;
        }
        return "\"" + value + "\"";
    }

    private static boolean isValidValue(String value) {
        return (value != null && value.length() > 0);
    }


    /**
     * Returns the term for the value provided. It will return null if no term exists for that value (e.g. the user
     * provides a new value or a combination of different values)
     */
    private static Term termForValue(String value, List<Term> terms) {
        if (value == null) {
            return null;
        }

        // remove quotes from the string, to be able to check the equals
        value = value.replaceAll("\"", "");

        for (Term term : terms) {
            if (value.equals(term.getName()) || value.equals(term.getExactSynonim())) {
                return term;
            }
        }

        return null;
    }

    /**
     * Decoration of the StringBuffer to append the write operands only if necessary (always after a string has been added first)
     */
    private static class QueryStringBufferDecorator {

        private StringBuffer sb;
        private boolean isOperandAllowed;

        public QueryStringBufferDecorator(StringBuffer sb) {
            this.sb = sb;
        }

        public StringBuffer append(String str) {
            isOperandAllowed = true;
            return sb.append(str);
        }

        public boolean appendOperand(AdvancedSearch advSearch) {
            if (isOperandAllowed) {
                String operand = advSearch.isConjunction() ? " AND " : " OR ";
                sb.append(operand);
            }

            return !isOperandAllowed;
        }

        public String toString() {
            return sb.toString();
        }
    }

}