/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */
package uk.ac.ebi.intact.confidence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.intact.bridges.blast.BlastServiceException;
import uk.ac.ebi.intact.confidence.attribute.ClassifierInputWriter;
import uk.ac.ebi.intact.confidence.dataRetriever.IntactDbRetriever;
import uk.ac.ebi.intact.confidence.expansion.SpokeExpansion;
import uk.ac.ebi.intact.confidence.model.InteractionSimplified;
import uk.ac.ebi.intact.confidence.util.AttributeGetter;
import uk.ac.ebi.intact.confidence.util.DataMethods;

/**
 * Class for scoring the interactions in intact
 * 
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since
 * 
 * <pre>
 * 29 Aug 2007
 * </pre>
 */
public class ConfidenceModel {
	/**
	 * Sets up a logger for that class.
	 */
	public static final Log		log	= LogFactory.getLog(ConfidenceModel.class);
	private String				uniprotPath;
	private MaxEntClassifier	classifier;
	private File				workDir;
	private File				blastArchiveDir;
	private File				dbFolder;
	private String				email;
	private int					nrPerSubmission;

	public ConfidenceModel() {
	}

	public ConfidenceModel(String dbFolderPath, String uniprotSwissprotPath, String tmpDirPath, String blastArchivePath,
			String email, int nrPerSubmission) {
		if (uniprotSwissprotPath == null || dbFolderPath == null) {
			throw new NullPointerException();
		}
		dbFolder = new File(dbFolderPath);
		dbFolder.mkdir();
		log.info("dbFolder: " + dbFolder.getPath());
		uniprotPath = uniprotSwissprotPath;

		workDir = new File(tmpDirPath, "ConfidenceModel");
		workDir.mkdir();

		blastArchiveDir = new File(blastArchivePath);
		blastArchiveDir.mkdir();
		
		this.email = email;
		testDir(dbFolder);
		testDir(workDir);
		testDir(blastArchiveDir);
		this.nrPerSubmission = nrPerSubmission;
	}

	/**
	 * gets data from db, generates LC set, gets the attributes, createsTadm
	 * input, runs Tadm, trains the model , classifies the medium confidence set
	 */
	public void buildModel() {
		long start = System.currentTimeMillis();
		getConfidenceListsFromDb();
		long aux1 = System.currentTimeMillis();
		long timeDb = aux1 - start;
		log.info("time for db retrieve (milisec): " + timeDb);

		aux1 = System.currentTimeMillis();
		generateLowconf(10000);
		long aux2 = System.currentTimeMillis();
		long timeGenerate = aux2 - aux1;
		log.info("time for generating lowconf (milisec): " + timeGenerate);

		aux1 = System.currentTimeMillis();
		getInterProGoAndAlign();
		aux2 = System.currentTimeMillis();
		long timeAttribs = aux2 - aux1;
		log.info("time for getting the attributes (milisec): " + timeAttribs);

		aux1 = System.currentTimeMillis();
		createTadmClassifierInput();
		runTadm();
		createModel();
		aux2 = System.currentTimeMillis();
		long timeCreateModel = aux2 - aux1;
		log.info("time for training the model (milisec): " + timeCreateModel);

		aux1 = System.currentTimeMillis();
		classifyMedConfSet();
		long stop = System.currentTimeMillis();

		log.info("time for db read (milisec): " + timeDb);
		log.info("time to generate lowconf (milisec): " + timeGenerate);
		log.info("time for getting the attributes (milisec): " + timeAttribs);
		log.info("time for training the model (milisec): " + timeCreateModel);
		log.info("time for classifying the medconf set (milisec): " + (stop - aux1));
		log.info("total time in milisec: " + (stop - start));
	}

	public void getConfidenceListsFromDb() {
		IntactDbRetriever intactdb = new IntactDbRetriever(workDir.getPath());
		long start = System.currentTimeMillis();

		try {
			// TODO: replace with a proper way of writing to files
			File file = new File(workDir.getPath(), "medconf_all.txt");
			// TODO: remove after plugin-debug phase is working
			log.info("file MC: " + file.getPath());
			FileWriter fw = new FileWriter(file);
			intactdb.retrieveMediumConfidenceSet(fw);
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		log.info("time needed : " + (end - start));
		List<InteractionSimplified> highconf = intactdb.retrieveHighConfidenceSet();

		DataMethods dm = new DataMethods();
		highconf = dm.expand(highconf, new SpokeExpansion());
		dm.export(highconf, new File(workDir.getPath(), "highconf_all.txt"), true);
	}

	public void generateLowconf(int nr) {
		DataMethods dm = new DataMethods();
		// TODO: make sure the fasta file is in that directory + create uniprot
		// remote get Proteins ->fasta
		File inFile = new File(workDir.getParent(), "40.S_cerevisiae.fasta");
		if (!inFile.exists()) {
			throw new RuntimeException(inFile.getAbsolutePath());
		}
		HashSet<String> yeastProteins = dm.readFasta(inFile, null);
		try {
			BinaryInteractionSet highConfBiSet = new BinaryInteractionSet(workDir.getPath() + "/highconf_all.txt");
			BinaryInteractionSet medConfBiSet = new BinaryInteractionSet(workDir.getPath() + "/medconf_all.txt");
			Collection<ProteinPair> all = highConfBiSet.getSet();
			all.addAll(medConfBiSet.getSet());
			BinaryInteractionSet forbidden = new BinaryInteractionSet(all);
			BinaryInteractionSet lowConf = dm.generateLowConf(yeastProteins, forbidden, nr);

			dm.export(lowConf, new File(workDir.getPath(), "lowconf_all.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void getInterProGoAndAlign() {
		try {
			BinaryInteractionSet biSet = new BinaryInteractionSet(workDir.getPath() + "/highconf_all.txt");
			AttributeGetter aG = new AttributeGetter(dbFolder,uniprotPath + "/uniprot_sprot.dat", biSet, workDir,
					blastArchiveDir, this.email, nrPerSubmission);
			biSet = new BinaryInteractionSet(workDir.getPath() + "/highconf_all.txt");
			HashSet<String> againstProteins = biSet.getAllProtNames();
			aG.getAllAttribs(biSet, againstProteins, workDir.getPath() + "/highconf_all_attribs.txt");

			biSet = new BinaryInteractionSet(workDir.getPath() + "/medconf_all.txt");
			aG.getAllAttribs(biSet, againstProteins, workDir.getPath() + "/medconf_all_attribs.txt");

			biSet = new BinaryInteractionSet(workDir.getPath() + "/lowconf_all.txt");
			aG.getAllAttribs(biSet, againstProteins, workDir.getPath() + "/lowconf_all_attribs.txt");
			
			aG.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BlastServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void createTadmClassifierInput() {
		try {
			ClassifierInputWriter ciw = new ClassifierInputWriter(workDir.getPath() + "/highconf_all_attribs.txt",
					workDir.getPath() + "/lowconf_all_attribs.txt", workDir.getPath() + "/tadm.input", "TADM");
			ciw.writeAttribList(workDir.getPath() + "/all_attribs.txt");
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void runTadm() {
		String cmd = "tadm -events_in " + workDir.getPath() + "/tadm.input" + " -params_out " + workDir.getPath()
				+ "/weights.txt";
		try {
			Process process = Runtime.getRuntime().exec(cmd);
			process.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void createModel() {
		try {
			classifier = new MaxEntClassifier(workDir.getPath() + "/all_attribs.txt", workDir.getPath()
					+ "/weights.txt");
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void classifyMedConfSet() {
		File file = new File(workDir.getPath(), "medconf_all_attribs.txt");
		BufferedReader br;
		try {
			FileWriter fw = new FileWriter(new File(workDir.getPath(), "medconf_FINAL_score.txt"));

			br = new BufferedReader(new FileReader(file));
			String line;

			while ((line = br.readLine()) != null) {
				double tScore = classifier.trueScoreFromLine(line);
				String[] str = line.split(",");
				fw.append(str[0] + ": " + tScore + "\n");
			}
			fw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void testDir(File workDir) {
		if (!workDir.exists()) {
			throw new IllegalArgumentException("WorkDir must exist! " + workDir.getPath());
		}
		if (!workDir.isDirectory()) {
			throw new IllegalArgumentException("WorkDir must be a directory! " + workDir.getPath());
		}
		if (!workDir.canWrite()) {
			throw new IllegalArgumentException("WorkDir must be writable! " + workDir.getPath());
		}
	}
}
