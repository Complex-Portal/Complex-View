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
package uk.ac.ebi.intact.confidence.psimi;

import org.junit.Assert;
import org.junit.Test;
import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.PsimiXmlReaderException;
import psidev.psi.mi.xml.model.Confidence;
import psidev.psi.mi.xml.model.Entry;
import psidev.psi.mi.xml.model.EntrySet;
import psidev.psi.mi.xml.model.Interaction;
import uk.ac.ebi.intact.bridges.blast.AbstractBlastService;
import uk.ac.ebi.intact.bridges.blast.BlastConfig;
import uk.ac.ebi.intact.bridges.blast.BlastServiceException;
import uk.ac.ebi.intact.bridges.blast.EbiWsWUBlast;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.confidence.global.GlobalTestData;
import uk.ac.ebi.intact.confidence.model.ConfidenceType;
import uk.ac.ebi.intact.confidence.utils.ParserUtils;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Test class for PsiMiXmlConfidence class.
 * (reads a psimixml file, computes the confidence score
 * and writes the score to a file.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.0
 *        <pre>
 *        03-Dec-2007
 *        </pre>
 */
public class PsiMiXmlConfidenceTest {

    @Test
    public void testAssingScore() throws Exception {
        File workDir =  GlobalTestData.getInstance().getTargetDirectory();
        File dbFolder = new File ( GlobalTestData.getTargetDirectory(),"DbFolder");

        String email = "myName@yahuo.com";
        prepareDB( dbFolder, email, workDir );

        File blastArchiveDir = new File( PsiMiTabConfidenceTest.class.getResource( "P15626.xml" ).getPath()).getParentFile();

        BlastConfig config = new BlastConfig(email);
        config.setBlastArchiveDir( blastArchiveDir);
        config.setDatabaseDir( dbFolder);

        String hcSetPath = PsiMiTabConfidenceTest.class.getResource( "hc_attributes.txt").getPath();
        Set<UniprotAc> againstProteins =  ParserUtils.parseProteins( new File(hcSetPath));

        File gisModel = new File( PsiMiXmlConfidence.class.getResource("gisModel.txt").getPath());
        File goaFile = new File ( PsiMiXmlConfidenceTest.class.getResource( "goaTest.txt" ).getPath());
        
        PsiMiXmlConfidence psixml = new PsiMiXmlConfidence( gisModel, config, againstProteins, goaFile, workDir);

        File inFile = new File( PsiMiXmlConfidenceTest.class.getResource( "9560268.xml").getPath());
        File outFile = new File(workDir, "outPsiMiXml.xml");
        Set<ConfidenceType> confs = new HashSet<ConfidenceType>();
        confs.add( ConfidenceType.GO);
        confs.add( ConfidenceType.InterPRO);
        psixml.appendConfidence(inFile,outFile, confs );
        checkOutput(outFile);
    }

    private void prepareDB(File dbFolder, String email, File workDir) throws BlastServiceException {
        AbstractBlastService wsBlast = new EbiWsWUBlast( dbFolder, "job", workDir, email, 20);
        wsBlast.deleteJobsAll();
        wsBlast.importCsv(new File(PsiMiXmlConfidenceTest.class.getResource("initDb.csv").getPath()));
    }

    private void checkOutput( File outFile ) {
        PsimiXmlReader reader  = new PsimiXmlReader();
        try {
            EntrySet entrySet = reader.read( outFile);
            for ( Iterator<Entry> iterator = entrySet.getEntries().iterator(); iterator.hasNext(); ) {
                Entry entry =  iterator.next();
                for ( Iterator<Interaction> iter = entry.getInteractions().iterator(); iter.hasNext(); ) {
                    Interaction interaction =  iter.next();
                    Assert.assertEquals( 1,interaction.getConfidences().size());
                    Confidence confidence = interaction.getConfidences().iterator().next();
                    Assert.assertNotNull( confidence.getValue() );
                    Assert.assertEquals( "intact confidence", confidence.getUnit().getNames().getShortLabel() );
                    Assert.assertEquals( "interaction confidence score", confidence.getUnit().getNames().getFullName() );
                }
            }      
        } catch ( PsimiXmlReaderException e ) {
            e.printStackTrace();
        }
    }

}
