package uk.ac.ebi.intact.site.complex.utils;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexSolrSearcher;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 22/10/13
 */
public abstract class SearcherIndex {
    static HttpSolrServer solrServer = null ;

    public static ComplexSolrSearcher getSearcher(String Url) {
        String solrUrl = Url;
        //default settings for solr server
        int maxTotalConnections = 128 ;
        int defaultMaxConnectionsPerHost = 24 ;
        boolean allowCompression = true ;
        boolean needToCommitOnClose = false ;
        SchemeRegistry schemeRegistry = new SchemeRegistry ( ) ;
        schemeRegistry.register ( new Scheme( "http",   80, PlainSocketFactory.getSocketFactory() ) ) ;
        schemeRegistry.register ( new Scheme ( "https", 443, org.apache.http.conn.ssl.SSLSocketFactory.getSocketFactory() ) ) ;
        PoolingClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
        cm.setMaxTotal(maxTotalConnections);
        cm.setDefaultMaxPerRoute(defaultMaxConnectionsPerHost);
        HttpClient httpClient = new DefaultHttpClient(cm);
        HttpSolrServer server = new HttpSolrServer(solrUrl, httpClient);
        server.setMaxRetries(0);
        server.setAllowCompression(allowCompression);
        HttpSolrServer solrServer = server;
        return new ComplexSolrSearcher(solrServer);
    }

}
