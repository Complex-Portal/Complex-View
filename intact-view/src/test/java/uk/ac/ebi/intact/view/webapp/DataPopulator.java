package uk.ac.ebi.intact.view.webapp;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.converter.xml2tab.Xml2Tab;
import psidev.psi.mi.tab.expansion.SpokeWithoutBaitExpansion;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.xml.model.EntrySet;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.unit.IntactMockBuilder;
import uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrIndexer;
import uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.PsiExchange;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.IntactEntry;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Publication;
import uk.ac.ebi.intact.psimitab.IntactXml2Tab;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;

import java.io.ByteArrayInputStream;
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
        
        IntactMockBuilder mockBuilder = new IntactMockBuilder(intactContext.getInstitution());

        Publication publication = mockBuilder.createPublication("1234567");
        Experiment experiment = mockBuilder.createExperimentRandom(4);
        experiment.setPublication(null);
        publication.addExperiment(experiment);

        storePublication(publication);


//        Interaction interaction1 = mockBuilder.createInteractionRandomBinary();
//        interaction1.getExperiments().iterator().next().setPublication(publication);

    }

    public void storePublication(Publication publication) throws Exception {
        intactContext.getCorePersister().saveOrUpdate(publication);

        EntrySet entrySet = psiExchange.exportToEntrySet(toIntactEntry(publication));

        Xml2Tab xml2tab = new IntactXml2Tab();
        xml2tab.setExpansionStrategy(new SpokeWithoutBaitExpansion());

        Collection<BinaryInteraction> binaryInteractions = xml2tab.convert(entrySet);

        StringWriter sw = new StringWriter();

        PsimiTabWriter psimitabWriter = new PsimiTabWriter();
        psimitabWriter.write(binaryInteractions, sw);

        System.out.println(sw.toString());

        solrServerTestController.getIndexer().indexMitab(new ByteArrayInputStream(sw.toString().getBytes()), false);
    }

    private IntactEntry toIntactEntry(Publication publication) {
        Collection<Interaction> interactions = new ArrayList<Interaction>();

        for (Experiment exp : publication.getExperiments()) {
            interactions.addAll(exp.getInteractions());
        }

        return new IntactEntry(interactions);
    }

}
