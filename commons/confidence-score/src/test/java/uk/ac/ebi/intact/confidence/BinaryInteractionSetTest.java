/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.confidence;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;

/**
 * TODO comment this ... someday
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since <pre>3 Oct 2007</pre>
 */
@Ignore
public class BinaryInteractionSetTest {


	public static void main(String[] args) {
		new BinaryInteractionSetTest().Stat();
//		new BinaryInteractionSetTest().uniqueProts();
	}
	
	@Test
	public void uniqueProts() {
		String path = "H:\\test\\medconf_test.txt";
		BinaryInteractionSet biS;
		try {
			biS = new BinaryInteractionSet(path);
			Set<String> proteinsBiS = biS.getAllProtNames();
			Set<UniprotAc> prots = new HashSet<UniprotAc>(proteinsBiS.size());
			for (String ac : proteinsBiS) {
				prots.add(new UniprotAc(ac));
			}

			File protsFile = new File("H:\\proteinsOrderToBlast.txt");
			System.out.println("protsOut: " + protsFile.getPath());
			Writer w = new FileWriter(protsFile);
			for (UniprotAc uniprotAc : prots) {
				w.append(uniprotAc +"\n");
			}
			w.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Ignore
	@Test
	public void Stat(){
		String path = "E:\\tmp\\ConfidenceModel\\IntactDbRetriever\\highconf_all.txt";//"E:\\iarmean\\backupData\\highconf_all.txt";
			//"H:\\tmp\\ConfidenceModel\\highconf_all.txt";//"E:\\tmp\\ConfidenceModel\\highconf_all.txt";
		BinaryInteractionSet biS;
		try {
			biS = new BinaryInteractionSet(path);
			Set<String> prots = biS.getAllProtNames();
			int nr = biS.getAllProtNames().size();
			System.out.println("highconf prots : " + nr + " #int: " + biS.size());
			
			path = "E:\\tmp\\ConfidenceModel\\medconf_all.txt";
			BinaryInteractionSet biS2 = new BinaryInteractionSet(path);
			prots.addAll(biS2.getAllProtNames());
			nr = biS2.getAllProtNames().size();
			System.out.println("medconf prots : " + nr + " #int: " + biS2.size());
			
			path = "E:\\tmp\\ConfidenceModel\\lowconf_all_all.txt";
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

    @Test
    public void stats(){
             String path ="E:\\iarmean\\backupData\\benchmark\\LC100.txt";
        //"E:\\iarmean\\backupData\\benchmark\\HC100-2310.txt";

		BinaryInteractionSet biS;
		try {
			biS = new BinaryInteractionSet(path);
			Set<String> prots = biS.getAllProtNames();
			int nr = biS.getAllProtNames().size();
			System.out.println("LC100 prots : " + nr + " #int: " + biS.size());

			path = "E:\\iarmean\\backupData\\benchmark\\LC050.txt";
            //"E:\\iarmean\\backupData\\benchmark\\HC050-2310.txt";
			BinaryInteractionSet biS2 = new BinaryInteractionSet(path);
			prots.addAll(biS2.getAllProtNames());
			nr = biS2.getAllProtNames().size();
			System.out.println("LC50 prots : " + nr + " #int: " + biS2.size());

			path = "E:\\iarmean\\backupData\\benchmark\\LC025.txt";
            //"E:\\iarmean\\backupData\\benchmark\\HC025-2310.txt";
			BinaryInteractionSet biS3 = new BinaryInteractionSet(path);
			prots.addAll(biS3.getAllProtNames());
			nr = biS3.getAllProtNames().size();
			System.out.println("LC25 prots : " + nr + " #int: " + biS3.size());
			System.out.println("total unique prots: " + prots.size());


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
}
