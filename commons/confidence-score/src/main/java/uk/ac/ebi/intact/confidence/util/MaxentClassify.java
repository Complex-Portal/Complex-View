/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.confidence.util;

import uk.ac.ebi.intact.confidence.MaxEntClassifier;

import java.io.IOException;

/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 17-Aug-2006
 */
public class MaxentClassify
{

    // front end for the MaxEntClassifier class

    // test variables
    static String dir = "/scratch/classify/";
    static String inPath = dir + "medconf_all_attribs.txt";
    //static String inPath = dir+"medconf_ip_attribs.txt";
    static String outPath = dir + "medconf_all_scores.txt";

    public static void main(String[] args)
    {

        try
        {
            MaxentClassify mc = new MaxentClassify();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.out.println(e.toString());
            System.exit(0);
        }

    }

    public MaxentClassify() throws IOException
    {

        MaxEntClassifier mec = new MaxEntClassifier(dir + "all_attribs.txt", dir + "all_params.txt");

        //mec.writeInteractionScores(inPath, outPath);
        mec.printScoreProfile(inPath);

    }

}
