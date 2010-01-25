package uk.ac.ebi.intact.psicquic.ws.legacy;

import org.hupo.psi.mi.psicquic.NotSupportedMethodException;
import org.hupo.psi.mi.psicquic.NotSupportedTypeException;
import org.hupo.psi.mi.psicquic.PsicquicServiceException;

import javax.ws.rs.*;

/**
 * RESTful web service.
 *
 * v.1.0/search/query/brca2
 * current/search/query/species:human?format=xml25&firstResult=50&maxResults=100
 *
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id: IntactPsicquicService.java 12873 2009-03-18 02:51:31Z baranda $
 */
@Path("/search")
public interface PsicquicRestService10 {

    @GET
    @Path("/interactor/{interactorAc}")
    Object getByInteractor(@PathParam("interactorAc") String interactorAc,
                           @DefaultValue("") @QueryParam("db") String db,
                           @DefaultValue("tab25") @QueryParam("format") String format,
                           @DefaultValue("0") @QueryParam("firstResult") String firstResult,
                           @DefaultValue("2147483647") @QueryParam("maxResults") String maxResults) throws PsicquicServiceException,
            NotSupportedMethodException,
            NotSupportedTypeException;

    @GET
    @Path("/interaction/{interactionAc}")
    Object getByInteraction(@PathParam("interactionAc") String interactionAc,
                            @DefaultValue("") @QueryParam("db") String db,
                            @DefaultValue("tab25") @QueryParam("format") String format,
                            @DefaultValue("0") @QueryParam("firstResult") String firstResult,
                            @DefaultValue("2147483647") @QueryParam("maxResults") String maxResults) throws PsicquicServiceException,
            NotSupportedMethodException,
            NotSupportedTypeException;

    @GET
    @Path("/query/{query}")
    Object getByQuery(@PathParam("query") String query,
                      @DefaultValue("tab25") @QueryParam("format") String format,
                      @DefaultValue("0") @QueryParam("firstResult") String firstResult,
                      @DefaultValue("2147483647") @QueryParam("maxResults") String maxResults) throws PsicquicServiceException,
            NotSupportedMethodException,
            NotSupportedTypeException;

    @GET
    @Path("/formats")
    Object getSupportedFormats() throws PsicquicServiceException,
            NotSupportedMethodException,
            NotSupportedTypeException;

    @GET
    @Path("/version")
    String getVersion();

}