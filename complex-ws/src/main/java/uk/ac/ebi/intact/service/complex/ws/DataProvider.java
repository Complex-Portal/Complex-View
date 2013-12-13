package uk.ac.ebi.intact.service.complex.ws;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexResultIterator;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexSolrSearcher;

/**
 * This class is the only one who has to query the searcher.
 * All queries must pass to this class
 *
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 08/11/13
 */
public class DataProvider {
    /********************************/
    /*      Private attributes      */
    /********************************/
    @Autowired
    private ComplexSolrSearcher searcher ;
    int chunkSize; // 500 is a good number
    private static final Log log = LogFactory.getLog(DataProvider.class);

    /*************************/
    /*      Constructor      */
    /*************************/
    public DataProvider( int size ) { this.chunkSize = size ; }
    public DataProvider( ) { }

    /********************************/
    /*      Setter and getters      */
    /********************************/
    public int getChunkSize() { return chunkSize; }
    public void setChunkSize(int size) { chunkSize = size; }

    /*******************************/
    /*      Protected methods      */
    /*******************************/
    // This method is for abstract the way to retrieve information from Solr.
    // It does not check the parameter because it is called from getData method
    // , where we control all that things
    protected ComplexResultIterator retrieve(String query, int offset, int size) {
        ComplexResultIterator iterator; // Used to stored the query's result
        try {
            iterator = this.searcher.search ( query, // query
                                             offset, // first result
                                               size, // end result
                                                "" ) // filters to use
            ;
            // Check if iterator has information and return the right result
            return iterator.hasNext() ? iterator : null;
        }
        catch (SolrServerException e) {
            if ( log.isInfoEnabled() )
                log.info( "DataProvider error, we could not connect to Solr Server", e);
            e.printStackTrace();
        }
        return null;
    }
    /****************************/
    /*      Public methods      */
    /****************************/
    // getData function return the results of the query
    public ComplexRestResult getData(String query, int first, int number) {
        int i = first;
        boolean in = false; // to know if we went into the loop
        ComplexResultIterator iterator = null; // Gives retrieve method results
        ComplexRestResult result = new ComplexRestResult(); // Return variable

        // Loop to retrieve information from the searcher
        // i = first parameter
        // until i + chunkSize >= number
        // increment -> i += chunkSize
        //
        // Example:
        // first = 10; number = 65; chunkSize = 20;
        // That means we have to get from 10 to 75 the results
        //
        // i = 10; 10 + 20 < 65 + 10, then query from 10 to 30
        // i = 30; 30 + 20 < 65 + 10, then query from 30 to 50
        // i = 50; 50 + 20 < 65 + 10, then query from 50 to 70
        // i = 70; 70 + 20 > 65 + 10, then exit
        for ( /* i = first */ ; i + chunkSize < number + first ; i+= chunkSize ) {
            in = true;
            iterator = retrieve(query, i, chunkSize);
            // This method will traverse the iterator and get the elements
            if ( iterator != null ) result.add(iterator);
            else return result;
        }
        // If we went into the loop
        if ( in ) i -= chunkSize; // Go back one step
        // We have to query for the rest of the elements. Carry on with the
        // previous example:
        //
        // i = 70 and number = 65, we have to query for from 70 to 75
        // for that we query from 70 to 75 ( number + first - i = 65 + 10 - 70 )
        if ( i < number + first ) {
            iterator = retrieve(query, i, number + first - i);
            // This method will traverse the iterator and get the elements
            if ( iterator != null ) result.add(iterator);
        }
        // Return the results
        return result;
    }
}