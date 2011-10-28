package uk.ac.ebi.intact.psicquic.ws.legacy;

import org.apache.commons.lang.StringUtils;
import org.hupo.psi.mi.psicquic.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import psidev.psi.mi.xml254.jaxb.EntrySet;
import uk.ac.ebi.intact.psicquic.ws.IntactPsicquicRestService;
import uk.ac.ebi.intact.psicquic.ws.IntactPsicquicService;
import uk.ac.ebi.intact.psicquic.ws.config.PsicquicConfig;
import uk.ac.ebi.intact.psicquic.ws.util.PsicquicStreamingOutput;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This web service is based on a PSIMITAB SOLR index to search and return the results.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id: IntactPsicquicService.java 12873 2009-03-18 02:51:31Z baranda $
 */
@Controller
public class IntactPsicquicRestService10 implements PsicquicRestService10 {

    public static final List<String> SUPPORTED_REST_RETURN_TYPES = Arrays.asList(
            IntactPsicquicRestService.RETURN_TYPE_XML25,
            IntactPsicquicRestService.RETURN_TYPE_MITAB25,
            IntactPsicquicRestService.RETURN_TYPE_COUNT);

     @Autowired
    private PsicquicConfig config;

    @Autowired
    private PsicquicService psicquicService;

    public Object getByInteractor(String interactorAc, String db, String format, String firstResult, String maxResults) throws PsicquicServiceException, NotSupportedMethodException, NotSupportedTypeException {
        String query = "id:"+createQueryValue(interactorAc, db)+ " OR alias:"+createQueryValue(interactorAc, db);
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
            if (maxResultsStr == null) {
                maxResults = Integer.MAX_VALUE;
            } else {
                maxResults = Integer.parseInt(maxResultsStr);
            }
        } catch (NumberFormatException e) {
            throw new PsicquicServiceException("maxResults parameter is not a number: "+maxResultsStr);
        }

        if (strippedMime(IntactPsicquicRestService.RETURN_TYPE_XML25).equals(format)) {
            return getByQueryXml(query, firstResult, maxResults);
        } else if (IntactPsicquicRestService.RETURN_TYPE_COUNT.equals(format)) {
            return count(query);
        } else if (strippedMime(IntactPsicquicRestService.RETURN_TYPE_MITAB25_BIN).equals(format)) {
            PsicquicStreamingOutput result = new PsicquicStreamingOutput(psicquicService, query, firstResult, maxResults, true);
            return Response.status(200).type("application/x-gzip").entity(result).build();
        } else if (strippedMime(IntactPsicquicRestService.RETURN_TYPE_MITAB25).equals(format) || format == null) {
            PsicquicStreamingOutput result = new PsicquicStreamingOutput(psicquicService, query, firstResult, maxResults);
            return Response.status(200).type(MediaType.TEXT_PLAIN).entity(result).build();
        } else {
            return Response.status(406).type(MediaType.TEXT_PLAIN).entity("Format not supported").build();
        }


    }

    public Object getSupportedFormats() throws PsicquicServiceException, NotSupportedMethodException, NotSupportedTypeException {
        List<String> formats = new ArrayList<String>(SUPPORTED_REST_RETURN_TYPES.size()+1);
        formats.add(strippedMime(IntactPsicquicRestService.RETURN_TYPE_MITAB25_BIN));

        for (String mime : SUPPORTED_REST_RETURN_TYPES) {
            formats.add(strippedMime(mime));
        }

        return Response.status(200)
                .type(MediaType.TEXT_PLAIN)
                .entity(StringUtils.join(formats, "\n")).build();
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

    private String strippedMime(String mimeType) {
        if (mimeType.indexOf("/") > -1) {
            return mimeType.substring(mimeType.indexOf("/")+1);
        } else {
            return mimeType;
        }
    }
}