/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.confidence.util;

import uk.ac.ebi.intact.confidence.RocAnalyzer;

import java.io.IOException;

/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 18-Aug-2006
 */
public class RocAnalyze
{
    // front-end for the RocAnalyzer class

    static String dir = "/scratch/classify/xvalidate/";

    public static void main(String[] args)
    {

        RocAnalyze ra = new RocAnalyze();
    }

    public RocAnalyze()
    {

        double runningtotal = 0.0;

        int points = 20; // number of RoC curve points to calculate

        double[] truePosTotals = new double[points];
        double[] trueNegTotals = new double[points];

        for (int i = 0; i < 10; i++)
        {  // loop between 1 and 10 times

            String attribPath = dir + "attribs" + i + ".txt";
            String weightPath = dir + "params" + i + ".txt";
            String posTest = dir + "highconf" + i + ".txt";
            String negTest = dir + "lowconf" + i + ".txt";

            try
            {
                RocAnalyzer ra = new RocAnalyzer(attribPath, weightPath, posTest, negTest);
                ra.printSummary();
//                double correct = ra.getFractionCorrect();
//                runningtotal = runningtotal + correct;
//                int step = i+1;
//                String comment = "Step "+step+" of 10:  Fraction correct = "+correct;
//                System.out.println(comment);
//                ra.printRocPoints(20);
                double[] truePos = ra.truePositives(points);
                double[] trueNeg = ra.trueNegatives(points);
                for (int j = 0; j < points; j++)
                {
                    truePosTotals[j] = truePosTotals[j] + truePos[j];
                    trueNegTotals[j] = trueNegTotals[j] + trueNeg[j];
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
                System.out.println(e.toString());
                System.exit(0);
            }
        }


        System.out.println("[Fraction of true positives] [Fraction of true negatives]");
        for (int i = 0; i < points; i++)
        {
            double meanTruePos = truePosTotals[i] / 10.0;
            double meanTrueNeg = trueNegTotals[i] / 10.0;
            String out = meanTruePos + "\t" + meanTrueNeg;
            System.out.println(out);
        }


        double overallCorrect = runningtotal / 10;
        String overall = "Average fraction correct = " + overallCorrect;
        System.out.println(overall);
    }


}
