/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.confidence.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TODO comment this
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since <pre>23 Aug 2007</pre>
 */
public class ProteinAlign {
	/**
	 * Sets up a logger for that class.
	 */
	public static final Log	log	= LogFactory.getLog(ProteinAlign.class);
	private String uniprotAc;
	
	private List<String> alignments;
	
	public ProteinAlign(){
		alignments = new ArrayList<String>();
	}
	
	public ProteinAlign(String uniprotAc){
		this.uniprotAc = uniprotAc;
	}

	public String getUniprotAc() {
		return uniprotAc;
	}

	public void setUniprotAc(String uniprotAc) {
		this.uniprotAc = uniprotAc;
	}

	public List<String> getAlignments() {
		return alignments;
	}

	public void setAlignments(List<String> alignments) {
		this.alignments = alignments;
	}
	
	public void addAlignment(String align){
		if (!alignments.contains(align)){
			alignments.add(align);
		}else{
			log.debug("alignment already contained");
		}
	}
	
}
