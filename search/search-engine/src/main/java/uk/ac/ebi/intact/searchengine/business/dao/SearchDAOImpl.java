package uk.ac.ebi.intact.searchengine.business.dao;

import org.apache.commons.collections.IterableMap;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.HashedMap;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.searchengine.SearchEngineConstants;
import uk.ac.ebi.intact.searchengine.lucene.model.SearchObject;
import uk.ac.ebi.intact.searchengine.util.SearchObjectProvider;
import uk.ac.ebi.intact.searchengine.util.sql.SqlSearchObjectProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This class provides methods to find Intact objects with an AC number and to get all search objects out of the
 * database to create an index on.
 *
 * @author Anja Friedrichsen
 * @version $Id$
 */
public class SearchDAOImpl implements SearchDAO {

    private SearchObjectProvider soProvider;

    /**
     * Logger for this class.
     */
    private static Logger logger = Logger.getLogger( "search" );

    /**
     * Constructs a SearchDAOImpl object.
     */
    public SearchDAOImpl() {
        this.soProvider = new SqlSearchObjectProvider();
    }


    public Map findObjectsbyACs( Map someACs ) throws IntactException {

        // Map to be returned
        final Map<String, Collection<? extends IntactObject>> someResults = new HashedMap();
        final Collection<Experiment> expResults = new ArrayList<Experiment>();
        final Collection<Protein> protResults = new ArrayList<Protein>();
        final Collection<Interaction> interResults = new ArrayList<Interaction>();
        final Collection<CvObject> cvResults = new ArrayList<CvObject>();
        final Collection<BioSource> bioResults = new ArrayList<BioSource>();
        final IterableMap map = ( IterableMap ) someACs;

        // Iterate throuth the Map holding the ACs and search for the corresponding intact object.
        // Add the located object to that collection that fits to the objclass
        for ( MapIterator it = map.mapIterator(); it.hasNext(); ) {

            final String ac = ( String ) it.next();
            String objclass = ( String ) it.getValue();

            objclass = objclass.trim();

            if ( objclass.equalsIgnoreCase( "uk.ac.ebi.intact.model.ProteinImpl" ) ) {

                Protein result = getDaoFactory().getProteinDao().getByAc( ac );
                if ( result == null ) {
                    // this should not happen unless the Lucene index is not in synch with the database.
                    logger.warning( "Looking for AC:" + ac + " Type: Protein.class and no object was found. Index and database not in synch." );
                } else {
                    protResults.add( result );
                }

            } else if ( objclass.equalsIgnoreCase( "uk.ac.ebi.intact.model.InteractionImpl" ) ) {
                Interaction result = getDaoFactory().getInteractionDao().getByAc( ac );
                if ( result == null ) {
                    // this should not happen unless the Lucene index is not in synch with the database.
                    logger.warning( "Looking for AC:" + ac + " Type: Interaction.class and no object was found. Index and database not in synch." );
                } else {
                    interResults.add( result );
                }
            } else if ( objclass.equalsIgnoreCase( "uk.ac.ebi.intact.model.Experiment" ) ) {
                Experiment result = getDaoFactory().getExperimentDao().getByAc( ac );
                if ( result == null ) {
                    // this should not happen unless the Lucene index is not in synch with the database.
                    logger.warning( "Looking for AC:" + ac + " Type: Experiment.class and no object was found. Index and database not in synch." );
                } else {
                    expResults.add( result );
                }
            } else if ( objclass.equalsIgnoreCase( "uk.ac.ebi.intact.model.CvObject" ) ) {
                CvObject result = getDaoFactory().getCvObjectDao( CvObject.class ).getByAc( ac );
                if ( result == null ) {
                    // this should not happen unless the Lucene index is not in synch with the database.
                    logger.warning( "Looking for AC:" + ac + " Type: CvObject.class and no object was found. Index and database not in synch." );
                } else {
                    cvResults.add( result );
                }
            } else if ( objclass.equalsIgnoreCase( "uk.ac.ebi.intact.model.BioSource" ) ) {
                BioSource result = getDaoFactory().getBioSourceDao().getByAc( ac );
                if ( result == null ) {
                    // this should not happen unless the Lucene index is not in synch with the database.
                    logger.warning( "Looking for AC:" + ac + " Type: Protein.class and no object was found. Index and database not in synch." );
                } else {
                    bioResults.add( result );
                }
            } else {
                throw new IntactException( "that class(" + objclass + ") is not part of the IntAct model" );
            }
        }

        // In case the result-collection is not empty, add it to the Map.
        if ( !expResults.isEmpty() ) {
            someResults.put( SearchEngineConstants.EXPERIMENT, expResults );
        }
        if ( !interResults.isEmpty() ) {
            someResults.put( SearchEngineConstants.INTERACTION, interResults );
        }
        if ( !protResults.isEmpty() ) {
            someResults.put( SearchEngineConstants.PROTEIN, protResults );
        }
        if ( !cvResults.isEmpty() ) {
            someResults.put( SearchEngineConstants.CVOBJECT, cvResults );
        }
        if ( !bioResults.isEmpty() ) {
            someResults.put( SearchEngineConstants.BIOSOURCE, bioResults );
        }

        return someResults;
    }

    public Collection getAllSearchObjects() throws IntactException {

        // join the collections of the different search object together to one collection
        // get first all experiments and interactions
        Collection searchObjects = soProvider.getAllExperiments( SearchEngineConstants.EXPERIMENT_QUERY );
        searchObjects.addAll( soProvider.getAllInteractions( SearchEngineConstants.INTERACTION_QUERY ) );
        // ... then add all CVs
        searchObjects.addAll( soProvider.getAllCvObjects( SearchEngineConstants.CV_OBJECT_QUERY ) );

        // ... then add all proteins
        searchObjects.addAll( soProvider.getAllProteins( SearchEngineConstants.PROTEIN_QUERY ) );

        //... and at last add all biosources
        searchObjects.addAll( soProvider.getAllBioSources( SearchEngineConstants.BIOSOURCE_QUERY ) );
        System.out.println( "\n\nThe total number of objects indexed: " + searchObjects.size() );

        return searchObjects;
    }

    public SearchObject getSearchObject( String ac, String objClass ) throws IntactException {
        return soProvider.getSearchObject( ac, objClass );
    }

    private DaoFactory getDaoFactory() {
        return IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
    }
}