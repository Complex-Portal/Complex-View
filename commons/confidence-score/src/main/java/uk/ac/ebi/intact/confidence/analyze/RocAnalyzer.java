/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.confidence.analyze;

import opennlp.maxent.GISModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.confidence.maxent.MaxentUtils;
import uk.ac.ebi.intact.confidence.weights.inputs.OpenNLP;

import java.io.*;

/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 17-Aug-2006
 */
public class RocAnalyzer {
    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( RocAnalyzer.class );

    // analyse false-positive and false-negative results from maxent classifier
    // determine false-positive and false-negative according to score threshold used
    // store data for receiver-operator characteristic (ROC)
    // ROC = (TP)/N against (1-FP)/N
    // TP = true positives, FP = false positives, N = total data points

    // for each point in positive test set, classification as positive = true positive
    //                                                        negative = false negative
    //                   negative test set, classification as positive = false positive
    //                                                        negative = true negative

   // MaxEntClassifier mec;
    GISModel mec;
    String posTest;
    String negTest;

    static boolean verbose = false; // debug switch


    /*String attribPath, String weightPath,*/
    public RocAnalyzer( File gisInput, String posTest, String negTest ) throws IOException {

        // posTest and negTest are paths to positive/negative test sets
        // attributes and weights must be for a classifier trained without postTest and negTest
        // points = number of points in ROC curve to calculate (eg. 50)

        this.posTest = posTest;
        this.negTest = negTest;

      //  mec = new MaxEntClassifier( attribPath, weightPath );
        mec = MaxentUtils.createModel(new FileInputStream(gisInput) );


    }

    public void printSummary(double threshold, boolean equality) throws IOException {

//        threshold = 0.5;
//        equality = true;
        int total = totalNeg() + totalPos();

        // first set threshold >= 0.5


        int truePos = truePositives( threshold, equality );
        int falsePos = falsePositives( threshold, equality );

        if ( log.isInfoEnabled() ) {
            log.info("Total pozitives: " + totalPos());
            log.info("Total negatives: " + totalNeg());
            String comment = "True positives = " + truePos;
            log.info( comment );
            comment = "False positives = " + falsePos;
            log.info( comment );
            comment = "Total = " + total;
            log.info( comment );
        }

        int trueNeg = totalNeg() - falsePos;
        int correct = truePos + trueNeg;
        double frac = new Double( correct ) / new Double( total );

        if ( log.isInfoEnabled() ) {
            String out = "Score threshold >"+ (equality ? "= " : " " )+ threshold;
            log.info( out );
            out = correct + " points of " + total + " correctly classified.\n";
            log.info( out );
            out = "Fraction correct = " + frac;
            log.info( out );
        }
     //   int greater = truePositives( threshold, equality );
    }

    public void printRocPoints( int points ) throws IOException {
        if(log.isInfoEnabled()){
            log.info("Printing Roc Points: " + points);
        }

        double[] thresholds = new double[points];
        for ( int i = 0; i < points; i++ ) {
            thresholds[i] = new Double( ( i + 1.0 ) ) * ( 1 / new Double( points ) );
            if ( log.isInfoEnabled() ) {
                String comment = "threshold = " + thresholds[i];
                log.info(comment );
            }
        }
        double[] truePositives = positives( thresholds, true );
        double[] falsePositives = positives( thresholds, false );
        double[] trueNegatives = new double[points];
        for ( int i = 0; i < points; i++ ) {
            trueNegatives[i] = 1.0 - falsePositives[i];
        }

        log.info( "[Fraction of true positives] [Fraction of true negatives]" );
        for ( int i = 0; i < points; i++ ) {
            String out = truePositives[i] + "\t" + trueNegatives[i];
            log.info( out );
        }

        if(log.isInfoEnabled()){
            log.info("Printing Roc Points End.");
        }
    }

    public double[] truePositives( int points ) throws IOException {

        double[] thresholds = new double[points];
        for ( int i = 0; i < points; i++ ) {
            thresholds[i] = new Double( ( i + 1.0 ) ) * ( 1 / new Double( points ) );
            if ( log.isInfoEnabled() ) {
                String comment = "threshold = " + thresholds[i];
                log.info( comment );
            }
        }

        return positives( thresholds, true );
    }

    public double[] trueNegatives( int points ) throws IOException {

        double[] thresholds = new double[points];
        for ( int i = 0; i < points; i++ ) {
            thresholds[i] = new Double( ( i + 1.0 ) ) * ( 1 / new Double( points ) );
            if ( log.isInfoEnabled() ) {
                String comment = "threshold = " + thresholds[i];
                log.info(comment );
            }
        }
        double[] falsePositives = positives( thresholds, false );
        double[] trueNegatives = new double[points];
        for ( int i = 0; i < points; i++ ) {
            trueNegatives[i] = 1.0 - falsePositives[i];
        }

        return trueNegatives;


    }

    public double getFractionCorrect() throws IOException {

        double threshold = 0.5;
        int total = totalNeg() + totalPos();

        int truePos = truePositives( threshold, true );
        int falsePos = falsePositives( threshold, true );

//        if (verbose) {
//            String comment = "True positives = " + truePos;
//            System.out.println(comment);
//            comment = "False positives = " + falsePos;
//            System.out.println(comment);
//            comment = "Total = " + total;
//            System.out.println(comment);
//        }

        int trueNeg = totalNeg() - falsePos;
        int correct = truePos + trueNeg;
        double frac = new Double( correct ) / new Double( total );

        return frac;
    }

    private double[] positives( double thresholds[], boolean positiveType )
            throws IOException {
        // get fractions of true (or false) positives for a given threshold
        // assume equality for threshold


        Double total = 0.0;
        if ( positiveType == true ) {
            total = new Double( totalPos() );
        } else {
            total = new Double( totalNeg() );
        }

        double[] positiveFractions = new double[thresholds.length];
        for ( int i = 0; i < thresholds.length; i++ ) {
            if ( positiveType == true ) {
                positiveFractions[i] = ( new Double( truePositives( thresholds[i], true ) ) / total );
            } else {
                positiveFractions[i] = ( new Double( falsePositives( thresholds[i], true ) ) / total );
            }

        }
        return positiveFractions;

    }

    private int truePositives( double threshold, boolean equality ) throws IOException {

        int truePos = 0;

        FileReader fr = new FileReader( posTest );
        BufferedReader br = new BufferedReader( fr );
        double tScore;
        String line;
        while ( ( line = br.readLine() ) != null ) {
          //  tScore = mec.trueScoreFromLine( line );
            String [] attribs =  OpenNLP.getAttribsFromLine( line);
            double [] scores = mec.eval( attribs);
            tScore = scores[0];
            if ( equality && tScore >= threshold ) {
                truePos++;
            } else if ( !equality && tScore > threshold ) {
                truePos++;
            }
        }
        fr.close();
        return truePos;
    }

    private int falsePositives( double threshold, boolean equality ) throws IOException {

        int falsePos = 0;

        FileReader fr = new FileReader( negTest );
        BufferedReader br = new BufferedReader( fr );
        double tScore;
        String line;
        while ( ( line = br.readLine() ) != null ) {
           //  tScore = mec.trueScoreFromLine( line );
            String [] attribs =  OpenNLP.getAttribsFromLine( line);
            double [] scores = mec.eval( attribs);
            tScore = scores[0];
            if ( equality && tScore >= threshold ) {
                falsePos++;
            } else if ( !equality && tScore > threshold ) {
                falsePos++;
            }
        }
        fr.close();
        return falsePos;

    }

    private int totalPos() throws IOException {
        // find total of positive & negative examples
        int total = 0;
        FileReader fr = new FileReader( posTest );
        BufferedReader br = new BufferedReader( fr );
        while ( br.readLine() != null ) {
            total++;
        }
        fr.close();
        return total;

    }

    private int totalNeg() throws IOException {
        // find total of negative examples

        int total = 0;
        FileReader fr = new FileReader( negTest );
        BufferedReader br = new BufferedReader( fr );
        while ( br.readLine() != null ) {
            total++;
        }
        fr.close();
        return total;

    }


}
