/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.exception;

/**
 * Exception thrown for any problems with IntactTypes resource file. For
 * example, it may be for not finding the resource file or an empty file.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class AuthenticateException extends BaseException {

    /**
     * Constructor with a message.
     * @param msg the message explaing the exception.
     */
    public AuthenticateException(String msg) {
        super(msg);
    }
}
