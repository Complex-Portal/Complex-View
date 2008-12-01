/*
Copyright (c) 2002-2005 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import uk.ac.ebi.intact.application.editor.business.EditUser;
import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.application.editor.exception.AuthenticateException;
import uk.ac.ebi.intact.context.IntactConfigurator;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.context.IntactSession;
import uk.ac.ebi.intact.context.UserContext;
import uk.ac.ebi.intact.context.impl.WebappSession;
import uk.ac.ebi.intact.context.impl.StandaloneSession;
import uk.ac.ebi.intact.config.impl.StandardCoreDataConfig;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * The custom authenticator for Intact editor.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class UserAuthenticator {

    private static final Log log = LogFactory.getLog(UserAuthenticator.class);

    /**
     * Authenticate a user by accessing a persistent system using given
     * user name and password
     * @param username the user name
     * @param password the password
     * @return a user object if a user exists for <code>username</code> and
     * <code>password</code>.
     * @exception AuthenticateException invalid user for given
     * <code>username</code> and <code>password</code>.
     */
    public static EditUserI authenticate(String username, String password, HttpServletRequest request)
            throws AuthenticateException {

        if (log.isDebugEnabled()) log.debug("Authenticating user: "+username);

        // The authenticate method is called from the LoginAction. This LoginAction handles the request ending with
        // *login. The request ending with *login are not filtered by the IntactSessionRequestFilter. Therefore we
        // need to create the IntactContext as this is normally done by the IntactSessionRequestFilter.
        // The IntactContext allows us to get the database connection url.
        // (N.B. : the login request is not filtered by the IntactSessionRequestFilter as this filter automatically
        // creates a connection using a the default user name and password and not the one provided in the login
        // page by the user. This connection will of course work but won't allow us to know if the specific login
        // and password given are valid).

//        HttpSession session = request.getSession();
//        IntactSession intactSession = new WebappSession( session.getServletContext(), session, request );
//        IntactContext context = IntactConfigurator.createIntactContext( intactSession );

        // check if the user exists by trying to open a JDBC connection for that user
        StandardCoreDataConfig stdDataConfig = new StandardCoreDataConfig(new StandaloneSession());

        Configuration configuration = stdDataConfig.getConfiguration();
        configuration.configure();

        String url = configuration.getProperty(Environment.URL);
        String driver = configuration.getProperty(Environment.DRIVER);

        try
        {
            Class.forName(driver);
        }
        catch (ClassNotFoundException e)
        {
            throw new AuthenticateException("Problem loading database driver", e);
        }

        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            conn.close();
        } catch (SQLException e) {
            throw new AuthenticateException("Could not get connection for user : " + username +": Problem " +
                        "spliting the url to get the db sid" );
        }


        // get the database name from the connection url
        String databaseName = null;

        String splitUrl[] = url.split(":");
        if (splitUrl.length != 0) {
            databaseName = splitUrl[splitUrl.length - 1];
        } else {
            throw new AuthenticateException("Couldn't get database name from connection URL: "+url);
        }

        IntactContext context = IntactContext.getCurrentInstance();

        // Set the userId and userPassword of the UserContext
        UserContext userContext = context.getUserContext();
        userContext.setUserId( username );
        userContext.setUserPassword( password );

        return new EditUser(username, password, databaseName);
    }


}
