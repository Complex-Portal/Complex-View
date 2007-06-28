package uk.ac.ebi.intact.searchengine.business.dao;

import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.searchengine.lucene.model.SearchObject;

import java.util.Collection;
import java.util.Map;

/**
 * Defines the requirements for a Search.
 *
 * @author Anja Friedrichsen
 * @version $Id$
 */
public interface SearchDAO {

    /**
     * This method is used to get the Intact object out of the database, corresponding to the Map of Acs. The key of the
     * Map (someAcs) is the AC number and the value to that key is the corresponding objclass The Map which will be
     * returned has the class names as keys and the value is a collection of the found IntAct objects.
     *
     * @param someACs Map of Acs to find the corresponding IntAct objects
     *
     * @return a Map with the objectclasses as keys and the value is a collection containing the located IntAct objects
     *
     * @throws IntactException
     */
    public Map findObjectsbyACs( final Map someACs ) throws IntactException;

    /**
     * This method collects all intact objects, which should be searchable, out of the database and merges them into one
     * collection. This collection is later used to create the index
     *
     * @return a collection with all search objects
     *
     * @throws IntactException
     */
    public Collection getAllSearchObjects() throws IntactException;

    /**
     * This method searches for one Object specified with the AC and the objectClass. It is used to update the index
     *
     * @param ac       AC number of the object to search for
     * @param objClass class of the object to search for
     *
     * @return the object retrieved from the search
     *
     * @throws IntactException
     */
    public SearchObject getSearchObject( final String ac, String objClass ) throws IntactException;
}
