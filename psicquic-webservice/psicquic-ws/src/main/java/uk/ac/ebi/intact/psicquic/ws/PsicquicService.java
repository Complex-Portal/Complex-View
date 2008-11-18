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
package uk.ac.ebi.intact.psicquic.ws;

import org.hupo.psi.mi.psicquic.*;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@WebService(name = "psicquicService", targetNamespace = "http://psi.hupo.org/mi/psicquic")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@XmlSeeAlso({
    psidev.psi.mi.xml.jaxb.ObjectFactory.class,
    org.hupo.psi.mi.psicquic.ObjectFactory.class,
    uk.ac.ebi.intact.service.psicquic.commons.mitab.ObjectFactory.class
})
public interface PsicquicService {


    /**
     *
     * @param dbRef
     * @return
     *     returns org.hupo.psi.mi.psicquic.QueryResponse
     * @throws org.hupo.psi.mi.psicquic.PsicquicServiceException
     * @throws org.hupo.psi.mi.psicquic.NotSupportedMethodException
     * @throws org.hupo.psi.mi.psicquic.NotSupportedTypeException
     */
    @WebMethod(action = "getByInteractor")
    @WebResult(name = "QueryResponse", targetNamespace = "http://psi.hupo.org/mi/psicquic", partName = "QueryResponse")
    public QueryResponse getByInteractor(
        @WebParam(name = "getByInteractorRequest", targetNamespace = "http://psi.hupo.org/mi/psicquic", partName = "dbRef")
        DbRefRequest dbRef)
        throws NotSupportedMethodException, NotSupportedTypeException, PsicquicServiceException
    ;

    /**
     *
     * @param dbRef
     * @return
     *     returns org.hupo.psi.mi.psicquic.QueryResponse
     * @throws PsicquicServiceException
     * @throws NotSupportedMethodException
     * @throws NotSupportedTypeException
     */
    @WebMethod(action = "getByInteraction")
    @WebResult(name = "QueryResponse", targetNamespace = "http://psi.hupo.org/mi/psicquic", partName = "QueryResponse")
    public QueryResponse getByInteraction(
        @WebParam(name = "getByInteractionRequest", targetNamespace = "http://psi.hupo.org/mi/psicquic", partName = "dbRef")
        DbRefRequest dbRef)
        throws NotSupportedMethodException, NotSupportedTypeException, PsicquicServiceException
    ;

    /**
     *
     * @return
     *     returns org.hupo.psi.mi.psicquic.QueryResponse
     * @throws PsicquicServiceException
     * @throws NotSupportedMethodException
     * @throws NotSupportedTypeException
     */
    @WebMethod(action = "getByInteractorList")
    @WebResult(name = "QueryResponse", targetNamespace = "http://psi.hupo.org/mi/psicquic", partName = "QueryResponse")
    public QueryResponse getByInteractorList(
        @WebParam(name = "dbRefListRequest", targetNamespace = "http://psi.hupo.org/mi/psicquic", partName = "dbRefListRequest")
        DbRefListRequest dbRefListRequest,
        @WebParam(name = "operand", targetNamespace = "http://psi.hupo.org/mi/psicquic", partName = "operand")
        String operand)
        throws NotSupportedMethodException, NotSupportedTypeException, PsicquicServiceException
    ;

    /**
     *
     * @param dbRefList
     * @return
     *     returns org.hupo.psi.mi.psicquic.QueryResponse
     * @throws PsicquicServiceException
     * @throws NotSupportedMethodException
     * @throws NotSupportedTypeException
     */
    @WebMethod(action = "getByInteractionList")
    @WebResult(name = "QueryResponse", targetNamespace = "http://psi.hupo.org/mi/psicquic", partName = "QueryResponse")
    public QueryResponse getByInteractionList(
        @WebParam(name = "getByInteractionListRequest", targetNamespace = "http://psi.hupo.org/mi/psicquic", partName = "dbRefList")
        DbRefListRequest dbRefList)
        throws NotSupportedMethodException, NotSupportedTypeException, PsicquicServiceException
    ;

    /**
     *
     * @param query
     * @return
     *     returns org.hupo.psi.mi.psicquic.QueryResponse
     * @throws PsicquicServiceException
     * @throws NotSupportedMethodException
     * @throws NotSupportedTypeException
     */
    @WebMethod(action = "getByQuery")
    @WebResult(name = "QueryResponse", targetNamespace = "http://psi.hupo.org/mi/psicquic", partName = "QueryResponse")
    public QueryResponse getByQuery(
        @WebParam(name = "getByQueryStringRequest", targetNamespace = "http://psi.hupo.org/mi/psicquic", partName = "query")
        QueryRequest query)
        throws NotSupportedMethodException, NotSupportedTypeException, PsicquicServiceException
    ;

    /**
     *
     * @return
     *     returns java.lang.String
     * @throws PsicquicServiceException
     */
    @WebMethod(action = "getVersion")
    @WebResult(name = "getVersionResponse", targetNamespace = "http://psi.hupo.org/mi/psicquic", partName = "output")
    public String getVersion()
        throws PsicquicServiceException
    ;

    /**
     *
     * @return
     *     returns org.hupo.psi.mi.psicquic.SupportedTypes
     * @throws PsicquicServiceException
     */
    @WebMethod(action = "getSupportedReturnTypes")
    @WebResult(name = "supportedTypes", targetNamespace = "http://psi.hupo.org/mi/psicquic", partName = "output")
    public SupportedTypes getSupportedReturnTypes()
        throws PsicquicServiceException
    ;

    /**
     *
     * @return
     *     returns org.hupo.psi.mi.psicquic.SupportedTypes
     * @throws PsicquicServiceException
     */
    @WebMethod(action = "getSupportedDbAcs")
    @WebResult(name = "supportedTypes", targetNamespace = "http://psi.hupo.org/mi/psicquic", partName = "output")
    public SupportedTypes getSupportedDbAcs()
        throws PsicquicServiceException
    ;

    /**
     *
     * @return
     *     returns org.hupo.psi.mi.psicquic.SupportedTypes
     * @throws PsicquicServiceException
     */
    @WebMethod(action = "getSupportedQueryTypes")
    @WebResult(name = "supportedTypes", targetNamespace = "http://psi.hupo.org/mi/psicquic", partName = "output")
    public SupportedTypes getSupportedQueryTypes()
        throws PsicquicServiceException
    ;

}

