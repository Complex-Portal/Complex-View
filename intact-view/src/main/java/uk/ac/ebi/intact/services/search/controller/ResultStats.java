package uk.ac.ebi.intact.services.search.controller;

import java.io.Serializable;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ResultStats implements Serializable {

    private int cvObjectCount;
    private int proteinCount;
    private int smallMoleculeCount;
    private int nucleicAcidCount;
    private int interactionCount;
    private int experimentCount;

    protected ResultStats() {
        
    }

    protected ResultStats(int cvObjectCount, int proteinCount, int smallMoleculeCount, int nucleicAcidCount,
                          int interactionCount, int experimentCount) {
        this.cvObjectCount = cvObjectCount;
        this.proteinCount = proteinCount;
        this.smallMoleculeCount = smallMoleculeCount;
        this.nucleicAcidCount = nucleicAcidCount;
        this.interactionCount = interactionCount;
        this.experimentCount = experimentCount;
    }

    public int getCvObjectCount() {
        return cvObjectCount;
    }

    public void setCvObjectCount(int cvObjectCount) {
        this.cvObjectCount = cvObjectCount;
    }

    public int getProteinCount() {
        return proteinCount;
    }

    public void setProteinCount(int proteinCount) {
        this.proteinCount = proteinCount;
    }

    public int getSmallMoleculeCount() {
        return smallMoleculeCount;
    }

    public void setSmallMoleculeCount(int smallMoleculeCount) {
        this.smallMoleculeCount = smallMoleculeCount;
    }

    public int getInteractionCount() {
        return interactionCount;
    }

    public void setInteractionCount(int interactionCount) {
        this.interactionCount = interactionCount;
    }

    public int getExperimentCount() {
        return experimentCount;
    }

    public void setExperimentCount(int experimentCount) {
        this.experimentCount = experimentCount;
    }

    public int getNucleicAcidCount() {
        return nucleicAcidCount;
    }
}
