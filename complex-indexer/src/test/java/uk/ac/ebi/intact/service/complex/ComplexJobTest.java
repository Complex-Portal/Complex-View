package uk.ac.ebi.intact.service.complex;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.dataexchange.psimi.solr.CoreNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.server.IntactSolrJettyRunner;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Test Spring bath beans
 *
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 06/09/13
 */
@ContextConfiguration ( locations = {"/META-INF/complex-indexer-spring.xml"} )
@Transactional ( propagation = Propagation.NEVER )
public class ComplexJobTest {
    /********************************/
    /*      Private attributes      */
    /********************************/
    @Resource(name = "intactBatchJobLauncher")
    private JobLauncher jobLauncher;
    @Autowired
    private ApplicationContext applicationContext;

    /****************************************************/
    /*      Tasks to do before and after each test      */
    /****************************************************/
    @Before
    public void before ( ) throws Exception {
        //solrJettyRunner = new IntactSolrJettyRunner ( ) ;
        //solrJettyRunner.setPort(18080) ;
        //solrJettyRunner.start ( ) ;
    }
    @After
    public void after ( ) throws Exception {
        //solrJettyRunner.stop ( ) ;
    }

    /*******************/
    /*      Tests      */
    /*******************/
    @Test
    @DirtiesContext
    public void testCuratedComplexReader() throws Exception {
        Job job = (Job) applicationContext.getBean("curatedComplexReader");
        JobExecution jobExecution = jobLauncher.run ( job, new JobParameters ( ) ) ;
        // TODO Asserts
    }

    @Test
    @DirtiesContext
    public void testCuratedComplexJob() throws Exception {
        Job job = (Job) applicationContext.getBean("indexSolrComplex");
        final SolrServer solrServer = new HttpSolrServer("http://127.0.0.1:8983/solr/core_complex_pub/");
        JobExecution jobExecution = jobLauncher.run ( job, new JobParameters ( ) ) ;
        // TODO Asserts
    }

}
