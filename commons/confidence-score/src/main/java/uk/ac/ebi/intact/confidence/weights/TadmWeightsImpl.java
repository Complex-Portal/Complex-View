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

import java.io.File;
import java.io.IOException;

/**
 * Using the TToolkit for Advanced Discriminative Modeling (TADM)
 * generates the attributes weights,
 *  necessary  for the MaxEnt model.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *                      22-Nov-2007
 *                      </pre>
 */
public class TadmWeightsImpl implements WeightsStrategy {
      /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( TadmWeightsImpl.class );

    private File workDir;

    public TadmWeightsImpl( File workDir ) {
        this.workDir = workDir;
    }

    public void computeWeights( String hcAttribsPath, String lcAttribsPath, String outAttribsPath, String outWeightsPath ) throws WeightsStrategyException {
        if (log.isInfoEnabled()){
            log.info("creating TADM input file ...");
        }
        File tadmFile = createTadmFile( hcAttribsPath, lcAttribsPath, outAttribsPath );
        if(log.isInfoEnabled()){
            log.info("creating TADM input file finished: " + tadmFile.getPath());
            log.info("running TADM ...");
        }
        runTadm( tadmFile.getPath(), outWeightsPath );
        if (log.isInfoEnabled()){
            log.info("running TADM finished: " + outWeightsPath);
        }
    }

    private File createTadmFile( String hcAttribsPath, String lcAttribsPath, String outAttribsPath ) throws WeightsStrategyException {
        File tadmFile = new File( workDir, "tadm.input" );
        try {
            ClassifierInputWriter ciw = new ClassifierInputWriter( hcAttribsPath, lcAttribsPath, tadmFile.getPath(), "TADM" );
            ciw.writeAttribList( outAttribsPath );
        } catch ( IllegalArgumentException e ) {
            throw new WeightsStrategyException( e );
        } catch ( IOException e ) {
            throw new WeightsStrategyException( e );
        }
        return tadmFile;
    }

    private void runTadm( String tadmInputPath, String outWeightsPath ) throws WeightsStrategyException {
        String cmd = "tadm -events_in " + tadmInputPath + " -params_out " + outWeightsPath;
        try {
            Process process = Runtime.getRuntime().exec( cmd );
            process.waitFor();
        } catch ( IOException e ) {
            throw new WeightsStrategyException( e );
        } catch ( InterruptedException e ) {
            throw new WeightsStrategyException( e );
        }
    }
}
