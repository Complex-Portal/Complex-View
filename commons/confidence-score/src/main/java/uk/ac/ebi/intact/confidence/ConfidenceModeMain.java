/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.confidence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TODO comment this ... someday
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since <pre>11 Sep 2007</pre>
 */
public class ConfidenceModeMain {
	/**
	 * Sets up a logger for that class.
	 */
	public static final Log		log	= LogFactory.getLog(ConfidenceModeMain.class);

	/**
	 * @param args
	 * for test only unter eclipse
	 */
	public static void main(String[] args) {
		String uniprotPath = "E:\\tmp";
		String workDir = "E:\\tmp";
		String blastArchive ="E:\\20071016_iarmean";
		String dbFolderPath = "E:\\dbFoder";
		String email = "iarmean@ebi.ac.uk";
		int nr = 20;
		ConfidenceModel cm = new ConfidenceModel(dbFolderPath, uniprotPath,workDir, blastArchive, email, nr);
		//cm.buildModel();
		classify(cm);
		System.out.println("Done.");
	}
	
	private static void classify(ConfidenceModel cm){
		long start = System.currentTimeMillis();
//		cm.getConfidenceListsFromDb();
		long aux1 = System.currentTimeMillis();
		long timeDb = aux1 - start;
		log.info("time for db retrieve (milisec): " + timeDb);

		aux1 = System.currentTimeMillis();
	//	cm.generateLowconf(10000);
		long aux2 = System.currentTimeMillis();
		long timeGenerate = aux2 - aux1;
		log.info("time for generating lowconf (milisec): " + timeGenerate);

		aux1 = System.currentTimeMillis();
		cm.getInterProGoAndAlign();
		aux2 = System.currentTimeMillis();
		long timeAttribs = aux2 - aux1;
		log.info("time for getting the attributes (milisec): " + timeAttribs);

		aux1 = System.currentTimeMillis();
//		cm.createTadmClassifierInput();
//		cm.runTadm();
//		cm.createModel();
//		aux2 = System.currentTimeMillis();
		long timeCreateModel = aux2 - aux1;
		log.info("time for training the model (milisec): " + timeCreateModel);

		aux1 = System.currentTimeMillis();
	//	cm.classifyMedConfSet();
		long stop = System.currentTimeMillis();

		log.info("time for db read (milisec): " + timeDb);
		log.info("time to generate lowconf (milisec): " + timeGenerate);
		log.info("time for getting the attributes (milisec): " + timeAttribs);
		log.info("time for training the model (milisec): " + timeCreateModel);
		log.info("time for classifying the medconf set (milisec): " + (stop - aux1));
		log.info("total time in milisec: " + (stop - start));
	}

}
