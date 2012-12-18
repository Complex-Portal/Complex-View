package uk.ac.ebi.intact.view.webapp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.transaction.TransactionStatus;
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.converter.xml2tab.TabConversionException;
import psidev.psi.mi.tab.converter.xml2tab.Xml2Tab;
import psidev.psi.mi.tab.expansion.SpokeWithoutBaitExpansion;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.builder.PsimiTabVersion;
import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.PsimiXmlReaderException;
import psidev.psi.mi.xml.converter.ConverterException;
import psidev.psi.mi.xml.model.EntrySet;
import uk.ac.ebi.intact.bridges.ontologies.OntologyMapping;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.entry.IntactEntryFactory;
import uk.ac.ebi.intact.dataexchange.psimi.solr.CoreNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrIndexer;
import uk.ac.ebi.intact.dataexchange.psimi.solr.server.IntactSolrJettyRunner;
import uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.PsiExchange;
import uk.ac.ebi.intact.model.Publication;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class DataPopulator  {

    private static final Log log = LogFactory.getLog(DataPopulator.class);

    private IntactContext intactContext;
    private PsiExchange psiExchange;
    
    private HttpSolrServer interactionsSolrServer;
    private HttpSolrServer ontologiesSolrServer;
    private IntactSolrIndexer indexer;

    public DataPopulator(IntactContext intactContext) throws Exception {
        this.intactContext = intactContext;
        this.psiExchange = (PsiExchange) intactContext.getSpringContext().getBean("psiExchange");

        this.interactionsSolrServer = new HttpSolrServer("http://localhost:33444/solr/"+ CoreNames.CORE_PUB);
        this.ontologiesSolrServer = new HttpSolrServer("http://localhost:33444/solr/"+CoreNames.CORE_ONTOLOGY_PUB);

        indexer = new IntactSolrIndexer( interactionsSolrServer, ontologiesSolrServer );
    }

    public static void main(String[] args) throws Exception {
        IntactSolrJettyRunner solrJettyRunner = new IntactSolrJettyRunner(new File("target/solr"));
        solrJettyRunner.setPort(33444);
        solrJettyRunner.start();

        IntactContext.initContext(new String[]{"classpath*:/META-INF/intact-view-test.spring.xml",
                "classpath*:/META-INF/intact-view.jpa-test.spring.xml"});


        DataPopulator dataPopulator = new DataPopulator(IntactContext.getCurrentInstance());
        dataPopulator.populateTestData();


        IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().close();

        IntactContext.getCurrentInstance().destroy();

        solrJettyRunner.stop();
    }

    public void populateTestData() throws Exception {
        if (log.isInfoEnabled()) log.info("POPULATING DATA SERVERS (H2/SOLR)...");
        indexCvObo();

        final TransactionStatus transactionStatus = IntactContext.getCurrentInstance().getDataContext().beginTransaction();

        importXmlData();

        IntactContext.getCurrentInstance().getDataContext().commitTransaction(transactionStatus);

        final TransactionStatus transactionStatus2 = IntactContext.getCurrentInstance().getDataContext().beginTransaction();

        indexInteractions();

        IntactContext.getCurrentInstance().getDataContext().commitTransaction(transactionStatus2);

    }



    private void indexCvObo() {
        if (log.isInfoEnabled()) log.info("Indexing CV ontology...");

        indexer.indexOntologies(new OntologyMapping[]{
                new OntologyMapping("psi-mi", DataPopulator.class.getResource("/META-INF/data/psi-mi25.obo"))});
        indexer.indexOntologies(new OntologyMapping[] {
                new OntologyMapping("go", DataPopulator.class.getResource("/META-INF/data/goslim_generic.obo"))});
    }

    private void importXmlData() throws TabConversionException, IOException, ConverterException, PsimiXmlReaderException {
        if (log.isInfoEnabled()) log.info("Importing some XML data...");

        PsimiXmlReader reader = new PsimiXmlReader();

        storeEntrySet(reader.read(DatabasePopulator.class.getResourceAsStream("/META-INF/data/10514511.xml")));
        storeEntrySet(reader.read(DatabasePopulator.class.getResourceAsStream("/META-INF/data/11554746.xml")));
        storeEntrySet(reader.read(DatabasePopulator.class.getResourceAsStream("/META-INF/data/17461779.xml")));
    }

    public void storeEntrySet(EntrySet entrySet) {
        psiExchange.importIntoIntact(entrySet);
    }

    private void storeBinaryInteractions(Collection<BinaryInteraction> binaryInteractions) throws IOException, ConverterException {
        StringWriter sw = new StringWriter();

        PsimiTabWriter psimitabWriter = new PsimiTabWriter(PsimiTabVersion.v2_7);
        psimitabWriter.write(binaryInteractions, sw);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(sw.toString().getBytes());
        try{
            indexer.indexMitab(inputStream, true);
        }
        finally{
            inputStream.close();
        }
    }

    private void indexInteractions() throws ConverterException, IOException, TabConversionException {
        final List<Publication> publications = intactContext.getDaoFactory().getPublicationDao().getAll();

        try{
            for (Publication pub : publications) {
                final EntrySet entrySet = psiExchange.exportToEntrySet(IntactEntryFactory.createIntactEntry(intactContext).addPublication(pub));

                Xml2Tab xml2tab = new Xml2Tab();
                xml2tab.setExpansionStrategy(new SpokeWithoutBaitExpansion());

                Collection<BinaryInteraction> binaryInteractions = xml2tab.convert(entrySet);

                storeBinaryInteractions(binaryInteractions);
            }
        }
        finally {
            psiExchange.close();
        }

    }

}
