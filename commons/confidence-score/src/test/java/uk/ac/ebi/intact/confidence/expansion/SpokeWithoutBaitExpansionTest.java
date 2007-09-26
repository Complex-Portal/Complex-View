/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.confidence.expansion;


import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.intact.confidence.expansion.ExpansionStrategy;
import uk.ac.ebi.intact.confidence.expansion.SpokeWithoutBaitExpansion;
import uk.ac.ebi.intact.confidence.global.GlobalTestData;
import uk.ac.ebi.intact.confidence.model.InteractionSimplified;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;

/**
 * TODO comment this
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since <pre>14-Aug-2007</pre>
 */
public class SpokeWithoutBaitExpansionTest extends IntactBasicTestCase{

	private ExpansionStrategy expander;
	private Collection<InteractionSimplified> binaryIntS;
	private Collection<InteractionSimplified> complIntS;
	private Collection<InteractionSimplified> complIntWithoutBaitS; 
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		expander = new SpokeWithoutBaitExpansion();
		
		binaryIntS = GlobalTestData.getInstance().getBinaryInteractions();
		complIntS = GlobalTestData.getInstance().getComplexInteractions();
		complIntWithoutBaitS =GlobalTestData.getInstance().getCompelxWithoutBaitInteractions();
		
		
//		initializeBinaryInteractionSet();
//		initializeComplexInteractionSet();
//		initializeComplexInteractionWithoutBaitSet();			
	}

//	private void initializeComplexInteractionWithoutBaitSet() {
//		Interactor interactor1 = super.getMockBuilder().createProtein("P12345", "p1");
//		Interactor interactor2 = super.getMockBuilder().createProtein("Q12345", "p2");
//		Interactor interactor3 = super.getMockBuilder().createProtein("X12345", "p3");
//		Interactor interactor4 = super.getMockBuilder().createProtein("Z12345",	"p4");
//		
//		Interaction interaction5 = super.getMockBuilder().createInteractionRandomBinary();
//		interaction5.setFullName("complex - interaction5");
//		interaction5.getComponents().clear();
//		interaction5.addComponent(getMockBuilder().createComponentNeutral(interaction5, interactor1));
//		interaction5.addComponent(getMockBuilder().createComponentNeutral(interaction5, interactor2));
//		interaction5.addComponent(getMockBuilder().createComponentNeutral(interaction5, interactor3));
//		interaction5.addComponent(getMockBuilder().createComponentNeutral(interaction5, interactor4));
//		
//		Interaction interaction6 = super.getMockBuilder().createInteractionRandomBinary();
//		interaction6.setFullName("complex - interaction6");
//		interaction6.getComponents().clear();
//		interaction6.addComponent(getMockBuilder().createComponentNeutral(interaction6, interactor4));
//		interaction6.addComponent(getMockBuilder().createComponentNeutral(interaction6, interactor2));
//		interaction6.addComponent(getMockBuilder().createComponentNeutral(interaction6, interactor3));
//		interaction6.addComponent(getMockBuilder().createComponentNeutral(interaction6, interactor1));
//		
//		complIntWithoutBaitS = Arrays.asList(interaction5, interaction6);
//		
//	}
//
//	private void initializeComplexInteractionSet() {
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
//	private void initializeBinaryInteractionSet2() {
//		Interaction interaction = super.getMockBuilder().createInteractionRandomBinary();
//		Interaction interaction1 = super.getMockBuilder().createInteractionRandomBinary();
//		Interaction interaction2 = super.getMockBuilder().createInteractionRandomBinary();
//		binaryIntS = Arrays.asList(interaction, interaction1, interaction2);	
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
	
	@Test
	public final void testExpandComplexWithoutBait() {
		Collection<InteractionSimplified> interactions = new ArrayList<InteractionSimplified>();

		// Apply matrix model
		for (InteractionSimplified interaction : complIntWithoutBaitS) {
			interactions.addAll(expander.expand(interaction));
		}

		Assert.assertNotNull(interactions);
		Assert.assertTrue(interactions.size() != complIntWithoutBaitS.size());
		//TODO: replace the fix nr '6' with a function
		Assert.assertEquals(6, interactions.size());
	}
}
