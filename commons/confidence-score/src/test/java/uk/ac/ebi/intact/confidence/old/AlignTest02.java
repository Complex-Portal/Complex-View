/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.confidence.old;

import uk.ac.ebi.intact.confidence.attribute.ClassifierInputWriter;
import uk.ac.ebi.intact.confidence.attribute.AnnotationConstants;
import uk.ac.ebi.intact.confidence.attribute.Attribute;
import uk.ac.ebi.intact.confidence.MaxEntClassifier;
import uk.ac.ebi.intact.confidence.FileMethods;

import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.regex.Pattern;
import java.util.HashSet;

/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 11-Aug-2006
 */
public class AlignTest02 implements TestConstants {

    static String myDir = dir + "alignment/";

    MaxEntClassifier mec;

    public static void main(String[] args) throws IOException {

        AlignTest02 test = new AlignTest02();
    }

    public AlignTest02() throws IOException {

        try {
            ClassifierInputWriter ciw = new ClassifierInputWriter(
//                    myDir + "highconf_alignment_attribs.txt",
//                    myDir + "lowconf_alignment_attribs.txt",
//                    myDir + "align_test.tadm",
                    myDir + "highconf_alignment_attribs.txt",
                    myDir + "lc_alignment_attribs.txt",
                    myDir + "align_test.tadm",
                    "TADM");
                ciw.writeAttribList(myDir+"align_test_attribs.txt");
        } catch (IllegalArgumentException iae) {
            String comment = "Warning: " + iae.toString();
            System.out.println(comment);
        }

//        String weightfile = myDir + "align_test_params.txt";
//        String attribfile = myDir+"align_test_attribs.txt";
//
//       mec = new MaxEntClassifier(attribfile, weightfile);
//        printTestScores(myDir+"medconf_alignment_attribs.txt",0.5,false);

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
