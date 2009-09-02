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
package uk.ac.ebi.intact.binarysearch.ws;

import psidev.psi.mi.search.SearchResult;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.builder.DocumentDefinition;
import uk.ac.ebi.intact.psimitab.IntactDocumentDefinition;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.servlet.ServletContext;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@WebService(name = "BinarySearch", targetNamespace = "http://ebi.ac.uk/intact/binarysearch/wsclient/generated")
public class BinarySearch {

    private static final String NAMESPACE = "http://ebi.ac.uk/intact/binarysearch/wsclient/generated";

    @Resource
    WebServiceContext context;

    @WebMethod(operationName = "findBinaryInteractions")
    public SimplifiedSearchResult findBinaryInteractions(@WebParam(name = "query", targetNamespace = NAMESPACE)
                                                         String query) {
        if (query == null) throw new NullPointerException("Query cannot be null");
        
        return findBinaryInteractionsLimited(query, 0, Integer.MAX_VALUE);
    }
                                                          
    @WebMethod(operationName = "findBinaryInteractionsLimited")
    public SimplifiedSearchResult findBinaryInteractionsLimited(
            @WebParam(name = "query", targetNamespace = NAMESPACE) final String query,
            @WebParam(name = "firstResult", targetNamespace = NAMESPACE) final Integer firstResult,
            @WebParam(name = "maxResults", targetNamespace = NAMESPACE) final Integer maxResults ) {

        if (firstResult == null) {
            throw new NullPointerException("firstResult must not be null");
        }
        if (maxResults == null) {
            throw new NullPointerException("maxResults must not be null");
        }

        final int chunkSize = 200;
        int startCurrentPage = firstResult;

        IntactPsicquicClient client = new IntactPsicquicClient(getPsicquicEndpoint());
        DocumentDefinition docDef = new IntactDocumentDefinition();

        SearchResult<BinaryInteraction> searchResult;
        List<String> interactionLines = new ArrayList<String>(chunkSize);

        boolean firstQueryDone = false;

        do {
            try {
                searchResult = client.getByQuery(query, startCurrentPage, maxResults);
                if( ! firstQueryDone ) {
                    firstQueryDone = true;

                    final Integer totalCount = searchResult.getTotalCount();
                    if( totalCount > maxResults && maxResults > 2000 ) {
                        throw new RuntimeException( "Please be gentle with our services ! " +
                                "Your query ("+ query +") hits "+ totalCount +" results. " +
                                "You must use the method 'findBinaryInteractionsLimited', set the 'maxResults' parameter to 2000 maximum and " +
                                "paginate through the dataset by setting 'firstResult' appropriately." );
                    }
                }
            } catch (Throwable e) {
                throw new RuntimeException("Problem executing query: " + query, e);
            }

            for (BinaryInteraction binteraction : searchResult.getData()) {
                interactionLines.add(docDef.interactionToString(binteraction));
            }

            startCurrentPage += chunkSize;

        } while (startCurrentPage < searchResult.getTotalCount());

        return new SimplifiedSearchResult(firstResult, interactionLines, query, maxResults, searchResult.getTotalCount());
    }

    @WebMethod()
    public String getVersion() {
        try {
            Properties props = new Properties();
            props.load(BinarySearch.class.getResourceAsStream("BuildInfo.properties"));
            return (String) props.get("build.version");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected String getPsicquicEndpoint() {
        MessageContext mc = context.getMessageContext();
        ServletContext servletContex = (ServletContext)
                mc.get(MessageContext.SERVLET_CONTEXT);


        return servletContex.getInitParameter("uk.ac.ebi.intact.binarysearch.PSICQUIC_ENDPOINT");
    }
}