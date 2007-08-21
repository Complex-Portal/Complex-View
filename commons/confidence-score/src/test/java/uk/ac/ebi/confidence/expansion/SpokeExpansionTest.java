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
import uk.ac.ebi.intact.confidence.expansion.SpokeExpansion;
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
public class SpokeExpansionTest extends IntactBasicTestCase{

	private ExpansionStrategy expander;
	private Collection<InteractionSimplified> binaryIntS;
	private Collection<InteractionSimplified> complIntS;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		expander = new SpokeExpansion();
		//intiOld();
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

//	private void intiOld() {
//		Interaction interaction = super.getMockBuilder()
//				.createInteractionRandomBinary();
//		Interaction interaction1 = super.getMockBuilder()
//				.createInteractionRandomBinary();
//		Interaction interaction2 = super.getMockBuilder()
//				.createInteractionRandomBinary();
//		binaryIntS = Arrays.asList(interaction, interaction1, interaction2);
//		
//		Interactor interactor1 = super.getMockBuilder().createProtein("P12345", "p1");
//		Interactor interactor2 = super.getMockBuilder().createProtein("Q12342", "p2");
//		Interactor interactor3 = super.getMockBuilder().createProtein("X12342", "p3");
//		Interactor interactor4 = super.getMockBuilder().createProtein("Z12342",	"p4");
//		
//		Interaction interaction3 = super.getMockBuilder().createInteractionRandomBinary();
//		interaction3.setFullName("complex - interaction3");
//		interaction3.getComponents().clear();
//		interaction3.addComponent(getMockBuilder().createComponentBait(interaction3, interactor1));
//		interaction3.addComponent(getMockBuilder().createComponentPrey(interaction3, interactor2));
//		interaction3.addComponent(getMockBuilder().createComponentPrey(interaction3, interactor3));
//		interaction3.addComponent(getMockBuilder().createComponentPrey(interaction3, interactor4));
//		
//		Interaction interaction4 = super.getMockBuilder().createInteractionRandomBinary();
//		interaction4.setFullName("complex - interaction4");
//		interaction4.getComponents().clear();
//		interaction4.addComponent(getMockBuilder().createComponentBait(interaction4, interactor4));
//		interaction4.addComponent(getMockBuilder().createComponentPrey(interaction4, interactor2));
//		interaction4.addComponent(getMockBuilder().createComponentPrey(interaction4, interactor3));
//		interaction4.addComponent(getMockBuilder().createComponentPrey(interaction4, interactor1));
//		
//		complIntS = Arrays.asList(interaction3, interaction4);
//		
//	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testExpandBinary() {
		Collection<InteractionSimplified> interactions = new ArrayList<InteractionSimplified>();

		// Apply matrix model
		for (InteractionSimplified interaction : binaryIntS) {
			interactions.addAll(expander.expand(interaction));
		}

		Assert.assertNotNull(interactions);
		Assert.assertEquals(binaryIntS.size(), interactions.size());
	}

	@Test
	public final void testExpandComplex() {
		Collection<InteractionSimplified> interactions = new ArrayList<InteractionSimplified>();

		// Apply matrix model
		for (InteractionSimplified interaction : complIntS) {
			interactions.addAll(expander.expand(interaction));
		}

		Assert.assertNotNull(interactions);
		Assert.assertTrue(interactions.size() != complIntS.size());
		//TODO: replace the fix nr '6' with a function
		Assert.assertEquals(6, interactions.size());
	}
}
