/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.confidence.attribute;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.ebi.intact.confidence.BinaryInteractionSet;
import uk.ac.ebi.intact.confidence.ProteinPair;

/**
 * TODO comment this ... someday
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since <pre>28 Sep 2007</pre>
 */
public class AnnotationFileMakerTest {

	private AnnotationFileMaker afm;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		Collection<ProteinPair> proteinPairs = Arrays.asList(new ProteinPair("P32871","P27986"),
				new ProteinPair("P32871","P27986"), new ProteinPair("P25847","P25336"));
		BinaryInteractionSet biS = new BinaryInteractionSet(AnnotationFileMakerTest.class.getResource("binaryInt.txt").getPath());
		//TODO: only for eclipse test String uniprotPath = "E:\\tmp\\uniprot_sprot.dat";
		//TODO: for unix test 
		String uniprotPath ="/scratch/uniprot_sprot.dat";
		afm = new AnnotationFileMaker(biS, uniprotPath);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link uk.ac.ebi.intact.confidence.attribute.AnnotationFileMaker#writeGoAnnotation(java.lang.String)}.
	 */
	@Test
	@Ignore
	public final void testWriteGoAnnotationString() {
		//TODO: only for eclipse test
		try {
			afm.writeGoAnnotation("testAnnotationFileMakerGo.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
