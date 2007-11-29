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
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

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
public class WekaWeightsImplTest {
    @Test
    @Ignore
    public void testPrintDouble() throws Exception {
        double d = 0.293642954864667648;
        System.out.println(d);
        //TODO: remove this useless test
        System.out.println("%d " + d);
    }

    @Test
    public void testWeights() throws Exception {
        WekaWeightsImpl  weka = new WekaWeightsImpl(getTargetDirectory());
        String hcPath = SparseArffTest.class.getResource( "hc_test.txt").getPath();
        String lcPath = SparseArffTest.class.getResource( "lc_test.txt").getPath();
        File weights = new File(getTargetDirectory(), "weightsWeka.txt");
        File attribs = new File(getTargetDirectory(), "attribsWeka.txt");
        weka.computeWeights( hcPath,lcPath, attribs.getPath(),weights.getPath());
        Assert.assertTrue (weights.exists());
        Assert.assertTrue(attribs.exists());
    }


     public File getTargetDirectory() {
		String outputDirPath = WekaWeightsImpl.class.getResource("/").getFile();
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
