/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */
package uk.ac.ebi.intact.confidence.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.intact.confidence.expansion.ExpansionStrategy;
import uk.ac.ebi.intact.model.util.AliasUtils;

/**
 * TODO comment this ... someday
 * 
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since
 * 
 * <pre>
 * 14 - Aug - 2007
 * </pre>
 */
public class DataMethods {

	/**
	 * Sets up a logger for that class.
	 */
	public static final Log		log	= LogFactory.getLog(AliasUtils.class);

	private ExpansionStrategy	expansionStrategy;

	/**
	 * getter for the private expansionStrategy field
	 * 
	 * @return
	 */
	public ExpansionStrategy getExpansionStrategy() {
		return expansionStrategy;
	}

	/**
	 * setter for the private expansionStrategy field
	 * 
	 * @param expansionStrategy
	 */
	public void setExpansionStrategy(ExpansionStrategy expansionStrategy) {
		this.expansionStrategy = expansionStrategy;
	}

	// Constructors
	public DataMethods() {
	}

	DataMethods(ExpansionStrategy expansionStrategy) {
		this.expansionStrategy = expansionStrategy;
	}

	/**
	 * given a file with one ebi-accession number per line, this method will
	 * return a list containing the ebi-accession numbers TODO: check the format
	 * of the string
	 * 
	 * @param filepath
	 * @return List of strings
	 */
	public List<String> exactRead(String filepath) throws IOException {
		if (filepath == null)
			throw new NullPointerException();

		List<String> ebiACs = new ArrayList<String>();

		File file = new File(filepath);
		FileReader fr = null;
		try {
			fr = new FileReader(file);
		} catch (FileNotFoundException e) {
			System.out.println("file not found : " + filepath);
		}

		BufferedReader br = new BufferedReader(fr);
		String line = "";
		while ((line = br.readLine()) != null) {
			ebiACs.add(line);
		}

		return ebiACs;
	}

	/**
	 * Given a file with one EBI-accession number per line, this method
	 * retrieves the interaction information from IntAct and stores it into a
	 * list.
	 * 
	 * @param filepath
	 * @return List of simplified interaction objects
	 */
	public List<InteractionSimplified> read(String filepath) throws IOException {
		List<String> ebiACs = exactRead(filepath);
		List<InteractionSimplified> interactions = getInteractions(ebiACs);
		return interactions;
	}

	/*
	 * gets the information out of the DB for each EBI-xxxx it retrievs the
	 * uniprotAcs
	 */
	private List<InteractionSimplified> getInteractions(List<String> ebiACs) {
		return (new IntActDb()).read(ebiACs);
	}

	/*
	 * reads the uniprotAcs for yeast, out of a fasta file and writes them to an
	 * outFile if specified
	 */

	/**
	 * Given a fasta file containing uniprotACs, this method extracts the
	 * accession numbers.
	 * 
	 * @param inFile
	 *            the fasta file to be read
	 * @param outFile
	 *            where the list of uniprotACs will be written
	 * @return List of uniprotACs
	 */
	public List<String> readFasta(File inFile, File outFile) {
		List<String> proteins = new ArrayList<String>();

		try {
			FileReader fr = new FileReader(inFile);
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			String uniprotAc = "^>(\\w{6}).*";
			while ((line = br.readLine()) != null) {
				if (Pattern.matches(uniprotAc, line)) {
					String[] pices = line.split("\\W+"); // split by non word
					// characters
					log.info("found uniportAc:	" + pices[1] + "\n");
					proteins.add(pices[1]);
				}

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// writing it to outFile
		if (outFile != null && proteins.size() != 0) {
			writeToFile(proteins, outFile);
		}

		return proteins;
	}

	/**
	 * Given a list of interactions(binary and complex interactions) it returns
	 * a list containing only binary interactions. The complex interactions are
	 * expanded according to the private field expansionStrategy.
	 * 
	 * @param interactions
	 * @return List of binary interactions
	 */
	public List<InteractionSimplified> expand(List<InteractionSimplified> interactions) {
		List<InteractionSimplified> expanded = new ArrayList<InteractionSimplified>();
		for (InteractionSimplified interaction : interactions) {
			expanded.addAll(expansionStrategy.expand(interaction));
		}
		return expanded;
	}

	/**
	 * Generates a random number of interactions that are not present in the
	 * highconf, medconf or lowconf list. The random interactions are build out
	 * of the list of proteins (yeastProtACs).
	 * 
	 * @param yeastProtACs
	 * @param highconf
	 *            list of high confidence interactions
	 * @param medconf
	 *            list of medium confidence interactions
	 * @param lowconf
	 *            list of low confidence interactions
	 * @param nr
	 *            the number of random interactions to be generated
	 * @return List of simplified interactions
	 */
	public List<InteractionSimplified> generateLCInteractions(List<String> yeastProtACs,
			List<InteractionSimplified> highconf, List<InteractionSimplified> medconf,
			List<InteractionSimplified> lowconf, int nr) {

		InteractionGenerator intGen = new InteractionGenerator();
		intGen.setHighconfidence(highconf);
		intGen.setLowconfidence(lowconf);
		intGen.setMediumconfidence(medconf);
		intGen.setProteinACs(yeastProtACs);

		List<InteractionSimplified> generatedLC = intGen.generate(nr);

		return generatedLC;
	}

	/**
	 * Exports the interaction and uniprot - accession numbers to an output
	 * stream
	 * @param interactions the exported list of interactions
	 * @param file	the output file
	 * @param uniprotAcsOnly a flag for the export
	 */
	public void export(List<InteractionSimplified> interactions, File file, boolean uniprotAcsOnly) {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			export(interactions, osw, uniprotAcsOnly);
			osw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Exports the interaction and uniprot - accession numbers to an output
	 * stream
	 * 
	 * @param interactions
	 *            the exported list of interactions
	 * @param osw
	 *            the OutputStreamWriter
	 * @param uniprotAcsOnly
	 *            a flag for exporting
	 */
	public void export(List<InteractionSimplified> interactions, OutputStreamWriter osw, boolean uniprotAcsOnly) {
		for (InteractionSimplified item : interactions) {

			if (item.getInteractors().size() != 2) {
				log.debug("interaction : " + item.getAc() + " is not binary!!!");
			}

			StringBuilder sb = new StringBuilder();
			if (!uniprotAcsOnly)
				sb.append(item.getAc() + ": ");
			ProteinSimplified prot1 = (ProteinSimplified) item.getInteractors().toArray()[0];
			ProteinSimplified prot2 = (ProteinSimplified) item.getInteractors().toArray()[1];
			sb.append(prot1.getUniprotAc() + "," + prot2.getUniprotAc() + "\n");

			try {
				osw.write(sb.toString());
			} catch (IOException e) {
				log.error("could not export");
				e.printStackTrace();
			}
		}
	}

	/*
	 * Writes the list of proteins to a file. One protein per line.
	 */
	private void writeToFile(Collection<String> proteins, File outFile) {
		try {
			FileWriter fw = new FileWriter(outFile);
			PrintWriter pw = new PrintWriter(fw);

			for (String uniprotAc : proteins) {
				pw.println(uniprotAc);
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
