/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.confidence;

/**
 * TODO comment this ... someday
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since <pre>11 Sep 2007</pre>
 */
public class ConfidenceModeMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String uniprotPath = "homes/iarmean/tmp/";
		String workDir = "homes/iarmean/tmp/";
		String blastArchive ="homes/iarmean/blastXml/";
		String email = "iarmean@ebi.ac.uk";
		ConfidenceModel cm = new ConfidenceModel(uniprotPath,workDir, blastArchive, email);
		cm.buildModel();
	}

}
