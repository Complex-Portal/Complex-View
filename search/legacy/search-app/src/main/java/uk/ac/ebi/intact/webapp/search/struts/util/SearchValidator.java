package uk.ac.ebi.intact.webapp.search.struts.util;

import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.searchengine.SearchClass;

/**
 * Provides basic validations for the search search struts action class.
 *
 * @author Michael Kleen
 * @version SearchClassValidator.java Date: Jan 25, 2005 Time: 08:21:01 AM
 */
public class SearchValidator {

    /**
     * Returns true if this object is valid.
     *
     * @param searchValue  a String
     *
     * @return true of the object is valid
     */
    public static boolean isValid( String searchValue ) {
        if ( searchValue.equalsIgnoreCase( "%" ) || searchValue.equalsIgnoreCase( "" ) ) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Returns true if this object is intact object.
     *
     * @param searchClass  a String
     *
     * @return true of the object is an intact object
     */
    public static boolean isIntactObject( String searchClass ) {
        Class clazz = null;
        try {
            clazz = Class.forName( "uk.ac.ebi.intact.model." + searchClass );
        }
        catch ( ClassNotFoundException e1 ) {
            // not a valid className
            return false;
        }
        if ( IntactObject.class.isAssignableFrom( clazz ) ) {
            return true;
        }
        // not an IntactObject
        return false;

    }

    /**
     * Returns true if the given class is searchable.
     *
     * @param searchClass the class to seach.
     *
     * @return a boolean which is true if the searchClass searchable and false if it's not searchable
     */
    public static boolean isSearchable( SearchClass searchClass ) {
        if (searchClass == null)
        {
            throw new NullPointerException("searchClass");
        }

        Class clazz = searchClass.getMappedClass();

        return isSearchable(clazz);
    }

    /**
     * Returns true if the given class is searchable.
     *
     * @param clazz the class to seach.
     *
     * @return a boolean which is true if the searchClass searchable and false if it's not searchable
     */
    public static boolean isSearchable( Class<? extends IntactObject> clazz ) {

        if ( IntactObject.class.isAssignableFrom( clazz ) ) {

            if ( Protein.class.isAssignableFrom( clazz ) ) {
                return true;
            }
            if ( Experiment.class.isAssignableFrom( clazz ) ) {
                return true;
            }
            if ( Interaction.class.isAssignableFrom( clazz ) ) {
                return true;
            }
            if ( CvObject.class.isAssignableFrom( clazz ) ) {
                return true;
            }
            if ( Interactor.class.isAssignableFrom( clazz ) ) {
                return true;
            }
        }
        // class is an IntactObject but not searchable
        return false;
    }
}