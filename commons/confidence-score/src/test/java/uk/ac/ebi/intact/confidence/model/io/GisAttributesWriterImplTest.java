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
package uk.ac.ebi.intact.confidence.model.io;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.intact.confidence.factory.MockFactory;
import uk.ac.ebi.intact.confidence.global.GlobalTestData;
import uk.ac.ebi.intact.confidence.model.BinaryInteractionAttributes;
import uk.ac.ebi.intact.confidence.model.Confidence;
import uk.ac.ebi.intact.confidence.model.io.impl.GisAttributesWriterImpl;

import java.io.File;

/**
 * Test-class for GisAttributesWriterImpl.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        12-Dec-2007
 *        </pre>
 */
public class GisAttributesWriterImplTest {

    @Test
    public void gisInputWriter() throws Exception {
        BinaryInteractionAttributes bia = MockFactory.createBinaryIntWithAttribs();
        bia.setConfidence( Confidence.HIGH);
        BinaryInteractionAttributesWriter biaw = new GisAttributesWriterImpl();
        File outFile = new File ( GlobalTestData. getTargetDirectory(), "gisAttribsWriter.txt");
        if(outFile.exists()){
            outFile.delete();
        }
        biaw.append(bia, outFile);
        checkContent(outFile);
    }

    private void checkContent(File file) {
        Assert.assertTrue(file.exists());
        //TODO: make better check
    }

}
