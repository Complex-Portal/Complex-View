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

import org.hupo.psi.mi.psicquic.wsclient.*;
import psidev.psi.mi.xml.model.EntrySet;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared.EntryConverter;
import uk.ac.ebi.intact.model.IntactEntry;

/**
 * Client for a PSICQUIC Web service that returns IntAct model objects.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class IntactPsicquicClient extends AbstractPsicquicClient<IntactSearchResult> {

    private XmlPsicquicClient xmlClient;

    public IntactPsicquicClient(String serviceAddress) {
        super(null);
        xmlClient = new XmlPsicquicClient(serviceAddress);
    }

    public IntactSearchResult getByQuery(String query, int firstResult, int maxResults) throws PsicquicClientException {
        return createSearchResult(xmlClient.getByQuery(query, firstResult, maxResults));
    }

    public IntactSearchResult getByInteractor(String identifier, int firstResult, int maxResults) throws PsicquicClientException {
        return createSearchResult(xmlClient.getByInteractor(identifier, firstResult, maxResults));
    }

    public IntactSearchResult getByInteraction(String identifier, int firstResult, int maxResults) throws PsicquicClientException {
        return createSearchResult(xmlClient.getByInteraction(identifier, firstResult, maxResults));
    }

    public IntactSearchResult getByInteractionList(String[] identifiers, int firstResult, int maxResults) throws PsicquicClientException {
        return createSearchResult(xmlClient.getByInteractionList(identifiers, firstResult, maxResults));
    }

    public IntactSearchResult getByInteractorList(String[] identifiers, QueryOperand operand, int firstResult, int maxResults) throws PsicquicClientException {
        return createSearchResult(xmlClient.getByInteractorList(identifiers, operand, firstResult, maxResults));
    }

    private IntactSearchResult createSearchResult(XmlSearchResult xmlResult) {
        EntrySet entrySet = xmlResult.getEntrySet();

        IntactEntry intactEntry;

        if (!entrySet.getEntries().isEmpty()) {
            EntryConverter entryConverter = new EntryConverter();
            intactEntry = entryConverter.psiToIntact(entrySet.getEntries().iterator().next());
        } else {
            intactEntry = new IntactEntry();
        }

        return new IntactSearchResult(intactEntry, xmlResult.getFirstResult(), xmlResult.getMaxResults(), xmlResult.getTotalResults());
    }
}
