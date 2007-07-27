/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.imex.idassigner.keyassigner;

import org.apache.axis.client.Stub;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.imex.idassigner.id.IMExRange;
import uk.ac.ebi.intact.imex.idassigner.keyassigner.generated.RegIMExService;
import uk.ac.ebi.intact.imex.idassigner.keyassigner.generated.SubmissionAxis;
import uk.ac.ebi.intact.imex.idassigner.keyassigner.generated.RegIMEx_PortType;
import uk.ac.ebi.intact.imex.idassigner.keyassigner.generated.RegIMExServiceLocator;
import uk.ac.ebi.intact.imex.idassigner.helpers.IMExHelper;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.persistence.dao.BaseDao;

import javax.xml.rpc.ServiceException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Wrapper around the Key Assigner Web Service.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>05-May-2006</pre>
 */
public class KeyAssignerService implements KeyAssignerServiceI {

    public static final Log log = LogFactory.getLog( KeyAssignerService.class );

    /**
     * System property defining where is the Key Store when using SSL protocol.
     */
    public static final String SYSTEM_KEYSTORE_PROPERTY = "javax.net.ssl.trustStore";

    //////////////////////////////
    // Instance variables

    /**
     * URL of the web service.
     */
    private String imexKeyAssignerUrl;

    /**
     * Where is our local key store.
     */
    private String keyStoreFilename;

    /**
     * Username accessing the Key Assigner.
     */
    private String username = null;

    /**
     * Password of the user accessing the Key Assigner.
     */
    private String password = null;

    ///////////////////////////////
    // Constructors

    /**
     *
     */
    public KeyAssignerService() throws KeyAssignerServiceException {
        initialise();
    }

    /**
     * Build a KeyAssignerService using a specific URL for the web service a s specific location of the key store.
     *
     * @param keyAssignerUrl
     * @param keyStoreFile
     */
    public KeyAssignerService( URL keyAssignerUrl, File keyStoreFile ) throws KeyAssignerServiceException {

        if ( keyStoreFile != null ) {

            if ( keyStoreFile.exists() ) {

                if ( keyStoreFile.canRead() ) {

                    log.info( "Setting keyStore from constructor's parameter (" + keyStoreFile.getAbsolutePath() + ")." );
                    keyStoreFilename = keyStoreFile.getAbsolutePath();

                } else {

                    System.out.println( "The given keyStore is not readable. will try to get valid value from properties file." );
                    log.warn( "either the given keyStoreFile was null, didn't exist or was not readable. Will use " + IMExHelper.IMEX_PROPERTIES_FILE + " instead." );
                }
            } else {
                // doesn't exits
                System.out.println( "The given KeyStore doesn't exist. Will use " + IMExHelper.IMEX_PROPERTIES_FILE + " instead." );
            }
        } else {
            // null
            System.out.println( "The given KeyStore was null. Will use " + IMExHelper.IMEX_PROPERTIES_FILE + " instead." );
        }

        if ( imexKeyAssignerUrl != null ) {
            imexKeyAssignerUrl = keyAssignerUrl.toString();
            System.out.println( "Using input param to set IMEx URL: " + imexKeyAssignerUrl );
        }

        initialise();
    }

    //////////////////////////
    // Private methods

    /**
     * Initialise configuration based on parameters given in the constructor of the service or if not given, from a
     * properties file (imex.properties).
     *
     * @throws KeyAssignerServiceException
     */
    private void initialise() throws KeyAssignerServiceException {

        if ( keyStoreFilename == null || imexKeyAssignerUrl == null ) {

            if ( IMExHelper.isIMExPropertiesFileAvailable() ) {

                System.out.println( "Initialising from properties file..." );

                if ( imexKeyAssignerUrl == null ) {
                    System.out.println( "Initialising IMEx URL..." );
                    imexKeyAssignerUrl = IMExHelper.getKeyAssignerUrl();
                    System.out.println( "imexKeyAssignerUrl = " + imexKeyAssignerUrl );
                }

                if ( keyStoreFilename == null ) {
                    System.out.println( "Initialising KeyStore..." );
                    keyStoreFilename = IMExHelper.getKeyStoreFilename();
                    System.out.println( "keyStoreFilename = " + keyStoreFilename );
                }

                // read username and password.
                username = IMExHelper.getKeyAssignerUsername();
                if ( username == null || username.trim().length() == 0 ) {
                    throw new KeyAssignerServiceException( "Could not find Key Assigner username. please check " + IMExHelper.IMEX_PROPERTIES_FILE );
                }

                password = IMExHelper.getKeyAssignerPassword();
                if ( password == null || password.trim().length() == 0 ) {
                    throw new KeyAssignerServiceException( "Could not find Key Assigner password. please check " + IMExHelper.IMEX_PROPERTIES_FILE );
                }

            } else {

                throw new KeyAssignerServiceException( "Could not load properties file: " + IMExHelper.IMEX_PROPERTIES_FILE );
            }
        }

        // check on current System props
        String currentSysProps = System.getProperty( SYSTEM_KEYSTORE_PROPERTY );

        if ( currentSysProps != null ) {

            System.out.println( "System's property is already set (" + SYSTEM_KEYSTORE_PROPERTY + " = " + currentSysProps + ")" );
            System.out.println( "Ignoring configuration coming from " + IMExHelper.IMEX_PROPERTIES_FILE );

        } else if ( keyStoreFilename != null ) {

            System.out.println( "Setting " + SYSTEM_KEYSTORE_PROPERTY + " to " + keyStoreFilename + " ..." );
            log.info( "Setting " + SYSTEM_KEYSTORE_PROPERTY + " to " + keyStoreFilename + " ..." );
            System.setProperty( SYSTEM_KEYSTORE_PROPERTY, keyStoreFilename );

        } else {

            System.out.println( "No setting for KeyStore..." );
        }
    }

    /**
     * Read username and password from a properties files and set them on the given Stub.
     *
     * @param stub the stub to configure
     */
    private void configureStub( Stub stub ) {

        if ( username != null ) {
            log.info( "Setting stub's username (" + username + ")" );
            stub.setUsername( username );
        }

        if ( password != null ) {
            log.info( "Setting stub's password" );
            stub.setPassword( password );
        }
    }

    /**
     * Log the given IMEx range in a specific table in the database.
     *
     * @param range the range to log.
     */
    private void logEvent( IMExRange range ) {

        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
        BaseDao dao = daoFactory.getBaseDao();
        Connection connection = ( ( Session ) dao.getSession() ).connection();

        try {


            String sql = "INSERT INTO IA_KEY_ASSIGNER_REQUEST (submission, fromId, toId, partner, service_url) \n" +
                         "VALUES (" + range.getSubmissionId() +
                         "        , " + range.getFrom() +
                         "        , " + range.getTo() +
                         "        , '" + range.getPartner() + "' " +
                         "        , '" + imexKeyAssignerUrl + "' )";

            log.info( "Logging Key Assigner request:" );
            log.info( sql );

            Statement statement = connection.createStatement();
            statement.execute( sql );

            int updateCount = statement.getUpdateCount();
            System.out.println( "[KEY ASSIGNER LOG] " + updateCount + " row(s) updated." );

            if ( updateCount != 1 ) {
                // failure
                System.out.println( "[KEY ASSIGNER LOG] failed (row updated: " + updateCount + ")" );
                System.out.println( "sql = " + sql );
            }

        } catch ( IntactException ie ) {
            System.out.println( "Error when updating table IA_KEY_ASSIGNER_REQUEST." );
            log.error( "Error when updating table IA_KEY_ASSIGNER_REQUEST.", ie );
            ie.printStackTrace();
        } catch ( SQLException e ) {
            System.out.println( "Error when updating table IA_KEY_ASSIGNER_REQUEST." );
            e.printStackTrace();
            log.error( "Error when updating table IA_KEY_ASSIGNER_REQUEST.", e );
        } finally {
            if( connection != null ) {
                try {
                    connection.close();
                } catch ( SQLException e ) {
                    log.error( "Error while closing JDBC connection. Nested exception attached.", e );
                }
            }
        }
    }

    ////////////////////////////
    // Service's methods

    /**
     * Request a range of IMEx IDs from the Key Assigner.
     *
     * @param howMany the count of IMEx ID requested.
     *
     * @return an IMExRange.
     *
     * @throws KeyAssignerServiceException
     */
    public IMExRange getAccessions( int howMany ) throws KeyAssignerServiceException {

        // Setup system
        System.out.println( "Calling Key Assigner..." );
        RegIMExService locator = new RegIMExServiceLocator();

        // Setup service
        RegIMEx_PortType service = null;
        try {
            service = locator.getRegIMEx( new URL( imexKeyAssignerUrl ) );

            // Cheeky setup of the Call via the Stub
            // In order to avoid altering autogenerated code, we get hold of the Stub, responsible for holding
            // the username and password (ie. a Call gets its username/password from its Stub).
            Stub stub = (Stub) service;
            configureStub( stub );

        } catch ( ServiceException e ) {
            throw new KeyAssignerServiceException( "Could not get hold of the service. See nested exception for details.", e );
        } catch ( MalformedURLException e ) {
            throw new KeyAssignerServiceException( "URL of the service is malformed.", e );
        }

        // access UCLA's web service
        SubmissionAxis submissionAxis = null;
        try {
            submissionAxis = service.newSubmission( howMany );
        } catch ( RemoteException e ) {
            throw new KeyAssignerServiceException( "Key Assigner failed to provide IMEx IDs.", e );
        }

        // return structure
        IMExRange range = new IMExRange( submissionAxis.getSubmissionId(),
                                         submissionAxis.getKeyRangeFrom(),
                                         submissionAxis.getKeyRangeTo(),
                                         submissionAxis.getPartner() );

        // Log that call in the database
        logEvent( range );

        return range;
    }
}