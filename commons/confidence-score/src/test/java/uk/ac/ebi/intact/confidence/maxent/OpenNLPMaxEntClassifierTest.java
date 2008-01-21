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
import uk.ac.ebi.intact.confidence.model.Attribute;
import uk.ac.ebi.intact.confidence.model.BinaryInteractionAttributes;
import uk.ac.ebi.intact.confidence.model.io.BinaryInteractionAttributesReader;
import uk.ac.ebi.intact.confidence.model.io.impl.BinaryInteractionAttributesReaderImpl;

import java.io.File;
import java.io.Writer;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import opennlp.maxent.GISModel;

/**
 * Test for creating a maximum entropy classifier
 * using the GISModel 
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        04-Dec-2007
 *        </pre>
 */
public class OpenNLPMaxEntClassifierTest {

    @Test
    public void testEmptyAttribs() throws Exception {
        File gisModel = new File(OpenNLPMaxEntClassifierTest.class.getResource( "gisModel.txt").getPath());
        OpenNLPMaxEntClassifier classifier = new OpenNLPMaxEntClassifier( gisModel);
        List<Attribute> attribs = new ArrayList<Attribute>();
        attribs.add( null);
        classifier.evaluate( attribs);
    }

    @Test
    @Ignore
    public void trainModel() throws Exception {
        String hcSetPath = "H:\\tmp\\highconf_set_seq_anno_filter_attribs.txt";
        String lcSetPath = "H:\\tmp\\lowconf_set_seq_anno_filter_attribs.txt";
        File workDir =  new File("H:\\tmp");
        OpenNLPMaxEntClassifier cl = new OpenNLPMaxEntClassifier( hcSetPath, lcSetPath, workDir);
        GISModel model = cl.getModel();
        File outFile = new File("H:\\tmp\\gisModel_seq.txt");
        MaxentUtils.writeModelToFile( model, outFile );
    }

    @Test
   @Ignore
    public void assignScore() throws Exception {
        File gisModel = new File("H:\\tmp\\gisModel_seq.txt");
        OpenNLPMaxEntClassifier cl = new OpenNLPMaxEntClassifier( gisModel);
        File medconfFile = new File("H:\\tmp\\medconf_set_seq_anno_filter_attribs.txt");
        BinaryInteractionAttributesReader biar = new BinaryInteractionAttributesReaderImpl();
        Writer writer = new FileWriter("H:\\tmp\\medconf_set_seq_scores.txt");
        int nrLine =0;
        for ( Iterator<BinaryInteractionAttributes> iter = biar.iterate( medconfFile ); iter.hasNext(); ){
            BinaryInteractionAttributes bia = iter.next();
            nrLine ++;
            if (bia == null){
                System.out.println("line: " + nrLine);
            }
            double[]  scores = cl.evaluate( bia.getAttributes() );
            double score = scores[cl.getIndex( "high" )];
            writer.append( bia.getFirstId() + ";" + bia.getSecondId() + ":" + score + "\n" );
        }
        writer.close();
    }

        
}
