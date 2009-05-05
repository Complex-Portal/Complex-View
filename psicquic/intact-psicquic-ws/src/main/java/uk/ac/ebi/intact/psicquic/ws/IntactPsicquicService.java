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
package uk.ac.ebi.intact.psicquic.ws;

import org.apache.commons.httpclient.HttpClient;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.common.SolrDocument;
import org.hupo.psi.mi.psicquic.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import psidev.psi.mi.tab.converter.tab2xml.Tab2Xml;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.builder.DocumentDefinition;
import psidev.psi.mi.xml.converter.impl254.EntrySetConverter;
import psidev.psi.mi.xml.dao.inMemory.InMemoryDAOFactory;
import psidev.psi.mi.xml254.jaxb.Attribute;
import psidev.psi.mi.xml254.jaxb.AttributeList;
import psidev.psi.mi.xml254.jaxb.Entry;
import psidev.psi.mi.xml254.jaxb.EntrySet;
import uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrSearcher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.SolrSearchResult;
import uk.ac.ebi.intact.dataexchange.psimi.solr.converter.SolrDocumentConverter;
import uk.ac.ebi.intact.psicquic.ws.config.PsicquicConfig;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactDocumentDefinition;
import uk.ac.ebi.intact.psimitab.IntactTab2Xml;

import java.util.*;

/**
 * This web service is based on a PSIMITAB lucene's directory to search and return the results.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
public class IntactPsicquicService implements PsicquicService {

    private final Logger logger = LoggerFactory.getLogger(IntactPsicquicService.class);

    public static final String RETURN_TYPE_XML25 = "psi-mi/xml25";
    public static final String RETURN_TYPE_MITAB25 = "psi-mi/tab25";
    public static final String RETURN_TYPE_COUNT = "count";

    private static final String NEW_LINE = System.getProperty("line.separator");

    private static final int BLOCKSIZE_MAX = 200;
    private static final String RETURN_TYPE_DEFAULT = RETURN_TYPE_MITAB25;

    private static final List<String> SUPPORTED_RETURN_TYPES = Arrays.asList(RETURN_TYPE_XML25, RETURN_TYPE_MITAB25, RETURN_TYPE_COUNT);
    
    @Autowired
    private PsicquicConfig config;
    
    private static final String IDENTIFIER_FIELD = "identifier";
    private static final String INTERACTION_ID_FIELD = "interaction_id";

    public IntactPsicquicService() { 
    }


    public QueryResponse getByInteractor(DbRef dbRef, RequestInfo requestInfo) throws NotSupportedMethodException, NotSupportedTypeException, PsicquicServiceException {
        String query = createQuery( IDENTIFIER_FIELD, dbRef);

        return getByQuery(query, requestInfo);
    }

    public QueryResponse getByInteraction(DbRef dbRef, RequestInfo requestInfo) throws NotSupportedMethodException, NotSupportedTypeException, PsicquicServiceException {
        String query = createQuery( INTERACTION_ID_FIELD, dbRef);

        return getByQuery(query, requestInfo);
    }

    public QueryResponse getByInteractorList(List<DbRef> dbRefs, RequestInfo requestInfo, String operand) throws NotSupportedMethodException, NotSupportedTypeException, PsicquicServiceException {
        String query = createQuery( IDENTIFIER_FIELD, dbRefs, operand);

        

        return getByQuery(query, requestInfo);
    }

    public QueryResponse getByInteractionList(List<DbRef> dbRefs, RequestInfo requestInfo) throws PsicquicServiceException, NotSupportedMethodException, NotSupportedTypeException {
        String query = createQuery( INTERACTION_ID_FIELD, dbRefs, "OR");

        return getByQuery(query, requestInfo);
    }

    private String createQuery(String fieldName, DbRef dbRef) {
        return createQuery(fieldName, Collections.singleton(dbRef), null);
    }

    private String createQuery(String fieldName, Collection<DbRef> dbRefs, String operand) {
        StringBuilder sb = new StringBuilder(dbRefs.size() * 64);
        sb.append(fieldName).append(":(");

        for (Iterator<DbRef> dbRefIterator = dbRefs.iterator(); dbRefIterator.hasNext();) {
            DbRef dbRef = dbRefIterator.next();

            sb.append(createQuery(dbRef));

            if (dbRefIterator.hasNext()) {
                sb.append(" ").append(operand).append(" ");
            }
        }

        sb.append(")");

        return sb.toString();
    }

    private String createQuery(DbRef dbRef) {
        String db = dbRef.getDbAc();
        String id = dbRef.getId();

        return "("+((db == null || db.length() == 0)? "\""+id+"\"" : "\""+db+"\" AND \""+id+"\"")+")";
    }

    public QueryResponse getByQuery(String query, RequestInfo requestInfo) throws NotSupportedMethodException,
                                                                                  NotSupportedTypeException,
                                                                                  PsicquicServiceException {
        final int blockSize = Math.min(requestInfo.getBlockSize(), BLOCKSIZE_MAX);

        final String resultType = requestInfo.getResultType();

        if (resultType != null && !getSupportedReturnTypes().contains(resultType)) {
            throw new NotSupportedTypeException("Not supported return type: "+resultType+" - Supported types are: "+getSupportedReturnTypes());
        }

        logger.debug("Searching: {} ({}/{}) with type {}", new Object[] {query, requestInfo.getFirstResult(), blockSize, resultType});

        /////////////////////////////////////////////////////////////////

        SolrSearchResult solrSearchResult;

        try {
            SolrServer solrServer = new CommonsHttpSolrServer(config.getSolrServerUrl(), createHttpClient());

            IntactSolrSearcher searcher = new IntactSolrSearcher(solrServer);
            solrSearchResult = searcher.search(query, requestInfo.getFirstResult(), blockSize);

        } catch (Throwable t) {
            logger.error("An error occured while searching the Solr index: " +
                    config.getSolrServerUrl(), t);
            throw new PsicquicServiceException("An error occured while searching the Solr index: " +
                    config.getSolrServerUrl(), t);
        }

        // preparing the response
        try {
            QueryResponse queryResponse = new QueryResponse();
            ResultInfo resultInfo = new ResultInfo();
            resultInfo.setBlockSize(blockSize);
            resultInfo.setFirstResult(requestInfo.getFirstResult());
            resultInfo.setTotalResults((int) solrSearchResult.getTotalCount() );

            queryResponse.setResultInfo(resultInfo);

            ResultSet resultSet = createResultSet(query, solrSearchResult, requestInfo);
            queryResponse.setResultSet(resultSet);

            return queryResponse;

        } catch (Throwable e) {
            logger.error("Problem creating the response for result type: "+resultType, e);
            throw new PsicquicServiceException("Problem creating the response for result type: "+resultType, e);
        }


        /////////////////////////////////////////////////////////////////

    }

    public String getVersion() {
        return config.getVersion();
    }

    public List<String> getSupportedReturnTypes()  {
        return SUPPORTED_RETURN_TYPES;
    }

    public List<String> getSupportedDbAcs() {
        return Collections.EMPTY_LIST;
    }

    // Lucene

    protected ResultSet createResultSet(String query, SolrSearchResult searchResult, RequestInfo requestInfo) throws PsicquicServiceException,
                                                                                                   NotSupportedTypeException {
        ResultSet resultSet = new ResultSet();

        String resultType = (requestInfo.getResultType() != null)? requestInfo.getResultType() : RETURN_TYPE_DEFAULT;

        if (RETURN_TYPE_MITAB25.equals(resultType)) {
            if (logger.isDebugEnabled()) logger.debug("Creating PSI-MI TAB");

            String mitab = createMitabResults(searchResult);
            resultSet.setMitab(mitab);
        } else if (RETURN_TYPE_XML25.equals(resultType)) {
            if (logger.isDebugEnabled()) logger.debug("Creating PSI-MI XML");

            EntrySet jEntrySet = createEntrySet(searchResult);
            resultSet.setEntrySet(jEntrySet);

            // add some annotations
            if (!jEntrySet.getEntries().isEmpty()) {
                AttributeList attrList = new AttributeList();

                Entry entry = jEntrySet.getEntries().iterator().next();

                Attribute attr = new Attribute();
                attr.setValue("Data retrieved using the PSICQUIC service. Query: "+query);
                attrList.getAttributes().add(attr);

                Attribute attr2 = new Attribute();
                attr2.setValue("Total results found: "+searchResult.getTotalCount());
                attrList.getAttributes().add(attr2);

                // add warning if the batch size requested is higher than the maximum allowed
                if (requestInfo.getBlockSize() > BLOCKSIZE_MAX && BLOCKSIZE_MAX < searchResult.getTotalCount()) {
                    Attribute attrWarning = new Attribute();
                    attrWarning.setValue("Warning: The requested block size (" + requestInfo.getBlockSize() + ") was higher than the maximum allowed (" + BLOCKSIZE_MAX + ") by PSICQUIC the service. " +
                            BLOCKSIZE_MAX + " results were returned from a total found of "+searchResult.getTotalCount());
                    attrList.getAttributes().add(attrWarning);

                }

                entry.setAttributeList(attrList);
            }

        } else if (RETURN_TYPE_COUNT.equals(resultType)) {
            if (logger.isDebugEnabled()) logger.debug("Count query");
            // nothing to be done here
        } else {
            throw new NotSupportedTypeException("Not supported return type: "+resultType+" - Supported types are: "+getSupportedReturnTypes());
        }

        return resultSet;
    }
    protected String createMitabResults(SolrSearchResult searchResult) {
        DocumentDefinition docDef = new IntactDocumentDefinition();

        Collection<IntactBinaryInteraction> binaryInteractions = searchResult.getBinaryInteractionList();

        StringBuilder sb = new StringBuilder(binaryInteractions.size() * 512);

        for (BinaryInteraction binaryInteraction : binaryInteractions) {
            String binaryInteractionString = docDef.interactionToString(binaryInteraction);
            sb.append(binaryInteractionString);
            sb.append(NEW_LINE);
        }
        return sb.toString();
    }

    private EntrySet createEntrySet(SolrSearchResult searchResult) throws PsicquicServiceException {
        IntactTab2Xml tab2Xml = new IntactTab2Xml();
        try {
            Collection binaryInteractions = searchResult.getBinaryInteractionList();

            psidev.psi.mi.xml.model.EntrySet mEntrySet = tab2Xml.convert(binaryInteractions);

            EntrySetConverter converter = new EntrySetConverter();
            converter.setDAOFactory(new InMemoryDAOFactory());

            return converter.toJaxb(mEntrySet);

        } catch (Exception e) {
            throw new PsicquicServiceException("Problem converting results to PSI-MI XML: "+e, e);
        }
    }

    protected String createMitabResults(org.apache.solr.client.solrj.response.QueryResponse searchResult) {
        SolrDocumentConverter converter = new SolrDocumentConverter( new IntactDocumentDefinition() );

        StringBuilder sb = new StringBuilder((int)searchResult.getResults().getNumFound() * 512);
        final ListIterator<SolrDocument> docIterator = searchResult.getResults().listIterator();
        while ( docIterator.hasNext() ) {
            SolrDocument solrDocument = docIterator.next();
            final String mitabLine = converter.toMitabLine( solrDocument );

            sb.append(mitabLine);
            sb.append(NEW_LINE);
        }
        return sb.toString();
    }

    private EntrySet createEntrySet(org.apache.solr.client.solrj.response.QueryResponse searchResult) throws PsicquicServiceException {
        if (searchResult.getResults().getNumFound() == 0) {
            return new EntrySet();
        }

        Tab2Xml tab2Xml = new IntactTab2Xml();

        // first collect all MITAB interactions
        SolrDocumentConverter solrConverter = new SolrDocumentConverter( new IntactDocumentDefinition() );
        final ListIterator<SolrDocument> docIterator = searchResult.getResults().listIterator();
        Collection<BinaryInteraction> interactions = new ArrayList<BinaryInteraction>( );
        while ( docIterator.hasNext() ) {
            SolrDocument solrDocument = docIterator.next();
            final BinaryInteraction bi = solrConverter.toBinaryInteraction( solrDocument );
            interactions.add( bi );
        }

        // then convert them into PSI-MI XML
        try {
            psidev.psi.mi.xml.model.EntrySet mEntrySet = tab2Xml.convert(interactions);

            EntrySetConverter converter = new EntrySetConverter();
            converter.setDAOFactory(new InMemoryDAOFactory());

            return converter.toJaxb(mEntrySet);

        } catch (Exception e) {
            throw new PsicquicServiceException("Problem converting results to PSI-MI XML: "+e, e);
        }
    }

     private HttpClient createHttpClient() {
        HttpClient httpClient = new HttpClient();

        String proxyHost = config.getProxyHost();
        String proxyPort = config.getProxyPort();

        if (isValueSet(proxyHost) && proxyHost.trim().length() > 0 &&
                isValueSet(proxyPort) && proxyPort.trim().length() > 0) {
            httpClient.getHostConfiguration().setProxy(proxyHost, Integer.valueOf(proxyPort));
        }
        return httpClient;
    }

    private boolean isValueSet(String value) {
        return value != null && !value.startsWith("$");
    }
}