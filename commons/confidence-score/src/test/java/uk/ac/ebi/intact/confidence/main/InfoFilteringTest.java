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
package uk.ac.ebi.intact.confidence.main;

import org.junit.Ignore;
import org.junit.Test;
import uk.ac.ebi.intact.confidence.filter.SeqAlignFilter;
import uk.ac.ebi.intact.confidence.model.Identifier;
import uk.ac.ebi.intact.confidence.model.ProteinAnnotation;
import uk.ac.ebi.intact.confidence.model.io.ProteinAnnotationReader;
import uk.ac.ebi.intact.confidence.model.io.ProteinAnnotationWriter;
import uk.ac.ebi.intact.confidence.model.io.impl.ProteinAnnotationReaderImpl;
import uk.ac.ebi.intact.confidence.model.io.impl.ProteinAnnotationWriterImpl;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Test class for Information filtering class.
 * (Step 2)
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        15-Jan-2008
 *        </pre>
 */
public class InfoFilteringTest {

    @Test
    @Ignore
    public void filterGO() throws Exception {
        File goAnnoFile = new File ("H:\\tmp\\medconf_set_go.txt");
        ProteinAnnotationReader bar = new ProteinAnnotationReaderImpl();

        File outFile = new File ("H:\\tmp\\medconf_set_go_filter.txt");
        if (outFile.exists()){
            outFile.delete();
        }
        ProteinAnnotationWriter paw = new ProteinAnnotationWriterImpl();

        Set<Identifier> done = new HashSet<Identifier>();
        for ( Iterator<ProteinAnnotation> iter = bar.iterate( goAnnoFile ); iter.hasNext();){
             ProteinAnnotation pa = iter.next();
            if (! done.contains( pa.getId() )){
                Set<Identifier> gos = new HashSet<Identifier>(pa.getAnnotations());
                InfoFiltering.filterGO(gos);                                     // TODO: filter gos on collection
                pa.setAnnotations( gos );
                paw.append( pa, outFile );
                done.add( pa.getId() );
            }
        }
    }

   @Test
   @Ignore
   public void filterSeq() throws Exception {
       File highconfFile = new File ("/net/nfs6/vol1/homes/iarmean/tmp/highconf_set.txt");
               //new File ("H:\\tmp\\highconf_set.txt");
       SeqAlignFilter.setHighConfProteins(highconfFile);

       File seqAnnoFile = new File ("/net/nfs6/vol1/homes/iarmean/tmp/medconf_set_seq_anno.txt");
               //new File ("H:\\tmp\\medconf_set_seq_anno.txt");
       ProteinAnnotationReader par = new ProteinAnnotationReaderImpl();

       File outFile =  new File ("/net/nfs6/vol1/homes/iarmean/tmp/medconf_set_seq_anno_filter.txt");
               //new File("H:\\tmp\\medconf_set_seq_anno_filter.txt");
       if (outFile.exists()){
           outFile.delete();
       }
       ProteinAnnotationWriter paw = new ProteinAnnotationWriterImpl();

       Set<Identifier> done = new HashSet<Identifier>();
       for (Iterator<ProteinAnnotation> iter = par.iterate( seqAnnoFile );iter.hasNext();){
           ProteinAnnotation pa = iter.next();
           if (!done.contains( pa.getId() )){
               Set<Identifier> prots = new HashSet<Identifier>( pa.getAnnotations() );
               SeqAlignFilter.filterHighConfAlign( prots );
               pa.setAnnotations( prots );
               paw.append( pa, outFile );
               done.add( pa.getId() );
           }
       }
   }

}
