/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.exception.validation;


/**
 * Thrown when the validation for an experiment fails.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class ExperimentException extends ValidationException {

    /**
     * Default constructor. Uses the default message and filter keys.
     */
    public ExperimentException() {
        this("error.exp.validation");
    }
    /**
     * Construst with given message key
     * @param mkey the message key.
     */
    public ExperimentException(String mkey) {
        this("exp.validation", mkey);
    }

    /**
     * Construst with given filter key and message key.
     * @param fkey the filter key.
     * @param mkey the message key.
     */
    public ExperimentException(String fkey, String mkey) {
        super(mkey, fkey);
    }
}
