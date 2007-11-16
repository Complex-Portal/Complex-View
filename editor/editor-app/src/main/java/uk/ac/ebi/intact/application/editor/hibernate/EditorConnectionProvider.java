package uk.ac.ebi.intact.application.editor.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.connection.ConnectionProvider;
import uk.ac.ebi.intact.config.impl.StandardCoreDataConfig;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.context.impl.StandaloneSession;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class EditorConnectionProvider implements ConnectionProvider
{
    private static final Log log = LogFactory.getLog(EditorConnectionProvider.class);

    private boolean driverLoaded;
    private String currentUser;

    private Connection userConnection;

    public void configure(Properties properties) throws HibernateException
    {

    }

    public Connection getConnection() throws SQLException
    {
        log.debug("Getting connection for user: " + currentUser);

        Connection connection = null;

        if (IntactContext.currentInstanceExists())
        {
            Configuration configuration = (Configuration)IntactContext.getCurrentInstance().getConfig().getDefaultDataConfig().getConfiguration();

            currentUser = IntactContext.getCurrentInstance().getUserContext().getUserId();
            String currentUserPassword = IntactContext.getCurrentInstance().getUserContext().getUserPassword();
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

            if (currentUser != null)
            {
                /*
                if (userConnection != null && !userConnection.isClosed()) {
                    if (log.isDebugEnabled()) log.debug("Reusing user connection: "+currentUser);
                    return userConnection;
                }
                if (log.isDebugEnabled()) log.debug("Creating new connection for user: "+currentUser);

                userConnection = DriverManager.getConnection(url, currentUser, currentUserPassword);
                connection = userConnection;
                */
                connection = ConnectionManager.getInstance().getCurrentConnectionForUser();
            }
            else
            {
                if (log.isDebugEnabled()) log.debug("Creating default connection (IntactContext exists)");
                connection = createDefaultConnection();
            }
        }
        else // currentInstance does not exist
        {
            if (log.isDebugEnabled()) log.debug("Creating default connection (No IntactContext available)");
             connection = createDefaultConnection();
        }

        return connection;
    }

    public void closeConnection(Connection connection) throws SQLException
    {
        if (currentUser != null) {
            //log.debug("Closing connection for user: "+IntactContext.getCurrentInstance().getUserContext().getUserId());
            //connection.close();
        }
    }

    public void close() throws HibernateException
    {
    }

    public boolean supportsAggressiveRelease()
    {
        return false;
    }

    private Connection createDefaultConnection() throws SQLException
    {
        StandardCoreDataConfig stdDataConfig = new StandardCoreDataConfig(new StandaloneSession());

        Configuration configuration = stdDataConfig.getConfiguration();
        configuration.configure();

        currentUser = configuration.getProperty(Environment.USER);
        String password = configuration.getProperty(Environment.PASS);
        String url = configuration.getProperty(Environment.URL);
        String driver = configuration.getProperty(Environment.DRIVER);

        try
        {
            Class.forName(driver);
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        log.debug("Using default connection - User: " + currentUser + " Url: " + url);

        return DriverManager.getConnection(url, currentUser, password);
    }
}