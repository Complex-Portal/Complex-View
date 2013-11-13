package uk.ac.ebi.intact.service.complex;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.intact.dataexchange.psimi.solr.server.IntactSolrJettyRunner;
import uk.ac.ebi.intact.model.CvInteractionType;
import uk.ac.ebi.intact.model.CvInteractorType;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.InteractionImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 28/08/13
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
"classpath:META-INF/complex-indexer-spring.xml"
})
public class ComplexSolrWriterTest {
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    private IntactSolrJettyRunner solrJettyRunner = null ;
    private HttpSolrServer httpSolrServer = null ;
    private ComplexSolrWriter complexSolrWriter = null ;
    private String OntolyUrl = null ;
    @Before
    public void setUp() throws Exception {
        this.solrJettyRunner = new IntactSolrJettyRunner ( ) ;
        this.solrJettyRunner.start ( ) ;
        this.complexSolrWriter = new ComplexSolrWriter ( ) ;
        this.httpSolrServer = this.solrJettyRunner.getSolrServer( "core_complex_pub" ) ;
        this.OntolyUrl = this.solrJettyRunner.getSolrServer( "core_ontology_pub" ).getBaseURL ( ) ;
    }

    @After
    public void tearDown() throws Exception {
        this.complexSolrWriter.close ( ) ;
        this.solrJettyRunner.stop ( ) ;
        FileUtils.forceDelete(this.solrJettyRunner.getSolrHome ( ) ) ;
        this.solrJettyRunner = null;
    }

    @Test
    public void testSetSolrUrl() throws Exception {
        this.complexSolrWriter.setSolrUrl ( this.httpSolrServer.getBaseURL ( ) ) ;
        Assert.assertEquals ( "Test set/get url", this.httpSolrServer.getBaseURL(), this.complexSolrWriter.getSolrUrl() ) ;
    }

    @Test
    public void testSetMaxTotalConnections() throws Exception {
        int max = 256 ;
        Assert.assertEquals ( "Test get original max total connections", 128, this.complexSolrWriter.getMaxTotalConnections() ) ;
        this.complexSolrWriter.setMaxTotalConnections(max) ;
        Assert.assertEquals("Test set/get max total connections", max, this.complexSolrWriter.getMaxTotalConnections()) ;
    }

    @Test
    public void testSetDefaultMaxConnectionsPerHost() throws Exception {
        int max = 32 ;
        Assert.assertEquals("Test get original max connections per host", 24, this.complexSolrWriter.getDefaultMaxConnectionsPerHost()) ;
        this.complexSolrWriter.setDefaultMaxConnectionsPerHost(max) ;
        Assert.assertEquals("Test set/get max connections per host", max, this.complexSolrWriter.getDefaultMaxConnectionsPerHost()) ;
    }

    @Test
    public void testSetAllowCompression() throws Exception {
        boolean compression = false ;
        Assert.assertTrue("Test get original compress", this.complexSolrWriter.getAllowCompression()) ;
        this.complexSolrWriter.setAllowCompression(compression) ;
        Assert.assertFalse("Test set/get compress", this.complexSolrWriter.getAllowCompression()) ;
    }

    @Test
    public void testSetNeedToCommitOnClose() throws Exception {
        boolean commit = true ;
        Assert.assertFalse ( "Test get original commit", this.complexSolrWriter.getNeedToCommitOnClose ( ) ) ;
        this.complexSolrWriter.setNeedToCommitOnClose ( commit ) ;
        Assert.assertTrue("Test set/get commit", this.complexSolrWriter.getNeedToCommitOnClose()) ;
    }

    @Test
    public void testOpen() throws Exception {
        JobExecution jobExecution = jobLauncherTestUtils.launchJob ( ) ;
        ExecutionContext executionContext = jobExecution.getExecutionContext ( ) ;
        // Test without Solr URL
        Throwable exception = null ;
        try{
            this.complexSolrWriter.open ( executionContext ) ;
        }
        catch ( Throwable e ) {
            exception = e;
        }
        Assert.assertTrue ( "Test Open without url", exception instanceof ItemStreamException ) ;
        // Test with Solr URL
        exception = null ;
        this.complexSolrWriter.setSolrUrl ( this.httpSolrServer.getBaseURL ( ) ) ;
        try{
            this.complexSolrWriter.open ( executionContext ) ;
        }
        catch ( Throwable e ) {
            exception = e;
        }
        Assert.assertTrue ( "Test Open without url", exception instanceof ItemStreamException) ;
    }

    @Test
    public void testUpdate() throws Exception {
        JobExecution jobExecution = jobLauncherTestUtils.launchJob ( ) ;
        ExecutionContext executionContext = jobExecution.getExecutionContext ( ) ;
        this.complexSolrWriter.setSolrUrl ( this.httpSolrServer.getBaseURL() ) ;
        this.complexSolrWriter.setOntologySolrUrl ( this.OntolyUrl ) ;
        this.complexSolrWriter.open ( executionContext ) ;
        // Test update
        Assert.assertFalse ( "Test update 1", this.complexSolrWriter.getNeedToCommitOnClose ( ) ) ;
        this.complexSolrWriter.update ( executionContext ) ;
        Assert.assertTrue ( "Test update 2", this.complexSolrWriter.getNeedToCommitOnClose ( ) ) ;
    }

    @Test
    public void testWrite() throws Exception {
        JobExecution jobExecution = jobLauncherTestUtils.launchJob ( ) ;
        ExecutionContext executionContext = jobExecution.getExecutionContext ( ) ;
        List< InteractionImpl > list = new ArrayList<InteractionImpl>( ) ;
        this.complexSolrWriter.setSolrUrl ( this.httpSolrServer.getBaseURL() ) ;
        this.complexSolrWriter.setOntologySolrUrl ( this.OntolyUrl ) ;
        this.complexSolrWriter.open ( executionContext ) ;
        this.complexSolrWriter.setNeedToCommitOnClose ( true ) ;
        // Test write an empty list
        this.complexSolrWriter.write ( list );
        Assert.assertTrue ( "Test write with an empty list", this.complexSolrWriter.getNeedToCommitOnClose ( ) ) ;
        // Test write
        InteractionImpl interaction = new InteractionImpl(
                new ArrayList(),
                new CvInteractionType(),
                new CvInteractorType(),
                "ShortLabel",
                new Institution ( "Institution.ShortLabel" )
        ) ;
        list.add ( interaction ) ;
        Throwable exception = null ;
        try {
            this.complexSolrWriter.write ( list ) ;
        }
        catch ( Throwable e ) {
            exception = e ;
        }
        Assert.assertTrue ("Test write", exception instanceof SolrServerException) ;
    }

}
