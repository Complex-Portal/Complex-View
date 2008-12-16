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
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import psidev.psi.mi.search.SearchResult;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactPsimiTabReader;

import java.util.List;
import java.util.Collection;
import java.util.ArrayList;

/**
 * Simple client for PSICQUIC web services.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public abstract class AbstractPsicquicClient<T> {

    private PsicquicService service;

    public AbstractPsicquicClient(String serviceAddress) {
        if (serviceAddress == null) return;
        
        ClientProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(PsicquicService.class);
        factory.setAddress(serviceAddress);

        this.service = (PsicquicService) factory.create();
    }

    public PsicquicService getService() {
        return service;
    }

    public abstract T getByQuery(String query, int firstResult, int maxResults) throws PsicquicClientException;

    public abstract T getByInteractor(String identifier, int firstResult, int maxResults) throws PsicquicClientException;

    public abstract T getByInteraction(String identifier, int firstResult, int maxResults) throws PsicquicClientException;

    public abstract T getByInteractionList(String[] identifiers, int firstResult, int maxResults) throws PsicquicClientException;

    public abstract T getByInteractorList(String[] identifiers, QueryOperand operand, int firstResult, int maxResults) throws PsicquicClientException;

    protected RequestInfo createRequestInfo(String returnType, int firstResult, int maxResults) {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setFirstResult(firstResult);
        requestInfo.setBlockSize(maxResults);
        requestInfo.setResultType(returnType);
        return requestInfo;
    }

    protected DbRef createDbRef(String identifier) {
        DbRef dbRef = new DbRef();
        dbRef.setId(identifier);
        return dbRef;
    }

    protected List<DbRef> createDbRefs(String ... identifiers) {
        List<DbRef> dbRefs = new ArrayList<DbRef>(identifiers.length);

        for (String identifier : identifiers) {
            DbRef dbRef = createDbRef(identifier);
            dbRefs.add(dbRef);
        }

        return dbRefs;
    }
}