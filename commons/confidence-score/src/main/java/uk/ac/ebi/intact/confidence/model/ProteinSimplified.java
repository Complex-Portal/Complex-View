/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.confidence.model;

/**
 * TODO comment this
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since <pre>14-Aug-2007</pre>
 */
public class ProteinSimplified {

	private String uniprotAc;
	private String role; // bait or prey or neutral
	private String seq;
//	private Collection<String> GOs;
//	private Collection<String> domains;
//	private Collection<String> alignments;
	
	
	public ProteinSimplified(){
	}
	
	public ProteinSimplified(String uniprotAc, String role){
		this.uniprotAc = uniprotAc;
		this.role = role;
	}

	public String getUniprotAc() {
		return uniprotAc;
	}

	public String getRole() {
		return role;
	}

	public void setUniprotAc(String uniprotAc) {
		this.uniprotAc = uniprotAc;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getSeq() {
		return seq;
	}

	public void setSeq(String seq) {
		this.seq = seq;
	}

	/* (non-Javadoc)
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

	/* (non-Javadoc)
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

//	public Collection<String> getGOs() {
//		return GOs;
//	}
//
//	public void setGOs(Collection<String> os) {
//		GOs = os;
//	}
//
//	public Collection<String> getDomains() {
//		return domains;
//	}
//
//	public void setDomains(Collection<String> domains) {
//		this.domains = domains;
//	}
//
//	public Collection<String> getAlignments() {
//		return alignments;
//	}
//
//	public void setAlignments(Collection<String> alignments) {
//		this.alignments = alignments;
//	}	
	
	
}
