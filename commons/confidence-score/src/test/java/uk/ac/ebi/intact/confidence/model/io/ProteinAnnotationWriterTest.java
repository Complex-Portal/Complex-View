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
import uk.ac.ebi.intact.confidence.model.ProteinAnnotation;
import uk.ac.ebi.intact.confidence.model.io.impl.ProteinAnnotationReaderImpl;
import uk.ac.ebi.intact.confidence.model.io.impl.ProteinAnnotationWriterImpl;
import uk.ac.ebi.intact.confidence.global.GlobalTestData;

import java.util.List;
import java.io.File;

/**
 * Test class for ProteinAnnotationWriter.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        10-Jan-2008
 *        </pre>
 */
public class ProteinAnnotationWriterTest {
    
    @Test
    public void writeListFile () throws Exception{
        ProteinAnnotationWriter paw = new ProteinAnnotationWriterImpl();

        ProteinAnnotationReaderTest part = new ProteinAnnotationReaderTest();
        List<ProteinAnnotation> expected = part.proteinAnnotations();

        File outFile = new File( GlobalTestData.getTargetDirectory(),"ProtAnnoWriterTest1.txt");

        paw.write( expected,outFile );

        ProteinAnnotationReader par = new ProteinAnnotationReaderImpl();
        List<ProteinAnnotation> observed = par.read( outFile );
        Assert.assertEquals( expected.size(), observed.size() );
        for (int i= 0; i< expected.size(); i++){
            Assert.assertEquals( expected.get( i ), observed.get( i ));
        }
    }

    @Test
    public void appendProteinAnnotationFile() throws Exception{
        ProteinAnnotationWriter paw = new ProteinAnnotationWriterImpl();

        ProteinAnnotationReaderTest part = new ProteinAnnotationReaderTest();
        List<ProteinAnnotation> expected = part.proteinAnnotations();

        File outFile = new File( GlobalTestData.getTargetDirectory(),"ProtAnnoWriterTest2.txt");
        if (outFile.exists()){
            outFile.delete();
        }

        for (int i= 0; i< expected.size(); i++){
            paw.append(expected.get( i ), outFile);
        }

        ProteinAnnotationReader par = new ProteinAnnotationReaderImpl();
        List<ProteinAnnotation> observed = par.read( outFile );
        Assert.assertEquals( expected.size(), observed.size() );
        for (int i= 0; i< expected.size(); i++){
            Assert.assertEquals( expected.get( i ), observed.get( i ));
        }
    }

}
