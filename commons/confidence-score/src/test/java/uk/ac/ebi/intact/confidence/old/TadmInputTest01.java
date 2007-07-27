/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.confidence.old;

import uk.ac.ebi.intact.confidence.attribute.ClassifierInputWriter;

import java.io.IOException;

/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 01-Aug-2006
 */
public class TadmInputTest01 implements TestConstants {

    static String mydir = "/scratch/classify/";
    static String hcAll = mydir+"highconf_all_attribs.txt";
    static String lcAll = mydir+"lowconf_all_attribs.txt";

    public static void main(String[] args) throws IOException {

        TadmInputTest01 test = new TadmInputTest01();
    }

    public TadmInputTest01() throws IOException {

        ClassifierInputWriter ciw =
                new ClassifierInputWriter(hiConfAll, lowConfFiltered, mydir+"all.tadm", "TADM");
        ciw.writeAttribList(mydir+"all_attribs.txt");
    }

}
