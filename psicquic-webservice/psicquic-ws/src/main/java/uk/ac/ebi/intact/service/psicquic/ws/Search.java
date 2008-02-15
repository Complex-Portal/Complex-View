package uk.ac.ebi.intact.service.psicquic.ws;


import org.hupo.psi.mi.psicquic.*;

import javax.jws.WebService;
import java.io.IOException;
import java.util.Properties;

/**
 * PSICQUIC Web Service
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id: SearchServiceClient.java 8975 2007-07-17 13:26:43Z baranda $
 */
@WebService(endpointInterface = "org.hupo.psi.mi.psicquic.PsicquicPortType")
public class Search implements PsicquicPortType{

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
        Properties props = new Properties();
        try {
            props.load(Search.class.getResourceAsStream("/META-INF/BuildInfo.properties"));
        } catch (IOException e) {
            throw new PsicquicServiceException("Problem loading BuildInfo properties", null, e);
        }
        return (String) props.get("version");
    }

    public SupportedDataTypes getSupportedDataTypes() throws PsicquicServiceException {
        SupportedDataTypes supportedDataTypes = new SupportedDataTypes();
        supportedDataTypes.getDataTypes().add("mif");
        //supportedDataTypes.getDataTypes().add("mitab");
        return supportedDataTypes;
    }
}
