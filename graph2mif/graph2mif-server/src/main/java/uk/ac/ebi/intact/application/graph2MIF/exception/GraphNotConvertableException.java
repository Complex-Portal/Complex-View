/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.graph2MIF.exception;

/**
 * This Exception should be thrown, if a fatal Error occours - so the PSIFormat
 * will NOT be valid without this mising Element
 * @author Henning Mersch <hmersch@ebi.ac.uk>
 * @version $Id$
 */
public class GraphNotConvertableException extends Exception {
    public GraphNotConvertableException() {
    }

    public GraphNotConvertableException(String message) {
        super(message);
    }
}
