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
package uk.ac.ebi.intact.confidence.utils;

import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import org.junit.Ignore;
import org.junit.Test;
import uk.ac.ebi.intact.confidence.global.GlobalTestData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *        14-Nov-2007
 *        </pre>
 */
public class MergeTest {

    //the test data is not biological correct
    @Test
    public void testMerge() throws Exception {
        System.out.println( "memory: " + ( Runtime.getRuntime().maxMemory() ) / ( 1024 * 1024 ) );

        String goPath = MergeTest.class.getResource( "set_go_attributes.txt").getPath();
        //goPath = "H:\\tmp\\ConfidenceModel\\HcAttribs\\set_go_attributes.txt";
        String ipPath = MergeTest.class.getResource( "set_ip_attributes.txt").getPath();
        //ipPath = "H:\\tmp\\ConfidenceModel\\HcAttribs\\set_ip_attributes.txt";
        String seqPath =  MergeTest.class.getResource( "set_align_attributes.txt").getPath();
        //seqPath = "H:\\tmp\\ConfidenceModel\\HcAttribs\\set_align_attributes.txt";
        
        String[] paths = {seqPath, ipPath, goPath};
        String outPath = GlobalTestData.getInstance().getTargetDirectory() + "/mergeTestLucene.txt";
       // outPath = "H:\\tmp\\ConfidenceModel\\HcAttribs\\merged_set_attributes.txt";
        ( new Merge() ).merge( paths, outPath );
        assertTrue(new File(outPath).exists());
         BufferedReader br = new BufferedReader(new FileReader(outPath));
        int i =0;
        String line;
        while ((line = br.readLine()) != null){
            i++;
        }
        Assert.assertEquals(11, i);
    }

    


}
