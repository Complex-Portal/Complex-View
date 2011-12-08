package uk.ac.ebi.intact.view.webapp;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.stereotype.Controller;
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.converter.xml2tab.TabConversionException;
import psidev.psi.mi.tab.converter.xml2tab.Xml2Tab;
import psidev.psi.mi.tab.expansion.SpokeWithoutBaitExpansion;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.converter.ConverterException;
import psidev.psi.mi.xml.model.EntrySet;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.PsiExchange;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.IntactEntry;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Publication;
import uk.ac.ebi.intact.psimitab.IntactXml2Tab;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
public class DataPopulator implements InitializingBean {

    @Autowired
    private IntactContext intactContext;

    @Autowired
    private PsiExchange psiExchange;

    @Autowired
    private SolrServerTestController solrServerTestController;


    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("POPULATING DATA SERVERS (H2/SOLR)...");
        
//        IntactMockBuilder mockBuilder = new IntactMockBuilder(intactContext.getInstitution());
//
//        Publication publication = mockBuilder.createPublication("1234567");
//        Experiment experiment = mockBuilder.createExperimentRandom(4);
//        experiment.setPublication(null);
//        publication.addExperiment(experiment);
//
//        storePublication(publication);


        PsimiXmlReader reader = new PsimiXmlReader();

        storeEntrySet(reader.read(DatabasePopulator.class.getResourceAsStream("/META-INF/data/10514511.xml")));
        storeEntrySet(reader.read(DatabasePopulator.class.getResourceAsStream("/META-INF/data/11554746.xml")));

    }

    public void storePublication(Publication publication) throws Exception {
        intactContext.getCorePersister().saveOrUpdate(publication);

        EntrySet entrySet = psiExchange.exportToEntrySet(toIntactEntry(publication));
        storeEntrySet(entrySet);
    }

    public void storeEntrySet(EntrySet entrySet) throws TabConversionException, IOException, ConverterException {
        Xml2Tab xml2tab = new IntactXml2Tab();
        xml2tab.setExpansionStrategy(new SpokeWithoutBaitExpansion());

        Collection<BinaryInteraction> binaryInteractions = xml2tab.convert(entrySet);

        storeBinaryInteractions(binaryInteractions);
    }

    public void storeBinaryInteractions(Collection<BinaryInteraction> binaryInteractions) throws IOException, ConverterException {
        StringWriter sw = new StringWriter();

        PsimiTabWriter psimitabWriter = new PsimiTabWriter();
        psimitabWriter.write(binaryInteractions, sw);

        solrServerTestController.getIndexer().indexMitab(new ByteArrayInputStream(sw.toString().getBytes()), true);
    }

    private IntactEntry toIntactEntry(Publication publication) {
        Collection<Interaction> interactions = new ArrayList<Interaction>();

        for (Experiment exp : publication.getExperiments()) {
            interactions.addAll(exp.getInteractions());
        }

        return new IntactEntry(interactions);
    }

}
