/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.event;

import java.util.Date;
import java.util.Calendar;

/**
 * The login event
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class LoginEvent extends AuthenticationEvent {

    /**
     * The login time.
     */
    private Date myLoginTime;

    public LoginEvent(String name) {
        super(name);
        myLoginTime = Calendar.getInstance().getTime();
    }

    // Read properties of this class.

    public Date getLoginTime() {
        return myLoginTime;
    }
}