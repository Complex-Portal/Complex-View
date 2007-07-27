/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.confidence.util;

import uk.ac.ebi.intact.confidence.attribute.FileCombiner;

import java.io.IOException;

/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 17-Aug-2006
 */
public class CombineFiles implements UtilConstants
{

    // front-end for the FileCombiner class
    // combine the attributes in files in standard format

    String[] inPaths;
    String outPath;

    public static void main(String[] args) throws IOException
    {

        // command-line version
        //CombineFiles combine = new CombineFiles(args);

        // test version
//        String[] testArgs = {dataDir+"highconf_all_attribs.txt", // first argument is output file
//                dataDir+"highconf_ip_attribs.txt", // subsequent arguments are input files
//                dataDir+"highconf_go_attribs.txt",
//                dataDir+"highconf_alignment_attribs.txt"
//        };

        String[] testArgs = {dataDir + "medconf_all_attribs.txt", // first argument is output file
                dataDir + "medconf_ip_attribs.txt", // subsequent arguments are input files
                dataDir + "medconf_go_attribs.txt",
                //dataDir+"medconf_alignment_attribs.txt"
        };

        CombineFiles combine = new CombineFiles(testArgs);

    }

    public CombineFiles(String[] args) throws IOException
    {

        outPath = args[0];
        inPaths = new String[args.length - 1];
        for (int i = 1; i < args.length; i++)
        {
            inPaths[i - 1] = args[i];
        }
        FileCombiner fc = new FileCombiner(inPaths, outPath);

    }

}
