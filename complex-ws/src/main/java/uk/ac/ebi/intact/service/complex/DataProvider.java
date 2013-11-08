package uk.ac.ebi.intact.service.complex;

import org.apache.solr.client.solrj.SolrServerException;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexResultIterator;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexSolrSearcher;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 08/11/13
 */
public class DataProvider {

    int chunkSize = 500;

    public DataProvider(){ }

    public ComplexRestResult getData(String query, ComplexSolrSearcher searcher) {
        if ( searcher != null ){
            int offset = 0;
            ComplexResultIterator iterator;
            ComplexRestResult result = new ComplexRestResult();
            do{
                try {
                    iterator = searcher.search(query, offset, chunkSize, "" );
                    if ( ! iterator.hasNext() ) break;
                    result.add(iterator);
                } catch (SolrServerException e) {
                    e.printStackTrace();
                }
                offset += chunkSize;
            }while(true);
            return result;
        }
        return null;
    }
}
