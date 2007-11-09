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

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import psidev.psi.mi.search.SearchResult;
import psidev.psi.mi.search.Searcher;
import psidev.psi.mi.tab.formatter.TabulatedLineFormatter;
import uk.ac.ebi.intact.psimitab.IntActBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntActColumnHandler;
import uk.ac.ebi.intact.psimitab.search.IntActSearchEngine;
import uk.ac.ebi.intact.psimitab.search.IntActDocumentBuilder;

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

    @Resource
    WebServiceContext context;

    @WebMethod(operationName = "findBinaryInteractions")
    public SimplifiedSearchResult findBinaryInteractions(@WebParam(name = "query")String query) {
        return findBinaryInteractionsLimited(query, null, null);
    }

    @WebMethod(operationName = "findBinaryInteractionsLimited")
    public SimplifiedSearchResult findBinaryInteractionsLimited(@WebParam(name = "query")String query,
                                                      @WebParam(name = "firstResult")Integer firstResult,
                                                      @WebParam(name = "maxResults")Integer maxResults
    ) {
        IntActSearchEngine searchEngine = null;
        try {
            searchEngine = new IntActSearchEngine(getIndexDirectory());
        } catch (IOException e) {
            throw new RuntimeException("Problem reading index", e);
        }
        SearchResult<IntActBinaryInteraction> sr = Searcher.search(query, firstResult, maxResults, null, searchEngine);

        List<String> interactionLines = new ArrayList<String>(sr.getInteractions().size());

        TabulatedLineFormatter lineFormatter = new TabulatedLineFormatter();
        lineFormatter.setBinaryInteractionClass(IntActBinaryInteraction.class);
        lineFormatter.setColumnHandler(new IntActColumnHandler());

        for (IntActBinaryInteraction intactBinaryInteraction : sr.getInteractions()) {
            String line = lineFormatter.format(intactBinaryInteraction);
            interactionLines.add(line);
        }

        return new SimplifiedSearchResult(firstResult, interactionLines, sr.getLuceneQuery(), maxResults, sr.getTotalCount());
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

    protected Directory getIndexDirectory() {
        Directory indexDirectory = null;

        MessageContext mc = context.getMessageContext();
        ServletContext servletContex = (ServletContext)
                mc.get(MessageContext.SERVLET_CONTEXT);

        String indexDir = servletContex.getInitParameter("uk.ac.ebi.intact.binarysearch.INDEX_DIR");
        try {
            indexDirectory = FSDirectory.getDirectory(indexDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return indexDirectory;
    }
}