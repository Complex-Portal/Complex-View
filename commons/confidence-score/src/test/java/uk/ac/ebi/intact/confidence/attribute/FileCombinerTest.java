/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.confidence.attribute;

import org.junit.*;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import uk.ac.ebi.intact.confidence.global.GlobalTestData;

/**
 * Test class for merging 2..* files.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since 1.6.0
 * <pre>19 Oct 2007</pre>
 */
public class FileCombinerTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link uk.ac.ebi.intact.confidence.attribute.FileCombiner#merge2(java.lang.String[], java.lang.String)}.
	 */
	@Test
	public final void testMerge2() throws Exception{
		FileCombiner fc=  new FileCombiner();
		String goPath = FileCombinerTest.class.getResource("set_go_attributes.txt").getPath();
		String ipPath = FileCombinerTest.class.getResource("set_ip_attributes.txt").getPath();
		String alignPath = FileCombinerTest.class.getResource("set_align_attributes.txt").getPath();
		String[] paths = { goPath, ipPath, alignPath };
		File outFile = new File( GlobalTestData.getInstance().getTargetDirectory(), "testMerge2.txt");
		fc.merge2(paths, outFile.getPath());
		assertTrue(outFile.exists());
		Assert.assertTrue(outFile.exists());
	}

    @Test
    @Ignore
    public void testOutOfMemeory() throws Exception {
       FileCombiner fc=  new FileCombiner();
		String goPath = "H:\\tmp\\ConfidenceModel\\McAttribs\\set_go_attributes.txt";
		String ipPath = "H:\\tmp\\ConfidenceModel\\McAttribs\\set_ip_attributes.txt";
		String alignPath = "H:\\tmp\\ConfidenceModel\\McAttribs\\set_align_attributes.txt";
		String[] paths = { goPath, ipPath, alignPath };
		File outFile = new File(GlobalTestData.getInstance().getTargetDirectory(), "testMergeOutOfMem2.txt");
	    fc.merge(paths, outFile.getPath());
		assertTrue(outFile.exists());
		Assert.assertTrue(outFile.exists()); 
    }


}
