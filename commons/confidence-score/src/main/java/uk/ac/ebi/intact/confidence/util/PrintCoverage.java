/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.confidence.util;

import uk.ac.ebi.intact.confidence.FileMethods;

import java.io.IOException;

/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 16-Aug-2006
 */
public class PrintCoverage implements UtilConstants
{

    public static void main(String[] args) throws IOException
    {

        // test version -- comment this out for command-line utility
        //String path = dataDir + "lowconf_go_attribs.txt";
        String path = dataDir + "lowconf_all_attribs.txt";

        // command-line utility version
        //String path = args[0];

        PrintCoverage print = new PrintCoverage(path);
    }

    public PrintCoverage(String path) throws IOException
    {

        double cov = FileMethods.findCoverage(path);
        String out = "Proportion of interactions without attributes = " + cov;
        System.out.println(out);
    }

}
