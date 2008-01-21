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
package uk.ac.ebi.intact.confidence.model;

import org.junit.Test;
import org.junit.Assert;

/**
 * Test class for BinaryInteraction.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        10-Jan-2008
 *        </pre>
 */
public class BinaryInteractionTest {
    @Test
    public void equals(){
        Identifier id1 = new UniprotIdentifierImpl("P12345");
        Identifier id2 = new UniprotIdentifierImpl("P12346");
        BinaryInteraction bi1 = new BinaryInteraction(id1, id2, Confidence.UNKNOWN);

        BinaryInteraction bi2 = new BinaryInteraction(id1, id2, Confidence.HIGH);
        Assert.assertTrue(bi1.equals( bi2 ));

        BinaryInteraction bi3 = new BinaryInteraction(id2, id1, Confidence.LOW);
        Assert.assertTrue(bi1.equals( bi3 ));

        Identifier id3 = new UniprotIdentifierImpl("P12347");
        BinaryInteraction bi4 = new BinaryInteraction(id1, id3, Confidence.UNKNOWN);
        Assert.assertFalse(bi1.equals( bi4 ));
    }
}
