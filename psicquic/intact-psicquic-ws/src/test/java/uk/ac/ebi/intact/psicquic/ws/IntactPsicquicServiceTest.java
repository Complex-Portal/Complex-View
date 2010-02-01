/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.psicquic.ws;

import org.hupo.psi.mi.psicquic.DbRef;
import org.hupo.psi.mi.psicquic.PsicquicService;
import org.hupo.psi.mi.psicquic.QueryResponse;
import org.hupo.psi.mi.psicquic.RequestInfo;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.intact.dataexchange.psimi.solr.CoreNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrIndexer;
import uk.ac.ebi.intact.dataexchange.psimi.solr.server.SolrJettyRunner;
import uk.ac.ebi.intact.psicquic.ws.config.PsicquicConfig;

import java.io.InputStream;
import java.util.Arrays;

/**
 * IntactPsicquicService Tester.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class IntactPsicquicServiceTest {

    private static PsicquicService service;

    private static SolrJettyRunner solrJettyRunner;

    @BeforeClass
    public static void setupSolrPsicquicService() throws Exception {

        // Start a jetty server to host the solr index
        solrJettyRunner = new SolrJettyRunner();
        solrJettyRunner.setPort( 19876 );
        solrJettyRunner.start();

        // index data to be hosted by PSICQUIC
        InputStream mitabStream = IntactPsicquicServiceTest.class.getResourceAsStream("/META-INF/imatinib.mitab.txt");

        Assert.assertNotNull("Input stream for test file is null", mitabStream);

        IntactSolrIndexer indexer = new IntactSolrIndexer(solrJettyRunner.getSolrUrl( CoreNames.CORE_PUB ),
                                                          solrJettyRunner.getSolrUrl( CoreNames.CORE_ONTOLOGY_PUB ));
        final int lineCount = indexer.indexMitab( mitabStream, true );
        System.out.println( "Line indexed: " + lineCount );

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"/META-INF/beans.spring.test.xml"});
        PsicquicConfig config = (PsicquicConfig)context.getBean("testPsicquicConfig");
        config.setSolrServerUrl( solrJettyRunner.getSolrUrl( CoreNames.CORE_PUB ) );

	    service = (PsicquicService) context.getBean("intactPsicquicService");
    }

    @AfterClass
    public static void afterClass() throws Exception {

//        solrJettyRunner.join(); // keep the server running ...

        solrJettyRunner.stop();
        solrJettyRunner = null;
        service = null;
    }

    @Test
    public void testGetByInteractor() throws Exception {
        RequestInfo info = new RequestInfo();
        info.setResultType( IntactPsicquicService.RETURN_TYPE_MITAB25 );
        info.setBlockSize(50);

        DbRef dbRef = new DbRef();
        dbRef.setId("Imatinib");

        final QueryResponse response = service.getByInteractor(dbRef, info);

        Assert.assertEquals(11, response.getResultInfo().getTotalResults());
        Assert.assertEquals(11, response.getResultSet().getMitab().split("\n").length);
    }

    @Test
    public void testGetByInteraction() throws Exception {
        RequestInfo info = new RequestInfo();
        info.setResultType( IntactPsicquicService.RETURN_TYPE_MITAB25 );
        info.setBlockSize(50);

        DbRef dbRef = new DbRef();
        dbRef.setId("DGI-337977");

        final QueryResponse response = service.getByInteraction(dbRef, info);

        Assert.assertEquals(1, response.getResultInfo().getTotalResults());
    }

    @Test
    public void testGetByInteractorList_operandOR() throws Exception {
        RequestInfo info = new RequestInfo();
        info.setResultType( IntactPsicquicService.RETURN_TYPE_MITAB25 );
        info.setBlockSize(50);

        DbRef dbRef1 = new DbRef();
        dbRef1.setId("MDR1");
        DbRef dbRef2 = new DbRef();
        dbRef2.setId("FMS");

        final QueryResponse response = service.getByInteractorList(Arrays.asList(dbRef1, dbRef2), info, "OR");

        Assert.assertEquals(2, response.getResultInfo().getTotalResults());

    }

    @Test
    public void testGetByInteractorList_operandAND() throws Exception {
        RequestInfo info = new RequestInfo();
        info.setResultType( IntactPsicquicService.RETURN_TYPE_MITAB25 );
        info.setBlockSize(50);

        DbRef dbRef1 = new DbRef();
        dbRef1.setId("Imatinib");
        DbRef dbRef2 = new DbRef();
        dbRef2.setId("FMS");

        final QueryResponse response = service.getByInteractorList(Arrays.asList(dbRef1, dbRef2), info, "AND");

        Assert.assertEquals(1, response.getResultInfo().getTotalResults());

    }

    @Test
    public void testGetByInteractionList() throws Exception {
        RequestInfo info = new RequestInfo();
        info.setResultType( IntactPsicquicService.RETURN_TYPE_MITAB25 );
        info.setBlockSize(50);

        DbRef dbRef1 = new DbRef();
        dbRef1.setId("DGI-337977");
        DbRef dbRef2 = new DbRef();
        dbRef2.setId("DGI-337968");

        final QueryResponse response = service.getByInteractionList(Arrays.asList(dbRef1, dbRef2), info);

        Assert.assertEquals(2, response.getResultInfo().getTotalResults());
    }

    @Test
    public void testGetByQuery() throws Exception {
        RequestInfo info = new RequestInfo();
        info.setResultType( IntactPsicquicService.RETURN_TYPE_MITAB25 );
        info.setBlockSize(50);

        final QueryResponse response = service.getByQuery("imatinib", info);

        Assert.assertEquals(11, response.getResultInfo().getTotalResults());
        Assert.assertEquals(11, response.getResultSet().getMitab().split("\n").length);
    }

    @Test
    public void testGetVersion() {
        Assert.assertEquals("TEST.VERSION", service.getVersion());
    }
}