/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.  
All rights reserved. Please see the file LICENSE 
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.util.simplegraph;

import java.util.Map;

/**
 * A simple graph package for temporary processing, for example to prepare output for graph analysis packages.
 * Extends Map to allow easy implementation of key-value functionality.
 */

public interface BasicGraphI extends Map{

    public String getId();

    public void setId(String anId);

    public String getLabel();

    public void setLabel(String aLabel);
    
    public String getAc();

}

