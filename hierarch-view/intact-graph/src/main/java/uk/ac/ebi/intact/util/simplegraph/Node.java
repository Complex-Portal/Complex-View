/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.util.simplegraph;

import uk.ac.ebi.intact.model.Interactor;

/**
 * A simple node class for temporary processing, for example to prepare output for graph analysis packages.
 */
public class Node extends uk.ac.ebi.intact.util.simplegraph.BasicGraph implements NodeI {

    private Interactor interactor;

    public Node (Interactor interactor) {
        this.interactor = interactor;
    }

    ///////////////////////////////////////
    // access methods for attributes
    public Interactor getInteractor () {
        return interactor;
    }

    public String getAc() {
        return interactor.getAc();
    }
    
    public String getLabel() {
        return interactor.getShortLabel();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;

        final Node node = (Node) o;
        final String ac  = getAc(),
                     _ac = node.getAc();

        if (ac != null ? !ac.equals(_ac) : _ac != null) return false;
        return true;
    }

    public int hashCode() {
        return getAc().hashCode();
    }

    public String toString() {
        return "[Node: "+ getAc() +"]";
    }
}
