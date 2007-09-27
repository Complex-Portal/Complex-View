/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.exception.validation;

/**
 * Thrown when the validation for a BioSource fails.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class BioSourceException extends ValidationException {

    /**
     * Construst with given message key
     * @param mkey the message key.
     */
    public BioSourceException(String mkey) {
        super(mkey, "bs.validation");
    }

	/**
	 * Construst with given filter key and message key.
	 * @param fkey the filter key.
	 * @param mkey the message key.
	 */
	public BioSourceException(String fkey, String mkey) {
		super(mkey, fkey);
	}    
}
