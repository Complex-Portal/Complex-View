/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */
package uk.ac.ebi.intact.confidence.attribute;

import uk.ac.ebi.intact.confidence.BinaryInteractionSet;
import uk.ac.ebi.intact.confidence.ProteinPair;

import java.util.Arrays;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Pattern;
import java.io.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TODO comment that
 * 
 * @author Iain Bancarz
 * @version $Id$
 * @since 31-Jul-2006 <p/> Find attributes for a given interaction set Read
 *        annotation from uniprot file Write protein annotation file(s)
 */
public class AnnotationFileMaker implements AnnotationConstants {

	/**
	 * Sets up a logger for that class.
	 */
	public static final Log		log	= LogFactory.getLog(AnnotationFileMaker.class);
	
	File			uniprotFile;
	Set<String>	allProts;
	static boolean	verbose		= true;					// debug switch

	public AnnotationFileMaker(){
		allProts = new HashSet<String>();
	}
	
	public AnnotationFileMaker(BinaryInteractionSet biSet, String uniPath) {

		allProts = biSet.getAllProtNames(); // proteins to find annotation for

		if (uniPath != null) {
			uniprotFile = new File(uniPath);
		} else {
			uniprotFile	= new File(uniprotPath);
		}

	}

	public void writeInterproAnnotation(String outPath) throws IOException {

		HashSet<String> equivalentProts = new HashSet<String>();
		// set of equivalent names from protNameSet
		ArrayList<String> termList = new ArrayList<String>();
		// list of annotation terms for a given protein
		HashSet<String> noAnnotation = new HashSet<String>();
		// record whether annotation has been read for a given protein

		// initialize variables to read/write files
		FileReader fr = new FileReader(uniprotFile);
		BufferedReader br = new BufferedReader(fr);
		String line;
		String[] items;
		FileWriter fw = new FileWriter(outPath);
		PrintWriter pw = new PrintWriter(fw);

		int protCount = 0; // if debugging -- keep track of number of proteins
							// read
		int totalProts = allProts.size();
		while ((line = br.readLine()) != null) {

			if (Pattern.matches(uniprotNameExpr, line)) {
				items = line.split("\\W+"); // split by nonword characters
				for (String name : items) {
					if (allProts.contains(name)) {
						equivalentProts.add(name);
					}
				}
			} else if (equivalentProts.isEmpty()) {
				continue; // current protein is not found in allProts
			} else if (Pattern.matches(ipLineExprUniProt, line)) {
				items = line.split(";*\\s+");
				if (Pattern.matches(ipTermExpr, items[2])) {
					termList.add(items[2]);
				}
				// if (verbose) System.out.println(line);
			} else if (Pattern.matches(endExprUniProt, line)) {
				// end of uniProt entry -- write output lines
				// for each interaction, output a comma-separated list of
				// attributes

				StringBuffer outBuf;

				for (String name : equivalentProts) {

					if (termList.isEmpty()) {
						noAnnotation.add(name);
					}
					outBuf = new StringBuffer(name);
					outBuf.append(",");
					for (String term : termList) {
						term = term + ",";
						outBuf.append(term);
					}
					String outLine = outBuf.toString();
					pw.println(outLine);
					// if (verbose) System.out.println(outLine);
				}

				if (verbose) {
					protCount++;
					String comment = termList.size() + " annotation term(s) found for protein " + protCount + " of "
							+ totalProts + ".";
					log.debug(comment);
					//System.out.println(comment);
				}

				// clear collections for next protein
				equivalentProts.clear();
				termList.clear();

			}
		}

		// finally, print number of proteins for which no annotation was found
		if (verbose) {
			String comment = "No annotation found for " + noAnnotation.size() + " of " + totalProts + " proteins.";
			log.debug(comment);
			//System.out.println(comment);
		}
		fw.close();
		fr.close();

	}

	public void writeInterproAnnotation(ProteinPair proteinPair, String outPath) throws IOException{
		resetAllProteins(proteinPair);
		writeInterproAnnotation(outPath);
	}
	
	public void writeGoAnnotation(String outFile) throws IOException {

		HashSet<String> forbiddenGo = new HashSet<String>();
		for (String goTerm : forbiddenGoTerms) {
			// use static array of forbidden GO terms, recorded in
			// AnnotationConstants interface
			forbiddenGo.add(goTerm);
		}

		HashSet<String> equivalentProts = new HashSet<String>();
		// set of equivalent names from protNameSet
		ArrayList<String> termList = new ArrayList<String>();
		// list of annotation terms for a given protein
		HashSet<String> noAnnotation = new HashSet<String>();
		// record whether annotation has been read for a given protein

		FileReader fr = new FileReader(uniprotFile);
		BufferedReader br = new BufferedReader(fr);
		String line;
		String[] items;
		FileWriter fw = new FileWriter(outFile);
		PrintWriter pw = new PrintWriter(fw);

		int protCount = 0; // if debugging -- keep track of number of proteins
							// read
		int forbidCount = 0; // also number of disallowed GO terms
		int totalProts = allProts.size();

		while ((line = br.readLine()) != null) {

			if (Pattern.matches(uniprotNameExpr, line)) {
				items = line.split("\\W+"); // split by nonword characters
				for (String name : items) {
					if (allProts.contains(name)) {
						equivalentProts.add(name);
					}
				}
			} else if (equivalentProts.isEmpty()) {
				continue; // current protein is not found in allProts
			} else if (Pattern.matches(goLineExprUniProt, line)) {
				items = line.split(";*\\s+");
				if (Pattern.matches(goTermExpr, items[2]) && !forbiddenGo.contains(items[2])) {
					termList.add(items[2]);
				} else if (forbiddenGo.contains(items[2])) {
					forbidCount++;
				}
			} else if (Pattern.matches(endExprUniProt, line)) {
				// end of uniProt entry -- write output lines
				// for each protein, output a comma-separated list of annotation
				// terms

				StringBuffer outBuf;
				for (String name : equivalentProts) {

					if (termList.isEmpty()) {
						noAnnotation.add(name);
					}

					outBuf = new StringBuffer(name);
					outBuf.append(",");
					for (String term : termList) {
						term = term + ",";
						outBuf.append(term);
					}
					String outLine = outBuf.toString();
					pw.println(outLine);
				}

				if (verbose) {
					protCount++;
					String comment = termList.size() + " annotation term(s) found for protein " + protCount + " of "
							+ totalProts + ".";
					//System.out.println(comment);
					log.debug(comment);
				}

				// clear collections for next protein
				equivalentProts.clear();
				termList.clear();

			}
		}

		// finally, print proteins for which no annotation was found
		if (verbose) {
			String comment = "No annotation found for " + noAnnotation.size() + " of " + totalProts + " proteins.";
			//System.out.println(comment);
			log.debug(comment);
			comment = forbidCount + " instances of GO terms on disallowed list.";
			//System.out.println(comment);
			log.debug(comment);
		}

		fw.close();
		fr.close();

	}

	public void writeGoAnnotation(ProteinPair proteinPair, File outFile) throws IOException{
		resetAllProteins(proteinPair);		
		// call 
		writeGoAnnotation(outFile.getPath());
	}

	private void resetAllProteins(ProteinPair proteinPair) {
		// setting the wanted proteins
		if (this.allProts !=null){
			allProts.clear();
		}
		allProts.addAll(Arrays.asList(proteinPair.getFirstId(), proteinPair.getSecondId()));	
	}

	public void setUniprotFile(File uniprotFile) {
		this.uniprotFile = uniprotFile;
	}

	public void setAllProts(HashSet<String> allProts) {
		this.allProts = allProts;
	}
	
}
