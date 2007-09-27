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
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.business.IntactTransactionException;
import uk.ac.ebi.intact.context.*;
import uk.ac.ebi.intact.context.impl.WebappSession;
import uk.ac.ebi.intact.webapp.IntactSessionRequestFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.ResultSet;

/**
 * The custom authenticator for Intact editor.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class UserAuthenticator {

    private static final Log log = LogFactory.getLog(UserAuthenticator.class);

    // boolean to knwow if the db driver has been loaded. (boolean default value is false)
    private static boolean driverLoaded;

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

        Connection connection = null;

        // The authenticate method is called from the LoginAction. This LoginAction handles the request ending with
        // *login. The request ending with *login are not filtered by the IntactSessionRequestFilter. Therefore we
        // need to create the IntactContext as this is normally done by the IntactSessionRequestFilter.
        // The IntactContext allows us to get the database connection url.
        // (N.B. : the login request is not filtered by the IntactSessionRequestFilter as this filter automatically
        // creates a connection using a the default user name and password and not the one provided in the login
        // page by the user. This connection will of course work but won't allow us to know if the specific login
        // and password given are valid).
        HttpSession session = request.getSession();
        IntactSession intactSession = new WebappSession( session.getServletContext(), session, request );
        IntactContext context = IntactConfigurator.createIntactContext( intactSession );

        // Set the userId and userPassword of the UserContext
        UserContext userContext = context.getUserContext();
        userContext.setUserId(username.toUpperCase());
        userContext.setUserPassword(password);

        String databaseName = "";
        try {
            // Get the Connection for the given username and password.
            connection = getConnection(username.toUpperCase(), password);
            // Get the databaseName to give it to the EditUser constructor, this value will be used to display the
            // database name on the side bar of the editor page. I'm taking it from the url, but I guess it could
            // be taken from somewhere else.
            String url = connection.getMetaData().getURL();
            String splitUrl[] = url.split(":");
            if(splitUrl.length != 0){
                databaseName = splitUrl[splitUrl.length - 1];
            }else{
                //The connection must be associated to a url, if it's not send an AuthenticateException.
                throw new AuthenticateException("Could not get connection for user : " + username +": Problem " +
                        "spliting the url to get the db sid" );
            }
            log.debug("The database name is : " + databaseName);
        } catch (SQLException e) {
            // Was used before when *login request was filetered by the  IntactSessionRequestFilter. Not used anymore
            // but let it here as an example in case we would put back the filtering.
            //IntactSessionRequestFilter.setCommitErrorMessage(IntactSessionRequestFilter.COULD_NOT_LOGIN, IntactContext.getCurrentInstance().getSession());

            // If getConnection() or connection.getMetaData().getUrl() throw an SQLException, send an
            // AuthenticateException. The web.xml of the Editor is set so that it will display a specific message for
            // authenticate Exception ('Wrong login or password')
            throw new AuthenticateException("Could not get connection for user : " + username + ": " + e);
        }finally{
            // We make sure that the Connection is closed
            if(connection != null){
                try{
                    if(connection.isClosed()){
                        connection.close();
                    }
                }catch(SQLException e){
                    log.error("Could not close connection : " + e);
                    throw new AuthenticateException("Could not close connection : " + e);
                }

            }
        }
        return new EditUser(username, password, databaseName);
    }


    private static Connection getConnection(String userLogin, String userPassword) throws SQLException {
        Connection connection = null;

        if (IntactContext.currentInstanceExists())
        {
            Configuration configuration = (Configuration)IntactContext.getCurrentInstance().getConfig().getDefaultDataConfig().getConfiguration();

            String url = configuration.getProperty(Environment.URL);

            if (!driverLoaded)
            {
                String driverClass = configuration.getProperty(Environment.DRIVER);
                try
                {
                    Class.forName(driverClass);
                }
                catch (ClassNotFoundException e)
                {
                    e.printStackTrace();
                }
                driverLoaded = true;
            }

            if (userLogin != null)
            {
                connection = DriverManager.getConnection(url, userLogin, userPassword);
            }
        }
        return connection;
    }
}
