/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.exception.validation;

import uk.ac.ebi.intact.application.editor.exception.BaseException;

/**
 * Super exception class for all the validation exceptions.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class ValidationException extends BaseException {

    /**
     * The filter key to filter messages.
     */
    private String myFilterKey;

    /**
     * Constructs with message and filter keys.
     * @param mkey the message key.
     * @param fkey the filter key.
     */
    protected ValidationException(String mkey, String fkey) {
        setMessageKey(mkey);
        myFilterKey = fkey;
    }

    // Getter methods.

    public String  getFilterKey() {
        return myFilterKey;
    }
}
