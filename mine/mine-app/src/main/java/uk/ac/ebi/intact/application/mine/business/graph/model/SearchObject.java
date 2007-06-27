/*
 * Created on 19.04.2004
 */

package uk.ac.ebi.intact.application.mine.business.graph.model;

import java.util.BitSet;

import jdsl.core.api.Sequence;

/**
 * The class <tt>SearchObject</tt> is used as template for the search in the
 * graph. For each accession number which is used in the search for the minimal
 * connecting network a corresponding <tt>SearchObject</tt> is created. <br>
 * Each instance of this class stores the shortest path found for the search
 * accession number and additionally a <tt>BitSet</tt> which is needed to
 * speed up the algorithm. Furthermore each <tt>SearchObject</tt> memorize its
 * index in the search. This is important to set the bit in the other
 * <tt>BitSet</tt> correctly. <br>
 * The BitSet for each <tt>SearchObject</tt> stores whether the path to
 * another <tt>SearchObject</tt> is already found. If a path is found a bit
 * representing the other <tt>SearchObject</tt> is set in the <tt>BitSet</tt>
 * of the instance.
 * 
 * @author Andreas Groscurth
 */
public class SearchObject {
    private BitSet bitID;
    private int index;
    private Sequence path;

    /**
     * Creates a new <tt>SearchObject</tt> and initialise a new
     * <tt>BitSet</tt> which at least can hold <tt>size</tt> number of bits.
     * 
     * @param index the index of object
     * @param numberOfSearchObjects the number of SearchObjects which are used
     *            totally in the application
     * @throws IndexOutofBoundsException whether the index is less than 0 or
     *             greater than numberOfSearchObjects - 1
     */
    public SearchObject(int index, int numberOfSearchObjects) {
        if ( index < 0 || index > numberOfSearchObjects - 1 ) {
            throw new IndexOutOfBoundsException( "given index " + index
                    + ", expected 0.." + ( numberOfSearchObjects - 1 ) );
        }
        this.index = index;
        bitID = new BitSet( numberOfSearchObjects );
        // the bit at the 'index' position is set
        bitID.set( index );
    }

    /**
     * Returns whether a path was already found for this object and the given
     * one. <br>
     * This test is done by checking whether the correct bit is set. <br>
     * If so (see the method pathWasFound for details) this means that a search
     * between the two elements are already was done.
     * 
     * @see pathWasFound
     * @param so the other <tt>SearchObject</tt>
     * @return whether a path already exists between the two of them.
     */
    public boolean hasPathAlreadyFound(SearchObject so) {
        /*
         * If at the position of the index of the parameter object the bit is
         * set -> this means a path is already computed between these two
         * objects
         */
        return bitID.get( so.index );
    }

    /**
     * Notifies the object that a path was found between it and the given
     * object. <br>
     * E.g: One searches for <i>A </i>, <i>B </i>, <i>C </i>. The three have the
     * following initial <tt>BitSet</tt> structure: <br>
     * <ol>
     * <li>A: 00001</li>
     * <li>B: 00010</li>
     * <li>C: 00100</li>
     * </ol>
     * If a path was found the initial set bit is set for each element. So if
     * the path was found from <i>A </i> to <i>B </i> the <tt>BitSet</tt> of
     * the elements are looking like this:
     * <ol>
     * <li>A: 00011</li>
     * <li>B: 00011</li>
     * <li>C: 00100</li>
     * 
     * @param so the object for which the path was found
     */
    public void pathWasFound(SearchObject so) {
        /*
         * Sets the bit of this object to indicate that a path was found between
         * the two objects
         */
        bitID.set( so.index );
    }

    /**
     * Returns the shortest path for this object.
     * 
     * @return Returns the shortest path
     */
    public Sequence getPath() {
        return path;
    }

    /**
     * Returns the length of the current shortest path
     * 
     * @return Integer.MAX_VALUE if no path is given or the length of the path
     */
    public int getPathLength() {
        return path == null ? Integer.MAX_VALUE : path.size();
    }

    /**
     * Sets the shortest path
     * 
     * @param sequence the shortest path
     */
    public void setPath(Sequence sequence) {
        path = sequence;
    }
}