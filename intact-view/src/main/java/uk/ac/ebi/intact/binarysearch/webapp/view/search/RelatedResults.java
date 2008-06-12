/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.binarysearch.webapp.view.search;

import java.io.Serializable;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class RelatedResults implements Serializable {

    private int numOfExperiments;
    private int numOfInteractors;

    public RelatedResults() {
    }

    public RelatedResults(int numOfExperiments, int numOfInteractors) {
        this.numOfExperiments = numOfExperiments;
        this.numOfInteractors = numOfInteractors;
    }

    public int getNumOfExperiments() {
        return numOfExperiments;
    }

    public void setNumOfExperiments(int numOfExperiments) {
        this.numOfExperiments = numOfExperiments;
    }

    public int getNumOfInteractors() {
        return numOfInteractors;
    }

    public void setNumOfInteractors(int numOfInteractors) {
        this.numOfInteractors = numOfInteractors;
    }
}