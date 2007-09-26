package uk.ac.ebi.intact.confidence.global;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Ignore;
import org.junit.Test;

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
	
	private String uniprotPathDir;
	
	/**
	 * @param args
	 */
//	public static void main(String[] args) {
//		getConfidenceListsFromDb();
//		String uniprotPathDir	= args[0];
//		getInterProAndGo(uniprotPathDir);
//		//getAlignments();	
//	}

	@Test
	@Ignore
	public void getConfidenceListsFromDb() {
		HashMap<String, File> paths = GlobalTestData.getInstance().getRightPahts();
		String tmpDirPath = paths.get("workDir").getPath() + "/IntactDbRetriever/"; //GlobalTestData.getInstance().getTargetDirectory().getPath() + "/IntactDbRetriever/";
		IntactDbRetriever intactdb = new IntactDbRetriever(tmpDirPath);
		long start = System.currentTimeMillis();
		
		try {
			File file = new File(GlobalTestData.getInstance().getTargetDirectory().getPath(), "mediumConfidence.txt");
			FileWriter fw = new FileWriter(file);
			intactdb.retrieveMediumConfidenceSet(fw);
			fw.close();
		} catch (IOException e) {
			fail(e.toString());
		}
		long end = System.currentTimeMillis();
		log.info ("time needed : " + (end-start));
		List<InteractionSimplified> highconf = intactdb.retrieveHighConfidenceSet();
		
		DataMethods dm = new DataMethods();
		highconf =  dm.expand(highconf, new SpokeExpansion());
		File file = new File(GlobalTestData.getInstance().getTargetDirectory().getPath() + "highConf.txt");
		dm.export(highconf, file, true);		
	}
	
	@Test
	@Ignore
	public void getInterProAndGo() {
		String path = GlobalTestData.getInstance().getTargetDirectory().getPath() + "/ProtPairsTest.txt";
		BinaryInteractionSet biSet;
		try {
			biSet = new BinaryInteractionSet(path);
			String uniprotPath = uniprotPathDir + "uniprot_sprot.dat";
			AnnotationFileMaker afm = new AnnotationFileMaker(biSet, uniprotPath);
			System.out.println("Finding Interpro annotation:");
	        afm.writeInterproAnnotation(GlobalTestData.getInstance().getTargetDirectory().getPath() + "/ProtTest_interpro.txt");
	        System.out.println("Finding GO annotation:");
	        File f = new File(GlobalTestData.getInstance().getTargetDirectory().getPath(), "ProtTest_go.txt");
	        afm.writeGoAnnotation(f.getPath());
		} catch (IOException e) {
			fail(e.toString());
		}				
	}	
}
