package uk.ac.ebi.intact.editor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.stereotype.Controller;
import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.PsimiXmlReaderException;
import psidev.psi.mi.xml.model.EntrySet;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.lifecycle.LifecycleManager;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.core.persister.CorePersister;
import uk.ac.ebi.intact.core.unit.IntactMockBuilder;
import uk.ac.ebi.intact.dataexchange.cvutils.CvUpdater;
import uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.PsiExchange;
import uk.ac.ebi.intact.model.BioSource;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.Publication;
import uk.ac.ebi.intact.model.user.User;
import uk.ac.ebi.intact.model.util.CvObjectBuilder;

import java.io.IOException;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@DependsOn("intactInitializer")
public class DataPopulator implements InitializingBean {
    private static final Log log = LogFactory.getLog(DataPopulator.class);

    @Autowired
    private IntactContext intactContext;

    @Autowired
    private CorePersister corePersister;

    @Autowired
    private DaoFactory daoFactory;

    @Autowired
    private CvUpdater cvUpdater;

    @Autowired
    private LifecycleManager lifecycleManager;

    @Autowired
    private PsiExchange psiExchange;


    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("POPULATING DATA...");

        User admin = daoFactory.getUserDao().getByLogin("admin");
        intactContext.getUserContext().setUser(admin);

        cvUpdater.executeUpdateWithLatestCVs();

        IntactMockBuilder mockBuilder = new IntactMockBuilder(intactContext.getInstitution());

        createRandomPublication(mockBuilder);
        createBioSourceHuman(mockBuilder);

        User curator = mockBuilder.createCurator("curator", "CuratorName", "CuratorLast", "curator@example.com");
        curator.setPassword("103b9534772356f52e338307c9cf42294a3f28f7");

        corePersister.saveOrUpdate(curator);

        User reviewer = mockBuilder.createReviewer("reviewer", "ReviewerName", "ReviewerLast", "reviewer@example.com");
        reviewer.setPassword("0b7cec9c67d6e0cfa008efe01c74ab89b5c5513f");

        corePersister.saveOrUpdate(reviewer);

//        importXmlDataAs(curator);
    }

    private void createBioSourceHuman(IntactMockBuilder mockBuilder) {
        BioSource human = mockBuilder.createBioSource(9606, "human");
        corePersister.saveOrUpdate(human);
    }

    private void createRandomPublication(IntactMockBuilder mockBuilder) {
        Publication publicationRandom = mockBuilder.createPublicationRandom();
        lifecycleManager.getNewStatus().claimOwnership(publicationRandom);
        lifecycleManager.getAssignedStatus().startCuration(publicationRandom);
        corePersister.saveOrUpdate(publicationRandom);
    }

    private void importXmlDataAs(User user) throws IOException, PsimiXmlReaderException {
        if (log.isInfoEnabled()) log.info("Importing some XML data...");
        User currentUser = intactContext.getUserContext().getUser();

        intactContext.getUserContext().setUser(user);

        PsimiXmlReader reader = new PsimiXmlReader();

        storeEntrySet(reader.read(DatabasePopulator.class.getResourceAsStream("/META-INF/data/10514511.xml")));
        storeEntrySet(reader.read(DatabasePopulator.class.getResourceAsStream("/META-INF/data/11554746.xml")));

        intactContext.getUserContext().setUser(currentUser);
    }

    public void storeEntrySet(EntrySet entrySet) throws IOException {
        psiExchange.importIntoIntact(entrySet);
    }

}
