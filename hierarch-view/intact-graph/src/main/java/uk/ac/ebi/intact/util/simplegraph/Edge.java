/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.  
All rights reserved. Please see the file LICENSE 
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.util.simplegraph;

import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.Interactor;

public class Edge extends uk.ac.ebi.intact.util.simplegraph.BasicGraph implements EdgeI {

    ///////////////////////////////////////
    // attributes
    private BasicGraphI node1;
    private BasicGraphI node2;
    private Component Component1;
    private Component Component2;

    public Edge() {
        super();
    }

    public Edge(String ac, String label) {
        super(ac, label);
    }

    ///////////////////////////////////////
    // access methods for attributes
    public BasicGraphI getNode1() {
        return node1;
    }

    public void setNode1(BasicGraphI node1) {
        this.node1 = node1;
    }

    public BasicGraphI getNode2() {
        return node2;
    }

    public void setNode2(BasicGraphI node2) {
        this.node2 = node2;
    }

    public Component getComponent1() {
        return Component1;
    }

    public void setComponent1(Component Component1) {
        this.Component1 = Component1;
    }

    public Component getComponent2() {
        return Component2;
    }

    public void setComponent2(Component Component2) {
        this.Component2 = Component2;
    }

    ///////////////////////////////////////////////////
    // Instance methods
    public boolean equals( Object o ) {
        if (this == o) return true;
        if (!(o instanceof Edge)) return false;
        if (!super.equals(o)) return false;

        // An edge object is symmetric. Check if either
        // this.attribute1 equals o.attribute1   A N D   this.attribute1 equals o.attribute2
        //   O R
        // this.attribute1 equals o.attribute2   A N D   this.attribute2 equals o.attribute1

        final Edge edge = (Edge) o;

        Interactor i11 = this.getComponent1().getInteractor(),
                   i12 = this.getComponent2().getInteractor(),
                   i21 = edge.getComponent1().getInteractor(),
                   i22 = edge.getComponent2().getInteractor();

        return ((i11.equals(i21) && i12.equals(i22)) || (i11.equals(i22) && i12.equals(i21)));
    }

}
