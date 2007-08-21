/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.confidence.model;

import java.util.Collection;

/**
 * TODO comment this
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since <pre>14-Aug-2007</pre>
 */
public class InteractionSimplified {

	private String ebiAc;
	private Collection<ProteinSimplified> components;
	//private String type; //complex or binary
	
	public InteractionSimplified(){}
	public InteractionSimplified(String ebiAC, Collection<ProteinSimplified> interactors){
		this.ebiAc = ebiAC;
		this.components = interactors;
	}
	public Collection<ProteinSimplified> getInteractors() {
		return components;
	}
	public void setInteractors(Collection<ProteinSimplified> interactors) {
		this.components = interactors;
	}
	public String getAc() {
		return ebiAc;
	}
	public void setAc(String ebiAc) {
		this.ebiAc = ebiAc;
	}
}
