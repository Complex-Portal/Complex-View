/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.confidence.expansion;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.intact.confidence.global.GlobalTestData;
import uk.ac.ebi.intact.confidence.model.InteractionSimplified;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Test-class for the MatrixExpansion.
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
		binaryIntS = GlobalTestData.getInstance().getBinaryInteractions();
		complIntS = GlobalTestData.getInstance().getComplexInteractions();
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
		Assert.assertEquals(12, interactions.size());
	}
}
