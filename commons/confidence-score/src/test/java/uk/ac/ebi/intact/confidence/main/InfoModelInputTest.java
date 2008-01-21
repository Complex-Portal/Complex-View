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
package uk.ac.ebi.intact.confidence.main;

import org.junit.Test;
import org.junit.Ignore;
import uk.ac.ebi.intact.confidence.model.*;
import uk.ac.ebi.intact.confidence.model.io.*;
import uk.ac.ebi.intact.confidence.model.io.impl.BinaryInteractionAttributesWriterImpl;
import uk.ac.ebi.intact.confidence.model.io.impl.BinaryInteractionReaderImpl;
import uk.ac.ebi.intact.confidence.model.io.impl.ProteinAnnotationReaderImpl;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Test class for Information Modeling class.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        10-Dec-2007
 *        </pre>
 */
public class InfoModelInputTest {
    @Test
    @Ignore
    public void testCombine() throws Exception {

        File annotationFile = new File ("/net/nfs6/vol1/homes/iarmean/tmp/medconf_set_seq_anno_filter.txt");
        //new File("H:\\tmp\\medconf_set_seq_anno_filter.txt");
        //new File ("/net/nfs6/vol1/homes/iarmean/tmp/medconf_set_ip.txt");


        File binaryInteractionFile = new File ("/net/nfs6/vol1/homes/iarmean/tmp/medconf_set.txt");
                //new File("H:\\tmp\\medconf_set.txt");


        Map<Identifier, ProteinAnnotation > annoMap = createMap(annotationFile);
        System.out.println(annoMap.size());

        BinaryInteractionAttributesWriter biaw = new BinaryInteractionAttributesWriterImpl();
        File outFile = new File ("/net/nfs6/vol1/homes/iarmean/tmp/medconf_set_seq_anno_filter_attribs.txt"); 
                //new File ("H:\\tmp\\medconf_set_seq_anno_filter_attribs.txt");

        //new File ("H:\\tmp\\medconf_set_ip_attribs.txt");
        if (outFile.exists()){
            outFile.delete();
        }

        BinaryInteractionReader biar = new BinaryInteractionReaderImpl();

        InfoModelInput infoMI = new InfoModelInput();
        for ( Iterator<BinaryInteraction> biaIter = biar.iterate( binaryInteractionFile ); biaIter.hasNext(); )
        {
            BinaryInteraction bi =  biaIter.next();

            ProteinAnnotation protA = annoMap.get( bi.getFirstId()  );
            ProteinAnnotation protB = annoMap.get( bi.getSecondId() );

            BinaryInteractionAttributes bia = new BinaryInteractionAttributes(bi.getFirstId(), bi.getSecondId(), Confidence.UNKNOWN);
            infoMI.populateAttributes( bia, protA, protB  );

            biaw.append( bia, outFile );
        }
    }

    private Map<Identifier, ProteinAnnotation> createMap( File annotationFile ) throws IOException {
        ProteinAnnotationReader reader = new ProteinAnnotationReaderImpl();
        List<ProteinAnnotation> annotations = reader.read( annotationFile);
        System.out.println(annotations.size());

        Map<Identifier, ProteinAnnotation> mapped = new HashMap<Identifier, ProteinAnnotation>();
        for (int i =0; i< annotations.size(); i++){
            ProteinAnnotation pa = annotations.get( i );
            mapped.put( pa.getId(), pa );
        }

        return mapped;
    }

}
