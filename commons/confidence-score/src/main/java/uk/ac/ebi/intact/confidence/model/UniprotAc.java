/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.confidence.model;

import java.util.regex.Pattern;

/**
 * TODO comment this ... someday
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since <pre>24 Sep 2007</pre>
 */
public class UniprotAc {
	private String	acNr;
	// TODO: ask sam for a proper regex
	private String	uniprotTermExpr	= "\\w{6,6}-\\d";

	public UniprotAc(String accessionNr) {
		if (accessionNr == null) {
			throw new IllegalArgumentException("Ac must not be null!");
		}
		if (Pattern.matches(uniprotTermExpr, accessionNr)) {
			this.acNr = accessionNr;
		} else {
			throw new IllegalArgumentException("Ac must be be built out of 6 characters!");
		}
	}

	/**
	 * @return the ac
	 */
	public String getAcNr() {
		return acNr;
	}

	@Override
	public String toString() {
		return acNr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof UniprotAc) {
			UniprotAc ac = (UniprotAc) obj;
			return this.acNr.equals(ac.acNr);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.acNr.hashCode();
	}
	
}
