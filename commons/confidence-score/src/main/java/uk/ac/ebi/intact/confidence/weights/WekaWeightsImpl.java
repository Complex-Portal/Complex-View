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
package uk.ac.ebi.intact.confidence.weights;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.confidence.weights.inputs.SparseArff;
import weka.attributeSelection.GainRatioAttributeEval;
import weka.core.Instances;

import java.io.*;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *        22-Nov-2007
 *        </pre>
 */
public class WekaWeightsImpl implements WeightsStrategy {
    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( WekaWeightsImpl.class );

    private File workDir;

    public WekaWeightsImpl( File workDir ) {
        this.workDir = workDir;
    }

    public void computeWeights( String hcAttribsPath, String lcAttribsPath, String outAttribsPath, String outWeightsPath ) {
        if (log.isInfoEnabled()){
            log.info("creating arff file ...");
        }
        SparseArff arff = new SparseArff();
        File arffFile = new File(workDir, "arffFile.txt");
        arff.createArff(hcAttribsPath, lcAttribsPath, outAttribsPath, arffFile.getPath());

        if (log.isInfoEnabled()){
            log.info("creating arff finished: " + arffFile.getPath());
            log.info("evaluating attributes ...");
        }
        evaluateAttributes(arffFile, new File(outWeightsPath));
        if(log.isInfoEnabled()){
            log.info("evaluating attributes finished: " + outWeightsPath);
        }

    }

    private void evaluateAttributes( File arffFile, File outWeightsPath ) {
        try {
            if (log.isInfoEnabled()){
                log.info("reading the instances from arff file ...");
            }
            //data should be in arff format
            Reader reader = new FileReader( arffFile );
            Instances instances = new Instances( reader );
            // Make the last attribute be the class
            instances.setClassIndex( instances.numAttributes() - 1 );
             if (log.isInfoEnabled()){
                log.info("reading the instances form arff finished: " + instances.numInstances());
                log.info("building evaluator (gainRatio)");
            }

            GainRatioAttributeEval gainRatio = new GainRatioAttributeEval();
            gainRatio.buildEvaluator( instances );
            if(log.isInfoEnabled()){
                log.info("finished building evaluator (gainRatio)");
                log.info("evaluating each attribute ..");
            }
            Writer w = new FileWriter( outWeightsPath );
            int count =0;
            for ( int i = 0; i < instances.numAttributes(); i++ ) {
                double d = gainRatio.evaluateAttribute( i );
                w.append( d + "\n" );
                count ++;
                if(log.isInfoEnabled() && (count %20) == 0) {
                    log.info("evaluated " + count + " attributes from total " + instances.numAttributes() );
                }
            }
            w.close();
            if (log.isInfoEnabled()){
                log.info("evaluated " + count + " attributes from total " + instances.numAttributes() );
                log.info("finished evaluating each attribute.");
            }
        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

//    public static void main(String [] args){
//        WekaWeightsImpl maxEnt = new WekaWeightsImpl( new File("H://tmp"));
//        File arffFile = new File("E:\\iarmean\\ConfidenceScore\\components\\go\\go_arffFile.txt");
//        File weightsFile = new File ("E:\\iarmean\\ConfidenceScore\\components\\go\\go_weights.txt");
//        maxEnt.evaluateAttributes( arffFile,weightsFile);
//    }
}
