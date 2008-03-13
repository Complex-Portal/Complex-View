/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.confidence.model;

import java.util.Iterator;
import java.util.List;

/**
 *  InteractionSimplified class represents an IntAct interaction containing only
 * the interaction ac and the components stored as ProteinSimplified objects.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since 1.6.0
 * <pre>14-Aug-2007</pre>
 */
public class InteractionSimplified {

	private String ebiAc;
	private List<ProteinSimplified> components;
	//private String type; //complex or binary
	
	public InteractionSimplified(){}
	public InteractionSimplified(String ebiAC, List<ProteinSimplified> interactors){
		this.ebiAc = ebiAC;
		this.components = interactors;
	}
	public List<ProteinSimplified> getInteractors() {
		return components;
	}
	public void setInteractors(List<ProteinSimplified> interactors) {
		this.components = interactors;
	}
	public String getAc() {
		return ebiAc;
	}
	public void setAc(String ebiAc) {
		this.ebiAc = ebiAc;
	}
	
	public List<ProteinSimplified> getComponents(){
		return components;
	}

    public String convertToString(){
        StringBuilder result =new StringBuilder( ebiAc + ":");
        for ( Iterator<ProteinSimplified> proteinSimplifiedIterator = components.iterator(); proteinSimplifiedIterator.hasNext(); )
        {
            ProteinSimplified proteinSimplified =  proteinSimplifiedIterator.next();
            result.append( proteinSimplified.convertToString() +",");
        }
        return result.toString();
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((components == null) ? 0 : components.hashCode());
		result = prime * result + ((ebiAc == null) ? 0 : ebiAc.hashCode());
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
		final InteractionSimplified other = (InteractionSimplified) obj;
		if (components == null) {
			if (other.components != null)
				return false;
		} else if (!components.equals(other.components))
			return false;
		if (ebiAc == null) {
			if (other.ebiAc != null)
				return false;
		} else if (!ebiAc.equals(other.ebiAc))
			return false;
		return true;
	}
}
