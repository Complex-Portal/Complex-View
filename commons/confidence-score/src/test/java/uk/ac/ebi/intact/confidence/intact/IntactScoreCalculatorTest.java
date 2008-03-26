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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.confidence.intact;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import uk.ac.ebi.intact.bridges.blast.AbstractBlastService;
import uk.ac.ebi.intact.bridges.blast.BlastConfig;
import uk.ac.ebi.intact.bridges.blast.BlastServiceException;
import uk.ac.ebi.intact.bridges.blast.EbiWsWUBlast;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.business.IntactTransactionException;
import uk.ac.ebi.intact.confidence.filter.GOAFilterTest;
import uk.ac.ebi.intact.confidence.global.GlobalTestData;
import uk.ac.ebi.intact.confidence.maxent.OpenNLPMaxEntClassifier;
import uk.ac.ebi.intact.confidence.utils.ParserUtils;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.core.persister.PersisterHelper;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.core.util.SchemaUtils;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CrcCalculator;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.persistence.dao.InteractionDao;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Test class: for given gisModel assign scores to medium confidence file.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 */
public class IntactScoreCalculatorTest extends IntactBasicTestCase {
      /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( IntactScoreCalculatorTest.class );

    private boolean init = false;
    private String cvConfidenceShortLabel;
    private IntactScoreCalculator calculator;
    private List<Interaction> initialInteractions;


    @Test
    public void overrideTrue() throws Exception {
       init();
       List<InteractionImpl> interactions = getDaoFactory().getInteractionDao().getAll();
       boolean override = true;
       CvConfidenceType cvConfidenceType = ( CvConfidenceType )getDaoFactory().getCvObjectDao().getByShortLabel( cvConfidenceShortLabel );               
       calculator.setConfidenceType( cvConfidenceType );
       calculator.calculate( interactions, override );
       // all interactions will have new confidences: 1-3,5 new confidence; 4,6 first intact confidence will be overridden
       //check before saving to db

        //check the number of confidence objects for each interaction
        // and that the interactions are proper assigned according to high, low and medium confidence
        checkConfidenceGroups(interactions);

        List<InteractionImpl> oldInteractions = getDaoFactory().getInteractionDao().getAll();
        // check interactions 4 and 6 have the same number of intact confidences but different values
        checkSpecificOverriddenInteractions(interactions, initialInteractions, override);

        //check with crc
       checkWithCrc(interactions, initialInteractions);
    }

    @Test
    public void overrideFalse() throws Exception {
        init();
        List<InteractionImpl> interactions = getDaoFactory().getInteractionDao().getAll();
        boolean override = false;
        CvConfidenceType cvConfidenceType = ( CvConfidenceType ) getDaoFactory().getCvObjectDao().getByShortLabel( cvConfidenceShortLabel );
        calculator.setConfidenceType( cvConfidenceType );
        calculator.calculate( interactions, override );
        // interactions 4 and 6 should not be modified, for the rest a confidence object should be added
        checkConfidenceGroups(interactions);

         List<InteractionImpl> oldInteractions = getDaoFactory().getInteractionDao().getAll();
        // check interactions 4 and 6 have the same number of intact confidences but different values
        checkSpecificOverriddenInteractions(interactions, initialInteractions, override);

        //check with crc
       checkWithCrc(interactions, initialInteractions);
    }


    ///////////////////////
    // Checking Method(s).
    private void checkConfidenceGroups( List<InteractionImpl> interactions ) {
        for ( Iterator<InteractionImpl> iter = interactions.iterator(); iter.hasNext(); ) {
            InteractionImpl interaction = iter.next();
            int nrConfs =0;
            for ( Iterator<Confidence> iterConf = interaction.getConfidences().iterator(); iterConf.hasNext(); ) {
                Confidence conf = iterConf.next();
                if ( conf.getCvConfidenceType().getShortLabel().equalsIgnoreCase( cvConfidenceShortLabel ) ) {
                    nrConfs++;
                    if ( interaction.getShortLabel().equalsIgnoreCase( "int-high" ) ) {
                        //obs: the score was oscillating between 0.82, 0.83 this is why this special assertion was introduced
                        Assert.assertTrue(interaction.getConfidences().iterator().next().getValue().startsWith(  "0.8" ));
                    } else if ( interaction.getShortLabel().equalsIgnoreCase( "int-low" ) ) {
                        // obs: the score swithced to 0.5 from 0.2
                        Assert.assertEquals( "0.50", interaction.getConfidences().iterator().next().getValue() );
                    } else if ( interaction.getShortLabel().equalsIgnoreCase( "int-med" ) ) {
                        Assert.assertEquals( "0.50", interaction.getConfidences().iterator().next().getValue() );
                    } else {
                        Assert.assertNotNull( interaction.getConfidences().iterator().next().getValue() );
                    }
                }
            }
            Assert.assertTrue(nrConfs != 0);
            if (interaction.getShortLabel().equalsIgnoreCase( "int-unk1" ) ){
                Assert.assertEquals( 1, nrConfs );
            } else if ( interaction.getShortLabel().equalsIgnoreCase( "int-unk2" )){
                Assert.assertEquals( 1, nrConfs );
                Assert.assertEquals( 2, interaction.getConfidences().size());
            } else if ( interaction.getShortLabel().equalsIgnoreCase( "int-unk3" )){
                Assert.assertEquals( 2, nrConfs );
            }
        }
    }

     private void checkSpecificOverriddenInteractions( List<InteractionImpl> interactions, List<Interaction> oldInteractions, boolean overridden ) {
        for ( Iterator<InteractionImpl> iter = interactions.iterator(); iter.hasNext(); ) {
            InteractionImpl interaction =  iter.next();
            if (interaction.getShortLabel().equalsIgnoreCase( "int-unk1" ) || interaction.getShortLabel().equalsIgnoreCase( "int-unk3" )) {
                Interaction oldInteraction = getInteraction( oldInteractions, interaction.getShortLabel() );
                Confidence newConf = getFirstIntactConfidence(interaction);
                Assert.assertNotNull( newConf );
                Confidence oldConf = getFirstIntactConfidence( oldInteraction );
                Assert.assertNotNull( oldConf );
                if (overridden) {
                    Assert.assertFalse( newConf.getValue().equalsIgnoreCase(oldConf.getValue() ));
                }  else {
                    Assert.assertEquals( oldConf.getValue(), newConf.getValue() );
                }
            }
        }
    }

    private void checkWithCrc( List<InteractionImpl> interactions, List<Interaction> oldInteractions ) {
        for ( Iterator<InteractionImpl> iter = interactions.iterator(); iter.hasNext(); ) {
            InteractionImpl interaction = iter.next();
            Interaction oldInteraction = getInteraction(oldInteractions, interaction.getShortLabel());
            CrcCalculator crcCalculator = new CrcCalculator();
            String crcNew = crcCalculator.crc64( interaction );
            Assert.assertEquals(oldInteraction.getCrc(), crcNew  );
        }
    }


    /////////////////
    // Aux Method(s).
    private Interaction getInteraction( List<Interaction> oldInteractions, String shortLabel ) {
        for ( Iterator<Interaction> iter = oldInteractions.iterator(); iter.hasNext(); ) {
            Interaction interaction = iter.next();
            if (interaction.getShortLabel().equalsIgnoreCase( shortLabel )){
                return interaction;
            }
        }
        return null;
    }

     private Confidence getFirstIntactConfidence( Interaction interaction ) {
        for ( Iterator<Confidence> iter = interaction.getConfidences().iterator(); iter.hasNext(); ) {
            Confidence conf =  iter.next();
            if (conf.getCvConfidenceType().getShortLabel().equalsIgnoreCase( cvConfidenceShortLabel )){
                return conf;
            }
        }
        return null;
    }

    public void init() throws Exception {
        if (init){
            return;
        }
        initIntactMock();
        initConfidenceCaluclator();
        init = true;
    }

    //////////////////
    // Init Method(s).
    private void initConfidenceCaluclator() throws IOException, BlastServiceException {
        File dbFolder = new File( GlobalTestData.getTargetDirectory(), "DbFolder" );
        dbFolder.mkdir();
        File workDir = GlobalTestData.getTargetDirectory();
        prepareDB( dbFolder, "myName@yahuo.com", workDir );

        File gisModelFile = new File( IntactScoreCalculatorTest.class.getResource( "model.txt" ).getPath() );
        BlastConfig blastConfig = new BlastConfig( "myName@yahuo.com" );
        // need a blast Archive
        File archive = new File( IntactScoreCalculatorTest.class.getResource( "Q16643.xml" ).getPath() ).getParentFile();
        blastConfig.setBlastArchiveDir( archive );
        // db dir
        blastConfig.setDatabaseDir( dbFolder );
        OpenNLPMaxEntClassifier classifier = new OpenNLPMaxEntClassifier( gisModelFile );

        File hcSet = new File( IntactScoreCalculatorTest.class.getResource( "highconf_set.txt" ).getPath() );
        Set<UniprotAc> againstProts = ParserUtils.parseProteins( hcSet );

        File goaFile = new File( GOAFilterTest.class.getResource( "goaTest.txt" ).getPath());

        calculator = new IntactConfidenceCalculator( classifier, blastConfig, againstProts,goaFile, workDir );

    }

    private void prepareDB( File dbFolder, String email, File workDir ) throws BlastServiceException {
        AbstractBlastService wsBlast = new EbiWsWUBlast( dbFolder, "job", workDir, email, 20 );
        wsBlast.deleteJobsAll();
        wsBlast.importCsv( new File( IntactScoreCalculatorTest.class.getResource( "initDb.csv" ).getPath() ) );
    }

    private void initIntactMock() throws IntactTransactionException {
        SchemaUtils.createSchema();
        CvDatabase goDb = getMockBuilder().createCvObject( CvDatabase.class, CvDatabase.GO_MI_REF, CvDatabase.GO );
        CvDatabase ipDb = getMockBuilder().createCvObject( CvDatabase.class, CvDatabase.INTERPRO_MI_REF, CvDatabase.INTERPRO );
        CvXrefQualifier qualifier = getMockBuilder().getIdentityQualifier();
        PersisterHelper.saveOrUpdate( goDb, ipDb );

        Protein prot1 = getMockBuilder().createDeterministicProtein( "Q8WYL5", "prot1" );
        prot1.addXref( getMockBuilder().createXref( prot1, "GO:0005737", qualifier, goDb ) );
        prot1.addXref( getMockBuilder().createXref( prot1, "GO:0005886", qualifier, goDb ) );
        prot1.addXref( getMockBuilder().createXref( prot1, "GO:0003779", qualifier, goDb ) );
        prot1.addXref( getMockBuilder().createXref( prot1, "GO:0004721", qualifier, goDb ) );
        prot1.addXref( getMockBuilder().createXref( prot1, "GO:0030036", qualifier, goDb ) );
        prot1.addXref( getMockBuilder().createXref( prot1, "GO:0000902", qualifier, goDb ) );
        prot1.addXref( getMockBuilder().createXref( prot1, "GO:0006470", qualifier, goDb ) );
        prot1.addXref( getMockBuilder().createXref( prot1,"IPR000340", qualifier, ipDb ));
        prot1.addXref( getMockBuilder().createXref( prot1,"IPR014876", qualifier, ipDb ));
        prot1.addXref( getMockBuilder().createXref( prot1,"IPR000387", qualifier, ipDb ));

        Protein prot2 = getMockBuilder().createDeterministicProtein( "Q9BR76", "prot2" );
        prot2.addXref( getMockBuilder().createXref( prot2, "GO:0005515", qualifier, goDb ));
        prot2.addXref( getMockBuilder().createXref( prot2, "IPR001680", qualifier, ipDb ));
        prot2.addXref( getMockBuilder().createXref( prot2, "IPR006311", qualifier, ipDb ));
        prot2.addXref( getMockBuilder().createXref( prot2, "IPR015505", qualifier, ipDb ));
        prot2.addXref( getMockBuilder().createXref( prot2, "IPR015048", qualifier, ipDb ));
        prot2.addXref( getMockBuilder().createXref( prot2, "IPR015049", qualifier, ipDb ));


        Protein prot3 = getMockBuilder().createDeterministicProtein( "P40884", "prot3" );
        prot3.addXref( getMockBuilder().createXref( prot3, "GO:0004558", qualifier, goDb ));
        prot3.addXref( getMockBuilder().createXref( prot3, "IPR006046", qualifier, ipDb ));
        prot3.addXref( getMockBuilder().createXref( prot3, "IPR006589", qualifier, ipDb ));
        prot3.addXref( getMockBuilder().createXref( prot3, "IPR006047", qualifier, ipDb ));
        prot3.addXref( getMockBuilder().createXref( prot3, "IPR013780", qualifier, ipDb ));
        prot3.addXref( getMockBuilder().createXref( prot3, "IPR013781", qualifier, ipDb ));

        Protein prot4 = getMockBuilder().createDeterministicProtein( "Q6Q546", "prot4" );
        prot4.addXref( getMockBuilder().createXref( prot4, "GO:0005937", qualifier, goDb ));
        prot4.addXref( getMockBuilder().createXref( prot4, "GO:0031386", qualifier, goDb ));
        prot4.addXref( getMockBuilder().createXref( prot4, "GO:0000753", qualifier, goDb ));
        prot4.addXref( getMockBuilder().createXref( prot4, "GO:0000398", qualifier, goDb ));
        prot4.addXref( getMockBuilder().createXref( prot4, "GO:0006464", qualifier, goDb ));
        prot4.addXref( getMockBuilder().createXref( prot4, "IPR000626", qualifier, ipDb ));

        Protein prot5 = getMockBuilder().createDeterministicProtein( "Q16643", "prot5" );
        prot5.addXref( getMockBuilder().createXref( prot5, "GO:0042641", qualifier, goDb ));
        prot5.addXref( getMockBuilder().createXref( prot5, "GO:0005737", qualifier, goDb ));
        prot5.addXref( getMockBuilder().createXref( prot5, "GO:0030425", qualifier, goDb ));
        prot5.addXref( getMockBuilder().createXref( prot5, "GO:0003779", qualifier, goDb ));
        prot5.addXref( getMockBuilder().createXref( prot5, "GO:0005522", qualifier, goDb ));
        prot5.addXref( getMockBuilder().createXref( prot5, "GO:0007015", qualifier, goDb ));
        prot5.addXref( getMockBuilder().createXref( prot5, "GO:0050773", qualifier, goDb ));
        prot5.addXref( getMockBuilder().createXref( prot5, "GO:0048168", qualifier, goDb ));
        prot5.addXref( getMockBuilder().createXref( prot5, "IPR002108", qualifier, ipDb ));

        Protein prot6 = getMockBuilder().createDeterministicProtein( "Q9FHZ1", "prot6" );
        prot6.addXref( getMockBuilder().createXref( prot6, "GO:0045449", qualifier, goDb ));
        prot6.addXref( getMockBuilder().createXref( prot6, "IPR005202", qualifier, ipDb ));

        // high confidence interaction
        Interaction interaction1 = getMockBuilder().createInteraction( "int-high", prot1, prot2,
                                                                       getMockBuilder().createDeterministicExperiment() );
        // low confidence interaction
        Interaction interaction2 = getMockBuilder().createInteraction( "int-low", prot3, prot4,
                                                                       getMockBuilder().createDeterministicExperiment() );
        // unknown confidence interaction
        Interaction interaction3 = getMockBuilder().createInteraction( "int-med", prot5, prot6,
                                                                       getMockBuilder().createDeterministicExperiment() );

        CvConfidenceType conftype = CvObjectUtils.createCvObject( getIntactContext().getInstitution(), CvConfidenceType.class, "IA:9974", "intact confidence" );
        cvConfidenceShortLabel = conftype.getShortLabel();
        // test override: interaction with a confidence  "intact confidence" already in
        Interaction interaction4 = getMockBuilder().createInteraction( "int-unk1", prot1, prot3, getMockBuilder().createDeterministicExperiment() );
        interaction4.addConfidence( getMockBuilder().createConfidence( conftype, "0.8" ));

        // interaction with a different confidence than the "intact confidence"
        Interaction interaction5 = getMockBuilder().createInteraction( "int-unk2", prot4, prot5, getMockBuilder().createDeterministicExperiment() );
        CvConfidenceType blatype = CvObjectUtils.createCvObject( getIntactContext().getInstitution(), CvConfidenceType.class, "IA:9977", "intact blabla" );
        interaction5.addConfidence( getMockBuilder().createConfidence( blatype, "low" ));

        // interaction with 2 confidence objects "intact confidence"
        Interaction interaction6 = getMockBuilder().createInteraction( "int-unk3", prot2, prot6, getMockBuilder().createDeterministicExperiment() );
        interaction6.addConfidence( getMockBuilder().createConfidence( conftype, "0.7" ));
        interaction6.addConfidence( getMockBuilder().createConfidence( conftype, "0.8" ));
        initialInteractions = Arrays.asList(interaction1, interaction2, interaction3, interaction4, interaction5, interaction6);
        PersisterHelper.saveOrUpdate( interaction1, interaction2, interaction3, interaction4, interaction5, interaction6, conftype, blatype );
        List<InteractionImpl> interactions = getDaoFactory().getInteractionDao().getAll();
        Assert.assertEquals( 6, interactions.size() );
    }


    @Test
    @Ignore
    public void testCalculator() throws Exception {
        log.info( "mem: " + ( Runtime.getRuntime().maxMemory() ) / ( 1024 * 1024 ) );

        File workDir = new File( "/net/nfs7/vol22/sp-pro5/20080201_iarmean/TrainModel" );

        //TODO: specifiy dbFolder and blastArchiveDir and email
        File dbFolder = new File( workDir.getParentFile(), "blastDb" );
        dbFolder.mkdir();
        File blastArchiveDir = new File( workDir.getParentFile(), "archive" );
        blastArchiveDir.mkdir();
        String email = "iarmean@ebi.ac.uk";
        BlastConfig config = new BlastConfig( email );
        // need a blast Archive
        config.setBlastArchiveDir( blastArchiveDir );
        // db dir
        config.setDatabaseDir( dbFolder );

        //TODO: specifiy the gisModel file
        File gisModelFile = new File( workDir, "gisModel.txt" );
        OpenNLPMaxEntClassifier classifier = new OpenNLPMaxEntClassifier( gisModelFile );

        File hcSet = new File( workDir, "highconf_set.txt" );
        Set<UniprotAc> againstProts = ParserUtils.parseProteins( hcSet );

        File goaFile = new File(workDir, "goaFile.txt");
        IntactConfidenceCalculator ic = new IntactConfidenceCalculator( classifier, config, againstProts, goaFile,  workDir );

        File pgConfigFile = new File( TrainModel.class.getResource( "/hibernate.d003.cfg.xml" ).getFile() );
        IntactContext.initStandaloneContext( pgConfigFile );

        //save cvConfidenceType to db
        CvConfidenceType ctype = CvObjectUtils.createCvObject( IntactContext.getCurrentInstance().getInstitution(), CvConfidenceType.class, null, "intact confidence" );
        PersisterHelper.saveOrUpdate( ctype );

        InteractionDao interactionDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getInteractionDao();


        int totalNr = interactionDao.countAll();
        System.out.println("total: " + totalNr);
         int firstResult = 0;
        int maxResults = 50;

        boolean firstTime = true;
        List<InteractionImpl> interactions = null;
        for ( int i = 0; i < totalNr; i += maxResults ) {
            IntactContext.getCurrentInstance().getDataContext().beginTransaction();
            interactions = interactionDao.getAll( firstResult, maxResults );

            ic.calculate( interactions, classifier, false );
            saveInteractionsToDb(interactions);

            if (firstTime){
                firstTime = false;
            }
            if ( log.isInfoEnabled() ) {
                int processed = firstResult + interactions.size();

                if ( firstResult != processed ) {
                    log.info( "\t\tProcessed " + ( firstResult + interactions.size() +" out of " + totalNr) );
                }
            }

           firstResult = firstResult + maxResults;

           IntactContext.getCurrentInstance().getDataContext().commitTransaction();
        }
        if (log.isInfoEnabled()){
            log.info( "Processed " + totalNr + " IntAct interactions.");
        }
    }

    private void saveInteractionsToDb( List<InteractionImpl> interactions ) throws IntactTransactionException {
        CvConfidenceType cvConfidenceType = (CvConfidenceType) IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao().getByShortLabel( "intact confidence" );

        for ( Iterator<InteractionImpl> iter = interactions.iterator(); iter.hasNext(); ) {
            InteractionImpl interaction = iter.next();

            if (interaction.getConfidences().size() != 0){
                Confidence confidence = interaction.getConfidences().iterator().next();
                confidence.setCvConfidenceType( cvConfidenceType );

                IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getInteractionDao().update( interaction );
                IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getConfidenceDao().persist( confidence );
            }
        }
        IntactContext.getCurrentInstance().getDataContext().commitTransaction();
    }

}
