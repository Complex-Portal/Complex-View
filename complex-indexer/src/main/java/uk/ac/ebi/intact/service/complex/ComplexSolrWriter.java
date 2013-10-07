package uk.ac.ebi.intact.service.complex;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemWriter;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.psimi.solr.converter.ComplexSolrConverter;
import uk.ac.ebi.intact.model.InteractionImpl;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

/**
 * Indexes information about complex into a Solr Server.
 *
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 25/07/13
 */
public class ComplexSolrWriter implements ItemWriter < InteractionImpl >, ItemStream {
    /********************************/
    /*      Private attributes      */
    /********************************/
    private static final Log log = LogFactory.getLog(ComplexSolrWriter.class);
    protected String solrUrl = null ;
    protected HttpSolrServer solrServer = null ;
    protected ComplexSolrConverter complexSolrConverter = null ;

    private int maxTotalConnections ;
    private int defaultMaxConnectionsPerHost ;
    private boolean allowCompression ;
    private boolean needToCommitOnClose;
    private String ontologySolrUrl;
    private HttpSolrServer ontologySolrServer = null ;

    /**************************/
    /*      Constructors      */
    /**************************/
    public ComplexSolrWriter ( ) {
        //default settings for solr server
        this.maxTotalConnections = 128 ;
        this.defaultMaxConnectionsPerHost = 24 ;
        this.allowCompression = true ;
        this.needToCommitOnClose = false ;
    }

    /*********************************/
    /*      Getters and Setters      */
    /*********************************/
    public void setSolrUrl ( String sorlUrl_ ) { this.solrUrl = sorlUrl_ ; }
    public String getSolrUrl ( ) { return this.solrUrl ; }
    public void setMaxTotalConnections ( int max ) { this.maxTotalConnections = max ; }
    public int getMaxTotalConnections ( ) { return this.maxTotalConnections ; }
    public void setDefaultMaxConnectionsPerHost ( int max ) { this.defaultMaxConnectionsPerHost = max ; }
    public int getDefaultMaxConnectionsPerHost ( ) { return this.defaultMaxConnectionsPerHost ; }
    public void setAllowCompression ( boolean compression ) { this.allowCompression = compression ; }
    public boolean getAllowCompression ( ) { return this.allowCompression ; }
    public void setNeedToCommitOnClose ( boolean commitOnClose ) { this.needToCommitOnClose = commitOnClose ; }
    public boolean getNeedToCommitOnClose ( ) { return this.needToCommitOnClose ; }
    public SolrServer getSolrServer ( ) throws ParserConfigurationException, SAXException, IOException {
        if (this.solrServer == null){
           this.solrServer = createSolrServer(this.solrUrl);
        }
        return this.solrServer ;
    }

    /*******************************/
    /*      Protected methods      */
    /*******************************/
    protected HttpClient createHttpClient ( ) {
        SchemeRegistry schemeRegistry = new SchemeRegistry ( ) ;
        schemeRegistry.register ( new Scheme ( "http",   80, PlainSocketFactory.getSocketFactory() ) ) ;
        schemeRegistry.register ( new Scheme ( "https", 443, SSLSocketFactory.getSocketFactory() ) ) ;
        PoolingClientConnectionManager cm = new PoolingClientConnectionManager ( schemeRegistry ) ;
        cm.setMaxTotal ( maxTotalConnections ) ;
        cm.setDefaultMaxPerRoute ( defaultMaxConnectionsPerHost ) ;
        HttpClient httpClient = new DefaultHttpClient ( cm ) ;
        return httpClient;
    }

    protected HttpSolrServer createSolrServer ( String url ) throws IOException, SAXException, ParserConfigurationException {
        HttpSolrServer server = new HttpSolrServer ( url, createHttpClient ( ) ) ;
        server.setMaxRetries ( 0 ) ;
        server.setAllowCompression ( this.allowCompression ) ;
        return server ;
    }

    /******************************/
    /*      Override methods      */
    /******************************/
    @Override
    public void open ( ExecutionContext executionContext ) throws ItemStreamException {
        if ( this.solrUrl != null ) {
            try {
                this.solrServer = createSolrServer ( this.solrUrl ) ;
            } catch (IOException e) {
                throw new ItemStreamException ( new StringBuilder ( ) .append ( "Cannot connect to SolrServer: " ) .append ( this.solrUrl ) .toString ( ), e ) ;
            } catch (SAXException e) {
                throw new ItemStreamException ( "Impossible to create a new HTTP solr server", e ) ;
            } catch (ParserConfigurationException e) {
                throw new ItemStreamException ( "Impossible to create a new HTTP solr server", e ) ;
            }
        }
        else{
            throw new ItemStreamException ( "The complexes solr url is mandatory" ) ;
        }
        if ( this.ontologySolrUrl != null ) {
            try {
                this.ontologySolrServer = createSolrServer ( this.ontologySolrUrl) ;
            } catch (IOException e) {
                throw new ItemStreamException ( new StringBuilder ( ) .append ( "Cannot connect to SolrServer: " ) .append ( this.solrUrl ) .toString ( ), e ) ;
            } catch (SAXException e) {
                throw new ItemStreamException ( "Impossible to create a new HTTP solr server", e ) ;
            } catch (ParserConfigurationException e) {
                throw new ItemStreamException ( "Impossible to create a new HTTP solr server", e ) ;
            }

            this.complexSolrConverter = new ComplexSolrConverter( this.ontologySolrServer ) ;
        }
        else{
            throw new ItemStreamException ( "The complexes solr url is mandatory" ) ;
        }
    }

    @Override
    public void update ( ExecutionContext executionContext ) throws ItemStreamException {
        if ( this.solrServer != null ) {
            try {
                this.solrServer.commit ( ) ;
                this.needToCommitOnClose = true ;
            } catch (SolrServerException e) {
                throw new ItemStreamException ( "Problem committing the results", e ) ;
            } catch (IOException e) {
                throw new ItemStreamException ( "Problem committing the results", e ) ;
            }
        }
    }

    @Override
    public void close ( ) throws ItemStreamException {
        if ( this.solrServer != null ) {
            if ( this.needToCommitOnClose ) {
                try {
                    this.solrServer.optimize ( ) ;
                } catch (SolrServerException e) {
                    throw new ItemStreamException("Problem closing solr server", e);
                } catch (IOException e) {
                    throw new ItemStreamException("Problem closing solr server", e);
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public void write ( List < ? extends InteractionImpl > interactions ) throws Exception {
        if ( solrServer == null ) { throw new IllegalStateException ( "No HttpSolrServer configured for ComplexSolrWriter" ) ; }
        if ( ! interactions.isEmpty ( ) ) {
            this.needToCommitOnClose = false ;
            for ( InteractionImpl interaction : interactions ) {
                // reattach the interaction object to the entity manager because connection may have been closed after reading the object
                InteractionImpl reloadedInteraction = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().merge(interaction);
                this.solrServer.add ( this.complexSolrConverter.convertComplexToSolrDocument ( reloadedInteraction ) ) ;
            }

            IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().clear();
        }
    }

    public String getOntologySolrUrl() {
        return ontologySolrUrl;
    }

    public void setOntologySolrUrl(String ontologySolrUrl) {
        this.ontologySolrUrl = ontologySolrUrl;
    }
}
