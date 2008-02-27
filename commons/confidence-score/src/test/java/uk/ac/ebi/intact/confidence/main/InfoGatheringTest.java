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

import org.junit.Ignore;
import org.junit.Test;
import uk.ac.ebi.intact.confidence.expansion.SpokeExpansion;
import uk.ac.ebi.intact.confidence.model.BinaryInteraction;
import uk.ac.ebi.intact.confidence.model.Confidence;
import uk.ac.ebi.intact.confidence.model.io.BinaryInteractionReader;
import uk.ac.ebi.intact.confidence.model.io.impl.BinaryInteractionReaderImpl;
import uk.ac.ebi.intact.context.IntactContext;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Test class for InfoGathering.
 * (Step 1)
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>07-Dec-2007</pre>
 */
public class InfoGatheringTest {
    @Test
   @Ignore
    public void testRetrieve() throws Exception {
        InfoGathering infoG = new InfoGathering( new SpokeExpansion() );
        IntactContext.initStandaloneContext( new File( InfoGatheringTest.class.getResource( "/hibernate.iweb2.cfg.xml" ).getFile() ) );
//        File workDir=  new File( "E:\\", "InfoGatheringTest");
        File workDir = new File( "/net/nfs7/vol22/sp-pro5/20080401_iarmean/InfoGather" );
        if ( workDir.exists() ) {
            workDir.delete();
        }
        workDir.mkdir();
        infoG.retrieveHighConfidenceAndMediumConfidenceSetWithAnnotations( workDir );
    }

    @Test
    @Ignore
    public void testRetreiveLC() throws Exception {
//        File workDir = new File( GlobalTestData.getInstance().getTargetDirectory(), "InfoGatheringTest" );
        File workDir = new File("/net/nfs7/vol22/sp-pro5/20080401_iarmean/InfoGather");
        InfoGathering infoG = new InfoGathering( new SpokeExpansion() );

        BinaryInteractionReader bir = new BinaryInteractionReaderImpl();
        File hcFile = new File( workDir, "highconf_set.txt" );
        bir.setConfidence( Confidence.HIGH );
        List<BinaryInteraction> birs = bir.read( hcFile );
        System.out.println( birs.size() );
        Set<BinaryInteraction> birs2 = new HashSet<BinaryInteraction>( birs );
        System.out.println( birs2.size() );


        File fastaFile = new File( "/net/nfs6/vol1/homes/iarmean/tmp/40.S_cerevisiae.fasta" ); // put a proper yeast fasta file
        infoG.retrieveLowConfidenceSet( workDir, fastaFile, birs2.size() );
        File lowconfFile = new File( workDir, "lowconf_set.txt" );
        infoG.retrieveLowConfidenceSetAnnotations( workDir, lowconfFile );
    }


}
