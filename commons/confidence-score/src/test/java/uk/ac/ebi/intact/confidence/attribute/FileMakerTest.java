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
package uk.ac.ebi.intact.confidence.attribute;

import junit.framework.Assert;
import org.junit.Test;
import uk.ac.ebi.intact.confidence.BinaryInteractionSet;
import uk.ac.ebi.intact.confidence.ProteinPair;

import java.io.File;
import java.util.Arrays;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *        29-Nov-2007
 *        </pre>
 */
public class FileMakerTest {

    @Test
    public void testMakeAttribute() throws Exception {
        ProteinPair protPair = new ProteinPair("P12345", "Q12345");
        BinaryInteractionSet bis = new BinaryInteractionSet( Arrays.asList( protPair));
        FileMaker fileM = new FileMaker(bis);
        String notePath = FileMakerTest.class.getResource( "go_annotation.txt").getPath();
        File outFile = new File(getTargetDirectory(), "go_attributes.txt");
        fileM.writeAnnotationAttributes(notePath, outFile.getPath() );
        Assert.assertTrue(outFile.exists());
        //FIXME: it fails because it differentiates between ab and ba ... which should mean the same
    }

    public File getTargetDirectory() {
		String outputDirPath = FileMakerTest.class.getResource("/").getFile();
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
