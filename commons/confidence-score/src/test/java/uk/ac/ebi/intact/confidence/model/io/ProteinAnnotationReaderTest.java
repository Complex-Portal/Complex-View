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

import java.io.File;
import java.util.List;
import java.util.Collection;
import java.util.Arrays;
import java.util.Iterator;

import uk.ac.ebi.intact.confidence.model.*;
import uk.ac.ebi.intact.confidence.model.io.impl.ProteinAnnotationReaderImpl;

/**
 * Test for the Protein annotation file reader
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        10-Dec-2007
 *        </pre>
 */
public class ProteinAnnotationReaderTest {
    @Test
    public void read() throws Exception {
       ProteinAnnotationReader par = new ProteinAnnotationReaderImpl();
        File testFile = new File( ProteinAnnotationReaderTest.class.getResource( "ProteinAnnotation.txt" ).getPath() );
        List<ProteinAnnotation> pas = par.read( testFile );
        Assert.assertEquals( 4, pas.size() );
        List<ProteinAnnotation> expected = proteinAnnotations();
        for ( int i = 0; i < expected.size(); i++ ) {
            Assert.assertEquals( expected.get( i ), pas.get( i ) );
        }
    }

    @Test
    public void iterate() throws Exception{
        ProteinAnnotationReader par = new ProteinAnnotationReaderImpl();
        File testFile = new File( ProteinAnnotationReaderTest.class.getResource( "ProteinAnnotation.txt" ).getPath() );
        List<ProteinAnnotation> expected = proteinAnnotations();
        int i =0 ;
        for ( Iterator<ProteinAnnotation> iter = par.iterate( testFile ); iter.hasNext(); )
        {
            ProteinAnnotation pa =  iter.next();
            Assert.assertEquals( expected.get( i ), pa);
            i++;
        }
        Assert.assertEquals( expected.size(), i );
    }

    public List<ProteinAnnotation> proteinAnnotations(){
        Identifier id0 = new GoIdentifierImpl( "GO:0007165");
        Identifier id1 = new GoIdentifierImpl( "GO:0005515");
        Identifier id2 = new GoIdentifierImpl( "GO:0004725");
        Collection<Identifier> annos = Arrays.asList( id0, id1, id2);
        ProteinAnnotation pa = new ProteinAnnotation(new UniprotIdentifierImpl("P18031"),annos);

        Identifier id01 = new InterProIdentifierImpl( "IPR003121" );
        Identifier id11 = new InterProIdentifierImpl( "IPR001841" );
        Identifier id21 = new InterProIdentifierImpl( "IPR001876" );
        Collection<Identifier> annos1 = Arrays.asList( id01, id11, id21);
        ProteinAnnotation pa1 = new ProteinAnnotation(new UniprotIdentifierImpl("Q00987"),annos1);

        Identifier id011 = new UniprotIdentifierImpl( "P38634");
        Collection<Identifier> annos2 = Arrays.asList( id011);
        ProteinAnnotation pa2 = new ProteinAnnotation(new UniprotIdentifierImpl("P38634"),annos2);
       
        ProteinAnnotation pa3 = new ProteinAnnotation(new UniprotIdentifierImpl("Q08345-2"));

        return Arrays.asList( pa, pa1, pa2, pa3 );
    }

}
