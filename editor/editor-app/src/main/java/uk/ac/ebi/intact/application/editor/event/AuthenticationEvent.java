/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.event;



/**
 * The authentication event
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class AuthenticationEvent {

    /**
     * The user name.
     */
    private String myUserName;

    /**
     * Creates an instance of this class with given user name.
     * @param name the user name.
     */
    public AuthenticationEvent(String name) {
        myUserName = name.toLowerCase();
    }

    // Read properties of this class.

    public String getUserName() {
        return myUserName;
    }

    // Override equals property and clone methods.
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            // x.equals(x)
            return true;
        }
        // Allow for slice comparision.
        if (object instanceof AuthenticationEvent) {
            // Comparision is based on the user name.
            return myUserName.equals(((AuthenticationEvent) object).myUserName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 17 * myUserName.hashCode();
    }
}