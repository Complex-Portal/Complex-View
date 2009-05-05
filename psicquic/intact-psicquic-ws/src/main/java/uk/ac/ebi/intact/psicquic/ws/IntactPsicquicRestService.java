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

    public Object getByInteractor(String interactorAc, String db, String format, String firstResult, String maxResults) throws PsicquicServiceException, NotSupportedMethodException, NotSupportedTypeException {
        String query = "id:"+createQueryValue(interactorAc, db);
        return getByQuery(query, format, firstResult, maxResults);
    }

    public Object getByInteraction(String interactionAc, String db, String format, String firstResult, String maxResults) throws PsicquicServiceException, NotSupportedMethodException, NotSupportedTypeException {
        String query = "interaction_id:"+createQueryValue(interactionAc, db);
        return getByQuery(query, format, firstResult, maxResults);
    }

    public Object getByQuery(String query, String format,
                                                 String firstResultStr,
                                                 String maxResultsStr) throws PsicquicServiceException,
                                                                 NotSupportedMethodException,
                                                                 NotSupportedTypeException {
        int firstResult;
        int maxResults;

        try {
            firstResult =Integer.parseInt(firstResultStr);
        } catch (NumberFormatException e) {
            throw new PsicquicServiceException("firstResult parameter is not a number: "+firstResultStr);
        }

        try {
            maxResults = Integer.parseInt(maxResultsStr);
        } catch (NumberFormatException e) {
            throw new PsicquicServiceException("maxResults parameter is not a number: "+maxResultsStr);
        }

        if ("xml25".equals(format)) {
            return getByQueryXml(query, firstResult, maxResults);
        } else if ("count".equals(format)) {
            return count(query);
        }

        return new PsicquicStreamingOutput(psicquicService, query, firstResult, maxResults);
    }
    public String getVersion() {
        return config.getVersion();
    }

    public EntrySet getByQueryXml(String query,
                                  int firstResult,
                                  int maxResults) throws PsicquicServiceException, NotSupportedMethodException, NotSupportedTypeException {
        RequestInfo reqInfo = new RequestInfo();
        reqInfo.setResultType("psi-mi/xml25");

        try {
            reqInfo.setFirstResult(firstResult);
        } catch (NumberFormatException e) {
            throw new PsicquicServiceException("firstResult parameter is not a number: "+firstResult);
        }

        try {
            reqInfo.setBlockSize(maxResults);
        } catch (NumberFormatException e) {
            throw new PsicquicServiceException("maxResults parameter is not a number: "+maxResults);
        }

        QueryResponse response = psicquicService.getByQuery(query, reqInfo);

        return response.getResultSet().getEntrySet();
    }

    private int count(String query) throws NotSupportedTypeException, NotSupportedMethodException, PsicquicServiceException {
        RequestInfo reqInfo = new RequestInfo();
        reqInfo.setResultType("count");
        QueryResponse response = psicquicService.getByQuery(query, reqInfo);
        return response.getResultInfo().getTotalResults();
    }

    private String createQueryValue(String interactorAc, String db) {
        StringBuilder sb = new StringBuilder(256);
        if (db.length() > 0) sb.append('"').append(db).append(':');
        sb.append(interactorAc);
        if (db.length() > 0) sb.append('"');

        return sb.toString();
    }
}
