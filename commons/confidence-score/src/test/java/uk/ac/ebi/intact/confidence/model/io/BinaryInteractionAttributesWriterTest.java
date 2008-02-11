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
import uk.ac.ebi.intact.confidence.factory.MockFactory;
import uk.ac.ebi.intact.confidence.global.GlobalTestData;
import uk.ac.ebi.intact.confidence.model.BinaryInteractionAttributes;
import uk.ac.ebi.intact.confidence.model.io.impl.BinaryInteractionAttributesReaderImpl;
import uk.ac.ebi.intact.confidence.model.io.impl.BinaryInteractionAttributesWriterImpl;

import java.io.File;
import java.util.List;

/**
 * Test class for BinaryInteractionAttributesWriter.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        11-Jan-2008
 *        </pre>
 */
public class BinaryInteractionAttributesWriterTest {

    @Test
    public void write() throws Exception {
        BinaryInteractionAttributesWriter biaw = new BinaryInteractionAttributesWriterImpl();
        BinaryInteractionAttributes expected = MockFactory.createDeterministicBinaryInteractionAttributes();
        File outFile = new File ( GlobalTestData.getTargetDirectory(), "BinIntAttribsWriterTest.txt");
        if (outFile.exists()){
            outFile.delete();
        }
        biaw.append( expected, outFile );

        // check output
        BinaryInteractionAttributesReader biar = new BinaryInteractionAttributesReaderImpl();
        List<BinaryInteractionAttributes> observed = biar.read( outFile );
        Assert.assertEquals( 1, observed.size() );

        BinaryInteractionAttributesReaderTest biart = new BinaryInteractionAttributesReaderTest();
        biart.assertSameInfo( expected, observed.get( 0 ));
    }


}
