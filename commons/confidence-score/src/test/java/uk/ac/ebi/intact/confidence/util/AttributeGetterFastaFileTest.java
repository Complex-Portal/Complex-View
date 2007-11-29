/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.confidence.util;

import org.junit.*;
import uk.ac.ebi.intact.bridges.blast.BlastServiceException;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.confidence.BinaryInteractionSet;
import uk.ac.ebi.intact.confidence.ProteinPair;
import uk.ac.ebi.intact.confidence.global.GlobalTestData;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


/**
 * TODO comment this
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since <pre>5 Sep 2007</pre>
 */
@Ignore
public class AttributeGetterFastaFileTest {

	private AttributeGetterFastaFile aG;
	private File tmpDir;
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		tmpDir = new File (GlobalTestData.getInstance().getTargetDirectory(), "/AttributeGetterFastaFileTest/");
		tmpDir.mkdir();
		File workDir = new File( AttributeGetterFastaFileTest.class.getResource("P43609.xml").getPath()).getParentFile();
		String uniprotPath = AttributeGetterFastaFileTest.class.getResource("uniprot_sprot_small.dat").getPath();
		File blastArchive = workDir;
		String email = "iarmean@ebi.ac.uk";
		int nr = 20;
		File dbFolder = new File(GlobalTestData.getInstance().getTargetDirectory(), "dbFolder");
		dbFolder.mkdir();
		aG = new AttributeGetterFastaFile(dbFolder, uniprotPath, GlobalTestData.getInstance().getBinaryInteractionSet(), workDir ,blastArchive, email, nr);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		aG.close();
		
	}

	/**
	 * Test method for {@link AttributeGetterFastaFile#writeGoAttributes(uk.ac.ebi.intact.confidence.ProteinPair, java.lang.String)}.
	 */
	@Test
	public final void testWriteGoAttributesProteinPairStringOneWithGO() {
		//the proteins should be also in the binaryInteractionSet
		ProteinPair pp = new ProteinPair("Q9W486", "P43609");
		String outPath =  tmpDir.getPath() + "/go_attrib_test_onlyOneWithGo.txt";
		aG.writeGoAttributes(pp, outPath);
        Assert.assertTrue(new File(outPath).exists());
    }
	
	@Test
	public final void testWriteGoAttributesProteinPairStringBothWithGO() {
		//the proteins should be also in the binaryInteractionSet
		ProteinPair pp = new ProteinPair("P43609","P12345");
		String outPath =  tmpDir.getPath() + "/go_attrib_test_bothWithGo.txt";
		aG.writeGoAttributes(pp, outPath);
        Assert.assertTrue(new File(outPath).exists());
    }
	
	/**
	 * Test method for {@link AttributeGetterFastaFile#writeGoAttributes(uk.ac.ebi.intact.confidence.BinaryInteractionSet, java.lang.String, java.io.File)}.
	 */
	@Test
	public final void testWriteGoAttributesBinaryInteractionSetString() {
		String outPath = tmpDir.getPath() + "/go_attrib_test_binarySet.txt";
		aG.writeGoAttributes(GlobalTestData.getInstance().getBinaryInteractionSet(),outPath, null);
        Assert.assertTrue(new File(outPath).exists());
    }

	/**
	 * Test method for {@link AttributeGetterFastaFile#writeIpAttributes(uk.ac.ebi.intact.confidence.ProteinPair, java.lang.String)}.
	 */
	@Test
	public final void testWriteIpAttributesProteinPairStringBothWithIp() {
		//the proteins should be also in the binaryInteractionSet
		ProteinPair pp = new ProteinPair("P43609","P12345");
		String outPath =  tmpDir.getPath() + "/ip_attrib_test.txt";
		aG.writeIpAttributes(pp, outPath);
        Assert.assertTrue(new File(outPath).exists());
    }

	/**
	 * Test method for {@link AttributeGetterFastaFile#writeIpAttributes(uk.ac.ebi.intact.confidence.BinaryInteractionSet, java.lang.String, java.io.File)}.
	 */
	@Test
	public final void testWriteIpAttributesBinaryInteractionSetString() {
		String outPath = tmpDir.getPath() + "/ip_attrib_test_binarySet.txt";
		aG.writeIpAttributes(GlobalTestData.getInstance().getBinaryInteractionSet(),outPath, null);
        Assert.assertTrue(new File(outPath).exists());
    }

	/**
	 * Test method for {@link AttributeGetterFastaFile#writeAlignmentAttributes(uk.ac.ebi.intact.confidence.ProteinPair, java.lang.String, java.util.Set, java.io.File)}.
	 * @throws BlastServiceException 
	 */
	@Test
    @Ignore
    public final void testWriteAlignmentAttributesProteinPairStringHashSetOfString() throws BlastServiceException {
		//the proteins should be also in the binaryInteractionSet
		ProteinPair pp = new ProteinPair("P43609","P12345");
		String outPath = tmpDir.getPath() + "/align_attrib_test_ProteinPair.txt";
		Set<UniprotAc> againstProt = new HashSet<UniprotAc>();
		againstProt.add(new UniprotAc("A4K2P4")); // for non of the pp acs
		againstProt.add(new UniprotAc("Q75AM0")); // for P43609
		againstProt.add(new UniprotAc("Q6FPM5")); // for P43609
		againstProt.add(new UniprotAc("P08907")); // for P12345 - rabit :)
		againstProt.add(new UniprotAc("P00506")); // for P12345
		aG.writeAlignmentAttributes(pp, outPath, againstProt, null);
        Assert.assertTrue(new File(outPath).exists());
    }

	/**
	 * Test method for {@link AttributeGetterFastaFile#writeAlignmentAttributes(uk.ac.ebi.intact.confidence.BinaryInteractionSet, java.lang.String, java.util.Set, java.io.File)}.
	 * @throws BlastServiceException 
	 */
	@Test
	public final void testWriteAlignmentAttributesBinaryInteractionSetStringHashSetOfString() throws BlastServiceException {
		String outPath = tmpDir.getPath() + "/align_attrib_test_binarySet.txt";
		Set<String> againstProt = new HashSet<String>();
		againstProt.add("A4K2P4"); // for non of the pp acs
		againstProt.add("Q75AM0"); // for P43609
		againstProt.add("Q6FPM5"); // for P43609
		againstProt.add("P08907"); // for P12345 - rabit :)
		againstProt.add("P00506"); // for P12345
		aG.writeAlignmentAttributes(GlobalTestData.getInstance().getBinaryInteractionSet(), outPath, againstProt, null);
        Assert.assertTrue(new File(outPath).exists());
    }

	/**
	 * Test method for {@link AttributeGetterFastaFile#merge(java.lang.String[], java.lang.String)}.
	 */
	@Test
	//TODO: retest it does not work ... only go and align?!? 
	public final void testMerge() throws IOException{
		String outPath = tmpDir.getPath() + "/merge_test_binarySet.txt";		
		String pathGo= tmpDir.getPath() + "/go_attrib_test_binarySet.txt";
		String pathIp = tmpDir.getPath() + "/ip_attrib_test_binarySet.txt";
		String pathAlign = tmpDir.getPath() + "/align_attrib_test_binarySet.txt";
		String [] paths = {pathGo, pathIp, pathAlign};
		aG.merge(paths, outPath);
        Assert.assertTrue( new File(outPath).exists());
    }

    @Test
    public void testMerge2() throws Exception {
        String outPath = tmpDir.getPath() + "/merge_test_uniprotIPGO.txt";		
		String pathGo= tmpDir.getPath() + "/go_attrib_test_binarySet.txt";
		String pathIp = tmpDir.getPath() + "/ip_attrib_test_binarySet.txt";
		String pathAlign = tmpDir.getPath() + "/align_attrib_test_binarySet.txt";
		String [] paths = {pathGo, pathIp, pathAlign};
		aG.merge(paths, outPath);
        Assert.assertTrue( new File(outPath).exists());
    }


    /**
	 * Test method for {@link AttributeGetterFastaFile#getAllAttribs(uk.ac.ebi.intact.confidence.BinaryInteractionSet, java.util.Set, java.lang.String, java.io.File)}.
	 * @throws BlastServiceException    : from the intact-blast module
	 */
	@Test
	public final void testGetAllAttribs() throws BlastServiceException, IOException {
		String outPath = tmpDir.getPath() + "/getAllAtribs_test_binarySet.txt";
		Set<String> againstProt = new HashSet<String>();
		againstProt.add("A4K2P4"); // for non of the pp acs
		againstProt.add("Q75AM0"); // for P43609
		againstProt.add("Q6FPM5"); // for P43609
		againstProt.add("P08907"); // for P12345 - rabit :)
		againstProt.add("P00506"); // for P12345
		aG.getAllAttribs(GlobalTestData.getInstance().getBinaryInteractionSet(), againstProt, outPath, null);
        Assert.assertTrue(new File(outPath).exists());
    }
	
	/**
	 * Test method for getting all attribs for a file of interactions
	 * @throws BlastServiceException    : from the intact-blast module
	 */
	@Test
	@Ignore
	public final void testGetAllAttrbisFile() throws BlastServiceException {
		BinaryInteractionSet biSet;
		try {
			biSet = new BinaryInteractionSet("E:\\tmp\\ConfidenceModel\\highconf_all.txt");
			File workDir = new File("E:\\tmp\\ConfidenceModel");
			File blastArchiveDir = new File("E:\\20071016_iarmean");
			String email = "iarmean@ebi.ac.uk";
			int nr =20;
			File dbFolder = new File(GlobalTestData.getInstance().getTargetDirectory().getParent(), "dbFolder");
			dbFolder.mkdir();
			AttributeGetterFastaFile aG = new AttributeGetterFastaFile(dbFolder, "E:\\tmp\\uniprot_sprot.dat", biSet, workDir,
					blastArchiveDir, email, nr);
			
			BinaryInteractionSet biSet2 = new BinaryInteractionSet(workDir.getPath() + "/medconf_all.txt");
			Set<String> againstProteins = biSet.getAllProtNames();
            String outPath= workDir.getPath() + "/medconf_all_attribs_test.txt";
            aG.getAllAttribs(biSet2, againstProteins, outPath, null);
			Assert.assertTrue(new File(outPath).exists());

        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
}
