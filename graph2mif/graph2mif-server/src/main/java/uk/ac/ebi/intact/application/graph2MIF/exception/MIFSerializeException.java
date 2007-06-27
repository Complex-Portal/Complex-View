/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.graph2MIF.exception;

/**
 * This Exception should be thrown, if there the DOM-Object could not be serialized
 * @author Henning Mersch <hmersch@ebi.ac.uk>
 * @version $Id$
 */
public class MIFSerializeException extends Exception {
    public MIFSerializeException() {
    }

    public MIFSerializeException(String message) {
        super(message);
    }

    public MIFSerializeException(String message, Throwable cause) {
        super(message, cause);
    }
}
