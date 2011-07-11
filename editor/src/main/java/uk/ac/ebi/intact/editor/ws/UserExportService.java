package uk.ac.ebi.intact.editor.ws;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

/**
 * User expot REST service.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.0
 */
@Path("/users")
public interface UserExportService {

    @GET
    @Path("/all")
    Object exportAll();

    /**
     *
     * @param userLogins a comma separated list of user login that we want to export.
     * @return
     */
    @GET
    @Path("/byLogin")
    Object export( @QueryParam("logins") String userLogins );
}
