package uk.ac.ebi.intact.editor.ws;

import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.intact.core.users.model.User;
import uk.ac.ebi.intact.core.users.persistence.dao.UsersDaoFactory;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * REST service to export users.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.0
 */
public class UserExportServiceImpl implements UserExportService {

    @Autowired
    private UsersDaoFactory usersDaoFactory;

    @Override
    public Object exportAll() {
        Response response = null;
        try {

            final List<User> all = usersDaoFactory.getUserDao().getAll();
            UsersStreamingOutput output = new UsersStreamingOutput( all );
            response = Response.status(200).type("application/xml").entity(output).build();
        } catch (Throwable e) {
            throw new RuntimeException("Problem exporting all users to XML", e);
        }
        return response;
    }

    @Override
    public Object export( String userLogins ) {
        System.out.println("logins="+userLogins);
        Response response = null;
        try {
            final String[] logins = userLogins.split( "," );
            final List<User> users = new ArrayList<User>( logins.length );
            for ( int i = 0; i < logins.length; i++ ) {
                String login = logins[i];
                User user = usersDaoFactory.getUserDao().getByLogin( login );
                if( user == null ) {
                     throw new RuntimeException("Could not export, unknown user: '" + login + "'" );
                }
                users.add( user );
            }

            UsersStreamingOutput output = new UsersStreamingOutput( users );
            response = Response.status(200).type("application/xml").entity(output).build();
        } catch (Throwable e) {
            throw new RuntimeException("Problem exporting all users to XML", e);
        }
        return response;
    }
}
