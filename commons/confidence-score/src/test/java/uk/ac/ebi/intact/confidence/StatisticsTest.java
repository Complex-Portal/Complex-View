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
package uk.ac.ebi.intact.confidence;

import org.junit.Ignore;
import org.junit.Test;
import uk.ac.ebi.intact.bridges.blast.model.BlastInput;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.confidence.model.ProteinAnnotation;
import uk.ac.ebi.intact.confidence.model.io.BlastInputReader;
import uk.ac.ebi.intact.confidence.model.io.ProteinAnnotationReader;
import uk.ac.ebi.intact.confidence.model.io.impl.BlastInputReaderImpl;
import uk.ac.ebi.intact.confidence.model.io.impl.ProteinAnnotationReaderImpl;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Class used only for creating statistics, and not with a test purpose.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 */
//@Ignore
public class StatisticsTest {

    @Test
  @Ignore
    public void scoreDistribution() throws Exception {
        File medConfFile = new File("H:\\tmp\\medconf_set_New_scores.txt");
        Statistics.scoreDistribution( medConfFile );
    }


    @Test
    @Ignore
    public void attributesCoverage() throws Exception {
        File hcFile = new File("H:\\tmp\\lowconf_set_attributes.txt");
        Statistics.attributesCoverage( hcFile );            
    }

    @Test
    @Ignore
    public void missingProts() throws Exception {
        File annoFile = new File("E:\\lowconf_set_seq_anno.txt");
        File seqFile = new File("E:\\lowconf_set_seq.txt");

        Set<UniprotAc> acs = new HashSet<UniprotAc>();
        ProteinAnnotationReader par = new ProteinAnnotationReaderImpl();
        for ( Iterator<ProteinAnnotation> iterator = par.iterate( annoFile ); iterator.hasNext(); ) {
            ProteinAnnotation pa =  iterator.next();
            acs.add( new UniprotAc( pa.getId().getId()));         
        }

        BlastInputReader blastInputR = new BlastInputReaderImpl();
        for ( Iterator<BlastInput> iter = blastInputR.iterate( seqFile ); iter.hasNext(); ) {
            BlastInput bl =  iter.next();
            if (!acs.contains( bl.getUniprotAc() )){
                System.out.println(bl.getUniprotAc());
            }
        }




    }


}
