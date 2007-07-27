/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */

package uk.ac.ebi.intact.util.simplegraph;

import uk.ac.ebi.intact.model.Component;

/**
 * Edge class which just stores two nodes (BasicGraphI). This class is used when
 * HV is built with the information provided by the mine database table
 * 
 * @author Andreas Groscurth
 */
public class MineEdge extends uk.ac.ebi.intact.util.simplegraph.BasicGraph implements EdgeI
{

    ///////////////////////////////////////
    // attributes
    private BasicGraphI node1;
    private BasicGraphI node2;

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
        return null;
    }

    public void setComponent1(Component Component1) {

    }

    public Component getComponent2() {
        return null;
    }

    public void setComponent2(Component Component2) {

    }

    ///////////////////////////////////////////////////
    // Instance methods
    public boolean equals(Object o) {
        if ( this == o )
            return true;
        if ( !( o instanceof MineEdge ) )
            return false;
        if ( !super.equals( o ) )
            return false;

        final MineEdge edge = (MineEdge) o;

        return ( node1.equals( edge.node1 ) && node2.equals( edge.node2 ) )
                || ( node1.equals( edge.node2 ) && node2.equals( edge.node1 ) );
    }
}