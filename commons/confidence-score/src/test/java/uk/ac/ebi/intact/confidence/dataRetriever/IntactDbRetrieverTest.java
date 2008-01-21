/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */
package uk.ac.ebi.intact.confidence.dataRetriever;

import org.junit.*;
import uk.ac.ebi.intact.confidence.ProteinPair;
import uk.ac.ebi.intact.confidence.expansion.SpokeExpansion;
import uk.ac.ebi.intact.confidence.global.GlobalTestData;
import uk.ac.ebi.intact.confidence.model.*;
import uk.ac.ebi.intact.confidence.model.io.ProteinAnnotationReader;
import uk.ac.ebi.intact.confidence.model.io.impl.ProteinAnnotationReaderImpl;
import uk.ac.ebi.intact.confidence.util.DataMethods;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.core.unit.mock.MockIntactContext;
import uk.ac.ebi.intact.core.unit.mock.MockInteractionDao;
import uk.ac.ebi.intact.core.persister.PersisterHelper;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.persistence.dao.CvObjectDao;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Tests the IntActDb class.
 * 
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since  1.6.0
 * 
 * <pre>
 * 17 Aug 2007
 * </pre>
 */

public class IntactDbRetrieverTest extends IntactBasicTestCase {

	private Set<String>		acs;
	private IntactDbRetriever	intactdb;
	private File				testDir;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
        //acs = new HashSet<String>( Arrays.asList( "EBI-987097", "EBI-446104", "EBI-79835", "EBI-297231", "EBI-1034130" ) );
        Protein p1 = getMockBuilder().createProtein( "P12345", "prot1" );
        Protein p2 = getMockBuilder().createProtein("P12346", "prot2");
        Protein p3 = getMockBuilder().createProtein( "P12347", "prot3" );
        Protein p4 = getMockBuilder().createProtein("P12348", "prot4");
        Protein p5 = getMockBuilder().createProtein( "P12349", "prot5" );
        Protein p6 = getMockBuilder().createProtein("P12340", "prot6");
        Experiment expr = getMockBuilder().createDeterministicExperiment();
        Interaction interaction1 = getMockBuilder().createInteraction( "interaction1",p1, p2, expr);
        Interaction interaction2 = getMockBuilder().createInteraction( "interaction2",p2, p3, expr);
        Interaction interaction3 = getMockBuilder().createInteraction( "interaction3",p3, p4, expr);
        Interaction interaction4 = getMockBuilder().createInteraction( "interaction4",p4, p5, expr);
        Interaction interaction5 = getMockBuilder().createInteraction( "interaction5",p5, p6, expr);

        CvDatabase goDb = getMockBuilder().createCvObject( CvDatabase.class, CvDatabase.GO_MI_REF, CvDatabase.GO);
        CvDatabase ipDb = getMockBuilder().createCvObject(CvDatabase.class, CvDatabase.INTERPRO_MI_REF, CvDatabase.INTERPRO);
        PersisterHelper.saveOrUpdate( goDb, ipDb );
        PersisterHelper.saveOrUpdate( interaction1, interaction2, interaction3, interaction4, interaction5);
        CvObjectDao<CvDatabase> cvDatabase = getDaoFactory().getCvObjectDao( CvDatabase.class );
        CvDatabase cvGo = cvDatabase.getByPsiMiRef( CvDatabase.GO_MI_REF );
         Assert.assertNotNull( cvGo );
        //TODO: add also GO, InterPro annotations to the interactions in db


        acs = new HashSet<String>();
        List<InteractionImpl> interactions = getDaoFactory().getInteractionDao().getAll();
        for ( Iterator<InteractionImpl> iter = interactions.iterator(); iter.hasNext(); ) {
            InteractionImpl interaction =  iter.next();
            acs.add(interaction.getAc());
        }
        testDir = GlobalTestData.getInstance().getTargetDirectory();
     
        intactdb = new IntactDbRetriever(testDir, new SpokeExpansion(),getDaoFactory());
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}


    @Test
    @Ignore
    public void readCuratedComplexes() throws Exception {
        InteractionSimplified intS = intactdb.read( new IntActIdentifierImpl("EBI-709481 ") );
        Assert.assertNotNull( intS );
        Assert.assertEquals(2, intS.getComponents().size());
    }

    /**
	 * 
	 */
	@Test
	public final void testRead() {
        List<InteractionSimplified> interactions = intactdb.read(acs);
		Assert.assertEquals(acs.size(), interactions.size());
        for (int i = 0; i <  interactions.size() ; i++){
            Assert.assertTrue(acs.contains( interactions.get( i).getAc()));
        }
	}

	@Test
	public final void testReadConfidenceSets() throws DataRetrieverException {
        File file = new File(testDir.getPath(), "medconf_test.txt");
        if (file.exists()){
            file.delete();
        }
        try {

			FileWriter fw = new FileWriter(file);
			long start = System.currentTimeMillis();
			intactdb.setDbNrForTest(true);
			intactdb.retrieveMediumConfidenceSet(fw);
			fw.close();
			long end = System.currentTimeMillis();
			long time = end - start;
			System.out.println("time for intact retrieve in milis: " + time);

            int nrLines = checkOutput(file);
            Assert.assertEquals( acs.size(), nrLines );

            FileWriter fw2 = new FileWriter(new File(testDir.getPath(), "time.txt"));
			fw2.write("time in milis: " + time);
			fw2.close();
		} catch (FileNotFoundException e1) {
			throw new DataRetrieverException( e1);
		} catch (IOException e) {
			throw new DataRetrieverException( e);
		}

		DataMethods dm = new DataMethods();
		List<ProteinPair> interactionsMC = dm.readImportProteinPairs(file);
		try {
			dm.export(interactionsMC, new FileWriter(new File(testDir.getPath(), "mc_test.txt")));
		} catch (IOException e) {
			throw new DataRetrieverException( e);
		}
	}

    @Test
    public void testReadConfidencesFileFile() throws Exception {
        File hcFile = new File( GlobalTestData.getTargetDirectory(), "hcFileTest.txt");
        File mcFile = new File( GlobalTestData.getTargetDirectory(), "mcFileTest.txt");
        intactdb.setDbNrForTest( true);
        Report report = new Report(hcFile, mcFile);
        intactdb.readConfidences(report);

        // checking only by line counting
        if (hcFile.exists()){
            checkFile(hcFile);
        }
        checkFile(mcFile);
    }

    private void checkFile( File file ) throws Exception{
       int nrLines = checkOutput(file);
        File goFile = new File( intactdb.fileName( file ) + "_go.txt" );
        int nrGo = checkAnnoFile(goFile);
        Assert.assertEquals( 2* nrLines, nrGo );

        File ipFile = new File( intactdb.fileName( file ) + "_ip.txt" );
        int nrIp = checkAnnoFile(ipFile);
        Assert.assertEquals( nrGo, nrIp );

        File seqFile = new File( intactdb.fileName( file ) + "_seq.txt" );
        int nrSeq = checkSeqFile(seqFile);
        Assert.assertEquals( 2 * nrGo, nrSeq );
    }

    private int checkSeqFile( File seqFile ) throws Exception {
        int nr =0;
            String regex1 = "^>.{6}/|.*";
            String regex2 = "[a-zA-Z]*";
            BufferedReader br = new BufferedReader(new FileReader(seqFile));
            String line = "";
            while((line =br.readLine()) != null ){
                if( nr %2 == 0){
                    Assert.assertTrue(Pattern.matches(regex1, line));
                } else {
                    Assert.assertTrue(Pattern.matches(regex2, line)); 
                }
                nr ++;
            }

        return nr;
    }

    private int checkAnnoFile( File goFile ) throws IOException {
        ProteinAnnotationReader par = new ProteinAnnotationReaderImpl();
        List<ProteinAnnotation> pas = par.read( goFile );
        return pas.size();
    }


    private int checkOutput( File file ) throws IOException{
        int nr = 0;
        String regex = ".*;.*";
        BufferedReader br = new BufferedReader( new FileReader( file ) );
        String line = "";

        while ( ( line = br.readLine() ) != null ) {
            Assert.assertTrue( Pattern.matches( regex, line ) );
            nr++;
        }

        return nr;
    }
}
