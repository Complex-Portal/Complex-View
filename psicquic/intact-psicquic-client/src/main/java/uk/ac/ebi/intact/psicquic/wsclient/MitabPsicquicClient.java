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
import psidev.psi.mi.search.SearchResult;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactPsimiTabReader;

import java.util.List;
import java.util.Collection;
import java.util.ArrayList;

/**
 * Client for a PSICQUIC Web service that returns PSIMITAB model objects.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class MitabPsicquicClient extends AbstractPsicquicClient <SearchResult<IntactBinaryInteraction>> {

    private static final String RETURN_TYPE = "psi-mi/tab25";

    public MitabPsicquicClient(String serviceAddress) {
        super(serviceAddress);
    }

    public SearchResult<IntactBinaryInteraction> getByQuery(String query, int firstResult, int maxResults) throws PsicquicClientException {
        RequestInfo requestInfo = createRequestInfo(RETURN_TYPE, firstResult, maxResults);

        QueryResponse response;
        try {
            response = getService().getByQuery(query, requestInfo);
        } catch (Exception e) {
            throw new PsicquicClientException("There was a problem running the service", e);
        }

        return createSearchResult(response);
    }

    public SearchResult<IntactBinaryInteraction> getByInteractor(String identifier, int firstResult, int maxResults) throws PsicquicClientException {
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

    public SearchResult<IntactBinaryInteraction> getByInteraction(String identifier, int firstResult, int maxResults) throws PsicquicClientException {
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

    public SearchResult<IntactBinaryInteraction> getByInteractionList(String[] identifiers, int firstResult, int maxResults) throws PsicquicClientException {
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

    public SearchResult<IntactBinaryInteraction> getByInteractorList(String[] identifiers, QueryOperand operand, int firstResult, int maxResults) throws PsicquicClientException {
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

    private SearchResult<IntactBinaryInteraction> createSearchResult(QueryResponse response) throws PsicquicClientException {
        String mitab = response.getResultSet().getMitab();

        PsimiTabReader reader = new IntactPsimiTabReader(true);
        List<IntactBinaryInteraction> interactions = null;
        try {
            Collection<BinaryInteraction> interactionCollection = reader.read(mitab);
            interactions = new ArrayList<IntactBinaryInteraction>();

            for (BinaryInteraction binaryInteraction : interactionCollection) {
                interactions.add((IntactBinaryInteraction)binaryInteraction);
            }

        } catch (Exception e) {
            throw new PsicquicClientException("Problem converting the results to IntactBinaryInteractions", e);
        }

        ResultInfo resultInfo = response.getResultInfo();

        return new SearchResult<IntactBinaryInteraction>(interactions, resultInfo.getTotalResults(),
                                                                       resultInfo.getFirstResult(),
                                                                       resultInfo.getBlockSize(), null);
    }

}
