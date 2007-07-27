/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.confidence.util;

import uk.ac.ebi.intact.confidence.attribute.FileMaker;
import uk.ac.ebi.intact.confidence.BinaryInteractionSet;

import java.io.File;
import java.io.IOException;

/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 14-Aug-2006
 *        <p/>
 *        Class to generate an alignment file in standard format
 *        Input:  Files with list of highconf proteins, list of proteins to align, list of hits
 *        Command-line arguments:
 *        -h highconf file
 *        -l main name list
 *        -a list of significant sequence alignnments
 */
public class GenerateAlignmentFile implements UtilConstants
{

    static String notes = "GenerateAlignmentFile.java\nRequired arguments:\n" +
            "\t-h [Path to high-confidence binary interaction list]\n" +
            "\t-l [Path to main protein list]\n" +
            "\t-o [Path for output file]\n" +
            "See documentation for file formats.";


    String hcPath;
    String hitPath;
    String outPath;


    public static void main(String[] args)
    {

        GenerateAlignmentFile generator = new GenerateAlignmentFile(args);


    }

    public GenerateAlignmentFile(String[] args)
    {

        if (args.length == 0)
        {
            System.out.println(notes);
            System.exit(0);
        }

        File hcFile;
        File hitFile;

        try
        {
            for (int i = 0; i < args.length; i++)
            {
                if (args[i].equals("-h"))
                {
                    hcPath = args[i + 1];
                    hcFile = new File(hcPath);
                    if (!hcFile.canRead())
                    {
                        throw new IOException("Incorrect argument to GenerateAlignmentFile.main()");
                    }

                }
                else if (args[i].equals("-a"))
                {
                    hitPath = args[i + 1];
                    hitFile = new File(hitPath);
                    if (!hitFile.canRead())
                    {
                        throw new IOException("Incorrect argument to GenerateAlignmentFile.main()");
                    }
                }
                else if (args[i].equals("-o"))
                {
                    outPath = args[i + 1];
                }
            }
            writeAlignmentFile();
        }
        catch (Exception e)
        {
            System.out.println(e.toString());
            e.printStackTrace();
            System.out.println(notes);
            System.exit(0);
        }


    }

    private void writeAlignmentFile
            () throws IOException
    {
        FileMaker fm = new FileMaker(new BinaryInteractionSet(hcPath));
        fm.writeAlignmentAttributes(hitPath, hcPath, outPath);
    }

}
