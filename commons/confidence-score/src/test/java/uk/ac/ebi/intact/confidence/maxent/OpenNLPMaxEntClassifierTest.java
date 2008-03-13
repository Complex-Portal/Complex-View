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
import org.junit.Ignore;
import org.junit.Test;
import uk.ac.ebi.intact.confidence.model.Attribute;
import uk.ac.ebi.intact.confidence.model.BinaryInteractionAttributes;
import uk.ac.ebi.intact.confidence.model.io.BinaryInteractionAttributesReader;
import uk.ac.ebi.intact.confidence.model.io.impl.BinaryInteractionAttributesReaderImpl;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        System.out.println( "memory: " + ( Runtime.getRuntime().maxMemory() ) / ( 1024 * 1024 ) );
        String hcSetPath = "E:\\iarmean\\backupData\\15.02 - IWEB2 - full filter\\highconf_set_go_ip_attribs.txt";
        String lcSetPath = "E:\\iarmean\\backupData\\15.02 - IWEB2 - full filter\\lowconf_set_go_ip_attribs.txt";
        File workDir =  new File("E:\\iarmean\\backupData\\15.02 - IWEB2 - full filter");
        OpenNLPMaxEntClassifier cl = new OpenNLPMaxEntClassifier( hcSetPath, lcSetPath, workDir);
        GISModel model = cl.getModel();
        File outFile = new File("E:\\iarmean\\backupData\\15.02 - IWEB2 - full filter\\gisModel_go_ip.txt");
        MaxentUtils.writeModelToFile( model, outFile );
    }

    @Test
   @Ignore
    public void assignScore() throws Exception {
        File gisModel = new File("E:\\iarmean\\backupData\\15.02 - IWEB2 - full filter\\gisModel_go_ip.txt");
        OpenNLPMaxEntClassifier cl = new OpenNLPMaxEntClassifier( gisModel);
        File medconfFile = new File("E:\\iarmean\\backupData\\15.02 - IWEB2 - full filter\\medconf_set_go_ip_attribs.txt");
        BinaryInteractionAttributesReader biar = new BinaryInteractionAttributesReaderImpl();
        Writer writer = new FileWriter("E:\\iarmean\\backupData\\15.02 - IWEB2 - full filter\\medconf_set_go_ip_scores.txt");
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
