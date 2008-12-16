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
package uk.ac.ebi.intact.psicquic.wsclient;

import org.hupo.psi.mi.psicquic.*;
import psidev.psi.mi.xml.model.EntrySet;
import psidev.psi.mi.xml.converter.impl254.EntrySetConverter;
import psidev.psi.mi.xml.converter.ConverterException;
import psidev.psi.mi.xml.dao.inMemory.InMemoryDAOFactory;

import java.util.List;

/**
 * Client for a PSICQUIC Web service that returns XML 2.5 model objects.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class XmlPsicquicClient extends AbstractPsicquicClient<XmlSearchResult> {

    private static final String RETURN_TYPE = "psi-mi/xml25";

    public XmlPsicquicClient(String serviceAddress) {
        super(serviceAddress);
    }

    public XmlSearchResult getByQuery(String query, int firstResult, int maxResults) throws PsicquicClientException {
        RequestInfo requestInfo = createRequestInfo(RETURN_TYPE, firstResult, maxResults);

        QueryResponse response;
        try {
            response = getService().getByQuery(query, requestInfo);
        } catch (Exception e) {
            throw new PsicquicClientException("There was a problem running the service", e);
        }

        return createSearchResult(response);
    }

    public XmlSearchResult getByInteractor(String identifier, int firstResult, int maxResults) throws PsicquicClientException {
        DbRef dbRef = createDbRef(identifier);
        RequestInfo requestInfo = createRequestInfo(RETURN_TYPE, firstResult, maxResults);

        QueryResponse response;
        try {
            response = getService().getByInteractor(dbRef, requestInfo);
        } catch (Exception e) {
            throw new PsicquicClientException("There was a problem running the service", e);
        }

        return createSearchResult(response);
    }

    public XmlSearchResult getByInteraction(String identifier, int firstResult, int maxResults) throws PsicquicClientException {
        DbRef dbRef = createDbRef(identifier);
        RequestInfo requestInfo = createRequestInfo(RETURN_TYPE, firstResult, maxResults);

        QueryResponse response;
        try {
            response = getService().getByInteraction(dbRef, requestInfo);
        } catch (Exception e) {
            throw new PsicquicClientException("There was a problem running the service", e);
        }

        return createSearchResult(response);
    }

    public XmlSearchResult getByInteractionList(String[] identifiers, int firstResult, int maxResults) throws PsicquicClientException {
        List<DbRef> dbRefs = createDbRefs(identifiers);
        RequestInfo requestInfo = createRequestInfo(RETURN_TYPE, firstResult, maxResults);

        QueryResponse response;
        try {
            response = getService().getByInteractionList(dbRefs, requestInfo);
        } catch (Exception e) {
            throw new PsicquicClientException("There was a problem running the service", e);
        }

        return createSearchResult(response);
    }

    public XmlSearchResult getByInteractorList(String[] identifiers, QueryOperand operand, int firstResult, int maxResults) throws PsicquicClientException {
        List<DbRef> dbRefs = createDbRefs(identifiers);
        RequestInfo requestInfo = createRequestInfo(RETURN_TYPE, firstResult, maxResults);

        QueryResponse response;
        try {
            response = getService().getByInteractorList(dbRefs, requestInfo, operand.toString());
        } catch (Exception e) {
            throw new PsicquicClientException("There was a problem running the service", e);
        }

        return createSearchResult(response);
    }

    private XmlSearchResult createSearchResult(QueryResponse response) throws PsicquicClientException {
        psidev.psi.mi.xml254.jaxb.EntrySet jaxbEntrySet = response.getResultSet().getEntrySet();

        EntrySetConverter converter = new EntrySetConverter();
        converter.setDAOFactory(new InMemoryDAOFactory());

        EntrySet entrySet;

        try {
            entrySet = converter.fromJaxb(jaxbEntrySet);
        } catch (ConverterException e) {
            throw new PsicquicClientException("Problem converting PSI-MI XML result to EntrySet", e);
        }

        ResultInfo resultInfo = response.getResultInfo();
        return new XmlSearchResult(entrySet, resultInfo.getFirstResult(),
                                                     resultInfo.getBlockSize(),
                                                     resultInfo.getTotalResults());
    }

}