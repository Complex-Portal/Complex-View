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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import uk.ac.ebi.intact.confidence.ehcache.GOACache;
import uk.ac.ebi.intact.confidence.filter.GOAFilter;
import uk.ac.ebi.intact.confidence.filter.GOAFilterCacheImpl;
import uk.ac.ebi.intact.confidence.filter.GOFilter;
import uk.ac.ebi.intact.confidence.model.Attribute;
import uk.ac.ebi.intact.confidence.model.*;
import uk.ac.ebi.intact.confidence.model.io.ProteinAnnotationReader;
import uk.ac.ebi.intact.confidence.model.io.ProteinAnnotationWriter;
import uk.ac.ebi.intact.confidence.model.io.impl.ProteinAnnotationReaderImpl;
import uk.ac.ebi.intact.confidence.model.io.impl.ProteinAnnotationWriterImpl;

import java.io.File;
import java.util.*;

/**
 *  Test class for GO filter.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        29-Nov-2007
 *        </pre>
 */
public class GOFilterTest {

    @Before
    public void setUp() throws Exception {
      //  GOFilter.getInstance().
    }

    @Test
    public void testFilterGOS() throws Exception {
        Set<Identifier> gos = new HashSet<Identifier>(3);
        gos.add(new GoIdentifierImpl("GO:0005515"));
        Identifier id = new GoIdentifierImpl("GO:0000001");
        gos.add(id);
        gos.add(new GoIdentifierImpl("GO:0008022"));
        GOFilter.filterForbiddenGOs( gos);
        Assert.assertEquals( 1, gos.size());
        Assert.assertEquals( id, gos.iterator().next() );
    }

    @Test
    public void filterGO() throws Exception {
        Set<Identifier> gos = new HashSet<Identifier>(3);
        gos.add(new GoIdentifierImpl("GO:0005515"));
        Identifier id1 = new GoIdentifierImpl("GO:0000001");
        gos.add(id1);
        Identifier id2 = new GoIdentifierImpl("GO:0008022");
        gos.add(id2);

        GOFilter.filterProteinBindingGO( gos);
        Assert.assertEquals( 2, gos.size());
        Assert.assertTrue( gos.contains( id1 ));
        Assert.assertTrue( gos.contains( id2 ));
    }


    @Test
    public void filterAttributesGOS() throws Exception {
        Set<Attribute>goAttribs = new HashSet<Attribute>(3);
        goAttribs.add( new IdentifierAttributeImpl<GoIdentifierImpl>(new GoIdentifierImpl( "GO:0005515"), new GoIdentifierImpl( "GO:0000001") ));
        goAttribs.add( new IdentifierAttributeImpl<GoIdentifierImpl>(new GoIdentifierImpl( "GO:0005515"), new GoIdentifierImpl( "GO:0008022") ));
        Attribute attr = new IdentifierAttributeImpl<GoIdentifierImpl>(new GoIdentifierImpl( "GO:0000002"), new GoIdentifierImpl( "GO:0000001") ); 
        goAttribs.add(attr);
        GOFilter.filterForbiddenGOsAttribs(goAttribs );
        Assert.assertEquals( 1, goAttribs.size() );
        Assert.assertEquals( attr, goAttribs.iterator().next() );
    }


    @Test
    @Ignore
    /*
       this test is failing when its run in all the tests in the project, but works when its run only
       for filtering in the current version the GOMap filter is used
     */
    /**
     * IEA =inferred by electronic evidence |A evidence code for GOA
     */
    public void filterIEA() throws Exception {
        List<ProteinAnnotation> pas = new ArrayList<ProteinAnnotation>(2);
        ProteinAnnotation pa = new ProteinAnnotation (new UniprotIdentifierImpl( "A0JPZ8"));
        pa.addAnnotation( new GoIdentifierImpl("GO:0003700" ) );
        Identifier id = new GoIdentifierImpl("GO:0003701" );
        pa.addAnnotation( id );
        pas.add( pa);

        ProteinAnnotation pa1 = new ProteinAnnotation (new UniprotIdentifierImpl( "P46011"));
        pa1.addAnnotation( new GoIdentifierImpl("GO:0000257" ) );
        Identifier id1 = new GoIdentifierImpl("GO:0000258" );
        pa1.addAnnotation(id1 );
        pas.add( pa1);

        File goaFile = new File( GOACache.class.getResource( "goaTest.txt" ).getPath());
        GOAFilter goaFilter = new GOAFilterCacheImpl();
        goaFilter.clean();
        goaFilter.initialize( goaFile );
        goaFilter.filterGO( pas);

        // check the filtering
        for ( Iterator<ProteinAnnotation> iter = pas.iterator(); iter.hasNext(); ) {
            ProteinAnnotation observed = iter.next();
            try{
                Assert.assertEquals(1, observed.getAnnotations().size() );
            } catch(AssertionError e){
                GOACache.getInstance().printCache(); 
                System.out.println(observed.convertToString());
            }
            if (observed.getId().equals( pa.getId() )){
                Assert.assertEquals( id, observed.getAnnotations().iterator().next() );
            }  else if (observed.getId().equals(pa1.getId() )){
                 Assert.assertEquals( id1, observed.getAnnotations().iterator().next() );
            }
        }
    }
        

   @Test
   @Ignore
    public void filterFile() throws Exception {
        File inFile = new File("E:\\iarmean\\ConfidenceScore\\IntactDbRetriever\\medconf_db_go.txt");
        File outFile = new File("E:\\tmp\\mc_go_filtered.txt");
        ProteinAnnotationReader reader = new ProteinAnnotationReaderImpl();
        List<ProteinAnnotation> proteinAnnos = reader.read( inFile);
        ProteinAnnotationWriter writer = new ProteinAnnotationWriterImpl();
        for ( Iterator<ProteinAnnotation> proteinAnnotationIterator = proteinAnnos.iterator(); proteinAnnotationIterator.hasNext(); )
        {
            ProteinAnnotation proteinAnnotation =  proteinAnnotationIterator.next();
            GOFilter.filterForbiddenGOs( proteinAnnotation.getAnnotations());
            writer.append( proteinAnnotation, outFile);
        }
    }


}
