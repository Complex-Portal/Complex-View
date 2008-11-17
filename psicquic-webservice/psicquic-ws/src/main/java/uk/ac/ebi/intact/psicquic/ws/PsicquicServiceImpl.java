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

import org.springframework.stereotype.Controller;
import org.hupo.psi.mi.psicquic.*;

import javax.jws.WebParam;

import psidev.psi.mi.xml.jaxb.Entry;
import psidev.psi.mi.xml.jaxb.EntrySet;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
public class PsicquicServiceImpl implements PsicquicService {

    public QueryResponse getByInteractor(DbRefRequestType dbRef) throws NotSupportedDataTypeException, NotSupportedMethodException, PsicquicServiceException {
        QueryResponse response = new QueryResponse();

        ResultInfoType info = new ResultInfoType();
        info.setFirstResult(dbRef.getFirstResult());
        info.setBlockSize(dbRef.getBlockSize());
        info.setTotalResults(12345);

        response.setResultInfo(info);

        ResultSetType rs = new ResultSetType();

        EntrySet entrySet = new EntrySet();
        entrySet.setLevel(2);
        entrySet.setVersion(5);
        entrySet.setMinorVersion(4);

        Entry entry = new Entry();
        entrySet.getEntries().add(entry);

        rs.setEntrySet(entrySet);

        response.setResultSet(rs);

        return response;
    }

    public QueryResponse getByInteraction(DbRefRequestType dbRef) throws NotSupportedDataTypeException, NotSupportedMethodException, PsicquicServiceException {
        return null;
    }

    public QueryResponse getByInteractorList(DbRefListRequestType dbRefList) throws NotSupportedDataTypeException, NotSupportedMethodException, PsicquicServiceException {
        return null;
    }

    public QueryResponse getBetweenList(DbRefListRequestType dbRefList) throws NotSupportedDataTypeException, NotSupportedMethodException, PsicquicServiceException {
        return null;
    }

    public QueryResponse getByInteractionList(DbRefListRequestType dbRefList) throws NotSupportedDataTypeException, NotSupportedMethodException, PsicquicServiceException {
        return null;
    }

    public QueryResponse getByQuery(GetByQueryStringRequest query) throws NotSupportedDataTypeException, NotSupportedMethodException, PsicquicServiceException {
        return null;
    }

    public String getVersion(EmptyType type) throws PsicquicServiceException {
        return "NO VERSION";
    }

    public SupportedTypes getSupportedReturnTypes(EmptyType type) throws PsicquicServiceException {
        return null;
    }

    public SupportedTypes getSupportedDbRefTypes(EmptyType type) throws PsicquicServiceException {
        return null;
    }

    public SupportedTypes getSupportedQueryTypes(EmptyType type) throws PsicquicServiceException {
        return null;
    }
}
