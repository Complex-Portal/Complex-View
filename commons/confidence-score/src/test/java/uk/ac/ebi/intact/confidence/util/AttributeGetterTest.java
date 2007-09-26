/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.confidence.util;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.intact.bridges.blast.BlastServiceException;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.confidence.ProteinPair;
import uk.ac.ebi.intact.confidence.attribute.FileCombiner;
import uk.ac.ebi.intact.confidence.global.GlobalTestData;

/**
 * TODO comment this
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since <pre>5 Sep 2007</pre>
 */
public class AttributeGetterTest {

	private AttributeGetter aG;
	private File tmpDir;
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		tmpDir = new File (GlobalTestData.getInstance().getTargetDirectory().getPath(), "/AttributeGetterTest/");
		tmpDir.mkdir();
		File blastDir = new File(AttributeGetterTest.class.getResource("P43609.xml").getPath());
		String uniprotPath = AttributeGetterTest.class.getResource("uniprot_sprot_small.dat").getPath();
		//TODO: for eclipse: File blastArchive = new File("H:/blastXml/");
		// for unix:
		File blastArchive = new File("/homes/iarmean/blastXml/");
		String email = "iarmean@ebi.ac.uk";
		aG = new AttributeGetter(uniprotPath, GlobalTestData.getInstance().getBinaryInteractionSet(), blastDir.getParentFile(),blastArchive, email);		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link uk.ac.ebi.intact.confidence.util.AttributeGetter#writeGoAttributes(uk.ac.ebi.intact.confidence.ProteinPair, java.lang.String)}.
	 */
	@Test
	public final void testWriteGoAttributesProteinPairStringOneWithGO() {
		//the proteins should be also in the binaryInteractionSet
		ProteinPair pp = new ProteinPair("Q9W486", "P43609");
		String outPath =  tmpDir.getPath() + "/go_attrib_test_onlyOneWithGo.txt";
		aG.writeGoAttributes(pp, outPath);
	}
	
	@Test
	public final void testWriteGoAttributesProteinPairStringBothWithGO() {
		//the proteins should be also in the binaryInteractionSet
		ProteinPair pp = new ProteinPair("P43609","P12345");
		String outPath =  tmpDir.getPath() + "/go_attrib_test_bothWithGo.txt";
		aG.writeGoAttributes(pp, outPath);
	}
	
	/**
	 * Test method for {@link uk.ac.ebi.intact.confidence.util.AttributeGetter#writeGoAttributes(uk.ac.ebi.intact.confidence.BinaryInteractionSet, java.lang.String)}.
	 */
	@Test
	public final void testWriteGoAttributesBinaryInteractionSetString() {
		String outPath = tmpDir.getPath() + "/go_attrib_test_binarySet.txt";
		aG.writeGoAttributes(GlobalTestData.getInstance().getBinaryInteractionSet(),outPath);
	}

	/**
	 * Test method for {@link uk.ac.ebi.intact.confidence.util.AttributeGetter#writeIpAttributes(uk.ac.ebi.intact.confidence.ProteinPair, java.lang.String)}.
	 */
	@Test
	public final void testWriteIpAttributesProteinPairStringBothWithIp() {
		//the proteins should be also in the binaryInteractionSet
		ProteinPair pp = new ProteinPair("P43609","P12345");
		String outPath =  tmpDir.getPath() + "/ip_attrib_test.txt";
		aG.writeIpAttributes(pp, outPath);
	}

	/**
	 * Test method for {@link uk.ac.ebi.intact.confidence.util.AttributeGetter#writeIpAttributes(uk.ac.ebi.intact.confidence.BinaryInteractionSet, java.lang.String)}.
	 */
	@Test
	public final void testWriteIpAttributesBinaryInteractionSetString() {
		String outPath = tmpDir.getPath() + "/ip_attrib_test_binarySet.txt";
		aG.writeIpAttributes(GlobalTestData.getInstance().getBinaryInteractionSet(),outPath);
	}

	/**
	 * Test method for {@link uk.ac.ebi.intact.confidence.util.AttributeGetter#writeAlignmentAttributes(uk.ac.ebi.intact.confidence.ProteinPair, java.lang.String, java.util.HashSet)}.
	 * @throws BlastServiceException 
	 */
	@Test
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
		aG.writeAlignmentAttributes(pp, outPath, againstProt);
	}

	/**
	 * Test method for {@link uk.ac.ebi.intact.confidence.util.AttributeGetter#writeAlignmentAttributes(uk.ac.ebi.intact.confidence.BinaryInteractionSet, java.lang.String, java.util.HashSet)}.
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
		aG.writeAlignmentAttributes(GlobalTestData.getInstance().getBinaryInteractionSet(), outPath, againstProt);
	}

	/**
	 * Test method for {@link uk.ac.ebi.intact.confidence.util.AttributeGetter#merge(java.lang.String[], java.lang.String)}.
	 */
	@Test
	//TODO: retest it does not work ... only go and align?!? 
	public final void testMerge() {
		String outPath = tmpDir.getPath() + "/merge_test_binarySet.txt";		
		String pathGo= tmpDir.getPath() + "/go_attrib_test_binarySet.txt";
		String pathIp = tmpDir.getPath() + "/ip_attrib_test_binarySet.txt";
		String pathAlign = tmpDir.getPath() + "/align_attrib_test_binarySet.txt";
		String [] paths = {pathGo, pathIp, pathAlign};
		aG.merge(paths, outPath);

	}

	/**
	 * Test method for {@link uk.ac.ebi.intact.confidence.util.AttributeGetter#getAllAttribs(uk.ac.ebi.intact.confidence.BinaryInteractionSet, java.util.HashSet, java.lang.String)}.
	 * @throws BlastServiceException 
	 */
	@Test
	public final void testGetAllAttribs() throws BlastServiceException {
		String outPath = tmpDir.getPath() + "/getAllAtribs_test_binarySet.txt";
		Set<String> againstProt = new HashSet<String>();
		againstProt.add("A4K2P4"); // for non of the pp acs
		againstProt.add("Q75AM0"); // for P43609
		againstProt.add("Q6FPM5"); // for P43609
		againstProt.add("P08907"); // for P12345 - rabit :)
		againstProt.add("P00506"); // for P12345
		aG.getAllAttribs(GlobalTestData.getInstance().getBinaryInteractionSet(), againstProt, outPath);
	}

}
