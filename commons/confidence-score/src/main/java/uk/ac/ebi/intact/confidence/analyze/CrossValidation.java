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

import uk.ac.ebi.intact.confidence.weights.inputs.OpenNLP;

import java.io.*;

/**
 * Class to evaluate the choosen model to be trained.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *        28-Nov-2007
 *        </pre>
 */
public class CrossValidation {
    private int k;
    private File workDir;
    private File foldsDir;
    private File trainDir;

    public CrossValidation(String positiveSetPath, String negativeSetPath,int k, File workDir) throws IOException {
        this.k = k;
        this.workDir = workDir;
        this.foldsDir = new File(workDir, "folds");
        foldsDir.mkdir();
        this.trainDir = new File(workDir, "trainTests");
        trainDir.mkdir();

       // 1. split in positive, negative set in each k files
        foldSets(new File(positiveSetPath), new File(negativeSetPath), k, foldsDir);
       // 2. create the merged 20% test set, 80% train set
        // form pozSet0+ pozSet1 = testSet0 and pozSet2...n = trainSet0
        generateTrainTestSets(k, trainDir);
        // 2.a) generate gis model input file
        generateGisInput(k, trainDir);
       // 3. for each train/test set pair do classification and
        // compute MAE (mean absolute error)

    }

     public void foldSets( File hcFile, File lcFile, int foldsNr, File dir ) {
        XvFold.k = foldsNr;
        XvFold.fold( hcFile, lcFile, dir );
    }

    public void generateTrainTestSets(int foldsNr, File dir) throws IOException {
        for ( int i = 0; i < foldsNr -1; i++ ) {
            String type = "highconf";
            merge( i, dir, type );
            mergeExcept( i, foldsNr, dir, type );
            type = "lowconf";
            merge( i, dir, type );
            mergeExcept( i, foldsNr, dir, type );
        }
    }

    private void mergeExcept( int nr, int totalNr, File outDir, String type ) throws IOException {
        File outFile = new File( outDir, "train_" + type + nr + ".txt" );
        Writer writer = null;
        for ( int i = 0; i < totalNr; i++ ) {
            if ( !( nr == i || ( nr + 1 ) == i ) ) {
                if ( writer == null ) {
                    writer = new FileWriter( outFile );
                } else {
                    writer = new FileWriter( outFile, true );
                }
                File f = new File( foldsDir, type + i + ".txt" );
                move( f, writer );
                writer.close();
            }
        }
    }

    private void merge( int i, File outDir, String type ) {
        File f1 = new File( foldsDir, type + i + ".txt" );
        File f2 = new File( foldsDir, type + ( i + 1 ) + ".txt" );
        File outFile = new File( outDir, "test_" + type + i + ".txt" );
        merge2Files( f1, f2, outFile );
    }

    /**
     * out of two folds it creates a test set
     *
     * @param f1
     * @param f2
     * @param outFile
     */
    private void merge2Files( File f1, File f2, File outFile ) {
        try {
            Writer writer = new FileWriter( outFile );
            move( f1, writer );
            writer.close();
            writer = new FileWriter( outFile, true );
            move( f2, writer );
            writer.close();
        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    private void move( File f1, Writer writer ) {
        try {
            BufferedReader br = new BufferedReader( new FileReader( f1 ) );
            String line = "";
            while ( ( line = br.readLine() ) != null ) {
                writer.append( line + "\n" );
            }
            br.close();
        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

     public void generateGisInput(int foldsNr, File outDir) {
        for ( int i = 0; i < foldsNr - 1; i++ ) {
            File hcFile = new File( outDir, "train_highconf" + i + ".txt" );
            File lcFile = new File( outDir, "train_lowconf" + i + ".txt" );
            File outFile = new File( outDir, "gisIn" + i + ".txt" );
            OpenNLP.createInput( hcFile.getPath(), lcFile.getPath(), outFile.getPath() );
        }
    }

}
