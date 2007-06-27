/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.  
All rights reserved. Please see the file LICENSE 
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.hierarchview.exception;

/**
 * Thrown when the user query give several interactors.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class MultipleResultException extends Exception {

    public MultipleResultException() {
    }

    public MultipleResultException(String message) {
        super(message);
    }
}
