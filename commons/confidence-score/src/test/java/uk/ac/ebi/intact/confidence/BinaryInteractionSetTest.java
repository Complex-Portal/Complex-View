/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.confidence;

import java.io.IOException;
import java.util.HashSet;

import org.junit.Ignore;
import org.junit.Test;

/**
 * TODO comment this ... someday
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since <pre>3 Oct 2007</pre>
 */
public class BinaryInteractionSetTest {


	public static void main(String[] args) {
		new BinaryInteractionSetTest().Stat();
	}
	
	@Ignore
	@Test
	public void Stat(){
		String path = "H:\\tmp\\ConfidenceModel\\highconf_all.txt";//"E:\\tmp\\ConfidenceModel\\highconf_all.txt";
		BinaryInteractionSet biS;
		try {
			biS = new BinaryInteractionSet(path);
			HashSet<String> prots = biS.getAllProtNames();
			int nr = biS.getAllProtNames().size();
			System.out.println("highconf prots : " + nr + " #int: " + biS.size());
			
			path = "H:\\tmp\\ConfidenceModel\\medconf_all.txt";
			BinaryInteractionSet biS2 = new BinaryInteractionSet(path);
			prots.addAll(biS2.getAllProtNames());
			nr = biS2.getAllProtNames().size();
			System.out.println("medconf prots : " + nr + " #int: " + biS2.size());
			
			path = "H:\\tmp\\ConfidenceModel\\lowconf_all.txt";
			BinaryInteractionSet biS3 = new BinaryInteractionSet(path);
			prots.addAll(biS3.getAllProtNames());
			nr = biS3.getAllProtNames().size();
			System.out.println("lowconf prots : " + nr + " #int: " + biS3.size());
			System.out.println("total unique prots: " + prots.size());
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
}
