package uk.ac.ebi.intact.searchengine.util.sql;

import uk.ac.ebi.intact.annotation.PotentialThreat;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.searchengine.SearchEngineConstants;
import uk.ac.ebi.intact.searchengine.lucene.model.*;
import uk.ac.ebi.intact.searchengine.util.SearchObjectProvider;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * This class provides methods which fetch the information about IntAct object out of the database (via SQL) and store
 * this information in the corresponding search objetcs.
 *
 * @author Anja Friedrichsen
 * @version $Id$
 */
public class SqlSearchObjectProvider implements SearchObjectProvider {

    /**
     * Set the maximum amount of row fetched when executing an SQL query.
     */
    public static final int MAX_FETCH_SIZE = 128;

    /**
     * Constructs a SqlSearchObjectProvider object.
     */
    public SqlSearchObjectProvider() {

    }

    ////////////////////////////////////////////
    // Implementation of SearchObjectProvider

    public SearchObject getSearchObject( String ac, String objClass ) throws IntactException {
        if ( objClass.equalsIgnoreCase( "uk.ac.ebi.intact.model.ProteinImpl" ) ) {
            String sqlQuery = SearchEngineConstants.PROTEIN_QUERY + " AND I.ac = '" + ac + "'";

            return getAllProteins( sqlQuery ).iterator().next();
        } else if ( objClass.equalsIgnoreCase( "uk.ac.ebi.intact.model.InteractionImpl" ) ) {
            String sqlQuery = SearchEngineConstants.INTERACTION_QUERY + " AND I.ac = '" + ac + "'";
            return getAllInteractions( sqlQuery ).iterator().next();
        } else if ( objClass.equalsIgnoreCase( "uk.ac.ebi.intact.model.Experiment" ) ) {
            String sqlQuery = SearchEngineConstants.EXPERIMENT_QUERY + " WHERE E.ac = '" + ac + "'";
            return getAllExperiments( sqlQuery ).iterator().next();
        } else if ( objClass.equalsIgnoreCase( "uk.ac.ebi.intact.model.CvObject" ) ) {
            String sqlQuery = SearchEngineConstants.CV_OBJECT_QUERY + " WHERE CV.ac = '" + ac + "'";
            return getAllCvObjects( sqlQuery ).iterator().next();
        } else if ( objClass.equalsIgnoreCase( "uk.ac.ebi.intact.model.BioSource" ) ) {
            String sqlQuery = SearchEngineConstants.BIOSOURCE_QUERY + " WHERE B.ac = '" + ac + "'";
            return getAllBioSources( sqlQuery ).iterator().next();
        } else {
            throw new IntactException( "Problems with the search object class" );
        }
    }

    @PotentialThreat( description = "Contains direct SQL, including usage of Xref and Alias related tables" )
    public Collection<ExperimentSearchObject> getAllExperiments( String sqlQuery ) throws IntactException {
        // collection to be returned
        Collection<ExperimentSearchObject> expSet = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = getConnection().createStatement();
            stmt.setFetchSize( MAX_FETCH_SIZE );
            resultSet = stmt.executeQuery( sqlQuery );

            String ac = null;
            String shortlabel = null;
            String fullname = null;
            String objclass = null;
            Map xrefs = null;
            Map annotations = null;
            Map alias = null;

            CvSearchObject cvIdent = null;
            CvSearchObject cvInter = null;

            // checks if the resultset is empty,
            // if so return an EMPTY_LIST
            if ( resultSet.next() ) {
                expSet = new ArrayList<ExperimentSearchObject>();
            } else {
                return Collections.EMPTY_LIST;
            }

            // counts the number of experiments
            int count = 0;
            // create one ExperimentSearchObject for every row/experiment
            System.out.println( "\nIndexing Experiments..." );
            do {
                count++;

                ac = resultSet.getString( 1 );
                shortlabel = resultSet.getString( 2 );
                fullname = resultSet.getString( 3 );
                objclass = resultSet.getString( 4 );

                // SQL query to get the xrefs for the specific experiment
                final String xref_query = "SELECT CV.shortlabel, X.primaryid FROM ia_experiment_xref X, ia_controlledvocab CV " +
                                          "WHERE parent_ac = '" + ac + "' and CV.ac = X.database_ac";
                // get out the xrefs
                xrefs = this.getResultMapBySQL( xref_query );

                // SQL query to get the annotations for the specific experiment
                String annot_query = "SELECT CV.shortlabel, A.description FROM ia_exp2annot EA, ia_annotation A, ia_controlledvocab CV " +
                                     "WHERE EA.experiment_ac = '" + ac + "' AND EA.annotation_ac = A.ac AND A.topic_ac = CV.ac";
                // retrieve the corresponding annotations
                annotations = this.getResultMapBySQL( annot_query );

                // SQL query to get the alias for the specific object
                String alias_query = "SELECT cv.shortlabel, a.name FROM ia_experiment_alias A, ia_controlledvocab CV  " +
                                     "WHERE CV.ac = A.aliastype_ac AND A.parent_ac = '" + ac + "'";
                // get the alias out
                alias = this.getResultMapBySQL( alias_query );

                // SQL query to get the corresponding CvIdentificationSearchObject
                String cvIdent_query = "SELECT CV.ac, CV.shortlabel, CV.fullname FROM ia_experiment E, ia_controlledvocab CV" +
                                       " WHERE identmethod_ac = CV.ac AND E.ac = '" + ac + "'";
                // get the CvIdentification out
                cvIdent = this.getCvObjectBySqlQuery( cvIdent_query );

                // SQL query to get the CvInteraction
                String cvInter_query = "SELECT CV.ac, CV.shortlabel, CV.fullname FROM ia_experiment E, ia_controlledvocab CV " +
                                       "WHERE detectmethod_ac = CV.ac AND E.ac = '" + ac + "'";
                // get the CvInteraction out
                cvInter = this.getCvObjectBySqlQuery( cvInter_query );

                ExperimentSearchObject so = new ExperimentSearchObject( ac, shortlabel, fullname, objclass,
                                                                        xrefs, annotations, cvIdent, cvInter, alias );
                // add the searchObject to the collection with all search objects
                expSet.add( so );

                System.out.print( "." );
                System.out.flush();

                if ( ( count % 50 ) == 0 ) {
                    System.out.println( " " + count );
                }

            } while ( resultSet.next() );

            System.out.println( "\n" + expSet.size() + " experiments processed." );

        } catch ( SQLException e ) {
            e.printStackTrace();
            throw new IntactException( "Sql Problems", e );
        } finally {
            // close the statement and the resultSet
            try {
                if ( stmt != null ) {
                    stmt.close();
                }
                if ( resultSet != null ) {
                    resultSet.close();
                }
            } catch ( SQLException e ) {
                e.printStackTrace();
            }
        }
        return expSet;
    }

    @PotentialThreat( description = "Contains direct SQL, including usage of Xref and Alias related tables" )
    public Collection<InteractionSearchObject> getAllInteractions( String sqlQuery ) throws IntactException {
        // collection to be returned
        Collection<InteractionSearchObject> interactionSet = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = getConnection().createStatement();
            stmt.setFetchSize( MAX_FETCH_SIZE );

            resultSet = stmt.executeQuery( sqlQuery );

            String ac = null;
            String shortlabel = null;
            String fullname = null;
            String objclass = null;
            Map xrefs = null;
            Map annotations = null;
            Map alias = null;
            CvSearchObject cvInterType = null;

            // checks if the resultset is empty,
            // if so return an EMPTY_LIST
            if ( resultSet.next() ) {
                interactionSet = new ArrayList<InteractionSearchObject>();
            } else {
                return Collections.EMPTY_LIST;
            }

            int count = 0;
            System.out.println( "\nIndexing Interactions..." );
            do {
                count++;

                ac = resultSet.getString( 1 );
                shortlabel = resultSet.getString( 2 );
                fullname = resultSet.getString( 3 );
                objclass = resultSet.getString( 4 );
                // get the xrefs to the specific interaction
                final String xref_query = "SELECT CV.shortlabel, X.primaryid FROM ia_interactor_xref X, ia_controlledvocab CV " +
                                          "WHERE parent_ac = '" + ac + "' and CV.ac = X.database_ac";
                // get the xrefs out
                xrefs = this.getResultMapBySQL( xref_query );

                // SQL query to get all annotations for a specific interaction
                String annot_query = "SELECT CV.shortlabel, A.description FROM ia_int2annot EA, ia_annotation A, ia_interactor I, ia_controlledvocab CV " +
                                     "WHERE EA.interactor_ac = '" + ac + "' AND CV.ac = A.topic_ac AND " +
                                     "EA.annotation_ac = A.ac AND I.ac = EA.interactor_ac AND I.objclass = 'uk.ac.ebi.intact.model.InteractionImpl'";
                // retrieve the annotations
                annotations = this.getResultMapBySQL( annot_query );

                // SQL query to get the alias for the specific object
                String alias_query = "SELECT cv.shortlabel, a.name FROM ia_interactor_alias A, ia_controlledvocab CV  " +
                                     "WHERE CV.ac = A.aliastype_ac AND A.parent_ac = '" + ac + "'";
                // get the alias out
                alias = this.getResultMapBySQL( alias_query );

                // SQL query to get the corresponding CvIdentificationSearchObject
                String cvInterType_query = "SELECT CV.ac as ac, CV.shortlabel as shortlabel, CV.fullname as fullname " +
                                           "FROM ia_controlledvocab CV, ia_interactor I " +
                                           "WHERE I.objclass = 'uk.ac.ebi.intact.model.InteractionImpl' AND " +
                                           "I.interactiontype_ac = CV.ac AND I.ac = '" + ac + "'";
                // get the CvIdentification out
                cvInterType = this.getCvObjectBySqlQuery( cvInterType_query );

                // create a new InteractionSearchObject with the retrieved attributes
                InteractionSearchObject so = new InteractionSearchObject( ac, shortlabel, fullname, objclass, xrefs,
                                                                          annotations, cvInterType, alias );
                // add the searchobject to the sollection with all searchObjects
                interactionSet.add( so );

                if ( ( count % 20 ) == 0 ) {
                    System.out.print( "." );
                    System.out.flush();

                    if ( ( count % 1000 ) == 0 ) {
                        // 50 dots per line (1000 / 20)
                        System.out.println( " " + count );
                    }
                }
            } while ( resultSet.next() );

            System.out.println( "\n" + interactionSet.size() + " interactions processed." );


        } catch ( SQLException e ) {
            e.printStackTrace();
            throw new IntactException( "Sql Problems", e );
        } finally {
            // close the statement and the resultSet
            try {
                if ( stmt != null ) {
                    stmt.close();
                }
                if ( resultSet != null ) {
                    resultSet.close();
                }
            } catch ( SQLException e ) {
                e.printStackTrace();
            }
        }
        return interactionSet;
    }

    @PotentialThreat( description = "Contains direct SQL, including usage of Xref and Alias related tables" )
    public Collection<ProteinSearchObject> getAllProteins( String sqlQuery ) throws IntactException {
        // collection with the set of protein searchObjects
        Collection<ProteinSearchObject> proteinSet = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = getConnection().createStatement();
            stmt.setFetchSize( MAX_FETCH_SIZE );

            resultSet = stmt.executeQuery( sqlQuery );

            String ac = null;
            String shortlabel = null;
            String fullname = null;
            String objclass = null;
            Map xrefs = null;
            Map annotations = null;
            Map alias = null;

            // checks if the resultset is empty,
            // if so return an EMPTY_LIST
            if ( resultSet.next() ) {
                proteinSet = new ArrayList<ProteinSearchObject>();
            } else {
                return Collections.EMPTY_LIST;
            }

            int count = 0;
            // loop through the resultSet row by row and create a ProteinSearchObject for every row/protein
            System.out.println( "\nIndexing Proteins..." );
            do {
                count++;

                ac = resultSet.getString( 1 );
                shortlabel = resultSet.getString( 2 );
                fullname = resultSet.getString( 3 );
                objclass = resultSet.getString( 4 );
                // retrieve all xrefs for that specific protein
                final String xref_query = "SELECT CV.shortlabel, X.primaryid FROM ia_interactor_xref X, ia_controlledvocab CV " +
                                          "WHERE parent_ac = '" + ac + "' and CV.ac = X.database_ac";
                // get out
                xrefs = this.getResultMapBySQL( xref_query );

                // SQL query to select all annotations for that specific protein
                String annot_query = "SELECT CV.shortlabel, A.description FROM ia_int2annot EA, ia_annotation A, ia_interactor I, ia_controlledvocab CV " +
                                     "WHERE EA.interactor_ac = '" + ac + "' AND CV.ac = A.topic_ac AND " +
                                     "EA.annotation_ac = A.ac AND I.ac = EA.interactor_ac AND I.objclass = 'uk.ac.ebi.intact.model.ProteinImpl'";
                // retrieve the annotations
                annotations = this.getResultMapBySQL( annot_query );

                // SQL query to get the alias for the specific object
                String alias_query = "SELECT cv.shortlabel, a.name FROM ia_interactor_alias A, ia_controlledvocab CV  " +
                                     "WHERE CV.ac = A.aliastype_ac AND A.parent_ac = '" + ac + "'";
                // get the alias out
                alias = this.getResultMapBySQL( alias_query );

                // create a new ProteinSearchObject with the attributes gotten out
                ProteinSearchObject so = new ProteinSearchObject( ac, shortlabel, fullname, objclass, xrefs, annotations, alias );
                proteinSet.add( so );

                if ( ( count % 20 ) == 0 ) {
                    System.out.print( "." );
                    System.out.flush();

                    if ( ( count % 1000 ) == 0 ) {
                        // 50 dots per line
                        System.out.println( " " + count );
                    }
                }
            } while ( resultSet.next() );

            System.out.println( "\n" + proteinSet.size() + " proteins processed." );

        } catch ( SQLException e ) {
            e.printStackTrace();
            throw new IntactException( "Sql Problems", e );
        } finally {
            // close the statement and the resultSet
            try {
                if ( stmt != null ) {
                    stmt.close();
                }
                if ( resultSet != null ) {
                    resultSet.close();
                }
            } catch ( SQLException e ) {
                e.printStackTrace();
            }
        }
        return proteinSet;
    }

    @PotentialThreat( description = "Contains direct SQL, including usage of Xref and Alias related tables" )
    public Collection<CvSearchObject> getAllCvObjects( String sqlQuery ) throws IntactException {
        // collection with CvSearchObjects to be returned
        Collection<CvSearchObject> cvSet = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = getConnection().createStatement();
            stmt.setFetchSize( MAX_FETCH_SIZE );

            resultSet = stmt.executeQuery( sqlQuery );

            String ac = null;
            String shortlabel = null;
            String fullname = null;
            String objclass = null;
            Map xrefs = null;
            Map annotations = null;
            Map alias = null;

            // checks if the resultset is empty,
            // if so return an EMPTY_LIST
            if ( resultSet.next() ) {
                cvSet = new ArrayList<CvSearchObject>();
            } else {
                return Collections.EMPTY_LIST;
            }

            int count = 0;

            System.out.println( "\nIndexing CV terms..." );
            do {
                count++;

                ac = resultSet.getString( 1 );
                shortlabel = resultSet.getString( 2 );
                fullname = resultSet.getString( 3 );
                objclass = resultSet.getString( 4 );
                // get out the xrefs of the specific CvObject
                final String xref_query = "SELECT CV.shortlabel, X.primaryid FROM ia_controlledVocab_xref X, ia_controlledvocab CV " +
                                          "WHERE parent_ac = '" + ac + "' and CV.ac = X.database_ac";
                xrefs = this.getResultMapBySQL( xref_query );

                // SQL query to get out the annotations for a specific CvObject
                String annot_query = "SELECT CV.shortlabel, A.description FROM ia_cvobject2annot OA, ia_annotation A, ia_controlledvocab CV " +
                                     "WHERE OA.cvobject_ac = '" + ac + "' AND OA.annotation_ac = A.ac AND CV.ac = A.topic_ac";
                // get out the annotation
                annotations = this.getResultMapBySQL( annot_query );

                // SQL query to get the alias for the specific object
                String alias_query = "SELECT cv.shortlabel, a.name FROM ia_controlledvocab_alias A, ia_controlledvocab CV  " +
                                     "WHERE CV.ac = A.aliastype_ac AND A.parent_ac = '" + ac + "'";
                // get the alias out
                alias = this.getResultMapBySQL( alias_query );

                // create a CvSearchObject with the retrieved attributes
                CvSearchObject so = new CvSearchObject( ac, shortlabel, fullname, objclass, xrefs, annotations, alias );
                cvSet.add( so );

                System.out.print( "." );
                System.out.flush();

                if ( ( count % 50 ) == 0 ) {
                    System.out.println( " " + count );
                }
            } while ( resultSet.next() );

            System.out.println( "\n" + cvSet.size() + " CV terms processed." );


        } catch ( SQLException e ) {
            e.printStackTrace();
            throw new IntactException( "Sql Problems", e );
        } finally {
            // close the statement and the resultSet
            try {
                if ( stmt != null ) {
                    stmt.close();
                }
                if ( resultSet != null ) {
                    resultSet.close();
                }
            } catch ( SQLException e ) {
                e.printStackTrace();
            }
        }
        return cvSet;
    }

    @PotentialThreat( description = "Contains direct SQL code, including Alias and Xref references" )
    public Collection<BioSourceSearchObject> getAllBioSources( String sqlQuery ) throws IntactException {
        // collection with BioSourceSearchObjects to be returned
        Collection<BioSourceSearchObject> bioSoSet = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = getConnection().createStatement();
            stmt.setFetchSize( MAX_FETCH_SIZE );

            resultSet = stmt.executeQuery( sqlQuery );

            String ac = null;
            String shortlabel = null;
            String fullname = null;
            String objclass = null;
            Map xrefs = null;
            Map annotations = null;
            Map alias = null;

            // checks if the resultset is empty,
            // if so return an EMPTY_LIST
            if ( resultSet.next() ) {
                bioSoSet = new ArrayList<BioSourceSearchObject>();
            } else {
                return Collections.EMPTY_LIST;
            }
            int count = 0;

            System.out.println( "\nIndexing BioSources..." );
            do {
                count++;

                ac = resultSet.getString( 1 );
                shortlabel = resultSet.getString( 2 );
                fullname = resultSet.getString( 3 );
                objclass = resultSet.getString( 4 );
                // get out the xrefs of the specific BioSourceObject
                final String xrefQuery = "SELECT CV.shortlabel, X.primaryid FROM ia_biosource_xref X, ia_controlledvocab CV " +
                                         "WHERE parent_ac = '" + ac + "' and CV.ac = X.database_ac";
                xrefs = this.getResultMapBySQL( xrefQuery );
                // SQL query to get out the annotations for a specific BioSourceObject
                String annotQuery = "SELECT CV.shortlabel, A.description FROM ia_biosource2annot BA, ia_annotation A, ia_controlledvocab CV " +
                                    "WHERE BA.biosource_ac = '" + ac + "' AND BA.annotation_ac = A.ac AND CV.ac = A.topic_ac";
                // get out the annotation
                annotations = this.getResultMapBySQL( annotQuery );

                // SQL query to get the alias for the specific object
                String aliasQuery = "SELECT cv.shortlabel, a.name FROM ia_biosource_alias A, ia_controlledvocab CV  " +
                                    "WHERE CV.ac = A.aliastype_ac AND A.parent_ac = '" + ac + "'";
                // get the alias out
                alias = this.getResultMapBySQL( aliasQuery );

                // create a BioSourceSearchObject with the retrieved attributes
                BioSourceSearchObject so = new BioSourceSearchObject( ac, shortlabel, fullname, objclass, xrefs, annotations, alias );
                bioSoSet.add( so );

                System.out.print( "." );
                System.out.flush();

                if ( ( count % 50 ) == 0 ) {
                    System.out.println( " " + count );
                }
            } while ( resultSet.next() );

            System.out.println( "\n" + bioSoSet.size() + " BioSources processed." );

        } catch ( SQLException e ) {
            e.printStackTrace();
            throw new IntactException( "Sql Problems", e );
        } finally {
            // close the statement and the resultSet
            try {
                if ( stmt != null ) {
                    stmt.close();
                }
                if ( resultSet != null ) {
                    resultSet.close();
                }
            } catch ( SQLException e ) {
                e.printStackTrace();
            }
        }
        return bioSoSet;
    }

    /////////////////////////////////////////
    // private methods

    /**
     * This method returns the result of the given SQL query. The query should select two colums, the first is planned
     * to be the name of the lucene field  and the second is the content of that lucene field. It should be used for
     * example for the CvDatabase-Xref Query, if you search for Xrefs which are in a specific database. In that case the
     * database shortlabel is going to be the fieldname(key) and the retrieved Xrefs are stored in a collection and are
     * used for the content of that key.
     *
     * @param sqlQuery the query to get the result for, should have 2 select colums
     *
     * @return a Map with the fieldname for the lucene index as key and a Collection with the found entries as value
     */
    private Map<String, Collection<String>> getResultMapBySQL( String sqlQuery ) throws IntactException {
        // map to be returned
        Map<String, Collection<String>> results = null;
        // collection to store the values for one key
        Collection<String> values;
        // key of the map
        String key = null;
        // value to add to the values collection
        String value = null;

        Statement stmt = null;
        ResultSet resultSet = null;

        try {
            stmt = getConnection().createStatement();
            stmt.setFetchSize( MAX_FETCH_SIZE );

            resultSet = stmt.executeQuery( sqlQuery );

            // checks if the resultset is empty,
            // if so return an EMPTY_LIST
            if ( resultSet.next() ) {
                results = new HashMap<String, Collection<String>>();
            } else {
                return Collections.EMPTY_MAP;
            }
            // loop through the resultset and fill the result map
            do {
                key = resultSet.getString( 1 );
                value = resultSet.getString( 2 );
                // if the key is already in the map, take out the corresponding collection
                if ( results.containsKey( key ) ) {
                    values = results.get( key );
                    // if the key not jet in the result Map, create a new collection
                } else {
                    values = new ArrayList<String>();
                }
                // add the new value to the collection if it not null
                if ( value != null ) {
                    values.add( value );
                    // if the new value is null, add an empty string
                } else {
                    values.add( "" );
                }
                // and put the collection with the key into the result map
                results.put( key, values );
            } while ( resultSet.next() );
        } catch ( SQLException e ) {
            e.printStackTrace();
            throw new IntactException( "Sql Problems", e );
        } finally {
            try {
                if ( resultSet != null ) {
                    resultSet.close();
                }
                if ( stmt != null ) {
                    stmt.close();
                }
            } catch ( SQLException e ) {
                e.printStackTrace();
            }
        }
        return results;
    }

    /**
     * This method creates CvSearchObjects out of the information it got from the database querying the SQL query, that
     * is given as an argument.
     *
     * @param sqlQuery SQL statement to query the database
     *
     * @return CvSearchObject holding the information fetched out of the database
     *
     * @throws IntactException
     */
    private CvSearchObject getCvObjectBySqlQuery( String sqlQuery ) throws IntactException {
        //  CvSearchObject to be returned
        CvSearchObject cvso = null;

        Statement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = getConnection().createStatement();
            stmt.setFetchSize( MAX_FETCH_SIZE );

            resultSet = stmt.executeQuery( sqlQuery );

            String ac = null;
            String shortlabel = null;
            String fullname = null;
            String objclass = null;

            while ( resultSet.next() ) {
                ac = resultSet.getString( 1 );
                shortlabel = resultSet.getString( 2 );
                fullname = resultSet.getString( 3 );

                // create a CvSearchObject with the retrieved attributes
                cvso = new CvSearchObject( ac, shortlabel, fullname, objclass );
            }
        } catch ( SQLException e ) {
            e.printStackTrace();
            throw new IntactException( "Sql Problems", e );
        } finally {

            try {
                if ( resultSet != null ) {
                    resultSet.close();
                }
                if ( stmt != null ) {
                    stmt.close();
                }
            } catch ( SQLException e ) {
                e.printStackTrace();
            }
        }
        return cvso;
    }

    private Connection getConnection() {
        return IntactContext.getCurrentInstance().getDataContext().getDaoFactory().connection();
    }
}