/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.imex.idassigner.update;

import uk.ac.ebi.intact.model.Interactor;

import java.util.Set;

/**
 * TODO comment this
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>15-May-2006</pre>
 */
public class InteractionClassifier { // implements Comparable {

    private Set<Interactor> distinctInteractors;

    private String imexId = null;

    public InteractionClassifier( Set<Interactor> interactors ) {

        if ( interactors == null || interactors.isEmpty() ) {
            throw new IllegalArgumentException( "The Set of interactors must not be null." );
        }

        this.distinctInteractors = interactors;
    }

    ////////////////////////////
    // Getters and Setters

    public Set<Interactor> getDistinctInteractors() {
        return distinctInteractors;
    }

    public String getImexId() {
        return imexId;
    }

    public void setImexId( String imexId ) {
        this.imexId = imexId;
    }

    ////////////////////////////
    // Comparable

//    /**
//     * @param o
//     *
//     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
//     *         the specified object.
//     */
//    public int compareTo( Object o ) {
//
//        InteractionClassifier otherKey = (InteractionClassifier) o;
//
//        boolean set1hasOnlyProteins = hasOnlyProteins( distinctInteractors );
//        boolean set2hasOnlyProteins = hasOnlyProteins( otherKey.distinctInteractors );
//
//        if ( set1hasOnlyProteins && set2hasOnlyProteins ) {
//
//            // TODO FIX - when inserting a value in the map, if compare == 0, the value is replaced.
//
//            return 0;
//        } else if ( set1hasOnlyProteins ) {
//            return Integer.MIN_VALUE; // current Key comes first
//        } else {
//            return Integer.MAX_VALUE;  // Other Key comes first
//        }
//    }
//
//    /**
//     * check weither all Interactors in the set are Proteins.
//     *
//     * @param set the set of Interactor to test.
//     *
//     * @return true of all are Protein, false otherwise.
//     */
//    private boolean hasOnlyProteins( Set<Interactor> set ) {
//
//        if ( set.isEmpty() ) {
//            throw new IllegalStateException( "The set of Interactors must not be empty." );
//        }
//
//        for ( Interactor interactor : set ) {
//            if ( ! proteinType.equals( interactor.getCvInteractorType() ) ) {
//                return false;
//            }
//        }
//        return true;
//    }

    ////////////////////////////
    // Object's override

    // Only the distinct set of Interactors can distinguish two classifier.

    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( o == null || getClass() != o.getClass() ) {
            return false;
        }

        final InteractionClassifier that = (InteractionClassifier) o;

        if ( !distinctInteractors.equals( that.distinctInteractors ) ) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = distinctInteractors.hashCode();
        return result;
    }
}