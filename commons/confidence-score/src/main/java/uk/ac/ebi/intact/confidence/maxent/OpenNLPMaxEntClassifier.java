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
import uk.ac.ebi.intact.confidence.model.Attribute;
import uk.ac.ebi.intact.confidence.model.ConfidenceSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Wrapper for the opennlp.maxent.GISModel
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0-SNAPSHOT
 *        <pre>
 *                      27-Nov-2007
 *                      </pre>
 */
public class OpenNLPMaxEntClassifier /*extends AbstractMaxEnt*/ {
    private GISModel model;

    public OpenNLPMaxEntClassifier( File gisModel) throws IOException {
        model = MaxentUtils.readModelFromFile( gisModel );
    }

    public OpenNLPMaxEntClassifier( String hcSetPath, String lcSetPath, File workDir ) throws IOException {
        File gisInput = new File( workDir, "gisInput.txt" );
        OpenNLP.createInput( hcSetPath, lcSetPath, gisInput.getPath() );
        model = MaxentUtils.createModel( new FileInputStream( gisInput ) );
    }

    public OpenNLPMaxEntClassifier( ConfidenceSet highconf, ConfidenceSet lowconf) {
        //OutputStream os = new BufferedOutputStream()
        //TODO: implement method
    }

    public GISModel getModel() {
        return model;
    }

    public double[] evaluate( List<Attribute> attributes ) {
        String[] names = attributesNames( attributes );
        return model.eval( names );
    }

    private String[] attributesNames( List<Attribute> attributes ) {
        Set<String> names = new HashSet<String>( attributes.size() );
        for ( Iterator<Attribute> attrIter = attributes.iterator(); attrIter.hasNext(); ) {
            Attribute attr = attrIter.next();
            if (attr != null){
                names.add( attr.convertToString() );
            }
        }
        return names.toArray( new String[names.size()] );
    }

    public int getIndex( String type ) {
        return model.getIndex( type );
    }
}
