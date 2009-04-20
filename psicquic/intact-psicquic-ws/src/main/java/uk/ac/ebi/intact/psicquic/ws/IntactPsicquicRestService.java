package uk.ac.ebi.intact.psicquic.ws;

import org.hupo.psi.mi.psicquic.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import psidev.psi.mi.xml254.jaxb.EntrySet;
import uk.ac.ebi.intact.psicquic.ws.config.PsicquicConfig;
import uk.ac.ebi.intact.psicquic.ws.util.PsicquicStreamingOutput;

/**
 * This web service is based on a PSIMITAB SOLR index to search and return the results.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id: IntactPsicquicService.java 12873 2009-03-18 02:51:31Z baranda $
 */
@Controller
public class IntactPsicquicRestService implements PsicquicRestService {

    @Autowired
    private PsicquicConfig config;

    @Autowired
    private PsicquicService psicquicService;

    public PsicquicStreamingOutput getByInteractor(String interactorAc, String db) throws PsicquicServiceException, NotSupportedMethodException, NotSupportedTypeException {
        String query = "id:"+createQueryValue(interactorAc, db);
        return getByQuery(query);
    }

    public PsicquicStreamingOutput getByInteraction(String interactionAc, String db) throws PsicquicServiceException, NotSupportedMethodException, NotSupportedTypeException {
        String query = "interaction_id:"+createQueryValue(interactionAc, db);
        return getByQuery(query);
    }

    public PsicquicStreamingOutput getByQuery(final String query) throws PsicquicServiceException,
                                                                 NotSupportedMethodException,
                                                                 NotSupportedTypeException {
        return new PsicquicStreamingOutput(psicquicService, query);
    }

    public String getVersion() {
        return config.getVersion();
    }

    public EntrySet getEntrySetByQuery(String query) throws PsicquicServiceException, NotSupportedMethodException, NotSupportedTypeException {
        RequestInfo reqInfo = new RequestInfo();
        reqInfo.setResultType("psi-mi/xml25");
        reqInfo.setFirstResult(0);
        reqInfo.setBlockSize(50);

        QueryResponse response = psicquicService.getByQuery(query, reqInfo);

        return response.getResultSet().getEntrySet();
    }

    private String createQueryValue(String interactorAc, String db) {
        StringBuilder sb = new StringBuilder(256);
        if (db.length() > 0) sb.append('"').append(db).append(':');
        sb.append(interactorAc);
        if (db.length() > 0) sb.append('"');

        return sb.toString();
    }
}
