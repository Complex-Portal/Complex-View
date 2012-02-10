package uk.ac.ebi.intact.editor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obo.dataadapter.OBOParseException;
import org.obo.datamodel.OBOSession;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.transaction.TransactionStatus;
import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.PsimiXmlReaderException;
import psidev.psi.mi.xml.model.EntrySet;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.lifecycle.LifecycleManager;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.core.persister.CorePersister;
import uk.ac.ebi.intact.core.unit.IntactMockBuilder;
import uk.ac.ebi.intact.dataexchange.cvutils.CvUpdater;
import uk.ac.ebi.intact.dataexchange.cvutils.OboUtils;
import uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.PsiExchange;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.user.User;
import uk.ac.ebi.intact.model.util.PublicationUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class DataPopulator {
    private static final Log log = LogFactory.getLog(DataPopulator.class);

    private IntactContext intactContext;

    private CorePersister corePersister;

    private DaoFactory daoFactory;

    private CvUpdater cvUpdater;

    private LifecycleManager lifecycleManager;

    private PsiExchange psiExchange;


    public DataPopulator(IntactContext intactContext) {
        this.intactContext = intactContext;
        this.corePersister = intactContext.getCorePersister();
        this.daoFactory = intactContext.getDaoFactory();
        this.cvUpdater = (CvUpdater) intactContext.getSpringContext().getBean("cvUpdater");
        this.lifecycleManager = intactContext.getLifecycleManager();
        this.psiExchange = (PsiExchange) intactContext.getSpringContext().getBean("psiExchange");
    }

    public static void main(String[] args) throws Exception {
        IntactContext.initContext(new String[]{"classpath*:/META-INF/intact-batch.spring.xml",
                "classpath*:/META-INF/editor-test.spring.xml",
                "classpath*:/META-INF/editor.jpa-test.spring.xml"});

        final TransactionStatus transactionStatus = IntactContext.getCurrentInstance().getDataContext().beginTransaction();

        DataPopulator dataPopulator = new DataPopulator(IntactContext.getCurrentInstance());
        dataPopulator.populateTestData();

        IntactContext.getCurrentInstance().getDataContext().commitTransaction(transactionStatus);

        IntactContext.getCurrentInstance().destroy();
    }

    public void populateTestData() throws Exception {
        log.info("POPULATING TEST DATA...");

        User admin = daoFactory.getUserDao().getByLogin("admin");
        intactContext.getUserContext().setUser(admin);

        createCVsFromLocalOboFile();

        IntactMockBuilder mockBuilder = new IntactMockBuilder(intactContext.getInstitution());

        createBioSourceHuman(mockBuilder);
        createPublication(createPublication_Ren2011_simplified(mockBuilder));
        createPublicationWithManyInteractions(mockBuilder, 55);
        createPublication(createPublicationMutable(mockBuilder));

        User curator = mockBuilder.createCurator("curator", "CuratorName", "CuratorLast", "curator@example.com");
        curator.setPassword("103b9534772356f52e338307c9cf42294a3f28f7");
        corePersister.saveOrUpdate(curator);

        User reviewer = mockBuilder.createReviewer("reviewer", "ReviewerName", "ReviewerLast", "reviewer@example.com");
        reviewer.setPassword("0b7cec9c67d6e0cfa008efe01c74ab89b5c5513f");
        corePersister.saveOrUpdate(reviewer);

//        importXmlDataAs(curator);
    }

    private Publication createPublicationMutable(IntactMockBuilder mockBuilder) {
        final Publication publication = mockBuilder.createPublication("1234567");
        publication.getExperiments().clear();

        final Experiment experimentEmpty = mockBuilder.createExperimentEmpty("mutable-2012-1");

        experimentEmpty.setPublication(publication);
        publication.addExperiment(experimentEmpty);

        return publication;
    }

    private void createPublicationWithManyInteractions(IntactMockBuilder mockBuilder, int numberOfInteractions) {
        Experiment experiment = mockBuilder.createExperimentRandom("bigexp-2012-1", numberOfInteractions);
        createPublication(experiment.getPublication());
    }

    private void createCVsFromLocalOboFile() throws IOException, OBOParseException {
        OBOSession oboSession = OboUtils.createOBOSession(DataPopulator.class.getResource("/META-INF/psi-mi.obo"));
        cvUpdater.executeUpdate(oboSession);
    }

    private void createBioSourceHuman(IntactMockBuilder mockBuilder) {
        BioSource human = mockBuilder.createBioSource(9606, "human");
        corePersister.saveOrUpdate(human);
    }

    private void createPublication(Publication publication) {
        lifecycleManager.getNewStatus().claimOwnership(publication);
        lifecycleManager.getAssignedStatus().startCuration(publication);
        corePersister.saveOrUpdate(publication);
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

    private Publication createPublication_Ren2011_simplified(IntactMockBuilder mockBuilder) {
        Publication publication = mockBuilder.createPublication("21386897");
//        publication.setFullName();

        Experiment experiment = new Experiment(mockBuilder.getInstitution(), "ren-2011-1", mockBuilder.createBioSourceRandom());
        experiment.setCvIdentification(mockBuilder.createCvObject(CvIdentification.class, "MI:0102", "sequence tag"));
        experiment.setCvInteraction(mockBuilder.createCvObject(CvInteraction.class, "MI:0676", "tap"));

        BioSource schpo = mockBuilder.createBioSource(284812, "schpo");

        Interaction interaction1 = mockBuilder.createInteraction(
                mockBuilder.createComponentBait(mockBuilder.createProtein("Q09685", "DRE4_SCHPO", schpo)),
                mockBuilder.createComponentPrey(mockBuilder.createProtein("Q09882", "PRP45_SCHPO", schpo)),
                mockBuilder.createComponentPrey(mockBuilder.createProtein("Q9USM4", "LUC7_SCHPO", schpo)));

        addFigureLegend(mockBuilder, interaction1, "Fig. 3");
        
        Component c1 = interaction1.getComponents().iterator().next();
        c1.getFeatures().clear();

        Feature f1 = mockBuilder.createFeature("region", mockBuilder.createCvObject(CvFeatureType.class, CvFeatureType.EXPERIMENTAL_FEATURE_MI_REF, CvFeatureType.EXPERIMENTAL_FEATURE));
        f1.addRange(mockBuilder.createRangeUndetermined());

        c1.addFeature(f1);

        Interaction interaction2 = mockBuilder.createInteraction(
                mockBuilder.createComponentEnzyme(mockBuilder.createProtein("O59706", "SAP61_SCHPO", schpo)),
                mockBuilder.createComponentEnzymeTarget(mockBuilder.createProtein("O43071", "PRP17_SCHPO", schpo)));

        addFigureLegend(mockBuilder, interaction2, "Fig 3");
        addComment(mockBuilder, interaction2, "This interaction is very nice");

        Interaction interaction3 = mockBuilder.createInteraction(
                mockBuilder.createComponentNeutral(mockBuilder.createProtein("O59734", "RUXF_SCHPO", schpo)),
                mockBuilder.createComponentNeutral(mockBuilder.createProtein("O13615", "PRP46_SCHPO", schpo)),
                mockBuilder.createComponentNeutral(mockBuilder.createProtein("O14011", "CWF8_SCHPO", schpo)));

        List<Component> components3 = new ArrayList<Component>(interaction3.getComponents());

        Component c2 = components3.get(0);
        c2.getFeatures().clear();

        Feature f2 = mockBuilder.createFeature("mut", mockBuilder.createCvObject(CvFeatureType.class, CvFeatureType.MUTATION_INCREASING_MI_REF, CvFeatureType.MUTATION_INCREASING));
        f2.addRange(mockBuilder.createRange(5, 5, 6, 6));

        Feature f3 = mockBuilder.createFeature("region", mockBuilder.createCvObject(CvFeatureType.class, CvFeatureType.EXPERIMENTAL_FEATURE_MI_REF, CvFeatureType.EXPERIMENTAL_FEATURE));
        f3.addRange(mockBuilder.createRange(1, 1, 4, 4));

        c2.addFeature(f2);
        c2.addFeature(f3);

        components3.get(1).getFeatures().clear();
        
        Component c3 = components3.get(2);
        c3.getFeatures().clear();

        Feature f4 = mockBuilder.createFeature("region", mockBuilder.createCvObject(CvFeatureType.class, CvFeatureType.EXPERIMENTAL_FEATURE_MI_REF, CvFeatureType.EXPERIMENTAL_FEATURE));
        Range rangeUndetermined = mockBuilder.createRangeUndetermined();
        f4.addRange(rangeUndetermined);

        f3.setBoundDomain(f4);
        f4.setBoundDomain(f3);

        c3.addFeature(f4);

        interaction1.getExperiments().clear();
        interaction2.getExperiments().clear();
        interaction3.getExperiments().clear();

        experiment.addInteraction(interaction1);
        experiment.addInteraction(interaction2);
        experiment.addInteraction(interaction3);

        publication.addExperiment(experiment);
        experiment.setPublication(publication);

        return publication;
    }

    private void addComment(IntactMockBuilder mockBuilder, Interaction interaction, String text) {
        interaction.addAnnotation(mockBuilder.createAnnotation(text, CvTopic.COMMENT_MI_REF, CvTopic.COMMENT));
    }

    private void addFigureLegend(IntactMockBuilder mockBuilder, Interaction interaction, String legend) {
        interaction.addAnnotation(mockBuilder.createAnnotation(legend, "MI:0599", "figure legend"));
    }

}
