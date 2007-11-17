/*
 * Copyright 2001-2007 The European Bioinformatics Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.application.editor.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;

import javax.servlet.ServletContext;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ConnectionManager implements Serializable {

    /**
     * Sets up a logger for that class.
     */
    private static final Log log = LogFactory.getLog(ConnectionManager.class);

    private static final String CONNECTION_MANAGER_PARAM = "editor.CONNECTION_MANAGER";


    public static ConnectionManager getInstance() {
        ConnectionManager cm = null;

        if (IntactContext.currentInstanceExists()) {

            cm = (ConnectionManager) IntactContext.getCurrentInstance().getSession().getApplicationAttribute(CONNECTION_MANAGER_PARAM);

            if (cm == null) {
                cm = new ConnectionManager();
                IntactContext.getCurrentInstance().getSession().setApplicationAttribute(CONNECTION_MANAGER_PARAM, cm);
            }

        } else {
            throw new IllegalStateException("ConnectionManager is only available if the IntactContext exists when using the ConnectionManager.getInstance() method" +
                    " without parameters");
        }

        return cm;
    }

    public static ConnectionManager getInstance(ServletContext servletContext) {
        ConnectionManager cm = null;

            cm = (ConnectionManager) servletContext.getAttribute(CONNECTION_MANAGER_PARAM);

            if (cm == null) {
                cm = new ConnectionManager();
                servletContext.setAttribute(CONNECTION_MANAGER_PARAM, cm);
            }

        return cm;
    }

    private Map<String, Connection> connectionMap;
    private Configuration configuration;
    private String defaultUser;

    private ConnectionManager() {
        this.connectionMap = new HashMap<String,Connection>();

        this.configuration = (Configuration) IntactContext.getCurrentInstance().getConfig().getDefaultDataConfig().getConfiguration();

        this.defaultUser = configuration.getProperty(Environment.USER);

        String driverClass = configuration.getProperty(Environment.DRIVER);
        try {
            Class.forName(driverClass);
        }
        catch (ClassNotFoundException e) {
            throw new IntactException("Driver not found: "+driverClass, e);
        }
    }

    protected Connection getCurrentConnectionForUser() {
        final String user = getCurrentUser();
        Connection userConnection = connectionMap.get(user);

        try {
            if (userConnection != null && !userConnection.isClosed()) {
                return userConnection;
            }

            if (log.isDebugEnabled()) log.debug("Creating new connection for user: "+user);
            String url = configuration.getProperty(Environment.URL);

            userConnection = DriverManager.getConnection(url, user, getCurrentPassword());

            connectionMap.put(user, userConnection);
            
        } catch (SQLException e) {
            throw new IntactException("Problem creating connection for user: "+user, e);
        }

        return userConnection;
    }


    protected void evictConnectionForUser(String user) {
        if (user.equals(defaultUser)) {
            if (log.isDebugEnabled()) log.debug("Not evicting and closing connection for default user: "+user);
            return;
        }

        if (log.isDebugEnabled()) log.debug("Evicting and closing connection for user: "+user);

        Connection userConnection = connectionMap.get(user);

        try {
            if (userConnection != null && !userConnection.isClosed()) {
                userConnection.close();
            }
        } catch (SQLException e) {
            throw new IntactException("Problem closing connection for user: "+user, e);
        }

        connectionMap.remove(getCurrentUser());
    }

    protected String getCurrentUser() {
        return IntactContext.getCurrentInstance().getUserContext().getUserId();

    }

    protected String getCurrentPassword() {
        return  IntactContext.getCurrentInstance().getUserContext().getUserPassword();
    }



}