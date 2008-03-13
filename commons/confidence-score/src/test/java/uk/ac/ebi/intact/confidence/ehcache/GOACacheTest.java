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
package uk.ac.ebi.intact.confidence.ehcache;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.intact.confidence.model.GoIdentifierImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Test class for the GeneOntology Annotation filter on
 * the IEA (inferred by electronic evidence) annotation tag.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 */
public class GOACacheTest {

    @Before
    public void setUp() throws Exception {
        GOACache.getInstance().clean();  
    }

    @Test
    public void testCache() throws Exception {
        List<GoIdentifierImpl> expected = new ArrayList<GoIdentifierImpl>();
        GoIdentifierImpl goA = new GoIdentifierImpl( "GO:1234567");
        GoIdentifierImpl goB = new GoIdentifierImpl( "GO:1234560");
        GoIdentifierImpl goC = new GoIdentifierImpl( "GO:1234561");
        expected.add( goA); expected.add( goB); expected.add( goC);

        GOACache.put("P12345",expected);
        List<GoIdentifierImpl> observed = GOACache.getInstance().fetchByUniprotAc( "P12345");
        Assert.assertNotNull( observed );
        Assert.assertEquals( observed.size(), expected.size() );
        for ( Iterator<GoIdentifierImpl> iter = expected.iterator(); iter.hasNext(); ) {
            GoIdentifierImpl expectedGO =  iter.next();           
            Assert.assertTrue( observed.contains( expectedGO));
        }
    }

    @Test
    public void testCacheWithGOAFile() throws Exception {
        List<String> uniprotAcs = Arrays.asList( "A0JPZ8", "O04017", "O22193", "P46011" );
        GOACache.getInstance().loadGOA( new File(GOACacheTest.class.getResource( "goaTest.txt" ).getPath()), uniprotAcs);
        List<GoIdentifierImpl> gos = GOACache.getInstance().fetchByUniprotAc( "A0JPZ8" );
        Assert.assertEquals(4, gos.size());
        Assert.assertTrue( gos.contains( new GoIdentifierImpl( "GO:0003700") ));
        Assert.assertTrue( gos.contains( new GoIdentifierImpl( "GO:0006350") ));
        Assert.assertTrue( gos.contains( new GoIdentifierImpl( "GO:0006355") ));
        Assert.assertTrue( gos.contains( new GoIdentifierImpl( "GO:0009873") ));
    }        
}
