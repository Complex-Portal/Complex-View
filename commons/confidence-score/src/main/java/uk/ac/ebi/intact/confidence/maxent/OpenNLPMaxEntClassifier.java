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

import opennlp.maxent.GISModel;
import uk.ac.ebi.intact.confidence.weights.inputs.OpenNLP;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *               27-Nov-2007
 *               </pre>
 */
public class OpenNLPMaxEntClassifier /*extends AbstractMaxEnt*/ {
    private GISModel model;
    private File workDir;

    public OpenNLPMaxEntClassifier( String hcSetPath, String lcSetPath, File workDir ) {
        File gisInput = new File( workDir, "gisInput.txt" );
        OpenNLP.createInput( hcSetPath, lcSetPath, gisInput.getPath() );
        try {
            model = MaxentUtils.createModel( new FileInputStream( gisInput ) );
        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        }
    }

    public GISModel getModel() {
        return model;
    }

    public double[] evaluate(String[] attributes){
        return model.eval( attributes);
    }

}
