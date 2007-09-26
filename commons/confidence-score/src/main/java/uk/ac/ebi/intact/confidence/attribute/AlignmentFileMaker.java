/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */
package uk.ac.ebi.intact.confidence.attribute;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.intact.bridges.blast.BlastService;
import uk.ac.ebi.intact.bridges.blast.BlastServiceException;
import uk.ac.ebi.intact.bridges.blast.jdbc.BlastJobEntity;
import uk.ac.ebi.intact.bridges.blast.model.BlastResult;
import uk.ac.ebi.intact.bridges.blast.model.Hit;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.confidence.global.GlobalTestData;
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
	/**
	 * Sets up a logger for that class.
	 */
	public static final Log	log	= LogFactory.getLog(AlignmentFileMaker.class);

	private BlastService	blast;
	private File			workDir;
	private Float			threshold;

	// private String fastaRefPath = "/scratch/blast/intact.fasta";
	// private String blastPath = "/scratch/blast/blast-2.2.14/bin/blastall";

	/**
	 * Constructor
	 */
	public AlignmentFileMaker(BlastService blast) {
		this(new Float(0.001), null, blast);
	}

	/**
	 * Constructor
	 * 
	 * @param threshold
	 * @param workingDirectory
	 */
	public AlignmentFileMaker(Float threshold, File workingDirectory, BlastService blast) {

		this.blast = blast;
		this.threshold = threshold;

		if (workingDirectory == null) {
			String workPath = AlignmentFileMaker.class.getResource("doNotRemoveThis.file").getPath();
			workDir = new File(workPath);
			HashMap<String, File> paths = GlobalTestData.getInstance().getRightPahts();//getTargetDirectory(); // new
			workDir = paths.get("workDir");
			// File(workDir.getParent());
		} else {
			this.workDir = workingDirectory;
		}
		if (!workDir.isDirectory()) {
			workDir.mkdir();
		}
	}

	// //////////////////
	// // Public Methods
	/**
	 * Blasts the proteins in the first set against the proteins in the second
	 * set. If the writer is not null, the result will be also written on it.
	 * 
	 * @param intToBlast
	 * @param againstList
	 * @param writer
	 * @throws BlastServiceException
	 */
	public void blast(List<InteractionSimplified> intToBlast, List<InteractionSimplified> againstList, Writer writer)
			throws BlastServiceException {
		if (intToBlast == null || againstList == null || writer == null) {
			throw new NullPointerException("params must not be null!");
		}
		Set<UniprotAc> proteins = getProteinList(intToBlast);
		Set<UniprotAc> againstProteins = getProteinList(againstList);

		blast(proteins, againstProteins, writer);
	}

	/**
	 * 
	 * @param proteins
	 * @param againstProteins
	 * @param fileWriter
	 * @throws BlastServiceException
	 */
	public void blast(Set<UniprotAc> proteins, Set<UniprotAc> againstProteins, Writer writer)
			throws BlastServiceException {
		if (proteins == null || againstProteins == null || writer == null) {
			throw new NullPointerException("params must not be null!");
		}

		List<BlastResult> results = blast.fetchAvailableBlasts(proteins);
		processResults(results, againstProteins, writer);

		Set<UniprotAc> missingProteins = notIncluded(results, proteins);
		if (missingProteins.size() != 0) {
			List<BlastJobEntity> submitted = blast.submitJobs(missingProteins);
			List<BlastResult> tmpResults = blast.fetchAvailableBlasts(submitted);
			results.addAll(tmpResults);
			while (results.size() != proteins.size()) {
				//TODO: reassess if it needs more time than the fetchBlast code
				// try{
				// Thread.sleep(5000);
				// } catch(InterruptedException e){
				// e.printStackTrace();
				// }
				missingProteins = notIncluded(results, proteins);
				tmpResults = blast.fetchAvailableBlasts(missingProteins);
				processResults(tmpResults, againstProteins, writer);
				results.addAll(tmpResults);
			}
		}
	}

	// ///////////////////
	// // Private Methods
	private HashSet<UniprotAc> getProteinList(List<InteractionSimplified> interactions) {
		HashSet<UniprotAc> proteins = new HashSet<UniprotAc>();
		for (InteractionSimplified intS : interactions) {
			for (ProteinSimplified protS : intS.getInteractors()) {
				proteins.add(new UniprotAc(protS.getUniprotAc()));
			}
		}
		return proteins;
	}

	private Set<UniprotAc> notIncluded(List<BlastResult> results, Set<UniprotAc> proteins) {
		Set<UniprotAc> protNotIn = new HashSet<UniprotAc>();

		Set<UniprotAc> resultProt = new HashSet<UniprotAc>();
		for (BlastResult blastResult : results) {
			resultProt.add(new UniprotAc(blastResult.getUniprotAc()));
		}

		for (UniprotAc ac : proteins) {
			if (!resultProt.contains(ac)) {
				protNotIn.add(ac);
			}
		}
		return protNotIn;
	}

	private void processResults(List<BlastResult> results, Set<UniprotAc> againstProteins, Writer writer) {
		// TODO remove this after finalized
		// process the results according to the thresholds : against
		// proteins and eval < 0.001
		// add to the alignmentLine
		// append the alignmentLine to a writer
		for (BlastResult result : results) {
			String alignmentLine = result.getUniprotAc();
			for (Hit hit : result.getHits()) {
				Float evalue = hit.getEValue();
				String ac = hit.getUniprotAc();
				if (evalue < threshold && againstProteins.contains(new UniprotAc(ac))) {
					alignmentLine += "," + ac;
				}
			}
			try {
				writer.append(alignmentLine + "\n");
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
}
