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

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.intact.confidence.weights.inputs.SparseArff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;

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
public class SparseArffTest {
     @Test
    public void testCreateArff() throws Exception {
        SparseArff arff = new SparseArff();
        String hcPath = SparseArffTest.class.getResource( "hc_test.txt").getPath();
        String lcPath = SparseArffTest.class.getResource( "lc_test.txt").getPath();
        File outFile =  new File(getTargetDirectory(), "sparseArff.txt");
        String outPath  = outFile.getPath();
        File attribsFile = new File(getTargetDirectory(), "sparseAttribs.txt");
        arff.createArff( hcPath, lcPath, attribsFile.getPath(), outPath);
        Assert.assertTrue(outFile.exists());
        BufferedReader br = new BufferedReader(new FileReader(outFile));
        String line ="";
        int count =0;
        while ((line =br.readLine()) != null){
            count ++;
        }
        Assert.assertEquals(16, count );
    }

    @Test
    public void testStort() throws Exception {
        int [] notSorted = {1,3,7,8,4,3};
        Arrays.sort(notSorted);
        Assert.assertEquals( 1, notSorted[0]);
        Assert.assertEquals( 3, notSorted[1]);
        Assert.assertEquals( 3, notSorted[2]);
        Assert.assertEquals( 4, notSorted[3]);
        Assert.assertEquals( 7, notSorted[4]);
        Assert.assertEquals( 8, notSorted[5]);
    }

    public File getTargetDirectory() {
		String outputDirPath = SparseArffTest.class.getResource("/").getFile();
		Assert.assertNotNull(outputDirPath);
		File outputDir = new File(outputDirPath);
		// we are in confidence-score\target\test-classes , move 1 up
		outputDir = outputDir.getParentFile();
		Assert.assertNotNull(outputDir);
		Assert.assertTrue(outputDir.getAbsolutePath(), outputDir.isDirectory());
		Assert.assertEquals("target", outputDir.getName());
		return outputDir;
	}

}
