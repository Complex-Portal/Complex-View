
package uk.ac.ebi.intact.application.mine.business.graph.model;

/*
 * Created on 08.04.2004
 */

/**
 * A <tt>EdgeObject</tt> is attached to the edge of the graph which is used to
 * compute the minimal connecting network. <br>
 * It stores the interaction accession number of the interaction which the edge
 * is representing. <br>
 * Furthermore it provides due to the <tt>EdgeElement</tt> interface a weight
 * for the edge. If no specific weight is given the default value is 1.
 * 
 * @author Andreas Groscurth
 */
public class EdgeObject implements EdgeElement {
    private double weight;
    private String interactionAcc;

    /**
     * Creates a new <tt>EdgeObject</tt>
     * 
     * @param inAcc the accession number of the interaction the edge represents
     * @param weight the weight of the edge
     * @throws IllegalArgument if the interaction accession number is null or
     *             empty
     */
    public EdgeObject(String inAcc, double weight) {
        if ( inAcc == null || "".equals( inAcc ) ) {
            throw new IllegalArgumentException(
                    "the parameter inAcc has to be a non empty value !" );
        }
        interactionAcc = inAcc;
        this.weight = weight;
    }

    /**
     * Creates a new <tt>EdgeObject</tt> with an edge weight of 1
     * 
     * @param inAcc the accnr of the interaction the edge represents
     */
    public EdgeObject(String inAcc) {
        this( inAcc, 1 );
    }

    /*
     * (non-Javadoc)
     * 
     * @see EdgeElement#getWeight()
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Returns the accnr of the interaction the edge represents
     * 
     * @return Returns the accrnr
     */
    public String getInteractionAcc() {
        return interactionAcc;
    }

    /**
     * Returns the interaction accnr
     */
    public String toString() {
        return interactionAcc;
    }
}