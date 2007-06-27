/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.graph2MIF.exception;

/**
 * This Exception should be thrown, if there were no interactors found for the ac.
 * @author Henning Mersch <hmersch@ebi.ac.uk>
 * @version $Id$
 */
public class NoInteractorFoundException extends Exception {
    public NoInteractorFoundException() {
    };

    public NoInteractorFoundException(String message) {
        super(message);
    };
}
