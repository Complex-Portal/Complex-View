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

import org.junit.Test;
import org.junit.Assert;
import uk.ac.ebi.intact.confidence.model.InteractionSimplified;
import uk.ac.ebi.intact.confidence.model.BinaryInteraction;
import uk.ac.ebi.intact.confidence.model.io.impl.BinaryInteractionReaderImpl;
import uk.ac.ebi.intact.confidence.model.io.impl.CompactInteractionSWriterImpl;
import uk.ac.ebi.intact.confidence.test.MockFactory;
import uk.ac.ebi.intact.confidence.global.GlobalTestData;

import java.io.File;
import java.util.List;

/**
 * Class to write only the uniprotAcs from an interaction.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        10-Jan-2008
 *        </pre>
 */
public class CompactInteractionSWriterImplTest {
    @Test
    public void appendDeterministicInteraction() throws Exception{
        InteractionSimplifiedWriter writer = new CompactInteractionSWriterImpl();

        InteractionSimplified expected = MockFactory.createDeterministicInteractionSimplified();
        File outFile =  new File( GlobalTestData.getTargetDirectory(), "CompactIntSWriterTest1.txt");
        if (outFile.exists()){
            outFile.delete();
        }
        writer.append( expected , outFile);

        // check output
        BinaryInteractionReader bir = new BinaryInteractionReaderImpl();
        List<BinaryInteraction> observed = bir.read( outFile );
        Assert.assertEquals( 1, observed.size() );

        Assert.assertEquals( expected.getComponents().get( 0 ).getUniprotAc().getAcNr(), observed.get( 0 ).getFirstId().getId());
        Assert.assertEquals( expected.getComponents().get( 1 ).getUniprotAc().getAcNr(), observed.get( 0 ).getSecondId().getId());
    }

    @Test
    public void appendRandomInteraction() throws Exception{
        InteractionSimplifiedWriter writer = new CompactInteractionSWriterImpl();

        InteractionSimplified expected = MockFactory.createRandomInteractionSimplified();
        File outFile =  new File( GlobalTestData.getTargetDirectory(), "CompactIntSWriterTest2.txt");
        if (outFile.exists()){
            outFile.delete();
        }
        writer.append( expected , outFile);

        // check output
        BinaryInteractionReader bir = new BinaryInteractionReaderImpl();
        List<BinaryInteraction> observed =bir.read( outFile );
        Assert.assertEquals( 1, observed.size() );

        Assert.assertEquals( expected.getComponents().get( 0 ).getUniprotAc().getAcNr(), observed.get( 0 ).getFirstId().getId());
        Assert.assertEquals( expected.getComponents().get( 1 ).getUniprotAc().getAcNr(), observed.get( 0 ).getSecondId().getId());

    }
}
