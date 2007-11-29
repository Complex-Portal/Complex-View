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

import junit.framework.Assert;
import org.junit.Test;
import uk.ac.ebi.intact.confidence.weights.inputs.OpenNLP;

import java.io.File;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *        26-Nov-2007
 *        </pre>
 */
public class OpenNLPTest {
    @Test
    public void testCreateInput() throws Exception {
        String hcAttribsPath = OpenNLPTest.class.getResource( "hc_test.txt").getPath();
        String lcAttribsPath = OpenNLPTest.class.getResource( "lc_test.txt").getPath();
        File outFile = new File(getTargetDirectory(), "openNLP.input");
        OpenNLP.createInput(hcAttribsPath, lcAttribsPath, outFile.getPath());
        Assert.assertTrue( outFile.exists());
    }

    	public File getTargetDirectory() {
		String outputDirPath = OpenNLPTest.class.getResource("/").getFile();
		Assert.assertNotNull(outputDirPath);
		File outputDir = new File(outputDirPath);
		// we are in confidence-score\target\test-classes , move 1 up
		outputDir = outputDir.getParentFile();
		Assert.assertNotNull(outputDir);
		Assert.assertTrue(outputDir.getAbsolutePath(), outputDir.isDirectory());
		Assert.assertEquals("target", outputDir.getName());
		return outputDir;
	}

    @Test
    public void testGetAttribsFromLine() throws Exception {
        String [] attribs = OpenNLP.getAttribsFromLine( "me,mo,mu");
        Assert.assertEquals( 2, attribs.length);
        Assert.assertTrue(attribs[0].equalsIgnoreCase( "mo"));
        Assert.assertTrue(attribs[1].equalsIgnoreCase( "mu"));
    }


}
