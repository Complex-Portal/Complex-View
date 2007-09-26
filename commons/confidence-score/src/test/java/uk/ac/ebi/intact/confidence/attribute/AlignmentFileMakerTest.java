/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */
package uk.ac.ebi.intact.confidence.attribute;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.ebi.intact.bridges.blast.BlastService;
import uk.ac.ebi.intact.bridges.blast.BlastServiceException;
import uk.ac.ebi.intact.bridges.blast.EbiWsWUBlast;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.confidence.blastmapping.BlastMappingException;
import uk.ac.ebi.intact.confidence.blastmapping.BlastMappingReader;
import uk.ac.ebi.intact.confidence.blastmapping.jaxb.EBIApplicationResult;
import uk.ac.ebi.intact.confidence.blastmapping.jaxb.TAlignment;
import uk.ac.ebi.intact.confidence.blastmapping.jaxb.THit;

/**
 * TODO comment this
 * 
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since
 * 
 * <pre>
 * 24 Aug 2007
 * </pre>
 */
public class AlignmentFileMakerTest {

	private AlignmentFileMaker afm;
	private File	testDir;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		testDir = new File(AlignmentFileMakerTest.class.getResource("P12345.xml").getPath());
		testDir = testDir.getParentFile();
		String email = "iarmean@ebi.ac.uk";
		String tableName  = "job";
		File workDir  = new File("/homes/iarmean/blastXml");
		BlastService bs = new EbiWsWUBlast(tableName, workDir, email);//new File(testDir.getPath(), "/Blast/"), email);
		afm = new AlignmentFileMaker(new Float(0.001),testDir, bs);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {

	}

	/**
	 * Test method for
	 * {@link uk.ac.ebi.intact.confidence.attribute.AlignmentFileMaker#blast(java.util.Set, java.util.Set, java.io.Writer)}.
	 */
	@Test
	public final void testBlastSetSetWriter() {
		Set<UniprotAc> proteins = new HashSet<UniprotAc>(3);
		proteins.addAll(Arrays.asList(new UniprotAc("P43609"),new UniprotAc("P06730"),new UniprotAc("P12345")));
		Set<UniprotAc> againstProteins = new HashSet<UniprotAc>(7);
		againstProteins.addAll(Arrays.asList(new UniprotAc("A7A277"), new UniprotAc("Q75AM0"), 
				new UniprotAc("Q4R7N0"), new UniprotAc("P63074"), 
				new UniprotAc("P08907"), new UniprotAc("Q8IPY3"), new UniprotAc("Q75BC5")));
		try {
			afm.blast(proteins, againstProteins, new FileWriter(new File(testDir,"blastProteins.txt")));
		} catch (IOException e) {
			fail();
		} catch (BlastServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testExpectationValue(){
		BlastMappingReader	bmr = new BlastMappingReader();
		String fileName = "P12345.xml";
		try {
			EBIApplicationResult appResult = bmr.read(new File(testDir.getPath(), fileName));
			List<THit> hits = appResult.getSequenceSimilaritySearchResult().getHits().getHit();
			for (THit hit : hits) {
				String accession = hit.getAc();
				if (accession.equals("Q862R2")){
					List<TAlignment> alignments = hit.getAlignments().getAlignment();
					//TODO: change :takes the last alignment
					for (TAlignment align : alignments) {
						System.out.println(align.getExpectation());
					}
				}
			}
		} catch (BlastMappingException e) {
			fail();
		}
	}
}
