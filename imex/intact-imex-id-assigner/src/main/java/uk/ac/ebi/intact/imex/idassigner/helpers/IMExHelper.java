/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.imex.idassigner.helpers;

import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvXrefQualifier;
import uk.ac.ebi.intact.util.PropertyLoader;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.persistence.dao.CvObjectDao;

import java.util.Properties;
import java.util.List;

/**
 * Helper method on the IMEx.properties file.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>16-May-2006</pre>
 */
public class IMExHelper {

    public static final String IMEX_PROPERTIES_FILE = "/imex.properties";

    ////////////////////////////////
    // Properties name

    private static final String KEYSTORE_FILENAME_KEY = "keystore.filename";

    private static final String KEY_ASSIGNER_URL_KEY = "key.assigner.url";
    private static final String USERNAME_KEY = "key.assigner.username";
    private static final String PASSWORD_KEY = "key.assigner.password";

    private static final String DATABASE_NAME_KEY = "database.name";
    private static final String DATABASE_PSI_ID_KEY = "database.psimi.id";

    /**
     * Load the IMEx.properties set by the user and made available from the classpath.
     *
     * @return properties or null if not found.
     */
    private static Properties getIMExConfig() {

        return PropertyLoader.load( IMEX_PROPERTIES_FILE );
    }

    /**
     * Looks in the IMEx.properties file, get the MI id of the current database and fetch it from the IntAct repository
     * as a CvDatabase.
     *
     * @return a CvDatabase or throws an exception.
     *
     * @throws IntactException if the configuration is wrong or if we cannot find a single instance of a CvDatabase.
     */
    public static CvDatabase whoAmI( ) throws IntactException {

        CvDatabase psi = CvHelper.getPsi( );
        CvXrefQualifier identity = CvHelper.getIdentity( );

        CvDatabase database = null;

        // read imex.properties
        Properties properties = getIMExConfig();

        if ( properties == null ) {
            throw new IntactException( "Could not read configuration from : " + IMEX_PROPERTIES_FILE );
        }

        // get psi id and shortlabel
        String psiId = properties.getProperty( DATABASE_PSI_ID_KEY );
        if ( psiId == null ) {
            throw new IntactException( "Could not find a property '" + DATABASE_PSI_ID_KEY + "' in : " + IMEX_PROPERTIES_FILE );
        }

        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
        CvObjectDao<CvDatabase> cvDao = daoFactory.getCvObjectDao(CvDatabase.class);
        List<CvDatabase> databases = cvDao.getByXrefLike( psi, identity, psiId );

        if ( databases.isEmpty() ) {
            throw new IntactException( "Could not find CvDatabase(" + psiId + ") in your intact controlled vocabularies." +
                                       "Please check the content of " + IMEX_PROPERTIES_FILE + " property='" + DATABASE_PSI_ID_KEY + "'" );
        } else if ( databases.size() > 1 ) {
            throw new IntactException( "Found " + databases.size() + " instances of CvDatabase(" + psiId + ") in your intact controlled vocabularies." +
                                       "Please check the content of " + IMEX_PROPERTIES_FILE + " property='" + DATABASE_PSI_ID_KEY + "'" );
        }

        // get the single instance.
        database = databases.iterator().next();

        return database;
    }

    ///////////////////////////////
    // Access to the properties

    /**
     * Answers the question: "Is the IMEx properties file available ?".
     *
     * @return true if the properties file could be read from disk.
     */
    public static boolean isIMExPropertiesFileAvailable() {
        return ( null != getIMExConfig() );
    }

    public static String getKeyAssignerUrl() {
        Properties properties = getIMExConfig();
        if ( properties == null ) {
            return null;
        }

        return properties.getProperty( KEY_ASSIGNER_URL_KEY );
    }

    public static String getKeyAssignerUsername() {
        Properties properties = getIMExConfig();
        if ( properties == null ) {
            return null;
        }

        return properties.getProperty( USERNAME_KEY );
    }

    public static String getKeyAssignerPassword() {
        Properties properties = getIMExConfig();
        if ( properties == null ) {
            return null;
        }

        return properties.getProperty( PASSWORD_KEY );
    }

    public static String getKeyStoreFilename() {
        Properties properties = getIMExConfig();
        if ( properties == null ) {
            return null;
        }

        return properties.getProperty( KEYSTORE_FILENAME_KEY );
    }
}