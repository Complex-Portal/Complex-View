/*
 * Copyright 2001-2007 The European Bioinformatics Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.binarysearch.wsclient;


import psidev.psi.mi.search.SearchResult;
import psidev.psi.mi.tab.converter.txt2tab.MitabLineException;
import uk.ac.ebi.intact.binarysearch.wsclient.generated.BinarySearch;
import uk.ac.ebi.intact.binarysearch.wsclient.generated.BinarySearchService;
import uk.ac.ebi.intact.binarysearch.wsclient.generated.SimplifiedSearchResult;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactDocumentDefinition;

import javax.xml.namespace.QName;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.search.BooleanQuery;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class BinarySearchServiceClient {


    private static final String DEFAULT_URL = "http://www.ebi.ac.uk/intact/binary-search-ws/binarysearch?wsdl";

    private static final String QNAME_TARGET_NAMESPACE = "http://ebi.ac.uk/intact/binarysearch/wsclient/generated";
    private static final String QNAME_PORT_NAME = "BinarySearchService";

    private BinarySearch binarySearch;


    public BinarySearchServiceClient() {

        try {
            BinarySearchService searchService = new BinarySearchService(new URL(DEFAULT_URL), new QName(QNAME_TARGET_NAMESPACE, QNAME_PORT_NAME));
            this.binarySearch = searchService.getBinarySearchPort();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    } // constructor

    public BinarySearchServiceClient(String searchWsUrl) {

        try {
            BinarySearchService searchService = new BinarySearchService(new URL(searchWsUrl), new QName(QNAME_TARGET_NAMESPACE, QNAME_PORT_NAME));
            this.binarySearch = searchService.getBinarySearchPort();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public BinarySearch getBinarySearchPort() {
        return binarySearch;
    }

    public SearchResult<IntactBinaryInteraction> findBinaryInteractions(String query) {
        return toSearchResult(binarySearch.findBinaryInteractions(query));
    }

    public SearchResult<IntactBinaryInteraction> findBinaryInteractionsLimited(String query, Integer firstResult, Integer maxResults) {
        return toSearchResult(binarySearch.findBinaryInteractionsLimited(query, firstResult, maxResults));
    }

    public SearchResult<IntactBinaryInteraction> findBinaryInteractionsByIdentifiers(String... identifiers) {
        String query = "identifier:(" + arrayElementsToOR(identifiers)+")";
        return toSearchResult(binarySearch.findBinaryInteractions(query));
    }

    private static SearchResult<IntactBinaryInteraction> toSearchResult(SimplifiedSearchResult ssr) {
        List<IntactBinaryInteraction> interactions = new ArrayList<IntactBinaryInteraction>(ssr.getInteractionLines().size());

        IntactDocumentDefinition docDef = new IntactDocumentDefinition();

        for (String line : ssr.getInteractionLines()) {
            IntactBinaryInteraction interaction = null;
            try {
                interaction = docDef.interactionFromString(line);
            } catch (Throwable e) {
                throw new RuntimeException("Wrong line returned by the server: "+line);
            }
            interactions.add(interaction);
        }

        return new SearchResult<IntactBinaryInteraction>(interactions, ssr.getTotalResults(), ssr.getFirstResult(), ssr.getMaxResults(), null);
    }


    private static String arrayElementsToOR(String... elements) {
        return arrayElementsToString(" OR ", elements);
    }

    private static String arrayElementsToString(String separator, String[] elements) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < elements.length; i++) {
            if (i > 0) {
                sb.append(separator);
            }
            sb.append(elements[i]);
        }

        return sb.toString();
    }
}