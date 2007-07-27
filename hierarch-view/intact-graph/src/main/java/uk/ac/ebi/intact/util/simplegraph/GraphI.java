/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.  
All rights reserved. Please see the file LICENSE 
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.util.simplegraph;

import uk.ac.ebi.intact.model.BasicObject;
import uk.ac.ebi.intact.model.Interactor;

import java.util.Collection;
import java.util.HashMap;

/**
 * A simple graph class for temporary processing, for example to prepare output for graph analysis packages.
 */
public interface GraphI extends uk.ac.ebi.intact.util.simplegraph.BasicGraphI
{

    public void addNode(BasicGraphI aNode);

    public BasicGraphI addNode(Interactor anInteractor);

    public void addEdge(EdgeI anEdge);

    public HashMap getNodes();

    public Collection getEdges();

    /** record that a Component has been visited during
     *  graph exploration.
     */
    public void addVisited(BasicObject graphElement);

    /** return true if a Component has been visited during graph exploration.
     *
     */
    public boolean isVisited(BasicObject graphElement);
}
