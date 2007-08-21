/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.confidence.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *  Tests the IntActDb class
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since <pre>17 Aug 2007</pre>
 */
public class IntActDbTest {

	private Collection<String> acs;
	private IntActDb intactdb;
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		acs = Arrays.asList("EBI-987097", "EBI-446104", "EBI-79835", "EBI-297231", "EBI-1034130");
		intactdb = new IntActDb();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * 
	 */
	//@Test
	public final void testRead() {
		Collection<InteractionSimplified> interactions = intactdb.read(acs);
		Assert.assertEquals(acs.size(), interactions.size());
		
		printout(interactions);
	}
	
	@Test
	public final void testReadMediumConfidence(){
		long start = System.currentTimeMillis();
		Collection<InteractionSimplified> interactionsMC = intactdb.readMediumConfidenceSet();
		long end = System.currentTimeMillis();
		long time = end - start;
		System.out.print("time in milis: " + time);
		
		FileWriter fw;
		try {
			fw = new FileWriter(new File("/net/nfs6/vol1/homes/iarmean/tmp/time.txt"));
			PrintWriter pw = new PrintWriter(fw);
	        pw.write("time in milis: " + time);
	        pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		
		Collection<InteractionSimplified> interactionsHC = intactdb.getHighconfidenceSet();
		Assert.assertEquals(50, interactionsMC.size() + interactionsHC.size());
		for (int i = 0; i < 10; i++) {
			InteractionSimplified intS = (InteractionSimplified)interactionsMC.toArray()[i];
			System.out.print(intS.getAc()+": ");
		}
		DataMethods dm = new DataMethods();
		interactionsMC = dm.expand(interactionsMC);
		interactionsHC = dm.expand(interactionsHC);
		dm.export(interactionsHC, new File("/net/nfs6/vol1/homes/iarmean/tmp/hc.txt"), false);
		dm.export(interactionsMC, new File("/net/nfs6/vol1/homes/iarmean/tmp/mc.txt"), false);
		dm.export(interactionsHC, new File("/net/nfs6/vol1/homes/iarmean/tmp/hc_ebiAc.txt"), true);
		dm.export(interactionsMC, new File("/net/nfs6/vol1/homes/iarmean/tmp/mc_ebiAc.txt"), true);
		
	}
	

	private void printout(Collection<InteractionSimplified> expandedInteractions) {
		for (InteractionSimplified item : expandedInteractions) {
			System.out.print(item.getAc()+": ");
			printProtein(item);
		}		
	}

	private void printProtein(InteractionSimplified interaction) {
		for (ProteinSimplified item : interaction.getInteractors()) {
			System.out.print(item.getUniprotAc()+"; ");
		}
		System.out.println();
	}
	
}
