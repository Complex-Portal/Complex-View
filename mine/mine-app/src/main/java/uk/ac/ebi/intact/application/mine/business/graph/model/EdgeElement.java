
package uk.ac.ebi.intact.application.mine.business.graph.model;

/*
 * Created on 08.04.2004
 */

/**
 * A class can implement the <tt>EdgeElement</tt> interface if it wants to be
 * used as an element attached to an edge of the graph. <br>
 * The only method which the class has to implement is the <tt>getWeight</tt>
 * method which provides to get the weight the edges.
 * 
 * @author Andreas Groscurth
 */
public interface EdgeElement {
    /**
     * Returns the weight of the edge
     * 
     * @return Returns the weight of the edge
     */
    public double getWeight();
}