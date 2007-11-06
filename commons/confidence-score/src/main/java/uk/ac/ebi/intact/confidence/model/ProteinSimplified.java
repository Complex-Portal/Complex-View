/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */
package uk.ac.ebi.intact.confidence.model;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.bridges.blast.model.Sequence;

/**
 * TODO comment this
 * 
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since
 * 
 * <pre>
 * 14 - Aug - 2007
 * </pre>
 */
public class ProteinSimplified {

	private UniprotAc		uniprotAc;
	private String			role;			// bait or prey or neutral
	private Sequence		seq;
	private Set<GoId>		goSet;
	private Set<InterProId>	interProSet;

	// private Collection<String> alignments;

	public ProteinSimplified() {
	}

	public ProteinSimplified(UniprotAc uniprotAc){
		this.uniprotAc = uniprotAc;
	}
	
	public ProteinSimplified(UniprotAc uniprotAc, String role) {
		this.uniprotAc = uniprotAc;
		this.role = role;
	}

	public ProteinSimplified(UniprotAc uniprotAc, Sequence seq) {
		this.uniprotAc = uniprotAc;
		this.seq = seq;
	}

	public UniprotAc getUniprotAc() {
		return uniprotAc;
	}

	public String getRole() {
		return role;
	}

	public void setUniprotAc(UniprotAc uniprotAc) {
		this.uniprotAc = uniprotAc;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public Sequence getSequence() {
		return seq;
	}

	public void setSequence(Sequence seq) {
		this.seq = seq;
	}

	public Set<GoId> getGoSet() {
		return goSet;
	}

	public void setGoSet(Set<GoId> goSet) {
		this.goSet = goSet;
	}

	public void addGo(GoId go) {
		if (goSet == null) {
			goSet = new HashSet<GoId>();
		}
		goSet.add(go);
	}

	/**
	 * @return the interProSet
	 */
	public Set<InterProId> getInterProSet() {
		return interProSet;
	}

	/**
	 * @param interProSet
	 *            the interProSet to set
	 */
	public void setInterProSet(Set<InterProId> interProSet) {
		this.interProSet = interProSet;
	}

	public void addInterProId(InterProId ip) {
		if (interProSet == null) {
			interProSet = new HashSet<InterProId>();
		}
		interProSet.add(ip);
	}

	// public Collection<String> getDomains() {
	// return domains;
	// }
	//
	// public void setDomains(Collection<String> domains) {
	// this.domains = domains;
	// }
	//
	// public Collection<String> getAlignments() {
	// return alignments;
	// }
	//
	// public void setAlignments(Collection<String> alignments) {
	// this.alignments = alignments;
	// }
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((role == null) ? 0 : role.hashCode());
		result = prime * result + ((seq == null) ? 0 : seq.hashCode());
		result = prime * result + ((uniprotAc == null) ? 0 : uniprotAc.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ProteinSimplified other = (ProteinSimplified) obj;
		if (role == null) {
			if (other.role != null)
				return false;
		} else if (!role.equals(other.role))
			return false;
		if (seq == null) {
			if (other.seq != null)
				return false;
		} else if (!seq.equals(other.seq))
			return false;
		if (uniprotAc == null) {
			if (other.uniprotAc != null)
				return false;
		} else if (!uniprotAc.equals(other.uniprotAc))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return uniprotAc.toString();
	}

}
