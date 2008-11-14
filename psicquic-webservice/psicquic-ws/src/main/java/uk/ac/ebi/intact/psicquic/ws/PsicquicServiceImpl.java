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
import uk.ac.ebi.intact.context.IntactContext;

import javax.jws.WebParam;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
public class PsicquicServiceImpl implements PsicquicPortType {

    public QueryResponse getByInteractor(DbRefRequestType dbRef) throws NotSupportedDataTypeException, NotSupportedMethodException, PsicquicServiceException {
        return null;
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

    public QueryResponse getByInteractionList(GetByInteractionListRequest dbRefList) throws NotSupportedDataTypeException, NotSupportedMethodException, PsicquicServiceException {
        return null;
    }

    public QueryResponse getByQuery(GetByQueryStringRequest query) throws NotSupportedDataTypeException, NotSupportedMethodException, PsicquicServiceException {
        return null;
    }

    public String getVersion() throws PsicquicServiceException {
        return "NO VERSION!";
    }

    public SupportedDataTypes getSupportedDataTypes() throws PsicquicServiceException {
        return null;
    }
}
