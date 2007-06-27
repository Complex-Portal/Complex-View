/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.graph2MIF.exception;

/**
 * This Exception should be thrown, if there were occoured a exception while retrieving the graph
 * @author Henning Mersch <hmersch@ebi.ac.uk>
 * @version $Id$
 */
public class NoGraphRetrievedException extends Exception {
    public NoGraphRetrievedException() {
    };

    public NoGraphRetrievedException(String message) {
        super(message);
    };
}
