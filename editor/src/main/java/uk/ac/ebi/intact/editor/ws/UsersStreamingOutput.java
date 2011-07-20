package uk.ac.ebi.intact.editor.ws;

import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.intact.core.persistence.svc.UserService;
import uk.ac.ebi.intact.model.user.User;

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

    @Autowired
    private UserService userService;

    private Collection<User> users;

    public UsersStreamingOutput( Collection<User> users ) {
        this.users = users;
    }

    @Override
    public void write( OutputStream outputStream ) throws IOException, WebApplicationException {
        try {
            userService.marshallUsers( users, outputStream );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }
}
