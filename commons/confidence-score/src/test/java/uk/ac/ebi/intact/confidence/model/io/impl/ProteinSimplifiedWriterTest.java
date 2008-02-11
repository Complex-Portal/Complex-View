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
package uk.ac.ebi.intact.confidence.model.io.impl;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.intact.confidence.factory.MockFactory;
import uk.ac.ebi.intact.confidence.global.GlobalTestData;
import uk.ac.ebi.intact.confidence.model.Identifier;
import uk.ac.ebi.intact.confidence.model.ProteinAnnotation;
import uk.ac.ebi.intact.confidence.model.ProteinSimplified;
import uk.ac.ebi.intact.confidence.model.io.ProteinAnnotationReader;
import uk.ac.ebi.intact.confidence.model.io.ProteinSimplifiedWriter;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Protein Simpld writer test class.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        10-Jan-2008
 *        </pre>
 */
public class ProteinSimplifiedWriterTest {
    @Test
    public void appendProteinSimplified() throws Exception {
        ProteinSimplifiedWriter psw = new ProteinSimplifiedWriterImpl();
        File outFile = new File( GlobalTestData.getTargetDirectory(), "ProtSimplWriterTest1.txt");
        cleanIfExists(outFile);

        if (outFile.exists()){
            outFile.delete();
        }
        ProteinSimplified ps = MockFactory.createRandomProteinSimplified();
        ps.addGo( MockFactory.createDeterministicGoId(1) );
        ps.addGo (MockFactory.createRandomGoId());
        psw.append(ps, outFile);

        //check the output
        ProteinAnnotationReader par = new ProteinAnnotationReaderImpl();
        ProteinSimplifiedWriterImpl pswi = new ProteinSimplifiedWriterImpl();
        String fileName = pswi.fileName( outFile );
        File goFile = new File(fileName + "_go.txt");
        List<ProteinAnnotation> pa = par.read( goFile );
        Assert.assertEquals( 1, pa.size() );

        Assert.assertEquals( ps.getUniprotAc().getAcNr(), pa.get( 0 ).getId().getId());
        Set<Identifier> expectedIds = ps.getGoSet();
        Collection<Identifier> observedIds = pa.get(0).getAnnotations();
        Assert.assertEquals( expectedIds.size(), observedIds.size() );

        for ( Iterator<Identifier> identifierIterator = expectedIds.iterator(); identifierIterator.hasNext(); ) {
            Identifier identifier = identifierIterator.next();
            Assert.assertTrue(observedIds.contains( identifier ));
        }
    }

    private void cleanIfExists( File outFile ) {
        ProteinSimplifiedWriterImpl pswi = new ProteinSimplifiedWriterImpl();
        pswi.existsDelete( outFile );
        pswi.cleanIfExists( outFile );
    }
}
