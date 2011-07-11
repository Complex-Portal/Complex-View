package uk.ac.ebi.intact.editor.ws;

import uk.ac.ebi.intact.core.users.model.User;
import uk.ac.ebi.intact.editor.util.UserMigrationUtils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

/**
 * Streaming the output of our REST service.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.0
 */
public class UsersStreamingOutput implements StreamingOutput {

    private Collection<User> users;

    public UsersStreamingOutput( Collection<User> users ) {
        this.users = users;
    }

    @Override
    public void write( OutputStream outputStream ) throws IOException, WebApplicationException {
        try {
            final UserMigrationUtils migrationUtils = new UserMigrationUtils();
            migrationUtils.exportUsers( users, outputStream );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }
}
