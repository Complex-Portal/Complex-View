package uk.ac.ebi.intact.psicquic.ws;

import org.hupo.psi.mi.psicquic.PsicquicServiceException;
import org.hupo.psi.mi.psicquic.NotSupportedMethodException;
import org.hupo.psi.mi.psicquic.NotSupportedTypeException;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import psidev.psi.mi.xml254.jaxb.EntrySet;

/**
 * This RESTful web service is based on a PSIMITAB SOLR index to search and return the results in PSIMITAB format.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id: IntactPsicquicService.java 12873 2009-03-18 02:51:31Z baranda $
 */
@Path("/interactions")
public interface PsicquicRestService {

    @GET
    @Path("/interactor/{interactorAc}")
    @Produces("text/plain")
    StreamingOutput getByInteractor(@PathParam("interactorAc") String interactorAc,
                                    @DefaultValue("") @QueryParam("db") String db) throws PsicquicServiceException,
            NotSupportedMethodException,
            NotSupportedTypeException;

    @GET
    @Path("/interaction/{interactionAc}")
    @Produces("text/plain")
    StreamingOutput getByInteraction(@PathParam("interactionAc") String interactionAc, 
                                    @DefaultValue("") @QueryParam("db") String db) throws PsicquicServiceException,
            NotSupportedMethodException,
            NotSupportedTypeException;

    @GET
    @Path("/query/{query}")
    @Produces("text/plain")
    StreamingOutput getByQuery(@PathParam("query") String query) throws PsicquicServiceException,
            NotSupportedMethodException,
            NotSupportedTypeException;

    @GET
    @Path("/version")
    String getVersion();

    @GET
    @Path("/entrySet/{query}")
    @Produces("application/xml")
    EntrySet getEntrySetByQuery(@PathParam("query") String query) throws PsicquicServiceException,
            NotSupportedMethodException,
            NotSupportedTypeException;
}
