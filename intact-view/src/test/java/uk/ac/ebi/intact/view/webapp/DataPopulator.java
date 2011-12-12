package uk.ac.ebi.intact.view.webapp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.stereotype.Controller;
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.converter.xml2tab.TabConversionException;
import psidev.psi.mi.tab.converter.xml2tab.Xml2Tab;
import psidev.psi.mi.tab.expansion.SpokeWithoutBaitExpansion;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.PsimiXmlReaderException;
import psidev.psi.mi.xml.converter.ConverterException;
import psidev.psi.mi.xml.model.EntrySet;
import uk.ac.ebi.intact.bridges.ontologies.OntologyMapping;
import uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.PsiExchange;
import uk.ac.ebi.intact.psimitab.IntactPsimiTabWriter;
import uk.ac.ebi.intact.psimitab.IntactXml2Tab;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@DependsOn("intactInitializer")
public class DataPopulator implements InitializingBean {

    private static final Log log = LogFactory.getLog(DataPopulator.class);
    
    @Autowired
    private PsiExchange psiExchange;

    @Autowired
    private SolrServerTestController solrServerTestController;


    @Override
    public void afterPropertiesSet() throws Exception {
        if (log.isInfoEnabled()) log.info("POPULATING DATA SERVERS (H2/SOLR)...");

        indexCvObo();

        importXmlData();

    }

    private void indexCvObo() {
        if (log.isInfoEnabled()) log.info("Indexing CV ontology...");

        solrServerTestController.getIndexer().indexOntologies(new OntologyMapping[]{
                new OntologyMapping("psi-mi", DataPopulator.class.getResource("/META-INF/data/psi-mi25.obo"))});
    }

    private void importXmlData() throws TabConversionException, IOException, ConverterException, PsimiXmlReaderException {
        if (log.isInfoEnabled()) log.info("Importing some XML data...");

        PsimiXmlReader reader = new PsimiXmlReader();

        storeEntrySet(reader.read(DatabasePopulator.class.getResourceAsStream("/META-INF/data/10514511.xml")));
        storeEntrySet(reader.read(DatabasePopulator.class.getResourceAsStream("/META-INF/data/11554746.xml")));
    }

    public void storeEntrySet(EntrySet entrySet) throws TabConversionException, IOException, ConverterException {
        psiExchange.importIntoIntact(entrySet);

        Xml2Tab xml2tab = new IntactXml2Tab();
        xml2tab.setExpansionStrategy(new SpokeWithoutBaitExpansion());

        Collection<BinaryInteraction> binaryInteractions = xml2tab.convert(entrySet);

        storeBinaryInteractions(binaryInteractions);
    }

    private void storeBinaryInteractions(Collection<BinaryInteraction> binaryInteractions) throws IOException, ConverterException {
        StringWriter sw = new StringWriter();

        PsimiTabWriter psimitabWriter = new IntactPsimiTabWriter();
        psimitabWriter.write(binaryInteractions, sw);

        solrServerTestController.getIndexer().indexMitab(new ByteArrayInputStream(sw.toString().getBytes()), true);
    }

}
