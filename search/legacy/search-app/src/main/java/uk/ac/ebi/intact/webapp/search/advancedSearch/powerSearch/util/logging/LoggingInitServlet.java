/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.webapp.search.advancedSearch.util.logging;

import org.apache.log4j.PropertyConfigurator;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Inititialize the Logger with the log4J properties file.
 * <p/>
 * Created by Samuel Kerrien (skerrien@ebi.ac.uk)
 */

public class LoggingInitServlet extends HttpServlet {

    public static final String CATALINA_HOME = System.getProperty("catalina.home");
    public static final String LOG4J_CONFIG_FILENAME = "log4j.properties";


    private void loadConfig() {
        String configFile = null;
        URL configUrl = null;

        boolean fileFound = false;

        if (null != CATALINA_HOME) {
            // get the log4j file from the config directory of tomcat - provide a flexible way of reloading it at runtime
            configFile = CATALINA_HOME + File.separator + "conf" + File.separator + LOG4J_CONFIG_FILENAME;
            System.out.println("Path built: " + configFile);

            File file = new File(configFile);

            if (file.exists()) {

                if (file.isFile()) {

                    if (file.canRead()) {
                        try {
                            configUrl = file.toURL();
                            System.out.println("URL: " + configUrl.toString());
                            fileFound = true;
                        } catch (MalformedURLException e) {
                            fileFound = false;
                        }
                    } else {
                        System.out.println("Not readable");
                    }
                } else {
                    System.out.println("Not a file");
                }
            } else {
                System.out.println("Does not exists");
            }
        }


        if (!fileFound) {
            // if the file doesn't exist in ${CATALINA_HOME}/conf, we try to pick it up from the war file.
            configFile = getInitParameter("log4j-init-file");
            if (configFile != null) {
                try {
                    configUrl = getServletContext().getResource(configFile);
                } catch (MalformedURLException e) {
                    System.out.println("LOGGING INIT: Couldn't get the logging file path from resource " + configFile);
                    return;
                }
            } else {
                System.out.println("LOGGING INIT: configuration file could not be found (" + configFile + ").");
                return;
            }
        }



        /* Load the configuration file */
        System.out.println("LOGGING INIT: load logging properties file " + configUrl.toString());
        PropertyConfigurator.configure(configUrl);
        System.out.println("LOGGING INIT: log4j has been initialised successfully.");

        /* For eventual later use, allow to reload the config at regular interval of time */
        // configureAndWatch(String configFilename, long delay_in_milliseconds)

    }


    /**
     * You should include the folowing lines in your web.xml file to load that
     * servlet on Tomcat startup.
     * <p/>
     * <p/>
     * &lt;servlet&gt;<br>
     * &lt;servlet-name>logging-init&lt;/servlet-name&gt;<br>
     * &lt;servlet-class&gt;uk.ac.ebi.intact.webapp.webapp.commons.logging.LoggingInitServlet&lt;/servlet-class&gt;
     * <br>
     * &lt;init-param&gt;<br>
     * &lt;param-name>log4j-init-file&lt;/param-name&gt;<br>
     * &lt;param-value&gt;/WEB-INF/classes/config/log4j.properties&lt;/param-value&gt;<br>
     * &lt;/init-param&gt;<br>
     * <br>
     * &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;<br>
     * &lt;/servlet&gt;<br>
     * </p>
     */
    public void init() {

        loadConfig();
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) {

        loadConfig();
    }
}

