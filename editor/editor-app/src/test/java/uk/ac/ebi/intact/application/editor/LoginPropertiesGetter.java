/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.editor;

import uk.ac.ebi.intact.util.PropertyLoader;

import java.util.Properties;

/**
 * TODO comment it.
 *
 * @author Catherine Leroy (cleroy@ebi.ac.uk)
 * @version $Id$
 */
public class LoginPropertiesGetter {
    private Properties properties;

    private static final String NAME = "name";

    private static final String PASSWORD = "password";

    public LoginPropertiesGetter(){
        properties = PropertyLoader.load("/uk/ac/ebi/intact/application/editor/Login.properties");
    }

    public String getName(){
        return properties.getProperty(NAME);
    }
    public String getPassword(){
        return properties.getProperty(PASSWORD);
    }

    public static void main(String[] args) {
        LoginPropertiesGetter p = new LoginPropertiesGetter();
        System.out.println("name : " + p.getName() );
    }
}
