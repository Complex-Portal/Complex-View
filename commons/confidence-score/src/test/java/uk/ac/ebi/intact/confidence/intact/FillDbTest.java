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

import opennlp.maxent.GISModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import uk.ac.ebi.intact.bridges.blast.AbstractBlastService;
import uk.ac.ebi.intact.bridges.blast.BlastConfig;
import uk.ac.ebi.intact.bridges.blast.BlastServiceException;
import uk.ac.ebi.intact.bridges.blast.EbiWsWUBlast;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.business.IntactTransactionException;
import uk.ac.ebi.intact.confidence.global.GlobalTestData;
import uk.ac.ebi.intact.confidence.maxent.MaxentUtils;
import uk.ac.ebi.intact.confidence.maxent.OpenNLPMaxEntClassifier;
import uk.ac.ebi.intact.confidence.utils.ParserUtils;
import uk.ac.ebi.intact.core.persister.PersisterHelper;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.core.util.SchemaUtils;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Simulates the use of the IntactScoreCalculator on a database.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *               16-Jan-2008
 *               </pre>
 */
public class FillDbTest extends IntactBasicTestCase {

    private boolean init = false;

    private File workDir;
    private BlastConfig blastConfig;
    private File goaFile;
    private OpenNLPMaxEntClassifier classifier;
    private File gisModelFile;
    private File highconfFile;
    private Set<UniprotAc> againstProts;
    private CvConfidenceType cvConfidenceType;



    @Before
    public void setUp() throws Exception {

    }

    @Test
    @Ignore
    public void trainModel() throws Exception {
        File hcFile = new File( FillDbTest.class.getResource( "highconf_set_attributes.txt" ).getPath() );
        File lcFile = new File( FillDbTest.class.getResource( "lowconf_set_attributes.txt" ).getPath() );
        File workDir = new File( GlobalTestData.getTargetDirectory(), "FillDbTest" );
        workDir.mkdir();

        OpenNLPMaxEntClassifier cl = new OpenNLPMaxEntClassifier( hcFile.getPath(), lcFile.getPath(), workDir );
        GISModel model = cl.getModel();
        File outFile = new File( workDir, "model.txt" );
        MaxentUtils.writeModelToFile( model, outFile );
    }

    @Test
    public void fillUsingIntactConfidenceCalculator() throws Exception {
        init();
        IntactConfidenceCalculator ic = new IntactConfidenceCalculator( classifier, blastConfig, againstProts,goaFile, workDir );
        List<InteractionImpl> interactions = getDaoFactory().getInteractionDao().getAll();
        boolean override = false;
        ic.setConfidenceType( cvConfidenceType );
        ic.calculate( interactions, classifier,  override );

        for ( Iterator<InteractionImpl> iter = interactions.iterator(); iter.hasNext(); ) {
            InteractionImpl interaction = iter.next();
//            Assert.assertEquals( 1, interaction.getConfidences().size() );
            System.out.println( "interaction: " + interaction.toString() );
            String expected = "0.50";
            if ( interaction.getShortLabel().equalsIgnoreCase( "int-high" ) ) {
                expected = "0.82";
            } else if ( interaction.getShortLabel().equalsIgnoreCase( "int-low" ) ) {
                expected = "0.20";
                expected = "0.50";
            }
            if ( interaction.getShortLabel().startsWith( "int-unk" ) ) {
                Assert.assertNotNull( interaction.getConfidences().iterator().next().getValue() );
            } else if(interaction.getShortLabel().equalsIgnoreCase( "int-high" )){
                //obs: the value for high was oscillating between 0.82 and 0.83
                Assert.assertTrue( interaction.getConfidences().iterator().next().getValue().startsWith( "0.8" ));
            } else {
                Assert.assertEquals(expected, interaction.getConfidences().iterator().next().getValue() );
            }
        }

        // persist to db
        getDataContext().beginTransaction();
        CvConfidenceType cvConfidenceType = ( CvConfidenceType ) getDaoFactory().getCvObjectDao().getByShortLabel( "intact confidence" );

        for ( Iterator<InteractionImpl> iter = interactions.iterator(); iter.hasNext(); ) {
            InteractionImpl interaction = iter.next();

            Confidence confidence = interaction.getConfidences().iterator().next();
            confidence.setCvConfidenceType( cvConfidenceType );

            getDaoFactory().getInteractionDao().update( interaction );
            getDaoFactory().getConfidenceDao().persist( confidence );
        }
        getDataContext().commitTransaction();

        checkConfidences();
    }

    @Test
    public void fillUsingFillDb() throws Exception {
        init();
        FillDb fill = new FillDb(workDir, blastConfig, highconfFile, gisModelFile, goaFile);
        fill.fillDb( true );

        checkConfidences();
    }


    private void init() throws IntactTransactionException, IOException, BlastServiceException {
        if (init){
            return;
        }

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

        Interaction interaction1 = getMockBuilder().createInteraction( "int-high", prot1, prot2,
                                                                       getMockBuilder().createDeterministicExperiment() );
        Interaction interaction2 = getMockBuilder().createInteraction( "int-low", prot3, prot4,
                                                                       getMockBuilder().createDeterministicExperiment() );
        Interaction interaction3 = getMockBuilder().createInteraction( "int-unk", prot5, prot6,
                                                                       getMockBuilder().createDeterministicExperiment() );

        CvConfidenceType conftype = CvObjectUtils.createCvObject( getIntactContext().getInstitution(), CvConfidenceType.class, "IA:9974", "intact confidence" );
        cvConfidenceType = conftype;
        // interaction with a confidence  "intact confidence" already in
        Interaction interaction4 = getMockBuilder().createInteraction( "int-unk2", prot1, prot3, getMockBuilder().createDeterministicExperiment() );
        interaction4.addConfidence( getMockBuilder().createConfidence( conftype, "0.8" ));

        // interaction with a different confidence than the "intact confidence"
        Interaction interaction5 = getMockBuilder().createInteraction( "int-unk3", prot4, prot5, getMockBuilder().createDeterministicExperiment() );
        CvConfidenceType blatype = CvObjectUtils.createCvObject( getIntactContext().getInstitution(), CvConfidenceType.class, "IA:9977", "intact blabla" );
        interaction5.addConfidence( getMockBuilder().createConfidence( blatype, "low" ));

        // interaction with 2 confidence objects "intact confidence"
        Interaction interaction6 = getMockBuilder().createInteraction( "int-unk2", prot2, prot6, getMockBuilder().createDeterministicExperiment() );
        interaction6.addConfidence( getMockBuilder().createConfidence( conftype, "0.7" ));
        interaction6.addConfidence( getMockBuilder().createConfidence( conftype, "0.8" ));

        PersisterHelper.saveOrUpdate( interaction1, interaction2, interaction3, interaction4, interaction5, interaction6, conftype, blatype );
        List<InteractionImpl> interactions = getDaoFactory().getInteractionDao().getAll();
        Assert.assertEquals( 6, interactions.size() );


        initFolders();


        init= true;
    }

    private void initFolders() throws BlastServiceException, IOException {
        File dbFolder = new File( GlobalTestData.getTargetDirectory(), "DbFolder" );
        dbFolder.mkdir();
        workDir = GlobalTestData.getTargetDirectory();
        prepareDB( dbFolder, "myName@yahuo.com", workDir );

        gisModelFile = new File( FillDbTest.class.getResource( "model.txt" ).getPath() );
        blastConfig = new BlastConfig( "myName@yahuo.com" );
        // need a blast Archive
        File archive = new File( FillDbTest.class.getResource( "Q16643.xml" ).getPath() ).getParentFile();
        blastConfig.setBlastArchiveDir( archive );
        // db dir
        blastConfig.setDatabaseDir( dbFolder );
        classifier = new OpenNLPMaxEntClassifier( gisModelFile );

        highconfFile = new File( FillDbTest.class.getResource( "highconf_set.txt" ).getPath() );
        againstProts = ParserUtils.parseProteins( highconfFile );

        goaFile = new File( FillDbTest.class.getResource( "goaTest.txt" ).getPath());
    }

    private void prepareDB( File dbFolder, String email, File workDir ) throws BlastServiceException {
        AbstractBlastService wsBlast = new EbiWsWUBlast( dbFolder, "job", workDir, email, 20 );
        wsBlast.deleteJobsAll();
        wsBlast.importCsv( new File( FillDbTest.class.getResource( "initDb.csv" ).getPath() ) );
    }

    private void checkConfidences(){
        for ( Iterator<InteractionImpl> iter = getDaoFactory().getInteractionDao().getAll().iterator(); iter.hasNext(); ){
            InteractionImpl interaction = iter.next();
            String expected = "0.50";
            if ( interaction.getShortLabel().equalsIgnoreCase( "int-high" ) ) {
                expected = "0.82";
            } else if ( interaction.getShortLabel().equalsIgnoreCase( "int-low" ) ) {
                expected = "0.20";
                expected ="0.50";
            }
            if ( interaction.getShortLabel().startsWith( "int-unk" ) ) {
                Assert.assertNotNull( interaction.getConfidences().iterator().next().getValue() );
            } else if (interaction.getShortLabel().equalsIgnoreCase( "int-high" )){
                //obs: the score was oscillating between 0.82, 0.83 this is why this special assertion was introduced
                Assert.assertTrue(interaction.getConfidences().iterator().next().getValue().startsWith( "0.8" ));
            } else {
                Assert.assertEquals(expected, interaction.getConfidences().iterator().next().getValue() );
            }
        }
    }

}
