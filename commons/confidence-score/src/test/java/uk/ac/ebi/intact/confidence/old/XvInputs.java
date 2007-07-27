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
 * @since 18-Aug-2006
 */
public class XvInputs {

    // create multiple TADM input files for cross-validation

    static String dir = "/scratch/classify/xvalidate/";

    static String hcAll = dir + "highconf_all_attribs.txt";
    static String lcAll = dir + "lowconf_all_attribs.txt";

    public static void main(String[] args) {

        XvInputs cvc = new XvInputs();
    }

    public XvInputs() {

        String prefix = "highconf";
        for (int i = 0; i < 10; i++) {

            String hcPath = dir + "hctrain" + i + ".txt";
            String lcPath = dir + "lctrain" + i + ".txt";
            String outPath = dir + "events" + i + ".txt";

            try {
                ClassifierInputWriter ciw =
                        new ClassifierInputWriter(hcPath, lcPath, outPath, "TADM");
                ciw.writeAttribList(dir+"attribs"+i+".txt");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(e.toString());
                System.exit(0);
            }

        }


    }


}
