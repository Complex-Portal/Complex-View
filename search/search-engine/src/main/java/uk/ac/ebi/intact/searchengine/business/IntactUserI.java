/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.searchengine.business;

import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.model.IntactObject;

import java.util.Collection;

/**
 * This interface defines methods common to all Intact WEB applications. Typically,
 * each WEB application has its own user and that specific user must extend from this
 * class.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public interface IntactUserI {

    /**
     * This method provides a means of searching intact objects, within the constraints
     * provided by the parameters to the method.
     *
     * @param objectType  the object type to be searched
     * @param searchParam the parameter to search on (eg field)
     * @param searchValue the search value to match with the parameter
     *
     * @return the results of the search (empty if no matches were found).
     *
     * @throws uk.ac.ebi.intact.business.IntactException
     *          thrown if problems are encountered during the
     *          search process.
     */
    public <T extends IntactObject> Collection<T> search( Class<T> objectType,
                                                          String searchParam,
                                                          String searchValue
    ) throws IntactException;

    /**
     * Returns the Intact user.
     *
     * @return the Intact user currently logged in. This methods could return null
     *         if there is no user associated with the current session (e.g., Editor) or
     *         for errors in retrieving user information from the database.
     */
    public String getUserName();

    /**
     * The name of the database connected to.
     *
     * @return the name of the database. Could be null for an error in getting
     *         the information from the database.
     */
    public String getDatabaseName();
}
