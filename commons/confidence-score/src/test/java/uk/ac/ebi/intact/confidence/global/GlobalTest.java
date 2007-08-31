package uk.ac.ebi.intact.confidence.global;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.intact.confidence.BinaryInteractionSet;
import uk.ac.ebi.intact.confidence.attribute.AnnotationFileMaker;
import uk.ac.ebi.intact.confidence.dataRetriever.IntactDbRetriever;
import uk.ac.ebi.intact.confidence.expansion.SpokeExpansion;
import uk.ac.ebi.intact.confidence.model.InteractionSimplified;
import uk.ac.ebi.intact.confidence.util.DataMethods;

/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */

/**
 * TODO comment this
 * 
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since
 * 
 * <pre>
 * 21 Aug 2007
 * </pre>
 */
public class GlobalTest {

	/**
	 * Sets up a logger for that class.
	 */
	public static final Log		log	= LogFactory.getLog(GlobalTest.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		getConfidenceListsFromDb();
		getInterProAndGo();
		
		
		//getAlignments();
		
		
	}

	private static void getConfidenceListsFromDb() {
		IntactDbRetriever intactdb = new IntactDbRetriever();
		long start = System.currentTimeMillis();
		
		try {
			//TODO: replace with a proper way of writing to files
			String fileName = GlobalTestData.getInstance().getTmpDir() +"mediumConfidence.txt";
			FileWriter fw = new FileWriter(fileName);
			intactdb.retrieveMediumConfidenceSet(fw);
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		log.info ("time needed : " + (end-start));
		List<InteractionSimplified> highconf = intactdb.retrieveHighConfidenceSet();
		
		DataMethods dm = new DataMethods();
		highconf =  dm.expand(highconf, new SpokeExpansion());
		//TODO: replace with a proper way of writing to files
		String filepath = GlobalTestData.getInstance().getTmpDir() +"highConf.txt";
		dm.export(highconf, new File(filepath), true);		
	}
	
	private static void getInterProAndGo() {
		String path = GlobalTestData.getInstance().getTmpDir() + "ProtPairsTest.txt";
		BinaryInteractionSet biSet;
		try {
			biSet = new BinaryInteractionSet(path);
			String uniprotPath = GlobalTestData.getInstance().getDataDir() + "uniprot_sprot.dat";
			AnnotationFileMaker afm = new AnnotationFileMaker(biSet, uniprotPath);
			System.out.println("Finding Interpro annotation:");
	        afm.writeInterproAnnotation(GlobalTestData.getInstance().getTmpDir() + "ProtTest_interpro.txt");
	        System.out.println("Finding GO annotation:");
	        afm.writeGoAnnotation(GlobalTestData.getInstance().getTmpDir() + "ProtTest_go.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
	}	
}
