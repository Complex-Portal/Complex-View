/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.confidence.psimi;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Confidence;
import psidev.psi.mi.xml.converter.ConverterException;
import uk.ac.ebi.intact.bridges.blast.AbstractBlastService;
import uk.ac.ebi.intact.bridges.blast.BlastConfig;
import uk.ac.ebi.intact.bridges.blast.BlastServiceException;
import uk.ac.ebi.intact.bridges.blast.EbiWsWUBlast;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.confidence.global.GlobalTestData;
import uk.ac.ebi.intact.confidence.model.ConfidenceType;
import uk.ac.ebi.intact.confidence.utils.ParserUtils;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.psimitab.IntActBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntActColumnHandler;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Test class for appending confidence values to a PSI-MI TAB file.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        28-Nov-2007
 *        </pre>
 */
public class PsiMiTabConfidenceTest {

    @Test
    public void testSaveScores() throws Exception {
        String hcSetPath = PsiMiTabConfidenceTest.class.getResource( "hc_attributes.txt").getPath();
        String gisInput = PsiMiTabConfidenceTest.class.getResource( "gisModel.txt").getPath();
        File workDir =  GlobalTestData.getInstance().getTargetDirectory();

        String email ="myName@yahuo.com";
        File dbFolder = new File ( GlobalTestData.getTargetDirectory(),"dbFolder");
                      prepareDB( dbFolder, email , workDir );
        File blastArchiveDir = new File( PsiMiTabConfidenceTest.class.getResource( "P15626.xml" ).getPath()).getParentFile();

        BlastConfig config = new BlastConfig(email);
        config.setBlastArchiveDir( blastArchiveDir);
        config.setDatabaseDir( dbFolder);
        Set<UniprotAc> againstProt =  ParserUtils.parseProteins( new File(hcSetPath));
        File goaFile = new File( PsiMiTabConfidenceTest.class.getResource( "goaTest.txt" ).getPath());
        PsiMiTabConfidence psi = new PsiMiTabConfidence(new File(gisInput), config, againstProt, goaFile,  workDir );
        File inFile = new File( PsiMiTabConfidenceTest.class.getResource( "psimitab.in").getPath());
        File outFile = new File(workDir, "outPSImitab.txt");
        Set<ConfidenceType> confTypes = new HashSet<ConfidenceType>();
        confTypes.add( ConfidenceType.GO);
        confTypes.add( ConfidenceType.InterPRO);

        psi.appendConfidence( inFile, true, outFile, confTypes,true);
        checkOutput(inFile,outFile);
    }

    @Test
//    @Ignore
    public void testConstructor() throws Exception {
        String hcSetPath = PsiMiTabConfidenceTest.class.getResource( "hc_attributes.txt").getPath();
        String lcSetPath = PsiMiTabConfidenceTest.class.getResource( "lc_attributes.txt").getPath();
        File workDir =  GlobalTestData.getInstance().getTargetDirectory();

        String email = "myName@yahuo.com";
        File dbFolder = new File ( GlobalTestData.getTargetDirectory(),"dbFolder");
        prepareDB( dbFolder, email, workDir );

       File blastArchiveDir = new File( PsiMiTabConfidenceTest.class.getResource( "P15626.xml" ).getPath()).getParentFile();

        BlastConfig config = new BlastConfig(email);
        config.setBlastArchiveDir( blastArchiveDir);
        config.setDatabaseDir( dbFolder);
        File goaFile = new File(PsiMiTabConfidenceTest.class.getResource("goaTest.txt").getPath());
        PsiMiTabConfidence psi = new PsiMiTabConfidence(hcSetPath, lcSetPath, goaFile, workDir, config);
        File inFile = new File( PsiMiTabConfidenceTest.class.getResource( "psimitab.in").getPath());
        File outFile = new File(workDir, "outPSImitab.txt");
        Set<ConfidenceType> confTypes = new HashSet<ConfidenceType>();
        boolean confidenceGO = true;
        boolean confidenceInterPro = true;
        boolean confidenceAlign = false;
            if (confidenceGO){
                confTypes.add( ConfidenceType.GO);
            }
            if(confidenceInterPro){
                confTypes.add( ConfidenceType.InterPRO);
            }
            if(confidenceAlign){
                confTypes.add( ConfidenceType.Alignment);
            }

        psi.appendConfidence( inFile, true, outFile, confTypes, true);
        checkOutput(inFile,outFile);
    }


     private void prepareDB(File dbFolder, String email, File workDir) throws BlastServiceException {
        AbstractBlastService wsBlast = new EbiWsWUBlast( dbFolder, "job", workDir, email, 20);
        wsBlast.deleteJobsAll();
        wsBlast.importCsv(new File(PsiMiTabConfidenceTest.class.getResource("initDb.csv").getPath()));
    }

    private void checkOutput(File inFile, File outFile) {
        PsimiTabReader reader = new PsimiTabReader(true);
        reader.setBinaryInteractionClass( IntActBinaryInteraction.class);
        reader.setColumnHandler( new IntActColumnHandler());

        try {
            Collection<BinaryInteraction> psiMiBefore = reader.read(inFile);
            Collection<BinaryInteraction> psiMiAfter = reader.read( outFile);
            Assert.assertEquals( psiMiBefore.size(), psiMiAfter.size());

            for ( Iterator<BinaryInteraction> iterator = psiMiBefore.iterator(); iterator.hasNext(); ) {
                BinaryInteraction binaryInt =  iterator.next();
                List<Confidence> confVals = binaryInt.getConfidenceValues();
                Assert.assertNotNull( confVals);
                Assert.assertEquals( 0, confVals.size());
            }

            for ( Iterator<BinaryInteraction> iterator = psiMiAfter.iterator(); iterator.hasNext(); ) {
                BinaryInteraction binaryInt =  iterator.next();
                List<Confidence> confVals = binaryInt.getConfidenceValues();
                Assert.assertNotNull( confVals);
                Assert.assertEquals( 1, confVals.size());
                Confidence conf1 = confVals.get( 0);
                Assert.assertTrue(conf1.getType().equalsIgnoreCase( "intact confidence"));
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        } catch ( ConverterException e ) {
            e.printStackTrace();
        }
    }

//    @Test
//    public void testGetAttribs() throws Exception {
//        String hcSetPath = PsiMiTabConfidenceTest.class.getResource( "hc_attributes.txt").getPath();
//        String gisInput = PsiMiTabConfidenceTest.class.getResource( "gisModel.txt").getPath();
//        File workDir =  GlobalTestData.getInstance().getTargetDirectory();
//        File dbFolder = new File("H:\\tmp\\blastDb");
//        File blastArchiveDir = new File("E:\\20071016_iarmean");
//
//        BlastConfig config = new BlastConfig("myName@yahuo.com");
//        config.setBlastArchiveDir( blastArchiveDir);
//        config.setDatabaseDir( dbFolder);
//        Set<UniprotAc> againstProt =  ParserUtils.parseProteins( new File(hcSetPath));
//        PsiMiTabConfidence psi = new PsiMiTabConfidence(new File(gisInput), config, againstProt, workDir );
//    }

    @Test
    @Ignore
    public void runPsiMiTabAssigner() throws Exception {
        File inFile = new File("/net/nfs7/vol22/sp-pro5/20080401_iarmean/intact.txt");//"E:\\iarmean\\data\\intact.txt");
        File outFile = new File("/net/nfs7/vol22/sp-pro5/20080401_iarmean/intact_out.txt");



        File gisModel = new File("/net/nfs7/vol22/sp-pro5/20080401_iarmean/TrainModel/gisModel.txt");//"E:\\psimitabAssign\\gisModel.txt");
        File workDir =  new File("/net/nfs7/vol22/sp-pro5/20080401_iarmean/");

        File dbFolder = new File("/net/nfs7/vol22/sp-pro5/20080401_iarmean/blastDb");//"E:\\iarmean\\backupDb\\play");
        File blastArchiveDir = new File("/net/nfs7/vol22/sp-pro5/20080401_iarmean/archive");//"E:\\20071016_iarmean");
        BlastConfig config = new BlastConfig("myName@yahuo.com");
        config.setBlastArchiveDir( blastArchiveDir);
        config.setDatabaseDir( dbFolder);
        File goaFile = new File ("/net/nfs7/vol22/sp-pro5/20080401_iarmean/goaTrimmed.goa_intact");

        File hcSet = new File("/net/nfs7/vol22/sp-pro5/20080401_iarmean/TrainModel/highconf_set.txt");
        Set<UniprotAc> againstProteins = ParserUtils.parseProteins( hcSet );

        IntactContext.initStandaloneContext( new File( PsiMiTabConfidenceTest.class.getResource( "/hibernate.iweb2.cfg.xml" ).getFile() ) );
        PsiMiTabConfidence psimitab = new PsiMiTabConfidence( gisModel, config, againstProteins, goaFile, workDir);
        Set<ConfidenceType> confs = new HashSet<ConfidenceType>();
        confs.add( ConfidenceType.GO);
        confs.add ( ConfidenceType.InterPRO);
        confs.add( ConfidenceType.Alignment );
        psimitab.appendConfidence(  inFile, true,outFile, confs, true);

    }
}
