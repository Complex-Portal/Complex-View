/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */
package uk.ac.ebi.intact.confidence.attribute;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;

import uk.ac.ebi.intact.bridges.blast.BlastClient;
import uk.ac.ebi.intact.confidence.BinaryInteractionSet;
import uk.ac.ebi.intact.confidence.model.InteractionSimplified;
import uk.ac.ebi.intact.confidence.model.ProteinSimplified;

/**
 * TODO comment that
 * 
 * @author Iain Bancarz
 * @version $Id$
 * @since 09-Aug-2006 <p/> <p/> Input proteins in FASTA format For each protein
 *        in list output significant BLAST hits (sequence alignments) in a
 *        reference file (reference file example -- all IntAct proteins) <p/>
 *        Output format: UniProt IDs delimited by commas Protein,Hit1,Hit2,Hit3
 *        ... <p/> Later use this file to find attributes of protein pairs If
 *        P;Q is a pair, attributes will be: P;Q,HP1;HQ1,HP2;HQ2, ... HP1 is
 *        first hit to P, HQ1 first hit to Q, etc.
 */
public class AlignmentFileMaker {

	// private String fastaRefPath = "/scratch/blast/intact.fasta";
	// private String blastPath = "/scratch/blast/blast-2.2.14/bin/blastall";
	BlastClient		bc;
	List<String>	results;

	public AlignmentFileMaker(){
		this(0.001);
	}
	
	public AlignmentFileMaker(double threshold) {
		bc = new BlastClient(threshold);
	}

	/**
	 * returns the blast results in the following format:
	 * uniprotAc,alignment1,alignment2 .... where all the alignments are
	 * represented by the uniprotAc
	 * 
	 * @return List of String
	 */
	public List<String> getBlastHits() {
		return results;
	}

	/**
	 * Blasts the proteins in the first set against the proteins in the second
	 * set. If the writer is not null, the result will be also written on it.
	 * 
	 * @param intToBlast
	 * @param againstList
	 * @param writer
	 */
	public void blast(List<InteractionSimplified> intToBlast, List<InteractionSimplified> againstList, Writer writer) {
		if (intToBlast == null || againstList == null || writer == null) {
			new NullPointerException("params must not be null!");
		}
		HashSet<String> proteins = getProteinList(intToBlast);
		HashSet<String> againstProteins = getProteinList(againstList);
		runBlast(proteins, againstProteins, writer);

	}
	
	public void blast(BinaryInteractionSet intToBlast, BinaryInteractionSet againstList, Writer writer) {
		if (intToBlast == null || againstList == null || writer == null) {
			new NullPointerException("params must not be null!");
		}
		HashSet<String> proteins = getProteinList(intToBlast);
		HashSet<String> againstProteins = getProteinList(againstList);
		runBlast(proteins, againstProteins, writer);
	}

	public void blast(HashSet<String> uniprotAcToBlast, HashSet<String> uniprotAcAgainst, Writer writer){
		runBlast(uniprotAcToBlast, uniprotAcAgainst, writer);
	}
	
	private void runBlast(HashSet<String> uniprotAc1, HashSet<String> uniprotAc2, Writer writer) {
		results = bc.blast(uniprotAc1, uniprotAc2);
		writeResults(results, writer);
	}

	private void writeResults(List<String> results, Writer writer) {
		for (String r : results) {
			try {
				writer.append(r + "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private HashSet<String> getProteinList(List<InteractionSimplified> interactions) {
		HashSet<String> proteins = new HashSet<String>();
		for (InteractionSimplified intS : interactions) {
			for (ProteinSimplified protS : intS.getInteractors()) {
				proteins.add(protS.getUniprotAc());
			}
		}
		return proteins;
	}
	
	private HashSet<String> getProteinList(BinaryInteractionSet biS) {
		HashSet<String> proteins =  biS.getAllProtNames();
		return proteins;
	}
}
