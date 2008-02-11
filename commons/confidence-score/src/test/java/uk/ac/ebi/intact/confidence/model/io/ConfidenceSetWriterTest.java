/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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
import uk.ac.ebi.intact.confidence.global.GlobalTestData;
import uk.ac.ebi.intact.confidence.model.BinaryInteractionAttributes;
import uk.ac.ebi.intact.confidence.model.Confidence;
import uk.ac.ebi.intact.confidence.model.ConfidenceSet;
import uk.ac.ebi.intact.confidence.model.io.impl.ConfidenceSetModelInputWriterImpl;
import uk.ac.ebi.intact.confidence.model.io.impl.ConfidenceSetReaderImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Test class for writing a confidenceSet.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        18-Jan-2008
 *        </pre>
 */
public class ConfidenceSetWriterTest {

    @Test
    public void confidenceSetModelInput() throws Exception {
        File bi = new File ( ConfidenceSetWriterTest.class.getResource( "BinaryInteractionAttributes.txt" ).getPath());
        ConfidenceSetWriter csw = new ConfidenceSetModelInputWriterImpl();
        ConfidenceSetReader csr = new ConfidenceSetReaderImpl();
        ConfidenceSet expected = csr.read( bi, Confidence.UNKNOWN );
        File outFile = new File ( GlobalTestData.getTargetDirectory(), "ConfSetWriterTest.txt");
        csw.write(expected, outFile);

        //check
        BufferedReader br = new BufferedReader(new FileReader(outFile));
        String observed ="";
        int nr = 0;
        while ((observed = br.readLine())!= null){
            BinaryInteractionAttributes expectedBiar = expected.getBinaryInteractions().get( nr );
            String expectedStr = expectedBiar.convertToModleInputString();
            Assert.assertEquals( expectedStr, observed );
            nr++;
        }
        br.close();
    }

}
