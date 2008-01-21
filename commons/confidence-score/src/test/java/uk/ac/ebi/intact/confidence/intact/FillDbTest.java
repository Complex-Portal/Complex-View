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
package uk.ac.ebi.intact.confidence.intact;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.Assert;

import java.io.File;
import java.util.*;

import uk.ac.ebi.intact.confidence.maxent.MaxentUtils;
import uk.ac.ebi.intact.confidence.maxent.OpenNLPMaxEntClassifier;
import uk.ac.ebi.intact.confidence.global.GlobalTestData;
import uk.ac.ebi.intact.confidence.intact.IntactConfidenceCalculator;
import uk.ac.ebi.intact.confidence.utils.ParserUtils;
import uk.ac.ebi.intact.bridges.blast.BlastConfig;
import uk.ac.ebi.intact.bridges.blast.AbstractBlastService;
import uk.ac.ebi.intact.bridges.blast.EbiWsWUBlast;
import uk.ac.ebi.intact.bridges.blast.BlastServiceException;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.core.persister.PersisterHelper;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import opennlp.maxent.GISModel;

/**
 * Simulates the use of the IntactScoreCalculator on a database.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        16-Jan-2008
 *        </pre>
 */
public class FillDbTest extends IntactBasicTestCase {

    @Test
    @Ignore
    public void trainModel() throws Exception {
        File hcFile = new File ( FillDbTest.class.getResource( "highconf_set_attributes.txt" ).getPath());
        File lcFile = new File ( FillDbTest.class.getResource( "lowconf_set_attributes.txt" ).getPath());
        File workDir = new File ( GlobalTestData.getTargetDirectory(), "FillDbTest");
        workDir.mkdir();

        OpenNLPMaxEntClassifier cl = new OpenNLPMaxEntClassifier( hcFile.getPath(), lcFile.getPath(), workDir);
        GISModel model = cl.getModel();
        File outFile = new File(workDir, "model.txt");
        MaxentUtils.writeModelToFile( model, outFile );
    }

    @Test
    public void test() throws Exception {
          Interaction interaction1 = getMockBuilder().createInteraction("int-high",  
                    getMockBuilder().createDeterministicProtein( "Q8WYL5", "prot1" ),
                    getMockBuilder().createDeterministicProtein( "Q9BR76", "prot2" ),
                    getMockBuilder().createDeterministicExperiment());
          Interaction interaction2 = getMockBuilder().createInteraction("int-low",   
                    getMockBuilder().createDeterministicProtein( "P40884", "prot1" ),
                    getMockBuilder().createDeterministicProtein( "Q6Q546", "prot2" ),
                    getMockBuilder().createDeterministicExperiment());
          Interaction interaction3 = getMockBuilder().createInteraction("int-unk",
                    getMockBuilder().createDeterministicProtein( "Q16643", "prot1" ),
                    getMockBuilder().createDeterministicProtein( "Q9FHZ1", "prot2" ),
                    getMockBuilder().createDeterministicExperiment());
          
          CvConfidenceType ctype = CvObjectUtils.createCvObject( getIntactContext().getInstitution(), CvConfidenceType.class, "IA:999", "intact-confidence" );
          PersisterHelper.saveOrUpdate( interaction1, interaction2, interaction3, ctype );
          List<InteractionImpl> interactions = getDaoFactory().getInteractionDao().getAll();
          Assert.assertEquals( 3, interactions.size() );


          File dbFolder = new File(GlobalTestData.getTargetDirectory(), "DbFolder");
          dbFolder.mkdir();
          File workDir = GlobalTestData.getTargetDirectory();
          prepareDB(dbFolder, "myName@yahuo.com", workDir);

          File gisModelFile = new File( FillDbTest.class.getResource( "model.txt" ).getPath() );
          BlastConfig config = new BlastConfig( "myName@yahuo.com" );
          // need a blast Archive
          File archive = new File( FillDbTest.class.getResource( "Q16643.xml" ).getPath()).getParentFile();
          config.setBlastArchiveDir(archive);
          // db dir
          config.setDatabaseDir(dbFolder);
          //
          OpenNLPMaxEntClassifier classifier = new OpenNLPMaxEntClassifier( gisModelFile );

          File hcSet = new File (FillDbTest.class.getResource( "highconf_set.txt" ).getPath());
          Set<UniprotAc> againstProts = ParserUtils.parseProteins( hcSet );

          IntactConfidenceCalculator ic = new IntactConfidenceCalculator( classifier, config, againstProts, workDir );
          ic.calculate( interactions,classifier );

          for ( Iterator<InteractionImpl> iter = interactions.iterator(); iter.hasNext(); ) {
              InteractionImpl interaction = iter.next();
              Assert.assertEquals( 1, interaction.getConfidences().size() );
              System.out.println("interaction: " + interaction.toString());
              String expected = "0.5";
              if (interaction.getShortLabel().equalsIgnoreCase( "int-high" )){
                  expected = "0.8202424636272737";
              } else if (interaction.getShortLabel().equalsIgnoreCase( "int-low" )){
                  expected = "0.20376662467861537";
              }
              if (interaction.getShortLabel().equalsIgnoreCase( "int-unk" )){
                    Assert.assertNotNull(interaction.getConfidences().iterator().next().getValue());
              } else {
                  Assert.assertEquals( expected, interaction.getConfidences().iterator().next().getValue() );
              }
          }
                
          // persist to db
          getDataContext().beginTransaction();
          CvConfidenceType cvConfidenceType = (CvConfidenceType) getDaoFactory().getCvObjectDao().getByShortLabel( "intact-confidence" );

          for ( Iterator<InteractionImpl> iter = interactions.iterator(); iter.hasNext(); ) {
              InteractionImpl interaction = iter.next();

              Confidence confidence = interaction.getConfidences().iterator().next();
              confidence.setCvConfidenceType( cvConfidenceType );

              getDaoFactory().getInteractionDao().update( interaction );
              getDaoFactory().getConfidenceDao().persist(confidence);
          }
          getDataContext().commitTransaction();


          //check if it was proper persisted
          for ( Iterator<InteractionImpl> iter = getDaoFactory().getInteractionDao().getAll().iterator(); iter.hasNext(); )
          {
              InteractionImpl interaction = iter.next();
              Assert.assertEquals( 1, interaction.getConfidences().size() );
               String expected = "0.5";
              if (interaction.getShortLabel().equalsIgnoreCase( "int-high" )){
                  expected = "0.8202424636272737";
              } else if (interaction.getShortLabel().equalsIgnoreCase( "int-low" )){
                  expected = "0.20376662467861537";
              }
              Assert.assertEquals( expected, interaction.getConfidences().iterator().next().getValue() );
          }
      }

    private void prepareDB(File dbFolder, String email, File workDir) throws BlastServiceException {
        AbstractBlastService wsBlast = new EbiWsWUBlast( dbFolder, "job", workDir, email, 20);
        wsBlast.deleteJobsAll();
        wsBlast.importCsv(new File(FillDbTest.class.getResource("initDb.csv").getPath()));
    }


}
