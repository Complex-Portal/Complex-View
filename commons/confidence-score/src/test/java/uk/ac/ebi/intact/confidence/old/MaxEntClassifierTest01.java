/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.confidence.old;

import uk.ac.ebi.intact.confidence.MaxEntClassifier;
import uk.ac.ebi.intact.confidence.FileMethods;
import uk.ac.ebi.intact.confidence.attribute.Attribute;
import uk.ac.ebi.intact.confidence.attribute.AnnotationConstants;

import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 04-Aug-2006
 */
public class MaxEntClassifierTest01 implements TestConstants {

    private MaxEntClassifier mec;

    public static void main(String[] args) {

        try {
            MaxEntClassifierTest01 test = new MaxEntClassifierTest01();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.toString());
            System.exit(0);
        }
    }

    public MaxEntClassifierTest01() throws IOException {

        String weightfile = dir + "params_test01.tadm";
        String attribfile = dir + "test01_attribs.txt";
        mec = new MaxEntClassifier(attribfile, weightfile);
        System.out.println("Score greater than or equal to 0.5:");
        System.out.println("High-confidence data:");
        printTestScores(hiConfAll, 0.5, true);
        //printTestScores( dir + "highconf_ip_attribs.txt", 0.5, true);
        System.out.println("Low-confidence data:");
        printTestScores(lowConfAll, 0.5, true);
        System.out.println("Medium-confidence data (all):");
        printTestScores(medConfAll, 0.5, true);

        System.out.println("\nScore greater than 0.5:");
        System.out.println("High-confidence data:");
        printTestScores(hiConfAll, 0.5, false);
        //printTestScores( dir + "highconf_ip_attribs.txt", 0.5, true);
        System.out.println("Low-confidence data:");
        printTestScores(lowConfAll, 0.5, false);
        System.out.println("Medium-confidence data (all):");
        printTestScores(medConfAll, 0.5, false);

        System.out.println("Additional low-confidence data (InterPro only):");
        printTestScores(dir + "lowconf2_ip_attribs.txt", 0.5, false);
        printTestScores(dir + "lowconf2_ip_attribs.txt", 0.5, true);

//
//         System.out.println("High-confidence data (InterPro only):");
//        printTestScores( dir + "highconf_ip_attribs.txt");
//                 System.out.println("Low-confidence data (InterPro only):");
//        printTestScores( dir + "lowconf_ip_attribs.txt");
//                         System.out.println("Medium-confidence data (InterPro only):");
//        printTestScores( dir + "medconf_ip_attribs.txt");
//
//        System.out.println("Low-confidence data 2 (InterPro only):");
//        printTestScores(dir + "lowconf2_ip_attribs.txt");


    }

    private void printTestScores(
            String inFile, double threshold, boolean equality) throws IOException {
        FileReader fr = new FileReader(inFile);
        BufferedReader br = new BufferedReader(fr);
        String line;


        int hiConfTotal = 0;
        int lineCount = 0;
        while ((line = br.readLine()) != null) {
            if (Pattern.matches(AnnotationConstants.commentExpr, line)) continue;
            lineCount++;
            HashSet<Attribute> attribs = new HashSet<Attribute>();
            try {
                attribs = FileMethods.parseAttributeLine(line);
            } catch (IllegalArgumentException e) {
                String comment = "Warning:  Cannot parse attribute " + line;
                System.out.println(comment);
            }
            Double[] probs = mec.probs(attribs);
//            Double tScore = mec.score(attribs, true);
//            Double fScore = mec.score(attribs, false);
            Double tScore = probs[0];
            if (equality && tScore >= threshold) hiConfTotal++;
            else if (tScore > threshold) hiConfTotal++;
            Double fScore = probs[1];
            String out = FileMethods.getFirstItem(line) + ": " + tScore + "\t" + fScore;
            //System.out.println(out);
        }
        String eqString;
        if (equality) {
            eqString = ">=";
        } else {
            eqString = ">";
        }
        String comment = hiConfTotal + " of " + lineCount
                + " interactions with true-interaction probability " + eqString + " " + threshold;
        System.out.println(comment);

    }

}
