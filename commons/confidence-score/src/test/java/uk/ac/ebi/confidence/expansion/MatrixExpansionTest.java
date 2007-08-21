/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.confidence.expansion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.intact.confidence.expansion.ExpansionStrategy;
import uk.ac.ebi.intact.confidence.expansion.MatrixExpansion;
import uk.ac.ebi.intact.confidence.model.ProteinSimplified;
import uk.ac.ebi.intact.confidence.model.InteractionSimplified;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;

/**
 * TODO comment this
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since <pre>14-Aug-2007</pre>
 */
public class MatrixExpansionTest extends IntactBasicTestCase {

	private ExpansionStrategy expander;
	private Collection<InteractionSimplified> binaryIntS;
	private Collection<InteractionSimplified> complIntS;

	@Before
	public void setUp() throws Exception {
		expander = new MatrixExpansion();
		ProteinSimplified comp1 = new ProteinSimplified("P12345","neutral");
		ProteinSimplified comp2 = new ProteinSimplified("Q12345","neutral");
		ProteinSimplified comp3 = new ProteinSimplified("R12345","neutral");
		ProteinSimplified comp4 = new ProteinSimplified("S12345","neutral");
		InteractionSimplified intS1 = new InteractionSimplified("EBI-1234", Arrays.asList(comp1, comp2));
		InteractionSimplified intS2 = new InteractionSimplified("EBI-1234", Arrays.asList(comp3, comp4));
		binaryIntS = Arrays.asList(intS1, intS2);
	
		ProteinSimplified comp11 = new ProteinSimplified("P12345","bait");
		ProteinSimplified comp12 = new ProteinSimplified("Q12345","neutral");
		ProteinSimplified comp13 = new ProteinSimplified("R12345","neutral");
		ProteinSimplified comp14 = new ProteinSimplified("S12345","neutral");
		
		InteractionSimplified intS11 = new InteractionSimplified("EBI-1234", Arrays.asList(comp11, comp12, comp13, comp14));
		comp11.setRole("neutral");
		comp14.setRole("bait");
		InteractionSimplified intS12 = new InteractionSimplified("EBI-1234", Arrays.asList(comp11, comp12, comp13, comp14));
		
		complIntS = Arrays.asList(intS11, intS12);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testExpandBinary() {
		Collection<InteractionSimplified> interactions = new ArrayList<InteractionSimplified>();

		// Apply matrix model
		for (InteractionSimplified interaction : binaryIntS) {
			interactions.addAll(expander.expand(interaction));
		}

		Assert.assertNotNull(interactions);
		Assert.assertEquals(binaryIntS.size(), interactions.size());
	}

	@Test
	public void testExpandComplex() {
		Collection<InteractionSimplified> interactions = new ArrayList<InteractionSimplified>();

		// Apply matrix model
		for (InteractionSimplified interaction : complIntS) {
			interactions.addAll(expander.expand(interaction));
		}

		Assert.assertNotNull(interactions);
		Assert.assertTrue(interactions.size() != complIntS.size());
		//TODO: replace the fix nr '12' with a function
		Assert.assertEquals(12, interactions.size());
	}
}
