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

import java.io.File;
import java.util.*;

import uk.ac.ebi.intact.confidence.model.BinaryInteraction;
import uk.ac.ebi.intact.confidence.model.UniprotIdentifierImpl;
import uk.ac.ebi.intact.confidence.model.Confidence;
import uk.ac.ebi.intact.confidence.model.io.impl.BinaryInteractionReaderImpl;

/**
 * Test class for BinaryInteractionReader.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        11-Jan-2008
 *        </pre>
 */
public class BinaryInteractionReaderTest {

    @Test
    public void read() throws Exception {
        File inFile = new File(BinaryInteractionReaderTest.class.getResource( "BinaryInteraction.txt" ).getPath());
        BinaryInteractionReader bir = new BinaryInteractionReaderImpl();
        bir.setConfidence( Confidence.HIGH );
        List<BinaryInteraction> observed = bir.read( inFile );
        List<BinaryInteraction> expected = expectedInfo();

        //check content
        Assert.assertEquals(expected.size(), observed.size()  );
        for (int i=0; i< expected.size(); i++){
            BinaryInteraction expectedBi = expected.get( i );
            BinaryInteraction observedBi = observed.get( i );
            Assert.assertEquals( Confidence.HIGH, observedBi.getConfidence() );
            Assert.assertEquals( expectedBi, observedBi );
        }
    }

    @Test
    public void iterate() throws Exception {
        File inFile = new File(BinaryInteractionReaderTest.class.getResource( "BinaryInteraction.txt" ).getPath());
        BinaryInteractionReader bir = new BinaryInteractionReaderImpl();
        List<BinaryInteraction> expected = expectedInfo();

        //check content
        int nr= 0;
        for ( Iterator<BinaryInteraction> obsIterator = bir.iterate( inFile ); obsIterator.hasNext(); ) {
            BinaryInteraction observedBi =  obsIterator.next();
            BinaryInteraction expectedBi = expected.get( nr );
             Assert.assertEquals( Confidence.UNKNOWN, observedBi.getConfidence() );
            Assert.assertEquals( expectedBi, observedBi );
            nr++;
        }
    }

    @Test
    public void readListToSet() throws Exception {
        File inFile = new File(BinaryInteractionReaderTest.class.getResource( "BinaryInteraction.txt" ).getPath());
        BinaryInteractionReader bir = new BinaryInteractionReaderImpl();
        Set<BinaryInteraction> observed = new HashSet<BinaryInteraction>(bir.read( inFile ));
        // OBS: only exactly matching interactions are removed
        Assert.assertEquals( 5, observed.size() );
    }
        

    private List<BinaryInteraction> expectedInfo(){
        BinaryInteraction bi = new BinaryInteraction(new UniprotIdentifierImpl("Q8WYL5"), new UniprotIdentifierImpl( "Q9BR76"), Confidence.UNKNOWN );
        BinaryInteraction bi1 = new BinaryInteraction(new UniprotIdentifierImpl("P06213"), new UniprotIdentifierImpl( "P18031"), Confidence.UNKNOWN );
        BinaryInteraction bi2 = new BinaryInteraction(new UniprotIdentifierImpl("P17706"), new UniprotIdentifierImpl( "O00401"), Confidence.UNKNOWN );
        BinaryInteraction bi3 = new BinaryInteraction(new UniprotIdentifierImpl("O00401"), new UniprotIdentifierImpl( "P17706"), Confidence.UNKNOWN );
        BinaryInteraction bi4 = new BinaryInteraction(new UniprotIdentifierImpl("Q9Y2R2"), new UniprotIdentifierImpl( "P06239"), Confidence.UNKNOWN );
        return Arrays.asList( bi, bi1, bi2, bi3, bi4, bi4 );
    }

}
