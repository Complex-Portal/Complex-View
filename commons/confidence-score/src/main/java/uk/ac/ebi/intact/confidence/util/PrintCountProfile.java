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
public class PrintCountProfile implements UtilConstants
{

    public static void main(String[] args) throws IOException
    {

        // test version -- comment this out for command-line utility
        String path = dataDir + "foo.txt"; //"medconf_go_attribs.txt";

        // command-line utility version
        //String path = args[0];

        PrintCountProfile print = new PrintCountProfile(path);
    }

    public PrintCountProfile(String path) throws IOException
    {

        FileMethods.printCountProfile(path);

        int total = FileMethods.findTotalInteractions(path);
        String comment = total + " total interactions present in file.";
        System.out.println(comment);
    }

}
