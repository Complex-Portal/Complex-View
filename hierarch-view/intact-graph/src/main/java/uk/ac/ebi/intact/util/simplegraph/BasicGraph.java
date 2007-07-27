/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */

package uk.ac.ebi.intact.util.simplegraph;

import java.util.HashMap;

/**
 * Common properties for objects in the SimpleGraph package. Extends HashMap to
 * allow easy implementation of key-value functionality.
 */
public class BasicGraph extends HashMap implements BasicGraphI {

    ///////////////////////////////////////
    // attributes
    private String id;

    private String label;
    
    public BasicGraph() {
        super();
    }
    
    public BasicGraph(String ac, String label) {
        super();
        id = ac;
        this.label = label;
    }

    ///////////////////////////////////////
    // access methods for attributes
    public void setId(String id) {
        this.id = id;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getAc() {
        return id;
    }
   
    public boolean equals(Object o) {
        if ( this == o )
            return true;
        if ( !( o instanceof BasicGraphI ) )
            return false;

        final BasicGraphI node = (BasicGraphI) o;
        final String ac = getAc(), _ac = node.getAc();

        if ( ac != null ? !ac.equals( _ac ) : _ac != null )
            return false;
        return true;
    }

    public int hashCode() {
        return id.hashCode() + label.hashCode();
    }
    
    public String toString() {
        return "<" + id + " ,  " + label + ">";
    }

}

