/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */
package uk.ac.ebi.intact.confidence.attribute;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.intact.confidence.BinaryInteractionSet;
import uk.ac.ebi.intact.confidence.global.GlobalTestData;
import uk.ac.ebi.intact.confidence.util.AttributeGetterTest;

/**
 * TODO comment this ... someday
 * 
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since
 * 
 * <pre>
 * 28 Sep 2007
 * </pre>
 */
public class AnnotationFileMakerTest {

	private AnnotationFileMaker	afm;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		BinaryInteractionSet biS = new BinaryInteractionSet(AnnotationFileMakerTest.class.getResource("binaryInt.txt")
				.getPath());
		String uniprotPath = AttributeGetterTest.class.getResource("uniprot_sprot_small.dat").getPath();
		afm = new AnnotationFileMaker(biS, uniprotPath);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for
	 * {@link uk.ac.ebi.intact.confidence.attribute.AnnotationFileMaker#writeGoAnnotation(java.lang.String)}.
	 */
	@Test
	public final void testWriteGoAnnotationString() {
		// TODO: only for eclipse test
		try {
			File outFile = new File (GlobalTestData.getInstance().getTargetDirectory(), "testAnnotationFileMakerGo.txt");
			afm.writeGoAnnotation(outFile.getPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
