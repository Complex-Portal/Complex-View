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
package uk.ac.ebi.intact.confidence.maxent;

import org.junit.Ignore;
import org.junit.Test;

import java.io.*;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *               21-Nov-2007
 *               </pre>
 */
@Ignore
public class WekaMaxEntClassifierTest {
    @Test
   @Ignore
    public void testClassifier() throws Exception {
        String attribPath = WekaMaxEntClassifierTest.class.getResource( "attribs.txt" ).getPath();
        String weightPath = WekaMaxEntClassifierTest.class.getResource( "weights.txt" ).getPath();

        File outAttribs = new File( "E:\\tmp\\ConfidenceModel\\components\\all_attribs_new.txt" );
        File outWeight = new File( "E:\\tmp\\ConfidenceModel\\components\\all_weights_new.txt" );
        AbstractMaxEnt model = new WekaMaxEntClassifier( outAttribs.getPath(), outWeight.getPath() );
        File outScoreFile = new File( "E:\\tmp\\ConfidenceModel\\components", "medconfidence_" + "all_new_" + "FINAL_SCORE.txt" );
        String medconfAttributesPath = "E:\\tmp\\ConfidenceModel\\components\\mc_all_attributes.txt";
        model.writeInteractionScores( medconfAttributesPath, outScoreFile.getPath());

//        String mcSet = "E:\\iarmean\\ConfidenceScore\\components\\go\\mc_go_attributes.txt";
//        String outPath = "E:\\iarmean\\ConfidenceScore\\components\\mc_go_FINAL_SCORE.txt";
//        score(model, mcSet, outPath);
//      String inPath = WekaMaxEntClassifierTest.class.getResource( "tobeClassified.txt").getPath();
//        model.printScoreProfile( inPath);
    }


    private void score( AbstractMaxEnt maxEnt, String setPath, String outPath ) {
        File file = new File( setPath );
        BufferedReader br;
        try {
            FileWriter fw = new FileWriter( new File( outPath ) );

            br = new BufferedReader( new FileReader( file ) );
            String line;
            int count = 0;
            while ( ( line = br.readLine() ) != null ) {
                double tScore = maxEnt.trueScoreFromLine( line );
                String[] str = line.split( "," );
                fw.append( str[0] + ": " + tScore + "\n" );
                count ++;
                if ((count%20 ) == 0){
                    System.out.println("processed : " + count);
                }
            }
            fw.close();
            System.out.println("processed : " + count);
        } catch ( FileNotFoundException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
