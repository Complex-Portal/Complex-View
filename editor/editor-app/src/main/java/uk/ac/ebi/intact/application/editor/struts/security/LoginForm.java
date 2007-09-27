/*
Copyright (c) 2002-2005 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.security;

import org.apache.struts.validator.ValidatorForm;

/**
 * The login form.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 *
 * @struts.form name="loginForm"
 */
public class LoginForm extends ValidatorForm {

    /**
     * The login name.
     */
    private String myUsername;

    /**
     * The password.
     */
    private String myPassword;

    /**
     * The AC (needed when accessing the editor directly)
     */
    private String myAc;

    /**
     * The type (needed when accessing the editor directly)
     */
    private String myType;

    // Getter/Setter methods

    public String getUsername() {
        return myUsername;
    }

    /**
     * @struts.validator type="required"
     * @struts.validator-args arg0resource="loginForm.label.username"
     */
    public void setUsername(String user) {
        myUsername = user;
    }

    public String getPassword() {
        return myPassword;
    }

    /**
     * @struts.validator type="required"
     * @struts.validator-args arg0resource="loginForm.label.password"
     */
    public void setPassword(String password) {
        myPassword = password;
    }

    public String getAc() {
        return myAc;
    }

    public void setAc(String ac) {
        myAc = ac;
    }

    public String getType() {
        return myType;
    }

    public void setType(String type) {
        myType = type;
    }
}
