/*
 * Created on 06.08.2004
 */
package uk.ac.ebi.intact.application.mine.business.graph.model;

import java.io.Serializable;

/**
 * A node object is attached to the graph used in the MiNe application. <br>
 * It stores the accession number and the shortlable for a protein.
 * 
 * @author Andreas Groscurth
 */
public class NodeObject implements Serializable
{
    private String acNr;
    private String shortLabel;

    public NodeObject(String a, String s) {
        acNr = a;
        shortLabel = s;
    }

    /**
     * @return Returns the acNr.
     */
    public String getAcNr() {
        return acNr;
    }

    /**
     * @return Returns the shortLabel.
     */
    public String getShortLabel() {
        return shortLabel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return acNr + " - " + shortLabel;
    }
}