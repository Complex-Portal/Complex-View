/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.confidence.old;

import uk.ac.ebi.intact.confidence.FileMethods;

import java.io.IOException;

/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 02-Aug-2006
 */
public class CountProfileTest01 implements TestConstants {

    public static void main(String[] args) {
        CountProfileTest01 test = new CountProfileTest01();
    }

    public CountProfileTest01() {
        //String path = dir + "highconf_all_attribs.txt";
        String path = dir + "highconf_go_attribs.txt";
        try {
            FileMethods.printCountProfile(path);
            FileMethods.findCountProfile(path, 100);
            path = dir + "lowconf_go_attribs.txt";
            FileMethods.printCountProfile(path);
            FileMethods.findCountProfile(path, 100);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.toString());
            System.exit(0);
        }
    }

}
