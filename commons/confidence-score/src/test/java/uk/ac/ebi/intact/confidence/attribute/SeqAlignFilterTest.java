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
package uk.ac.ebi.intact.confidence.attribute;

import org.junit.Test;
import org.junit.Assert;
import uk.ac.ebi.intact.confidence.model.Identifier;
import uk.ac.ebi.intact.confidence.model.UniprotIdentifierImpl;

import java.util.Set;
import java.util.HashSet;

/**
 * Test class for SeqAlign Filter.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        15-Jan-2008
 *        </pre>
 */
public class SeqAlignFilterTest {

    @Test
    public void filterProts() throws Exception {
        Set<Identifier> ids = new HashSet<Identifier>();
        Identifier id1 = new UniprotIdentifierImpl( "P12345");
        Identifier id2 = new UniprotIdentifierImpl( "P12346");
        ids.add( id1 );
        ids.add( id2);
        ids.add( new UniprotIdentifierImpl("P12347") );
        ids.add( new UniprotIdentifierImpl("P12348") );

        Set<Identifier> wanted = new HashSet<Identifier>();
        wanted.add( id1 );
        wanted.add(id2 );

        SeqAlignFilter.setHighConfProteins( wanted );
        SeqAlignFilter.filterHighConfAlign(  ids);

        Assert.assertEquals(2, ids.size());        
        Assert.assertTrue(ids.contains( id1));
        Assert.assertTrue(ids.contains( id2));
    }

}
