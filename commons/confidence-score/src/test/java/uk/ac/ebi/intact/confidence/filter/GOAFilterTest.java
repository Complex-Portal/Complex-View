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
package uk.ac.ebi.intact.confidence.filter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.intact.business.IntactTransactionException;
import uk.ac.ebi.intact.confidence.ehcache.GOACacheTest;
import uk.ac.ebi.intact.confidence.model.*;
import uk.ac.ebi.intact.confidence.model.io.ProteinAnnotationReader;
import uk.ac.ebi.intact.confidence.model.io.ProteinAnnotationWriter;
import uk.ac.ebi.intact.confidence.model.io.impl.ProteinAnnotationReaderImpl;
import uk.ac.ebi.intact.confidence.model.io.impl.ProteinAnnotationWriterImpl;
import uk.ac.ebi.intact.core.util.SchemaUtils;

import java.io.File;
import java.util.Iterator;

/**
 * Test class for the GOAFilter strategy.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 19.02.2008
 */
public class GOAFilterTest {
   @Before
   public void clearIntactSchema() throws IntactTransactionException {
       SchemaUtils.resetSchema();
   }
    @Test
    public void testCache() throws Exception {
        GOAFilter filter = new GOAFilterCacheImpl();
        ProteinAnnotation pa = new ProteinAnnotation(new UniprotIdentifierImpl("A0JPZ8"));
        pa.addAnnotation( new GoIdentifierImpl( "GO:0005515") );
        Identifier id1 = new GoIdentifierImpl( "GO:0005516");
        pa.addAnnotation( id1 );
        pa.addAnnotation( new GoIdentifierImpl( "GO:0006355") );
        Identifier id2 = new GoIdentifierImpl( "GO:0005518");
        pa.addAnnotation( id2 );
        Identifier id3 = new InterProIdentifierImpl("IPR000523 ");
        pa.addAnnotation( id3 );

        filter.initialize( new File( GOACacheTest.class.getResource( "goaTest.txt" ).getFile()));
        filter.filterGO( pa );

        Assert.assertNotNull( pa );
        Assert.assertEquals( 3, pa.getAnnotations().size() );
        Assert.assertTrue(pa.getAnnotations().contains(id1));
        Assert.assertTrue(pa.getAnnotations().contains(id2));
        Assert.assertTrue(pa.getAnnotations().contains(id3));
    }

    @Test
    public void testMap() throws Exception {
        GOAFilter filter = new GOAFilterMapImpl();
        ProteinAnnotation pa = new ProteinAnnotation(new UniprotIdentifierImpl("A0JPZ8"));
        pa.addAnnotation( new GoIdentifierImpl( "GO:0005515") );
        Identifier id1 = new GoIdentifierImpl( "GO:0005516");
        pa.addAnnotation( id1 );
        pa.addAnnotation( new GoIdentifierImpl( "GO:0006355") );
        Identifier id2 = new GoIdentifierImpl( "GO:0005518");
        pa.addAnnotation( id2 );
        Identifier id3 = new InterProIdentifierImpl("IPR000523 ");
        pa.addAnnotation( id3 );

        filter.initialize( new File( GOACacheTest.class.getResource( "goaTest.txt" ).getFile()));
        filter.filterGO( pa );

        Assert.assertNotNull( pa );
        Assert.assertEquals( 3, pa.getAnnotations().size() );
        Assert.assertTrue(pa.getAnnotations().contains(id1));
        Assert.assertTrue(pa.getAnnotations().contains(id2));
        Assert.assertTrue(pa.getAnnotations().contains(id3));
    }

//    @Test
//    public void testCacheUniprotAc() throws Exception {
//        IntactContext.initStandaloneContext( new File( GOAFilterTest.class.getResource( "/hibernate.iweb2.cfg.xml" ).getFile() ) );
//        ProteinAnnotation pa = new ProteinAnnotation(new UniprotIdentifierImpl("Q06609"));
//        pa.addAnnotation( new GoIdentifierImpl( "GO:0005515") );
//        Identifier id1 = new GoIdentifierImpl( "GO:0005516");
//        pa.addAnnotation( id1 );
//        pa.addAnnotation( new GoIdentifierImpl( "GO:0006281") );
//        Identifier id2 = new GoIdentifierImpl( "GO:0006974");
//        pa.addAnnotation( id2 );
//        Identifier id3 = new InterProIdentifierImpl("IPR000523 ");
//        pa.addAnnotation( id3 );
//
//        GOAFilter filter = new GOAFilterCacheImpl();
//        File goaFile = new File("E:\\iarmean\\data\\goaTrimmed.txt");
//        filter.initialize( goaFile);
//        filter.filterGO(pa);
//        Assert.assertNotNull( pa );
//        Assert.assertEquals( 2, pa.getAnnotations().size() );
//        Assert.assertTrue(pa.getAnnotations().contains(id1));
//        Assert.assertTrue(pa.getAnnotations().contains(id3));
//
//    }

     @Test
     public void testMapUniprotAc() throws Exception {
//          IntactContext.initStandaloneContext( new File( GOAFilterTest.class.getResource( "/hibernate.iweb2.cfg.xml" ).getFile() ) );
//          ProteinAnnotation pa = new ProteinAnnotation(new UniprotIdentifierImpl("Q06609"));
//          pa.addAnnotation( new GoIdentifierImpl( "GO:0005515") );
//          Identifier id1 = new GoIdentifierImpl( "GO:0005516");
//          pa.addAnnotation( id1 );
//          pa.addAnnotation( new GoIdentifierImpl( "GO:0006281") );
//          Identifier id2 = new GoIdentifierImpl( "GO:0006974");
//          pa.addAnnotation( id2 );
//          Identifier id3 = new InterProIdentifierImpl("IPR000523 ");
//          pa.addAnnotation( id3 );

          GOAFilter filter = new GOAFilterMapImpl();
          File goaFile = new File("E:\\iarmean\\data\\goaTrimmed.txt");
          filter.initialize( goaFile);
          File hcGoFile = new File("E:\\filterGOs\\highconf_set_go.txt");
         File outFile = new File("E:\\filterGOs\\highconf_set_go_filter.txt");
         ProteinAnnotationReader  par = new ProteinAnnotationReaderImpl();
         ProteinAnnotationWriter paw = new ProteinAnnotationWriterImpl();
         for ( Iterator<ProteinAnnotation> iter = par.iterate( hcGoFile ); iter.hasNext();){
             ProteinAnnotation pa = iter.next();
             filter.filterGO(pa);
             paw.append( pa, outFile );
         }
         Assert.assertTrue( outFile.exists() );

      }


}
