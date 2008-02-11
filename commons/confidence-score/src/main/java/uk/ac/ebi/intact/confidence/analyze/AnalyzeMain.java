/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package uk.ac.ebi.intact.confidence.analyze;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.confidence.weights.inputs.OpenNLP;

import java.io.*;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *                                                                13-Nov-2007
 *                                                                </pre>
 */
public class AnalyzeMain {
    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( AnalyzeMain.class );
    private File workDir;
    private File splitsDir;
    private File trainDir;
    private int nrRuns;

    public AnalyzeMain( File workDir, int nrRuns ) {
        this.workDir = workDir;
//        this.nrRuns = nrRuns;
//        this.splitsDir = new File( workDir, "splits" );
//        this.splitsDir.mkdir();
//        this.trainDir = new File( workDir, "trainTestSets" );
//        trainDir.mkdir();
    }

    public void splitToTrainTest( File posSet, File negSet, int percent, File outDir ) {
        splitFile( posSet, percent, outDir, "hc" );
        splitFile( negSet, percent, outDir, "lc" );
    }

    /**
     * @param inFile  : the best split if linesNr = (k* 100)
     * @param percent : nr (0..100) where 0 and 100 do not make any sence,
     *                it is indicated that percent is included in 20..80
     * @param outDir
     */
    private void splitFile( File inFile, int percent, File outDir, String type ) {
        try {
            File trainFile = new File( outDir, "trainSet_" + type + ".txt" );
            File testFile = new File( outDir, "testSet_" + type + ".txt" );
            Writer writerTrain = new FileWriter( trainFile );
            Writer writerTest = new FileWriter( testFile );
            BufferedReader br = new BufferedReader( new FileReader( inFile ) );
            String line = "";
            int lineNr = 0;
            int testNr = 0;
            int trainNr = 100 - percent;
            while ( ( line = br.readLine() ) != null ) {
                if ( testNr < percent ) {
                    writerTest.append( line + "\n" );
                    testNr++;
                } else if (trainNr > 0){
                    writerTrain.append( line + "\n" );
                    trainNr--;
                    if (trainNr == 0){
                       testNr =0; trainNr = 100 -percent;
                    }
                }
            }
            br.close();
            writerTest.close();
            writerTrain.close();
        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     * @param ioDir : must contain "trainSet_hc.txt" and "trainSet_lc.txt" Files;
     *              will contain the "gisInput.txt" output file
     */
    public void generateGisInput( File ioDir ) {
        File hcFile = new File( ioDir, "trainSet_hc.txt" );
        File lcFile = new File( ioDir, "trainSet_lc.txt" );
        File outFile = new File( ioDir, "gisInput.txt" );
        OpenNLP.createInput( hcFile.getPath(), lcFile.getPath(), outFile.getPath() );
    }

    public void rocAnalyze( double threshold, boolean equality, File inDir ) {
//        File gisInput = new File( inDir, "gisInput.txt" );
//        File posTest = new File( inDir, "testSet_hc.txt" );
//        File negTest = new File( inDir, "testSet_lc.txt" );
//        try {
//            RocAnalyzer ra = new RocAnalyzer( gisInput, posTest.getPath(), negTest.getPath() );
//            boolean equality = false;
//            for (double threshold = 0.1; threshold <1 ; threshold +=0.1){
//                ra.printSummary( threshold, equality );    //threshold >= 0.5
//                ra.printRocPoints( points );
//            }
//
//
//
//        } catch ( IOException e ) {
//            e.printStackTrace();
//        }


        double runningtotal = 0.0;
        nrRuns =20;
        int points = 20; // number of RoC curve points to calculate

        double[] truePosTotals = new double[points];
        double[] trueNegTotals = new double[points];

        for ( int i = 0; i < nrRuns; i++ ) {  // loop between 1 and 100 times


            File gisInput = new File( inDir, "gisInput.txt" );
            File posTest = new File( inDir, "testSet_hc.txt" );
            File negTest = new File( inDir, "testSet_lc.txt" );
            try {
                RocAnalyzer ra = new RocAnalyzer( gisInput, posTest.getPath(), negTest.getPath(), true );
                if ( log.isInfoEnabled() ) {
                    log.info( "Begin of runNr: " + i + "\n" );
                }
                ra.printSummary( threshold, equality );    //threshold >= 0.5
                ra.printRocPoints( points );
                double[] truePos = ra.truePositives( points );
                double[] trueNeg = ra.trueNegatives( points );
                for ( int j = 0; j < points; j++ ) {
                    truePosTotals[j] = truePosTotals[j] + truePos[j];
                    trueNegTotals[j] = trueNegTotals[j] + trueNeg[j];
                }
                if ( log.isInfoEnabled() ) {
                    log.info( "End of runNr: " + i + "\n" );
                }
            }
            catch ( IOException e ) {
                if ( log.isInfoEnabled() ) {
                    log.info( "rocAnalyze", e );
                }
            }
        }


        System.out.println( "[Fraction of true positives] [Fraction of true negatives]" );
        for ( int i = 0; i < points; i++ ) {
            double meanTruePos = truePosTotals[i] / ( double ) nrRuns;
            double meanTrueNeg = trueNegTotals[i] / ( double ) nrRuns;
            String out = meanTruePos + "\t" + meanTrueNeg;
            System.out.println( out );
        }


        double overallCorrect = runningtotal / nrRuns;
        String overall = "Average fraction correct = " + overallCorrect;
        System.out.println( overall );
    }


    public static void main( String[] args ) throws IOException {
        //  File workDir = new File( "H:/tmp/ConfidenceModel/bkup/benchmark" );
        File workDir = new File( "H:\\tmp\\ConfidenceModel\\benchmark" );

        if ( !workDir.isDirectory() ) {
            workDir.mkdir();
        }
        AnalyzeMain amain = new AnalyzeMain( workDir, 10 );
        File hcFile = new File( "H:/tmp/ConfidenceModel/benchmark/hc_all_attributes.txt" );
        File lcFile = new File( "H:/tmp/ConfidenceModel/benchmark/lc_all_attributes.txt" );
//        File hcFile = new File( "H:\\tmp\\ConfidenceModel\\benchmark\\hctest_all.txt" );
//        File lcFile = new File( "H:\\tmp\\ConfidenceModel\\benchmark\\lctest_all.txt" );
        amain.splitToTrainTest( hcFile, lcFile, 20, workDir );
        amain.generateGisInput( workDir );
        amain.rocAnalyze( 0.5, false, workDir );
    }


}
