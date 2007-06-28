package uk.ac.ebi.intact.searchengine.business;

import org.apache.commons.collections.IterableMap;
import org.apache.commons.collections.map.HashedMap;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.searchengine.SearchEngineConstants;
import uk.ac.ebi.intact.searchengine.business.dao.SearchDAO;
import uk.ac.ebi.intact.searchengine.lucene.Indexer;
import uk.ac.ebi.intact.searchengine.lucene.SearchObjectIndexer;
import uk.ac.ebi.intact.searchengine.lucene.model.SearchObject;
import uk.ac.ebi.intact.searchengine.parser.IQLParser;
import uk.ac.ebi.intact.util.Chrono;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * This class provides methods to search the database and to create an index of the database.
 *
 * @author Anja Friedrichsen
 * @version $Id$
 */
public class SearchEngineImpl implements SearchEngine {

    // the file where to create the index
    private final File index;
    private final SearchDAO dao;
    private final Analyzer analyzer;
    private final IQLParser iqlParser;
    private final int MAX_VALUE = 200;
    private final static Object mutex = new Object();

    private static Logger logger = Logger.getLogger( "search" );
    private int myPossibleSize;

    /**
     * Constructs a SearchEngineImpl object.
     *
     * @param analyzer  an Analyzer object
     * @param dir       a File object
     * @param dao       a SearchDAO object
     * @param iqlParser an IQLParser object
     */
    public SearchEngineImpl( final Analyzer analyzer,
                             final File dir,
                             final SearchDAO dao,
                             final IQLParser iqlParser
    ) {

        this.analyzer = analyzer;
        this.dao = dao;
        this.index = dir;
        this.iqlParser = iqlParser;
    }

    /**
     * Illegal constructor, prevent user from instanciating the no-arg constructor from Object.
     */
    private SearchEngineImpl() {
        throw new IllegalStateException();
    }

    /**
     * This method gets a result map for a specific iql query.
     *
     * @param iqlQuery        query string in the IQL syntax
     * @param numberOfResults maximum number of results
     *
     * @return a  Map containing the results, the keys in the Map are the ac number of the search objects and the values
     *         are the objclass
     *
     * @throws IntactException
     */
    public Map findObjectByIQL( final String iqlQuery, final int numberOfResults ) throws IntactException {

        logger.info( "IQL query: " + iqlQuery );

        String luceneQuery = null;
        try {
            // parse the IQL statement into an lucene statement
            luceneQuery = iqlParser.getLuceneQuery( iqlQuery );
            logger.info( "got luceneQuery: " + luceneQuery );
        } catch ( IntactException e ) {
            logger.error( "Problems with the IQL parsing \n" + e );
            throw new IntactException( "Problems with the IQL parsing", e );
        }

        logger.info( "lucene query: " + luceneQuery );
        logger.info( "send to findObjectByLucene..." );
        return this.findObjectByLucene( luceneQuery, numberOfResults );
    }

    /**
     * This method gets the result for a specific lucene query.
     *
     * @param luceneQuery     query string in the lucene syntax
     * @param numberOfResults maximum numbers of results
     *
     * @return a Map containing the results, the keys in the Map are the ac number of the search objects and the values
     *         are the objclass
     *
     * @throws IntactException
     */
    public Map findObjectByLucene( final String luceneQuery, final int numberOfResults ) throws IntactException {

        final Query query;

        try {
            // set the default field to Shortlabel
            QueryParser parser = new QueryParser( "SHORTLABEL", analyzer );
            query = parser.parse( luceneQuery );

            logger.info( "final query: " + query.toString() );

        } catch ( ParseException e ) {
            logger.error( "Unable to make any sense of the query", e );
            throw new IntactException( "Unable to make any sense of the query", e );
        }

        // Map to be returned, containing the results
        final IterableMap searchKeys = new HashedMap();
        try {
            final IndexReader reader = IndexReader.open( index );
            logger.info( "opened reader" );
            IndexSearcher searcher = new IndexSearcher( reader );
            logger.info( "opened searcher" );
            Hits hits = null;
            try {
                // a problem with the BooleanQuery is that the hits are limited
                // that causes an error for example if you search for EBI-*
                // set the maximum number of hits higher (default: 1024)
                BooleanQuery.setMaxClauseCount( 9999 );
                hits = searcher.search( query );
            } catch ( BooleanQuery.TooManyClauses e ) {
                logger.error( "Lucene is limited: BooleanQuery TooManyClauses!", e );
                throw new BooleanQuery.TooManyClauses();
            }
            logger.info( "found " + hits.length() + " hits" );

            //set the number of the hits
            myPossibleSize = hits.length();

            // iterate through the hits and put the found ac numbers together with the objectclass
            // into the map, that will be returned
            for ( int i = 0; i != hits.length(); ++i ) {
                final Document doc = hits.doc( i );
//                logger.info("found doc: " + doc.toString());
                final String ac = doc.getField( SearchEngineConstants.AC ).stringValue();
                logger.info( "found AC: " + ac );
                final String objclass = doc.getField( SearchEngineConstants.OBJCLASS ).stringValue();
                searchKeys.put( ac, objclass );
            }
            searcher.close();
            reader.close();
        } catch ( IOException e ) {
            logger.error( "problems with...", e );
            throw new IntactException( "Error while readin data from index", e );
        }
        return searchKeys;
    }

    /**
     * search by an IQL Query with the standard maximum number of results.
     *
     * @param iqlQuery query string in the IQL syntax
     *
     * @return a  Map containing the results, the keys in the Map are the ac number of the search objects and the values
     *         are the objclass
     *
     * @throws IntactException
     */
    public Map findObjectByIQL( final String iqlQuery ) throws IntactException {
        return this.findObjectByIQL( iqlQuery, MAX_VALUE );
    }


    /**
     * search by a Lucene query with the standard maximum number of results.
     *
     * @param luceneQuery query string in the lucene syntax
     *
     * @return a  Map containing the results, the keys in the Map are the ac number of the search objects and the values
     *         are the objclass
     *
     * @throws IntactException
     */
    public Map findObjectByLucene( String luceneQuery ) throws IntactException {
        return this.findObjectByLucene( luceneQuery, MAX_VALUE );
    }

    /**
     * This method gets a Map containing the result with the ac number as key and the objectclass as value and takes the
     * corresponding intact objects out of the database.
     *
     * @param searchKeys a Map with ACs as keys and the corresponding ojbclasses as value
     *
     * @return a Map with the objectclasses as keys and the value is a collection containing the located IntAct objects
     *
     * @throws IntactException
     */
    public Map getResult( Map searchKeys ) throws IntactException {
        final Map results = dao.findObjectsbyACs( searchKeys );
        return results;
    }


    /**
     * check if the limit of the results is exceeded.
     *
     * @return 'true' if the limit is exceeded and 'false' else
     */
    public boolean isTooLarge() {
        return myPossibleSize > MAX_VALUE;
    }

    /**
     * create the lucene index. Gets first all search objects (experiments, interactions, proteins, cvs and biosources)
     * out of the database and creates then the index of these objects. This is a very trivial (first) version it writes
     * everything on scratch without using the RAM. It is recommanded to use instead the method 'createLuceneIndex'
     *
     * @param aSearchObject a search object
     */
    public void createIndex( final SearchObject aSearchObject ) {
        SearchObjectIndexer searchObjectIndexer = new SearchObjectIndexer( analyzer, index.getPath() );
        logger.info( "Write the index into: " + index.getPath() );
        Collection allSearchObjects = null;
        Chrono time = new Chrono();
        try {
            time.start();
            allSearchObjects = dao.getAllSearchObjects();
            time.stop();
            System.out.println( "time to get " + allSearchObjects.size() + " search objects: " + time.toString() );
        } catch ( IntactException e ) {
            logger.error( e );
            e.printStackTrace();
        }

        for ( Iterator iterator = allSearchObjects.iterator(); iterator.hasNext(); ) {
            SearchObject searchObject = ( SearchObject ) iterator.next();
            searchObjectIndexer.createIndex( searchObject );
        }
    }

    /**
     * Creats a Lucene index out of the intact objects. It writes the index first into RAM and writes it
     * at the end into the directory
     *
     * @throws IOException     ...
     * @throws IntactException ...
     */
    public void createLuceneIndex( BufferedWriter logOutWriter ) throws IOException, IntactException {

        Indexer indexer = new Indexer( dao, new SearchObjectIndexer() );
        indexer.createIndex( index, logOutWriter );
    }

    /**
     * todo   prove if it really works..
     *
     * @param aSearchObject
     *
     * @throws IntactException
     */
    private void removeIndex( final SearchObject aSearchObject ) throws IntactException {
        try {
            final IndexReader reader = IndexReader.open( index );
            final Term term = new Term( SearchEngineConstants.AC, aSearchObject.getAc() );
            reader.deleteDocuments( term );
            reader.close();
        } catch ( IOException e ) {
            logger.error( "problems with removing a term", e );
            throw new IntactException( "Error while removing searchObject data from index", e );
        }
        throw new RuntimeException( "implement me" );
    }

    ;


    /**
     * todo prove if it really works...
     *
     * @param aSearchObject
     *
     * @throws IntactException
     */
    private void updateIndex( final SearchObject aSearchObject ) throws IntactException {
        final String ac = aSearchObject.getAc();
        final String objClass = aSearchObject.getObjClass();

        this.removeIndex( aSearchObject );
        SearchObject updatedObject = ( SearchObject ) dao.getSearchObject( ac, objClass );
        this.createIndex( updatedObject );
    }

    ;

}
