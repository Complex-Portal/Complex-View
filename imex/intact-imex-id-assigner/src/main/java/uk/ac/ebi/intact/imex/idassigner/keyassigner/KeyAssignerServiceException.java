/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.imex.idassigner.keyassigner;

/**
 * Exception thrown by the KeyAssignerService.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>11-May-2006</pre>
 */
public class KeyAssignerServiceException extends Exception {
    public KeyAssignerServiceException( String message ) {
        super( message );
    }

    public KeyAssignerServiceException( String message, Throwable cause ) {
        super( message, cause );
    }
}